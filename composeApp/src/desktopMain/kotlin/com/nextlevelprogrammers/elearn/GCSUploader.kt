package com.nextlevelprogrammers.elearn

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import org.json.JSONObject

object GCSUploader {
    private const val BASE_URL = "https://production-begonia-orchid-977741295366.asia-south1.run.app/v1"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    @Serializable
    data class SignedUrlResponse(val url: String)

    suspend fun testVideoUpload(file: File): String {
        val fileName = file.name
        val filePath = "videos/uploads"
        val mimeType = "video/mp4"

        val signedUrl = getSignedUploadUrl(fileName, filePath, mimeType)
        val success = uploadFileToSignedUrl(file, signedUrl, contentType = mimeType)

        if (!success) throw Exception("Upload failed")
        return "https://storage.googleapis.com/orchid-prod-data/$filePath/$fileName"
    }

    private suspend fun getSignedUploadUrl(fileName: String, filePath: String, fileMimeType: String): String {
        val token = getAccessToken()

        val response: HttpResponse = client.get("$BASE_URL/gcloud_storage") {
            parameter("file_name", fileName)
            parameter("file_path", filePath)
            parameter("file_mime_type", "video/mp4") // ✅ Add this line dynamically based on file
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        val rawBody = response.bodyAsText().trim()
        val contentType = response.headers[HttpHeaders.ContentType] ?: "unknown"

        // If response is a raw URL string, just return it
        if (rawBody.startsWith("http")) {
            return rawBody
        }

        return try {
            val json = JSONObject(rawBody)
            json.getString("url")
        } catch (e: Exception) {
            throw IllegalStateException("Invalid response. Content-Type: $contentType\nResponse: $rawBody", e)
        }
    }

    private suspend fun uploadFileToSignedUrl(file: File, signedUrl: String, contentType: String): Boolean {
        val uploadClient = HttpClient()
        try {
            val response: HttpResponse = uploadClient.put(signedUrl) {
                header(HttpHeaders.ContentType, contentType)
                header("x-goog-resumable", "start")
                setBody(file.readBytes())
            }

            println("Upload Status: ${response.status}")
            println("Upload Response: ${response.bodyAsText()}")

            return response.status.value in 200..299
        } catch (e: Exception) {
            println("Upload exception: ${e.message}")
            return false
        }
    }

    private fun getAccessToken(): String {
        return TokenStorage.getToken()
            ?: throw IllegalStateException("No access token found. Please sign in first.")
    }
}