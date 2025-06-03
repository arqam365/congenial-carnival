package com.nextlevelprogrammers.elearn

@kotlinx.serialization.Serializable
data class SectionRequest(
    val section_name: String,
    val section_description: String
)