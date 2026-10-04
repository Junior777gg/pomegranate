package org.unstabledev.pomegranate.common

import io.ktor.utils.io.core.toByteArray
import kotlinx.serialization.Serializable

@Serializable
data class SecurityConfig(
    var allowText: Boolean = true,
    var allowImages: Boolean = true,
    var allowAudio: Boolean = true,
    var allowFiles: Boolean = true,
    var allowCalls: Boolean = true,
) {
    override fun toString(): String {
        var out=""
        out += if(allowText) "1" else "0"
        out += if(allowImages) "1" else "0"
        out += if(allowAudio) "1" else "0"
        out += if(allowFiles) "1" else "0"
        out += if(allowCalls) "1" else "0"
        return out
    }
    fun fromString(str: String) {
        if (str.length!=5) return
        fun b(str: Char) = str=='1'
        allowText=b(str[0])
        allowImages=b(str[1])
        allowAudio=b(str[2])
        allowFiles=b(str[3])
        allowCalls=b(str[4])
    }
    fun toByteArray(): ByteArray {
        return this.toString().toByteArray()
    }
    fun fromByteArray(bytes: ByteArray) {
        fromString(bytes.decodeToString())
    }

    companion object {
    }
}