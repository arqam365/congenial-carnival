package com.nextlevelprogrammers.elearn

import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

object GCSUploader {
    private const val BUCKET_NAME = "orchid-prod-data"
    private const val MIME_TYPE = "video/mp4"

    /**
     * Public entry function to upload a large video file using resumable upload
     */
    suspend fun testVideoUpload(file: File): String {
        val objectPath = "videos/${file.name}"
        println("📁 Starting resumable upload for file: ${file.absolutePath}")

//        val accessToken = getAccessToken()
        val sessionUrl = startResumableSession(BUCKET_NAME, objectPath)

        println("🔄 Upload in progress to session: $sessionUrl")
        uploadFileToSession(sessionUrl, file)

        val publicUrl = "https://storage.googleapis.com/$BUCKET_NAME/$objectPath"
        println("✅ Upload complete. File available at: $publicUrl")
        return publicUrl
    }

    /**
     * Starts a resumable upload session and returns the session URL
     */
    private fun startResumableSession(bucketName: String, objectPath: String): String {
        val uploadUrl = "https://storage.googleapis.com/upload/storage/v1/b/$bucketName/o?uploadType=resumable"
        val connection = URL(uploadUrl).openConnection() as HttpsURLConnection

        connection.requestMethod = "POST"
        connection.setRequestProperty("Authorization", "Bearer ${getAccessToken()}")
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
        connection.setRequestProperty("X-Upload-Content-Type", "video/mp4")
        connection.doOutput = true

        val body = """
        {
          "name": "$objectPath"
        }
    """.trimIndent()

        connection.outputStream.use { it.write(body.toByteArray()) }

        val responseCode = connection.responseCode
        return if (responseCode in 200..299) {
            connection.getHeaderField("Location") ?: throw RuntimeException("❌ Missing resumable URL")
        } else {
            val error = connection.errorStream?.bufferedReader()?.readText()
            throw RuntimeException("❌ Failed to start resumable session. Code: $responseCode\n$error")
        }
    }

    /**
     * Uploads the file content to the provided resumable session URL
     */
    private fun uploadFileToSession(sessionUrl: String, file: File) {
        val connection = URL(sessionUrl).openConnection() as HttpURLConnection

        connection.requestMethod = "PUT"
        connection.doOutput = true
        connection.setRequestProperty("Content-Length", file.length().toString())
        connection.setRequestProperty("Content-Type", MIME_TYPE)

        try {
            file.inputStream().use { input ->
                connection.outputStream.use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            val errorText = connection.errorStream?.bufferedReader()?.readText()
            throw RuntimeException("❌ Error during upload: ${e.localizedMessage}\nServer says: $errorText")
        }

        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            val errorText = connection.errorStream?.bufferedReader()?.readText()
            throw RuntimeException("❌ Upload failed. Code: $responseCode\n$errorText")
        }
    }

    /**
     * Replace this with a secure token generation method
     */
    private fun getAccessToken(): String {
        // 🔐 This is your current manually generated token (keep secret!)
        return "ya29.a0AW4Xtxju388cVShqr5r-37BLy-7kRAMWtiQPmUfQliTcCPHZ04ko_b6qFnZJqIIXb0uWgYnQntlTqcMFhIftM0SN4wyqBxWIuWOsyMUyWNtWOnMkH8Y796iRKUcZ94GKhV94RZC5DJPdrtzlspc1k7wJXoo7Ta4r-C3DiETSfHUksHIaCgYKAT4SARQSFQHGX2Mijf8jqTwCPI07vdHHBao_wQ0182"
    }
}