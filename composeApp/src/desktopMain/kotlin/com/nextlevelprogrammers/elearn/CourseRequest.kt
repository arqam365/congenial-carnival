package com.nextlevelprogrammers.elearn

import kotlinx.serialization.Serializable

@Serializable
data class CourseRequest(
    val course_name: String,
    val course_description: String,
    val course_price: Int,
    val allow_free_access: Boolean,
    val can_be_purchased: Boolean,
    val is_published: Boolean,
    val expires_at: String?, // Optional
    val category_id: String,
    val head_img: String
)