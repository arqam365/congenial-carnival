package com.nextlevelprogrammers.elearn

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun VideoUploadScreen(authToken: String) {
    var showFilePicker by remember { mutableStateOf(false) }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    var uploadUrl by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Upload a Video File", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = { showFilePicker = true }) {
            Text("Select Video File")
        }

        selectedFilePath?.let {
            Text("Selected: ${File(it).name}", style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                selectedFilePath?.let { path ->
                    println("📁 Starting upload for file: $path")
                    isUploading = true
                    coroutineScope.launch {
                        try {
                            println("🔄 Upload in progress...")
                            val resultUrl = GCSUploader.testVideoUpload(File(path))
                            uploadUrl = resultUrl
                            println("✅ Uploaded to: $resultUrl")
                        } catch (e: Exception) {
                            println("❌ Upload failed: ${e.message}")
                            e.printStackTrace()
                        } finally {
                            isUploading = false
                            println("🛑 Upload finished")
                        }
                    }
                } ?: println("⚠️ No file selected for upload")
            },
            enabled = selectedFilePath != null && !isUploading
        ) {
            Text(if (isUploading) "Uploading..." else "Upload")
        }

        uploadUrl?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text("✅ Uploaded: $it", style = MaterialTheme.typography.bodySmall)
        }
    }

    FilePicker(
        show = showFilePicker,
        fileExtensions = listOf("mp4", "mov", "avi", "mkv")
    ) { platformFile ->
        showFilePicker = false
        selectedFilePath = platformFile?.path
    }
}