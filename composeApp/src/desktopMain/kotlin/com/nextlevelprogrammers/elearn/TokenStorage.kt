package com.nextlevelprogrammers.elearn

import java.io.File

object TokenStorage {
    private val tokenFile = File(System.getProperty("user.home"), ".elearn_token")

    fun saveToken(token: String) {
        tokenFile.writeText(token)
    }

    fun getToken(): String? {
        return if (tokenFile.exists()) tokenFile.readText() else null
    }

    fun clearToken() {
        if (tokenFile.exists()) tokenFile.delete()
    }
}