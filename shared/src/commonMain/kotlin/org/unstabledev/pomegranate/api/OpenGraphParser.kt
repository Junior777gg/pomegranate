package org.unstabledev.pomegranate.api

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.network.parseGetRequest

data class OpenGraphDescriptor(
    val title: String?,
    val description: String?,
    val type: String?,
    val imageUrl: String?,
    val url: String?,
    val siteName: String?,
)

object OpenGraphParser {
    suspend fun parse(url: String): OpenGraphDescriptor? {
        try {
            val response = Ksoup.parseGetRequest(url = url)
            fun get(property: String): String? {
                return response.select("meta[property=$property]").attr("content").takeIf { it.isNotBlank() }
                    ?: response.select("meta[name=$property]").attr("content").takeIf { it.isNotBlank() }
            }
            return OpenGraphDescriptor(
                get("og:title") ?: response.title(),
                get("og:description"),
                get("og:type"),
                get("og:image"),
                get("og:url"),
                get("og:sitename")
            )
        } catch (e: Exception) {
            println("Failed to parse OpenGraph: "+(e.message?: ""))
            return null
        }
    }
}