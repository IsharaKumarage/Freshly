package com.freshly.app.utils

import android.util.Base64

object ImageUtil {
    /**
     * Converts a string which can be either a normal URL/file/content URI or a data URI (base64)
     * into a model that Glide.load(...) can accept. For data URIs, returns a ByteArray.
     */
    fun asGlideModel(source: String?): Any? {
        if (source.isNullOrBlank()) return null
        return if (source.startsWith("data:image", ignoreCase = true)) {
            val comma = source.indexOf(',')
            if (comma != -1 && comma + 1 < source.length) {
                val base64 = source.substring(comma + 1)
                try {
                    Base64.decode(base64, Base64.NO_WRAP)
                } catch (_: Exception) {
                    null
                }
            } else null
        } else {
            source
        }
    }
}
