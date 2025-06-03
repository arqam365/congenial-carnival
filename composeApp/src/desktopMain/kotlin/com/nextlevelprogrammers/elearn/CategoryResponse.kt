package com.nextlevelprogrammers.elearn

import kotlinx.serialization.Serializable

@Serializable
data class CategoryDto(
    val category_id: String,
    val category_name: String,
    val category_description: String,
    val is_active: Boolean,
    val createdAt: String,
    val courses: List<CourseRef>
)

@Serializable
data class CourseRef(val course_id: String)

@Serializable
data class CategoryResponse(val data: List<CategoryDto>)