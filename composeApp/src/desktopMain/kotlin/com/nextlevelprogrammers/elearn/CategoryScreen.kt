package com.nextlevelprogrammers.elearn

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class Category(
    val name: String,
    val description: String,
    val courses: Int,
    val status: Boolean,
    val createdAt: String
)

@Composable
fun CategoryScreen(onBack: () -> Unit) {
    val categories = remember { mutableStateListOf<Category>() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }

    fun fetchCategories() {
        scope.launch {
            isLoading = true
            try {
                val result = ApiService.getCategories()
                categories.clear()
                categories.addAll(result)
            } catch (e: Exception) {
                println("❌ Failed to load categories: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchCategories()
    }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Categories Management", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = { /* Add logic later */ }) {
                Text("Add New Category")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        CategoryTableHeader()

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories) { category ->
                CategoryRow(
                    category = category,
                    onDelete = {
                        scope.launch {
//                            ApiService.deleteCategory(category)
                            fetchCategories()
                        }
                    },
                    onViewCourses = { /* future modal or navigation */ },
                    onToggleStatus = {
                        scope.launch {
//                            ApiService.toggleCategoryStatus(category.category_id, !category.status)
                            fetchCategories()
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onBack) {
            Text("Back to Dashboard")
        }
    }
}

@Composable
fun CategoryTableHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TableCell("Name", bold = true, modifier = Modifier.weight(1f))
        TableCell("Description", bold = true, modifier = Modifier.weight(1f))
        TableCell("Courses", bold = true, modifier = Modifier.weight(1f))
        TableCell("Status", bold = true, modifier = Modifier.weight(1f))
        TableCell("Created At", bold = true, modifier = Modifier.weight(1f))
        TableCell("View", bold = true, modifier = Modifier.weight(1f))
        TableCell("Toggle", bold = true, modifier = Modifier.weight(1f))
        TableCell("Delete", bold = true, modifier = Modifier.weight(1f))
    }
}

@Composable
fun CategoryRow(
    category: Category,
    onDelete: () -> Unit,
    onViewCourses: () -> Unit,
    onToggleStatus: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TableCell(category.name, modifier = Modifier.weight(1f))
        TableCell(category.description, modifier = Modifier.weight(1f))
        TableCell(category.courses.toString(), modifier = Modifier.weight(1f))
        TableCell(
            text = if (category.status) "Active" else "Inactive",
            color = if (category.status) Color(0xFF81C784) else Color.Gray,
            modifier = Modifier.weight(1f)
        )
        TableCell(category.createdAt, modifier = Modifier.weight(1f))
        TableCell(modifier = Modifier.weight(1f)) {
            Button(onClick = onViewCourses) {
                Text("View Courses")
            }
        }
        TableCell(modifier = Modifier.weight(1f)) {
            Switch(checked = category.status, onCheckedChange = { onToggleStatus() })
        }
        TableCell(modifier = Modifier.weight(1f)) {
            Button(onClick = onDelete, colors = ButtonDefaults.buttonColors(Color.Red)) {
                Text("Delete", color = Color.White)
            }
        }
    }
}

@Composable
fun TableCell(
    text: String = "",
    bold: Boolean = false,
    color: Color = Color.Black,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = text,
                color = color,
                fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp
            )
        }
    }
}