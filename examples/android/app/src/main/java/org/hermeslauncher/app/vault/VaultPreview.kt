package org.hermeslauncher.app.vault

import org.json.JSONObject

data class VaultPreview(
    val subText: String? = null,
    val bigText: String? = null,
    val infoText: String? = null,
    val summaryText: String? = null,
    val imageRef: String? = null,
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val imageIsLargeIcon: Boolean = false,
) {
    fun body(fallback: String?): String {
        return bigText?.takeIf { it.isNotBlank() } ?: fallback.orEmpty()
    }

    fun caption(): String {
        return listOfNotNull(subText, infoText, summaryText)
            .firstOrNull { it.isNotBlank() }
            .orEmpty()
    }

    fun encode(): String {
        val json = JSONObject()
        subText?.let { json.put("subText", it) }
        bigText?.let { json.put("bigText", it) }
        infoText?.let { json.put("infoText", it) }
        summaryText?.let { json.put("summaryText", it) }
        imageRef?.let { json.put("imageRef", it) }
        if (imageWidth > 0) json.put("imageWidth", imageWidth)
        if (imageHeight > 0) json.put("imageHeight", imageHeight)
        if (imageIsLargeIcon) json.put("imageIsLargeIcon", true)
        return json.toString()
    }

    fun withImage(
        ref: String,
        width: Int = 0,
        height: Int = 0,
        largeIcon: Boolean = false,
    ): VaultPreview {
        return copy(
            imageRef = ref,
            imageWidth = width,
            imageHeight = height,
            imageIsLargeIcon = largeIcon,
        )
    }

    companion object {
        fun parse(raw: String?): VaultPreview {
            if (raw.isNullOrBlank()) {
                return VaultPreview()
            }
            return runCatching {
                val json = JSONObject(raw)
                VaultPreview(
                    subText = json.optString("subText").ifBlank { null },
                    bigText = json.optString("bigText").ifBlank { null },
                    infoText = json.optString("infoText").ifBlank { null },
                    summaryText = json.optString("summaryText").ifBlank { null },
                    imageRef = json.optString("imageRef").ifBlank { null },
                    imageWidth = json.optInt("imageWidth", 0),
                    imageHeight = json.optInt("imageHeight", 0),
                    imageIsLargeIcon = json.optBoolean("imageIsLargeIcon", false),
                )
            }.getOrDefault(VaultPreview())
        }
    }
}
