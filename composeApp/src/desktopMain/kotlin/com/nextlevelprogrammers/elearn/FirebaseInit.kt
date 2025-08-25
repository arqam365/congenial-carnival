package com.nextlevelprogrammers.elearn

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import android.app.Application
import com.google.firebase.FirebasePlatform
import java.io.File
import java.util.Properties

private object DesktopFirebasePlatform : FirebasePlatform() {
    private val file = File(System.getProperty("user.home"), ".elearn/firebase-kv.properties")
    private val props = Properties().apply {
        file.parentFile?.mkdirs()
        if (file.exists()) file.inputStream().use(::load)
    }
    private fun flush() = file.outputStream().use { props.store(it, "elearn-firebase") }

    override fun store(key: String, value: String) { props.setProperty(key, value); flush() }
    override fun retrieve(key: String): String? = props.getProperty(key)
    override fun clear(key: String) { props.remove(key); flush() }
    override fun log(msg: String) = println("[Firebase] $msg")
}

fun initFirebaseDesktop() {
    // 1) Bridge for JVM (Auth persistence, logging, paths)
    FirebasePlatform.initializeFirebasePlatform(DesktopFirebasePlatform)

    // 2) Manual options (desktop can't read google-services.json)
    val options = FirebaseOptions(
        applicationId = "1:977741295366:web:13976fdc4cff630de13887",  // from Firebase Console
        apiKey        = "AIzaSyBTVltrEz-s6_p4jDk1KOlEIVQ8Dx2K_cM",
        projectId     = "iconic-strategy-453807-d1",
        // databaseUrl = "https://your-project-id.firebaseio.com",
        // storageBucket = "your-project-id.appspot.com"
    )

    // 3) Initialize BEFORE any Firebase.auth usage
    Firebase.initialize(Application(), options)
}