package com.nextlevelprogrammers.elearn

@kotlinx.serialization.Serializable
data class Section(
    val section_id: String,
    val section_name: String,
    val section_description: String
)
@kotlinx.serialization.Serializable
data class SectionResponse(
    val section_id: String,
    val section_name: String,
    val section_description: String,
    val section_index: Int,
    val contents: List<SectionDto>
)

@kotlinx.serialization.Serializable
data class SectionDto(
    val content_id: String,
    val content_name: String?,
    val content_type: String,
    val pdf_uri: String? = null,
    val full_hd_video_uri: String? = null,
    val hd_video_uri: String? = null,
    val sd_video_uri: String? = null,
    val content_index: Int
)

@kotlinx.serialization.Serializable
data class SecResponse<T>(
    val message: String? = null,
    val data: T
)