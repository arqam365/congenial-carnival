package com.nextlevelprogrammers.elearn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(onCategoryClick: () -> Unit,
                    onCourseClick: () -> Unit,
                    onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text("Welcome to Dashboard", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onLogout) {
            Text("Logout")
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = "Course Management",
                    description = "Learn to use HeroUI for amazing UI components.",
                    gradientColors = listOf(Color(0xFFB2EBF2), Color(0xFF81D4FA)),
                    modifier = Modifier.weight(1f),
                    onClick = onCourseClick
                )
                DashboardCard(
                    title = "Category Management",
                    description = "Manage your category.",
                    gradientColors = listOf(Color(0xFF9575CD), Color(0xFF7E57C2)),
                    modifier = Modifier.weight(1f),
                    onClick = onCategoryClick
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DashboardCard(
                    title = "Razorpay",
                    description = "View your Razorpay dashboard & payments.",
                    gradientColors = listOf(Color(0xFFE1BEE7), Color(0xFFCE93D8)),
                    modifier = Modifier.weight(1f)
                )
                DashboardCard(
                    title = "Analytics",
                    description = "View Google Analytics dashboards.",
                    gradientColors = listOf(Color(0xFFF48FB1), Color(0xFFF06292)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    description: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .aspectRatio(2f)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(color = Color.Black),
            )
            Column {
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    text = description,
                    color = Color.Black.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                )
            }
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.2f))
            ) {
                Text("Visit", color = Color.Black)
            }
        }
    }
}