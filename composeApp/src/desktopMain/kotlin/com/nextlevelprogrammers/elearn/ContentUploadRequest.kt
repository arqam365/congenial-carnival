package com.nextlevelprogrammers.elearn

import kotlinx.serialization.Serializable

@Serializable
data class ContentUploadRequest(
    val content_type: String,
    val content_name: String,
    val content_description: String,
    val sd_video_uri: String? = null,
    val hd_video_uri: String? = null,
    val full_hd_video_uri: String? = null,
    val sd_video_gs_bucket_uri: String? = null,
    val hd_video_gs_bucket_uri: String? = null,
    val full_hd_video_gs_bucket_uri: String? = null,
    val pdf_uri: String? = null,
    val pdf_gs_bucket_uri: String? = null,
    val live_video_id: String? = null,
    val is_published: Boolean = true
)