package com.nextlevelprogrammers.elearn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.darkrockstudios.libraries.mpfilepicker.FilePicker
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import java.io.File

@Composable
fun SectionScreen(courseId: String, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var expandedSectionId by remember { mutableStateOf<String?>(null) }
    var sectionDetailsMap by remember { mutableStateOf<Map<String, SectionResponse>>(emptyMap()) }
    var course by remember { mutableStateOf<CourseDto?>(null) }
    var sections by remember { mutableStateOf<List<Section>>(emptyList()) }
    var showAddContentDialog by remember { mutableStateOf(false) }
    var selectedSectionIdForContent by remember { mutableStateOf<String?>(null) }
    var expectedContentIndex by remember { mutableStateOf(1) }

    LaunchedEffect(courseId) {
        println("📦 Fetching course details for ID: $courseId")
        try {
            val fullCourse = ApiService.getCourseWithSections(courseId)
            println("✅ Course fetched: ${fullCourse.course_name}")
            sections = fullCourse.course_sections
            course = fullCourse
            println("📚 Sections loaded: ${sections.map { it.section_name }}")
        } catch (e: Exception) {
            println("❌ Error fetching course with sections: ${e.localizedMessage}")
        }
    }

    if (course == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Row(Modifier.fillMaxSize().padding(24.dp)) {
        Column(Modifier.weight(2f).padding(end = 16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))) {
                    Text("← Back")
                }
            }
            // Course Overview Header
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(course!!.course_name, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                CourseChip(course!!.category?.category_name ?: "Unknown", Color(0xFF9333EA))
                Spacer(Modifier.width(4.dp))
                CourseChip(if (course!!.is_published) "Published" else "Draft", Color(0xFF10B981))
            }

            Spacer(Modifier.height(16.dp))
            AsyncImage(
                url = course!!.head_img,
                modifier = Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(16.dp))
            )

            Spacer(Modifier.height(24.dp))
            Text("About This Course", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(course!!.course_description)

            Spacer(Modifier.height(24.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Course Content", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Row {
                    Button(onClick = { showAddSectionDialog = true }) { Text("Add Section") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { /* Prune */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))) {
                        Text("Prune Sections")
                    }
                }
            }

            // Section Accordion
            sections.forEachIndexed { index, section ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        if (expandedSectionId == section.section_id) {
                                            expandedSectionId = null
                                        } else {
                                            try {
                                                println("📂 Toggling section: ${section.section_id}")
                                                val detail = ApiService.getSectionDetails(course!!.course_id, section.section_id)
                                                println("✅ Section detail fetched: ${detail.contents.size} contents")
                                                sectionDetailsMap = sectionDetailsMap + (section.section_id to detail)
                                                expandedSectionId = section.section_id
                                            } catch (e: Exception) {
                                                println("❌ Error fetching section details: ${e.localizedMessage}")
                                            }
                                        }
                                    }
                                }
                        ) {
                            Text(
                                "Section ${index + 1}: ${section.section_name}",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (expandedSectionId == section.section_id) {
                            Divider(Modifier.padding(vertical = 8.dp))

                            val contents = sectionDetailsMap[section.section_id]?.contents ?: emptyList()
                            contents.forEachIndexed { idx, content ->
                                Text("Content ${idx + 1}: ${content.content_name} (${content.content_type})")
                            }

                            Spacer(Modifier.height(12.dp))

                            // ✅ Add Content Button
                            Button(
                                onClick = {
                                    println("➕ Add Content for Section: ${section.section_id}")
                                    selectedSectionIdForContent = section.section_id
                                    expectedContentIndex = (sectionDetailsMap[section.section_id]?.contents?.size ?: 0) + 1
                                    showAddContentDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text("Add Content", color = Color.White)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (showAddSectionDialog) {
                AddSectionDialog(
                    courseId = course!!.course_id,
                    onClose = { showAddSectionDialog = false },
                    onSuccess = {
                        println("🔄 Refreshing sections after new addition...")
                        try {
                            val updatedCourse = ApiService.getCourseWithSections(course!!.course_id)
                            sections = updatedCourse.course_sections
                            println("✅ Sections updated: ${sections.map { it.section_name }}")
                        } catch (e: Exception) {
                            println("❌ Failed to refresh sections: ${e.localizedMessage}")
                        }
                    }
                )
            }

            if (showAddContentDialog && selectedSectionIdForContent != null) {
                AddContentDialog(
                    courseId = course!!.course_id,
                    sectionId = selectedSectionIdForContent!!,
                    onClose = { showAddContentDialog = false },
                    onSuccess = {
                        println("🔄 Refreshing section content after content added...")
                        try {
                            val updated = ApiService.getSectionDetails(course!!.course_id, selectedSectionIdForContent!!)
                            sectionDetailsMap = sectionDetailsMap + (selectedSectionIdForContent!! to updated)
                        } catch (e: Exception) {
                            println("❌ Failed to refresh section content: ${e.localizedMessage}")
                        }
                    }
                )
            }
        }

        // Right Panel – Course Pricing & Calendar
        Column(Modifier.weight(1f)) {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("₹${course!!.course_price}", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(if (course!!.allow_free_access) "Free Access" else "Paid Access")

                    var selected by remember { mutableStateOf("created") }
                    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

                    Row {
                        Button(onClick = { selected = "created" }) { Text("Created Date") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { selected = "expires" }, enabled = course!!.expires_at != null) {
                            Text("Expiry Date")
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.fillMaxWidth().height(300.dp)) {
                        CustomCalendarView(selectedDate) { selectedDate = it }
                    }
                }
            }
        }
    }
}


@Composable
fun AddContentDialog(courseId: String, sectionId: String, onClose: () -> Unit, onSuccess: suspend () -> Unit) {
    val contentTypes = listOf("video", "pdf", "live_video")
    var selectedContentType by remember { mutableStateOf(contentTypes[0]) }
    var expanded by remember { mutableStateOf(false) }
    var contentName by remember { mutableStateOf("") }
    var contentDescription by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var liveUrl by remember { mutableStateOf("") }
    var sdVideoUrl by remember { mutableStateOf("") }
    var hdVideoUrl by remember { mutableStateOf("") }
    var fullHdVideoUrl by remember { mutableStateOf("") }
    var gsSdVideoUrl by remember { mutableStateOf("") }
    var gsHdVideoUrl by remember { mutableStateOf("") }
    var gsFullHdVideoUrl by remember { mutableStateOf("") }
    var pdfUrl by remember { mutableStateOf("") }
    var gsPdfUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Add New Content") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Content Type", fontWeight = FontWeight.Medium)
                Box(Modifier.fillMaxWidth()) {
                    ContentTypeDropdown(
                        selectedContentType = selectedContentType,
                        onContentTypeSelected = { selectedContentType = it }
                    )
                }
                OutlinedTextField(
                    value = contentName,
                    onValueChange = { contentName = it },
                    label = { Text("Content Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = contentDescription,
                    onValueChange = { contentDescription = it },
                    label = { Text("Content Description") },
                    modifier = Modifier.fillMaxWidth().height(100.dp)
                )

                if (selectedContentType == "video") {
                    Text("SD Video")
                    FilePickerFieldWithUpload(
                        label = "Choose SD File",
                        onUploadComplete = { url ->
                            sdVideoUrl = url
                            gsSdVideoUrl = url.replace("https://storage.googleapis.com/", "gs://")
                        }
                    )

                    Text("HD Video")
                    FilePickerFieldWithUpload(
                        label = "Choose HD File",
                        onUploadComplete = { url ->
                            hdVideoUrl = url
                            gsHdVideoUrl = url.replace("https://storage.googleapis.com/", "gs://")
                        }
                    )

                    Text("Full HD Video")
                    FilePickerFieldWithUpload(
                        label = "Choose Full HD File",
                        onUploadComplete = { url ->
                            fullHdVideoUrl = url
                            gsFullHdVideoUrl = url.replace("https://storage.googleapis.com/", "gs://")
                        }
                    )
                } else if (selectedContentType == "pdf") {
                    Text("Upload PDF")
//                    FilePickerField(label = "Choose File")
                } else if (selectedContentType == "live_video") {
                    OutlinedTextField(
                        value = liveUrl,
                        onValueChange = { liveUrl = it },
                        label = { Text("Live Stream URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                scope.launch {
                    isLoading = true

                    // ✅ Validate URLs before API call
                    if (selectedContentType == "video") {
                        if (sdVideoUrl.isBlank() || !sdVideoUrl.startsWith("http")) {
                            println("❌ Invalid SD Video URL: $sdVideoUrl")
                            isLoading = false
                            return@launch
                        }
                        if (hdVideoUrl.isBlank() || !hdVideoUrl.startsWith("http")) {
                            println("❌ Invalid HD Video URL: $hdVideoUrl")
                            isLoading = false
                            return@launch
                        }
                        if (fullHdVideoUrl.isBlank() || !fullHdVideoUrl.startsWith("http")) {
                            println("❌ Invalid Full HD Video URL: $fullHdVideoUrl")
                            isLoading = false
                            return@launch
                        }
                    }

                    if (selectedContentType == "pdf") {
                        if (pdfUrl.isBlank() || !pdfUrl.startsWith("http")) {
                            println("❌ Invalid PDF URL: $pdfUrl")
                            isLoading = false
                            return@launch
                        }
                    }

                    if (selectedContentType == "live_video") {
                        if (liveUrl.isBlank()) {
                            println("❌ Live Video ID cannot be empty")
                            isLoading = false
                            return@launch
                        }
                    }

                    try {
                        val request = ContentUploadRequest(
                            content_type = selectedContentType,
                            content_name = contentName,
                            content_description = contentDescription,
                            sd_video_uri = sdVideoUrl,
                            hd_video_uri = hdVideoUrl,
                            full_hd_video_uri = fullHdVideoUrl,
                            sd_video_gs_bucket_uri = gsSdVideoUrl,
                            hd_video_gs_bucket_uri = gsHdVideoUrl,
                            full_hd_video_gs_bucket_uri = gsFullHdVideoUrl,
                            pdf_uri = if (selectedContentType == "pdf") pdfUrl else null,
                            pdf_gs_bucket_uri = if (selectedContentType == "pdf") gsPdfUrl else null,
                            live_video_id = if (selectedContentType == "live_video") liveUrl else null,
                            is_published = true // ✅ Include if required by API
                        )
                        println("📦 Final API Request Payload: $request")
                        ApiService.uploadContent(courseId, sectionId, request)
                        println("📦 Final API Request Payload: $request")
                        onSuccess()
                        onClose()
                    } catch (e: Exception) {
                        println("❌ Failed to upload content: ${e.localizedMessage}")
                    } finally {
                        isLoading = false
                    }
                }
            }) {
                Text(if (isLoading) "Uploading..." else "Add Content")
            }
        },
        dismissButton = {
            Button(onClick = onClose) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContentTypeDropdown(
    selectedContentType: String,
    onContentTypeSelected: (String) -> Unit
) {
    val contentTypes = listOf("video", "pdf", "live_video")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedContentType,
            onValueChange = {},
            readOnly = true,
            label = { Text("Content Type") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            contentTypes.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.replace("_", " ").replaceFirstChar { it.uppercase() }) },
                    onClick = {
                        onContentTypeSelected(type)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun FilePickerFieldWithUpload(
    label: String,
    allowedExtensions: List<String> = listOf("mp4", "mov", "avi", "mkv", "pdf"),
    onUploadComplete: (String) -> Unit
) {
    var showFilePicker by remember { mutableStateOf(false) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadedUrl by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column {
        Box(modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isUploading) { showFilePicker = true }) {
            OutlinedTextField(
                value = selectedFile?.name ?: "",
                onValueChange = {},
                label = { Text(label) },
                readOnly = true,
                enabled = false, // prevents manual editing
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (isUploading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        uploadedUrl?.let {
            Text("✅ Uploaded: ${File(it).name}", style = MaterialTheme.typography.bodySmall)
        }
    }

    FilePicker(
        show = showFilePicker,
        fileExtensions = allowedExtensions
    ) { platformFile ->
        showFilePicker = false
        platformFile?.path?.let { path ->
            val file = File(path)
            selectedFile = file
            isUploading = true
            scope.launch {
                try {
                    val resultUrl = GCSUploader.testVideoUpload(file)
                    uploadedUrl = resultUrl
                    onUploadComplete(resultUrl)
                } catch (e: Exception) {
                    println("❌ Upload failed: ${e.message}")
                    e.printStackTrace()
                } finally {
                    isUploading = false
                }
            }
        }
    }
}

@Composable
fun AddSectionDialog(
    courseId: String,
    onClose: () -> Unit,
    onSuccess: suspend () -> Unit // ✅ NEW
) {
    var sectionName by remember { mutableStateOf("") }
    var sectionDescription by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Add New Section") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = sectionName,
                    onValueChange = { sectionName = it },
                    label = { Text("Section Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = sectionDescription,
                    onValueChange = { sectionDescription = it },
                    label = { Text("Section Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    println("Sending Section Request:")
                    println("Course ID: $courseId")
                    println("Name: $sectionName")
                    println("Description: $sectionDescription")

                    scope.launch {
                        try {
                            val request = SectionRequest(
                                section_name = sectionName,
                                section_description = sectionDescription
                            )
                            val response = ApiService.createSection(courseId = courseId, request = request)
                            println("Section Created: $response")

                            onSuccess()
                        } catch (e: Exception) {
                            println("Section Creation Failed: ${e.localizedMessage}")
                        } finally {
                            isLoading = false
                            onClose()
                        }
                    }
                },
                enabled = sectionName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text(if (isLoading) "Adding..." else "Add Section")
            }
        },
        dismissButton = {
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
            ) {
                Text("Cancel", color = Color.Black)
            }
        }
    )
}

// UI helpers
@Composable
fun CourseChip(text: String, color: Color) {
    Surface(color = color.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp)) {
        Text(text = text, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = color, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AsyncImage(url: String?, modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Color.LightGray), contentAlignment = Alignment.Center) {
        Text("Image here")
    }
}

@Composable
fun CustomCalendarView(selectedDate: LocalDate?, onDateSelected: (LocalDate) -> Unit) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val startMonth = remember { today.minus(DatePeriod(months = 12)).yearMonth }
    val endMonth = remember { today.plus(DatePeriod(months = 12)).yearMonth }
    val state = rememberCalendarState(startMonth = startMonth, endMonth = endMonth, firstDayOfWeek = DayOfWeek.MONDAY)

    VerticalCalendar(
        state = state,
        dayContent = { day ->
            val isSelected = day.date == selectedDate
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) Color(0xFF2563EB) else Color.Transparent)
                    .clickable { onDateSelected(day.date) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = day.date.dayOfMonth.toString(), color = if (isSelected) Color.White else Color.Black)
            }
        },
        monthHeader = { month ->
            Text(text = month.yearMonth.toString(), modifier = Modifier.padding(8.dp))
        }
    )
}