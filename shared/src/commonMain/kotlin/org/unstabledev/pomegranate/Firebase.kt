package org.unstabledev.pomegranate

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.contentType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


object Firebase {
    val url = Secrets.firebaseLink
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
}