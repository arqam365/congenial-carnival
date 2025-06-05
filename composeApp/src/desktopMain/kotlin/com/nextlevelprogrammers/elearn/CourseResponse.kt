package com.nextlevelprogrammers.elearn

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class CourseDto(
    val course_id: String,
    val course_name: String,
    val course_description: String,
    val course_price: Int,
    val category_id: String,
    val head_img: String? = null,
    val head_img_bucket_uri: String? = null,
    val allow_free_access: Boolean,
    val can_be_purchased: Boolean,
    val is_published: Boolean,
    val expires_at: String?,
    val createdAt: String,
    val updatedAt: String,
    val category: EmbeddedCategoryDto? = null,
    val course_sections: List<Section> = emptyList()
)

@Serializable
data class CourseResponse(val data: List<CourseDto>)

data class Course(
    val name: String,
    val price: Int,
    val isPublished: Boolean,
    val allowFree: Boolean,
    val expiresAt: String,
    val createdAt: String
)

@Serializable
data class ApiResponse(val message: String)

@Serializable
data class CourseSearchInnerData(
    val courses: List<CourseDto>,
    val count: Int
)

@Serializable
data class SearchCourseResponse(
    val data: CourseSearchInnerData
)

@Serializable
data class EmbeddedCategoryDto(
    val category_id: String,
    val category_name: String,
    val category_description: String,
    val is_active: Boolean,
    val createdAt: String,
    val updatedAt: String
)