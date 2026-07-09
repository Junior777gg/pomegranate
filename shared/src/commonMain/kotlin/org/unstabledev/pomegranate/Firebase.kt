package org.unstabledev.pomegranate

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException


object Firebase {
    val url: String
        get() = AppSettings.state.value.selectedFirebaseUrl
    val client = HttpClient()

    inline fun <reified T> serializer (value: T): String {
        try {
        return Json.encodeToString<T>(value)
        } catch (e: Exception) {
            return ""
        }
    }

    inline fun <reified T>deserializer(json: String): T?{
        try {
        return Json.decodeFromString<T>(json)}catch (e: Exception){
            return null
        }
    }

    suspend inline fun <reified T>get(path: String): T?{
        return deserializer<T>(client.get("$url$path.json").body<String>())
    }

    suspend inline fun <reified T> put(path: String, value: T) {
        try {
        client.put("$url$path.json") {
            setBody(serializer(value))
        }}catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun delete(path: String) {
        client.delete("$url$path.json")
    }

    suspend fun isAvailable(): Boolean {
        return try {
            val response = client.request("$url.json?shallow=true") { method = io.ktor.http.HttpMethod.Head }
            response.status.value in 200..499
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun isAvailable(address: String): Boolean {
        val normalizedAddress = address
            .trim()
            .removeSuffix(".json")
            .trimEnd('/')

        if (normalizedAddress.isBlank()) return false

        return try {
            val response = client.get("$normalizedAddress/.json") {
                parameter("shallow", "true")
            }

            response.status.value in 200..299
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        }
    }
}