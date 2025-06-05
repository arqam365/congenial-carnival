package com.nextlevelprogrammers.elearn

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive

object ApiService {
    private const val BASE_URL = "https://production-begonia-orchid-977741295366.asia-south1.run.app/v1"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    // 🔹 Fetch Categories
    suspend fun getCategories(showInactive: Boolean = false): List<Category> {
        val response: CategoryResponse = client.get("$BASE_URL/categories") {
            url {
                parameters.append("show_inactive", showInactive.toString())
            }
        }.body()

        return response.data.map {
            Category(
                name = it.category_name,
                description = it.category_description,
                courses = it.courses.size,
                status = it.is_active,
                createdAt = it.createdAt
            )
        }
    }

    suspend fun getAllCategoriesDto(showInactive: Boolean = false): List<CategoryDto> {
        val response: CategoryResponse = client.get("$BASE_URL/categories") {
            url {
                parameters.append("show_inactive", showInactive.toString())
            }
        }.body()

        return response.data
    }

    // 🔹 Toggle Category Status
    suspend fun toggleCategoryStatus(categoryId: String, isActive: Boolean) {
        client.patch("$BASE_URL/categories/$categoryId/switch-status") {
            setBody(mapOf("is_active" to !isActive))
        }
    }

    // 🔹 Fetch Courses by Category ID
    suspend fun getCoursesByCategory(categoryId: String): List<Course> {
        val response: CourseResponse = client.get("$BASE_URL/categories/$categoryId/courses").body()
        return response.data.map {
            Course(
                name = it.course_name,
                price = it.course_price,
                isPublished = it.is_published,
                allowFree = it.allow_free_access,
                expiresAt = it.expires_at ?: "No expiry",
                createdAt = it.createdAt
            )
        }
    }

    suspend fun deleteCategory(categoryId: String) {
        client.delete("$BASE_URL/course/categories/$categoryId")
    }

    suspend fun createCourse(course: CourseRequest): String {
        val response: ApiResponse = client.post("$BASE_URL/course") {
            contentType(ContentType.Application.Json)
            setBody(course)
        }.body()
        return response.message
    }

    suspend fun searchCourses(query: String, page: Int, pageSize: Int): Pair<List<CourseDto>, Int> {
        val response: SearchCourseResponse = client.get("$BASE_URL/course/search") {
            parameter("query", query)
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body()

        return response.data.courses to response.data.count
    }

    suspend fun getSectionDetails(courseId: String, sectionId: String): SectionResponse {
        return client.get("$BASE_URL/course/$courseId/section/$sectionId")
            .body<SecResponse<SectionResponse>>()
            .data
    }

    suspend fun createSection(courseId: String, request: SectionRequest): String {
        val response: ApiResponse = client.post("$BASE_URL/course/$courseId/section") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
        return response.message
    }

    suspend fun getCourseWithSections(courseId: String): CourseDto {
        return client.get("$BASE_URL/course/$courseId") {
            url {
                parameters.append("allow_unpublished", "true")
                parameters.append("allow_expired", "true")
            }
        }.body<SecResponse<CourseDto>>().data
    }

    // Add more API functions as needed...
}