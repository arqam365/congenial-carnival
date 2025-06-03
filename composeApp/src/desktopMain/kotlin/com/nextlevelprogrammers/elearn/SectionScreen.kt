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
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.yearMonth
import kotlinx.coroutines.launch
import kotlinx.datetime.*

@Composable
fun SectionScreen(courseId: String, onBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    var showAddSectionDialog by remember { mutableStateOf(false) }
    var expandedSectionId by remember { mutableStateOf<String?>(null) }
    var sectionDetailsMap by remember { mutableStateOf<Map<String, SectionResponse>>(emptyMap()) }
    var course by remember { mutableStateOf<CourseDto?>(null) }
    var sections by remember { mutableStateOf<List<Section>>(emptyList()) }

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
                                    // TODO: Open add content dialog or screen
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