package com.nextlevelprogrammers.elearn

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import org.json.JSONObject
import java.net.URLEncoder

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

        println("🔍 Preparing to upload file:")
        println("  ➤ File name      : $fileName")
        println("  ➤ File path      : $filePath")
        println("  ➤ MIME type      : $mimeType")
        println("  ➤ File size (MB) : ${file.length() / (1024.0 * 1024.0)}")

        val signedUrl = getSignedUploadUrl(fileName, filePath, mimeType)
        println("✅ Signed URL obtained:\n$signedUrl")

        val success = uploadFileResumable(file, signedUrl, contentType = mimeType)

        if (!success) throw Exception("Upload failed")
        val encodedFileName = URLEncoder.encode(fileName, "UTF-8").replace("+", "%20")
        val publicUrl = "https://storage.googleapis.com/orchid-prod-data/$filePath/$encodedFileName"
        println("✅ Final GCS File URL (after successful upload): $publicUrl")
        return publicUrl
    }

    private suspend fun getSignedUploadUrl(fileName: String, filePath: String, fileMimeType: String): String {
        val token = getAccessToken()

        println("📡 Requesting signed URL from backend...")
        val response: HttpResponse = client.get("$BASE_URL/gcloud_storage") {
            parameter("file_name", fileName)
            parameter("file_path", filePath)
            parameter("file_mime_type", fileMimeType)
            header(HttpHeaders.Authorization, "Bearer $token")
        }

        val statusCode = response.status
        val contentType = response.headers[HttpHeaders.ContentType] ?: "unknown"
        val rawBody = response.bodyAsText().trim()

        println("📩 Signed URL Response:")
        println("  ➤ Status     : $statusCode")
        println("  ➤ MIME Type  : $contentType")
        println("  ➤ Raw Body   : $rawBody")

        if (rawBody.startsWith("http")) return rawBody

        return try {
            val json = JSONObject(rawBody)
            json.getString("url")
        } catch (e: Exception) {
            throw IllegalStateException("Invalid response. Content-Type: $contentType\nResponse: $rawBody", e)
        }
    }

    private suspend fun uploadFileResumable(file: File, signedUrl: String, contentType: String): Boolean {
        val uploadClient = HttpClient {
            install(HttpTimeout) {
                requestTimeoutMillis = 300_000
                connectTimeoutMillis = 60_000
                socketTimeoutMillis = 300_000
            }
        }

        return try {
            println("📤 Step 1: Initiating resumable session...")
            val initiateResponse: HttpResponse = uploadClient.post(signedUrl) {
                header(HttpHeaders.ContentType, contentType)
                header("x-goog-resumable", "start")
            }

            val sessionUrl = initiateResponse.headers["Location"]
            if (sessionUrl.isNullOrBlank()) {
                println("❌ No resumable session URL returned.")
                return false
            }

            println("✅ Resumable session URL: $sessionUrl")
            println("📤 Step 2: Uploading file...")

            val uploadResponse: HttpResponse = uploadClient.put(sessionUrl) {
                header(HttpHeaders.ContentType, contentType)
                setBody(file.readBytes())
            }

            println("📥 Upload Response:")
            println("  ➤ Status Code : ${uploadResponse.status.value}")
            println("  ➤ Body        : ${uploadResponse.bodyAsText()}")

            uploadResponse.status.value in 200..299
        } catch (e: Exception) {
            println("❌ Upload exception: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    private fun getAccessToken(): String {
        return TokenStorage.getToken()
            ?: throw IllegalStateException("No access token found. Please sign in first.")
    }
}