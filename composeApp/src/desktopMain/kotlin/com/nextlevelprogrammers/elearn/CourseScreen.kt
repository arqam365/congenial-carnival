package com.nextlevelprogrammers.elearn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun CourseScreen(onBack: () -> Unit,onCourseClick: (CourseDto) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    var courses by remember { mutableStateOf<List<CourseDto>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var pendingCourse by remember { mutableStateOf<CourseRequest?>(null) }


    // Handle POST request for adding a new course
    LaunchedEffect(pendingCourse) {
        pendingCourse?.let {
            try {
                ApiService.createCourse(it)
                println("✅ Course created!")
                performSearch(searchQuery, onResult = { courses = it }, onLoading = { loading = it })
            } catch (e: Exception) {
                println("❌ Failed to create course: ${e.message}")
            } finally {
                pendingCourse = null
            }
        }
    }


    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Courses Management", style = MaterialTheme.typography.headlineMedium)
            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
            ) {
                Text("Create New Course")
            }
        }

        Spacer(Modifier.height(16.dp))

        SearchBar(
            searchQuery = searchQuery,
            onSearchChange = { searchQuery = it },
            onSearch = {
                performSearch(searchQuery, onResult = { courses = it }, onLoading = { loading = it })
            },
            onReset = {
                searchQuery = ""
                performSearch("", onResult = { courses = it }, onLoading = { loading = it })
            }
        )
        Spacer(Modifier.height(24.dp))


        if (loading) {
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (courses.isEmpty()) {
            Box(
                Modifier.fillMaxWidth().height(65.dp).background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Text("No courses found", color = Color.Gray, fontSize = 16.sp)
            }
        } else {
            CourseTable(courses = courses, onCourseClick = { selectedCourse ->
                onCourseClick(selectedCourse)
            })
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Back to Dashboard", color = Color.White)
        }
    }

    if (showDialog) {
        AddCourseDialog(
            onClose = { showDialog = false },
            onSubmit = { courseRequest ->
                pendingCourse = courseRequest
                showDialog = false
            }
        )
    }
}

// 🔍 Search logic (runs only on click or Enter)
fun performSearch(
    query: String,
    onResult: (List<CourseDto>) -> Unit,
    onLoading: (Boolean) -> Unit
) {
    println("🔍 Searching for: $query")
    onLoading(true)

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val (courses, _) = ApiService.searchCourses(query, page = 1, pageSize = 10)
            withContext(Dispatchers.Main) {
                onResult(courses)
            }
        } catch (e: Exception) {
            println("❌ Failed to search courses: ${e.message}")
        } finally {
            withContext(Dispatchers.Main) {
                onLoading(false)
            }
        }
    }
}

@Composable
fun CourseTable(courses: List<CourseDto>, onCourseClick: (CourseDto) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5))
                .padding(vertical = 8.dp, horizontal = 12.dp)
        ) {
            TableHeader("ID", 2f)
            TableHeader("NAME", 2f)
            TableHeader("PRICE ₹", 1f)
            TableHeader("CATEGORY", 1f)
            TableHeader("STATUS", 1f)
            TableHeader("CREATED AT", 1f)
        }

        HorizontalDivider()

        // Rows
        LazyColumn {
            items(courses) { course ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCourseClick(course) }
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                ) {
                    TableCell(course.course_id, 2f)
                    TableCell(course.course_name, 2f)
                    TableCell("₹${course.course_price}", 1f)
                    TableCell(course.category_id, 1f)
                    TableCell(if (course.is_published) "Published" else "Draft", 1f)
                    TableCell(course.createdAt.substringBefore("T"), 1f)
                }
                Divider(color = Color(0xFFE0E0E0))
            }
        }
    }
}

@Composable
fun RowScope.TableHeader(text: String, weight: Float) {
    Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(weight)
    )
}

@Composable
fun RowScope.TableCell(text: String, weight: Float) {
    Text(
        text = text,
        fontSize = 14.sp,
        modifier = Modifier.weight(weight),
        maxLines = 1
    )
}

@Composable
fun AddCourseDialog(
    onClose: () -> Unit,
    onSubmit: (CourseRequest) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("100") }
    var allowFreeAccess by remember { mutableStateOf(false) }
    var canBePurchased by remember { mutableStateOf(true) }
    var isPublished by remember { mutableStateOf(false) }
    var expiresAt by remember { mutableStateOf("") }
    var headImg by remember { mutableStateOf("") }

    var categories by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<CategoryDto?>(null) }

    // Fetch categories on open
    LaunchedEffect(Unit) {
        println("🔄 Fetching categories...")
        try {
            categories = ApiService.getAllCategoriesDto()
            println("✅ Categories fetched: ${categories.map { it.category_name }}")
        } catch (e: Exception) {
            println("❌ Failed to fetch categories: ${e.message}")
        }
    }

    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Add New Course") },
        text = {
            Column(modifier=Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course Name") })
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })

                // Dropdown with logs
                CategoryDropdown(
                    categories = categories,
                    selectedCategory = selectedCategory,
                    onCategorySelected = {
                        selectedCategory = it
                        println("📌 Selected Category: ${it.category_name} (${it.category_id})")
                    }
                )

                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (INR)") })
                OutlinedTextField(value = expiresAt, onValueChange = { expiresAt = it }, label = { Text("Expiry Date (optional)") })
                OutlinedTextField(value = headImg, onValueChange = { headImg = it }, label = { Text("Head Image URL") })

                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = allowFreeAccess, onCheckedChange = { allowFreeAccess = it }); Text("Allow Free Access") }
                Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = canBePurchased, onCheckedChange = { canBePurchased = it }); Text("Can be Purchased") }
                Row (verticalAlignment = Alignment.CenterVertically){ Checkbox(checked = isPublished, onCheckedChange = { isPublished = it }); Text("Publish Course") }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedCategory == null) {
                        println("⚠️ Cannot submit: No category selected")
                        return@Button
                    }

                    val request = CourseRequest(
                        course_name = name,
                        course_description = description,
                        course_price = price.toIntOrNull() ?: 0,
                        allow_free_access = allowFreeAccess,
                        can_be_purchased = canBePurchased,
                        is_published = isPublished,
                        expires_at = expiresAt.ifBlank { null },
                        category_id = selectedCategory!!.category_id,
                        head_img = headImg
                    )

                    println("📤 Submitting Course: $request")
                    onSubmit(request)
                    onClose()
                }
            ) {
                Text("Create Course")
            }
        },
        dismissButton = {
            TextButton(onClick = onClose) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDropdown(
    categories: List<CategoryDto>,
    selectedCategory: CategoryDto?,
    onCategorySelected: (CategoryDto) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedCategory?.category_name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor() // This replaces `.clickable`
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category.category_name) },
                    onClick = {
                        onCategorySelected(category)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), shape = RoundedCornerShape(25))
            .padding( 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            shape = RoundedCornerShape(25),
            onValueChange = onSearchChange,
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            placeholder = { Text("Search courses...") },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() })
        )
        Button(
            onClick = onSearch,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
            Text("Search")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA))
        ) {
            Text("Reset")
        }
    }
}