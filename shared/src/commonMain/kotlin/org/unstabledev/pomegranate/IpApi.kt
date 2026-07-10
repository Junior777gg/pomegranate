package org.unstabledev.pomegranate

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.unstabledev.pomegranate.screen.Profile

object IpApi {
    val client = HttpClient()
    val baseUrl = "http://ip-api.com/json/"
    val params = "status,message,country,countryCode,timezone,proxy,query"
    suspend fun getLocation(ip : String): IPLocation? {
        val response = client.get("$baseUrl/$ip?fields=$params")
        if(response.status.value==200) return Json { ignoreUnknownKeys = true }.decodeFromString(IPLocation.serializer(), response.bodyAsText())
        return null
    }
}

@Serializable
data class IPLocation(
    val status: String = "",
    val query: String = "",
    @SerialName("country") val country: String = "",
    @SerialName("country_code") val countryCode: String = "",
    @SerialName("proxy") val proxy: Boolean = false,
) {}