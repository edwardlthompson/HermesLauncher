package org.hermeslauncher.app.vault

import org.json.JSONObject

data class VaultPreview(
    val subText: String? = null,
    val bigText: String? = null,
    val infoText: String? = null,
    val summaryText: String? = null,
    val imageRef: String? = null,
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
        return json.toString()
    }

    fun withImage(ref: String): VaultPreview {
        return copy(imageRef = ref)
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
                )
            }.getOrDefault(VaultPreview())
        }
    }
}
