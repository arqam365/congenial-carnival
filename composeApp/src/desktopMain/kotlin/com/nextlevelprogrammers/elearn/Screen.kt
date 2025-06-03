package com.nextlevelprogrammers.elearn

sealed class Screen {
    object Dashboard : Screen()
    object Category : Screen()
    object Course : Screen()
    data class Section(val courseId: String) : Screen()
}