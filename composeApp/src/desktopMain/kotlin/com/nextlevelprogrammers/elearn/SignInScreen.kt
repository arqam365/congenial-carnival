package com.nextlevelprogrammers.elearn

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sunildhiman90.kmauth.google.compose.GoogleSignInButton
import org.apache.commons.logging.Log

@Composable
fun SignInScreen(onSignedIn: (String) -> Unit) {
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign in to ELearn", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        GoogleSignInButton(modifier = Modifier.fillMaxWidth()) { user, error ->
            if (error != null) {
                errorMessage = error.message
            }
            if (user != null) {
                val s= user.accessToken
                println(" id= ${user.toString()}")
                println("✅ Google sign-in successful: ${user.email}")
                println("✅ Google sign-in successful: $s")
                user.idToken?.let {
                    TokenStorage.saveToken(it)
                    onSignedIn(it)
                }
            }
        }

        errorMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text("❌ $it", color = MaterialTheme.colorScheme.error)
        }
    }
}