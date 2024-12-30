package com.example.loginregister

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.loginregister.ui.theme.LoginRegisterTheme
import com.google.firebase.auth.FirebaseAuth

import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Deconectăm utilizatorul la fiecare rulare a aplicației
        auth.signOut()

        setContent {
            LoginRegisterTheme {
                // Verificăm dacă utilizatorul este deja autentificat
                val user = auth.currentUser
                var currentScreen by remember { mutableStateOf("LoginRegisterChoice") } // Folosim o stare pentru a controla ecranul

                if (user != null) {
                    // Dacă utilizatorul este deja logat, schimbăm starea pentru a arăta MainScreen
                    currentScreen = "MainScreen"
                }

                // Afișăm ecranul corespunzător în funcție de starea curentă
                when (currentScreen) {
                    "LoginRegisterChoice" -> {
                        LoginRegisterChoiceScreen(
                            onNavigateToLogin = {
                                currentScreen = "LoginScreen" // Navigăm la LoginScreen
                            },
                            onNavigateToRegister = {
                                currentScreen = "RegisterScreen" // Navigăm la RegisterScreen
                            }
                        )
                    }
                    "LoginScreen" -> {
                        LoginScreen(
                            auth = auth,
                            onLoginSuccess = {
                                currentScreen = "MainScreen" // După login, navigăm la MainScreen
                            }
                        )
                    }
                    "RegisterScreen" -> {
                        RegisterScreen(
                            auth = auth,
                            onRegisterSuccess = {
                                // După înregistrare, revenim la LoginRegisterChoiceScreen
                                currentScreen = "LoginRegisterChoice"
                            }
                        )
                    }
                    "MainScreen" -> {
                        MainScreen(name = auth.currentUser?.displayName ?: "User")
                    }
                }
            }
        }
    }
}


@Composable
fun LoginRegisterChoiceScreen(onNavigateToLogin: () -> Unit, onNavigateToRegister: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(onClick = { onNavigateToLogin() }) {
            Text("Login")
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { onNavigateToRegister() }) {
            Text("Register")
        }
    }
}

@Composable
fun MainScreen(name: String) {
    Text(
        text = "Hello $name!",
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(Alignment.Center)
    )
}

@Composable
fun LoginScreen(auth: FirebaseAuth, onLoginSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            onLoginSuccess()
                        } else {
                            errorMessage = "Authentication failed: ${task.exception?.message}"
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }
    }
}

@Composable
fun RegisterScreen(auth: FirebaseAuth, onRegisterSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // După înregistrare, deconectăm utilizatorul și revenim la LoginRegisterChoice
                                auth.signOut()
                                onRegisterSuccess() // Navigăm înapoi la LoginRegisterChoice
                            } else {
                                errorMessage = "Registration failed: ${task.exception?.message}"
                            }
                        }
                } else {
                    errorMessage = "Passwords do not match!"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = Color.Red)
        }
    }
}




@Preview(showBackground = true)
@Composable
fun LoginRegisterChoicePreview() {
    LoginRegisterTheme {
        LoginRegisterChoiceScreen(
            onNavigateToLogin = {},
            onNavigateToRegister = {}
        )
    }
}

