package com.nextlevelprogrammers.elearn

import kotlinx.serialization.Serializable

@Serializable
data class ContentUploadResponse(
    val message: String,
    val data: ContentData
)

@Serializable
data class ContentData(
    val content_id: String,
    val section_id: String,
    val content_name: String,
    val content_description: String,
    val content_type: String,
    val sd_video_uri: String? = null,
    val hd_video_uri: String? = null,
    val full_hd_video_uri: String? = null,
    val sd_video_gs_bucket_uri: String? = null,
    val hd_video_gs_bucket_uri: String? = null,
    val full_hd_video_gs_bucket_uri: String? = null,
    val pdf_uri: String? = null,
    val pdf_gs_bucket_uri: String? = null,
    val live_video_id: String? = null,
    val content_index: Int? = null,
    val is_published: Boolean? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)