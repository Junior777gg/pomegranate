package org.unstabledev.pomegranate

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.headers

object Gravatar {
    val client = HttpClient()
    val apiKey = "9502:gk-DeJWwC3OtiKCelboht8NXNftgAWGbDaNeacM09C6CQp9gwpXIf9YwYEDqk5dR"
    val baseUrl = "https://api.gravatar.com/v3"
    suspend fun getProfile(email : String):String{
        val response = client.get("$baseUrl/profiles/$email"){
            headers {
                append(HttpHeaders.Authorization, "Bearer $apiKey")
            }
        }
        return response.bodyAsText()
    }
}