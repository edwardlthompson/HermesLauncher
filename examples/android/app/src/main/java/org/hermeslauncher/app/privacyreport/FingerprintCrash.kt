package org.hermeslauncher.app.privacyreport

import java.security.MessageDigest

object FingerprintCrash {
    fun of(stack: String?, exceptionType: String? = null): String {
        val cleaned = SanitizeReport.text(stack, stack = true)
        val frames = cleaned.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(12)
            .toList()
        val kind = (exceptionType ?: guessType(cleaned)).trim().ifEmpty { "Error" }
        val payload = kind + "\n" + frames.joinToString("\n")
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { b -> "%02x".format(b) }.take(12)
    }

    private fun guessType(stack: String): String {
        val first = stack.lineSequence().firstOrNull()?.trim().orEmpty()
        val match = Regex("^([A-Za-z][A-Za-z0-9_.$]+)").find(first)
        return match?.groupValues?.get(1) ?: "Error"
    }
}
