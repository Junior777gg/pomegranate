package org.unstabledev.pomegranate

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.screen.Profile

object Gravatar {
    val client = HttpClient()
    val apiKey = Secrets.gravatarApiKey
    val baseUrl = "https://api.gravatar.com/v3"
    suspend fun getProfile(email : String): Profile? {
        val response = client.get("$baseUrl/profiles/$email"){
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
            }
        }
        if(response.status.value==200) return Json { ignoreUnknownKeys = true }.decodeFromString(Profile.serializer(), response.bodyAsText())
        return null
    }
}