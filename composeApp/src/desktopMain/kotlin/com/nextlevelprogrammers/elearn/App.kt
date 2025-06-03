package com.nextlevelprogrammers.elearn

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var token by remember { mutableStateOf(TokenStorage.getToken()) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    MaterialTheme {
        when {
            token == null -> {
                SignInScreen { authToken ->
                    token = authToken
                    TokenStorage.saveToken(authToken)
                    currentScreen = Screen.Dashboard
                }
            }

            else -> {
                when (val screen = currentScreen) {
                    is Screen.Dashboard -> DashboardScreen(
                        onCategoryClick = { currentScreen = Screen.Category },
                        onCourseClick = { currentScreen = Screen.Course },
                        onLogout = {
                            TokenStorage.clearToken()
                            token = null
                        }
                    )
                    is Screen.Category -> CategoryScreen(onBack = { currentScreen = Screen.Dashboard })
                    is Screen.Course -> CourseScreen(
                        onBack = { currentScreen = Screen.Dashboard },
                        onCourseClick = { course -> currentScreen = Screen.Section(course.course_id) }
                    )
                    is Screen.Section -> SectionScreen(courseId = screen.courseId, onBack = { currentScreen = Screen.Course })
                }
            }
        }
    }
}