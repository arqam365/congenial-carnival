package com.nextlevelprogrammers.elearn

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
//import com.sunildhiman90.kmauth.core.KMAuthInitializer


fun main() = application {
//    KMAuthInitializer.initClientSecret(
//        clientSecret = "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"
//    )
//
//    KMAuthInitializer.init(
//        webClientId = "977741295366-h88ufv9vncvut78opgfb3qpdogkhd9v9.apps.googleusercontent.com"
//    )
    initFirebaseDesktop()

    Window(
        onCloseRequest = ::exitApplication,
        title = "ELearn",
    ) {
        App()
    }
}