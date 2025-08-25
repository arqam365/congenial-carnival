package com.nextlevelprogrammers.elearn

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
//import com.sunildhiman90.kmauth.google.compose.GoogleSignInButton
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.launch

@Composable
fun SignInScreen(onSignedIn: (String) -> Unit) {
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun setError(e: Throwable) {
        e.printStackTrace()
        errorMessage = e.message ?: "Something went wrong"
    }

    suspend fun signInEmailPassword() {
        loading = true
        try {
            val authResult = Firebase.auth
                .signInWithEmailAndPassword(email.trim(), password)
            val token = authResult.user?.getIdToken(false) ?: ""
            TokenStorage.saveToken(token)
            onSignedIn(token)
        } catch (e: Throwable) {
            setError(e)
        } finally {
            loading = false
        }
    }

    suspend fun signUpEmailPassword() {
        loading = true
        try {
            val authResult = Firebase.auth
                .createUserWithEmailAndPassword(email.trim(), password)
            val token = authResult.user?.getIdToken(false) ?: ""
            TokenStorage.saveToken(token)
            onSignedIn(token)
        } catch (e: Throwable) {
            setError(e)
        } finally {
            loading = false
        }
    }

    suspend fun sendResetEmail() {
        loading = true
        try {
            Firebase.auth.sendPasswordResetEmail(email.trim())
            errorMessage = "Password reset email sent to ${email.trim()}."
        } catch (e: Throwable) {
            setError(e)
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign in to ELearn", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(20.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        // Password
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { showPassword = !showPassword }) {
                    Text(if (showPassword) "HIDE" else "SHOW")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Actions row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email and password are required."
                    } else scope.launch { signInEmailPassword() }
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) { Text(if (loading) "Signing in..." else "Sign in") }

            OutlinedButton(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        errorMessage = "Email and password are required to create an account."
                    } else scope.launch { signUpEmailPassword() }
                },
                enabled = !loading,
                modifier = Modifier.weight(1f)
            ) { Text(if (loading) "Please wait..." else "Create account") }
        }

        TextButton(
            onClick = {
                if (email.isBlank()) {
                    errorMessage = "Enter your email first, then tap reset."
                } else scope.launch { sendResetEmail() }
            },
            enabled = !loading
        ) { Text("Forgot password?") }

        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        // Keep your Google button
//        GoogleSignInButton(modifier = Modifier.fillMaxWidth()) { user, error ->
//            if (error != null) {
//                errorMessage = error.message
//                return@GoogleSignInButton
//            }
//            if (user != null) {
//                val idToken = user.idToken
//                if (idToken == null) {
//                    errorMessage = "Missing Google ID token"
//                    return@GoogleSignInButton
//                }
//                TokenStorage.saveToken(idToken)
//                onSignedIn(idToken)
//            }
//        }

        errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text("❌ $it", color = MaterialTheme.colorScheme.error)
        }
    }
}