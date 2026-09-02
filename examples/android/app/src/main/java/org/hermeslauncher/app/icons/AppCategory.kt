package org.hermeslauncher.app.icons

enum class AppCategoryKind {
    GAME,
    AUDIO,
    VIDEO,
    IMAGE,
    SOCIAL,
    NEWS,
    MAPS,
    PRODUCTIVITY,
    ACCESSIBILITY,
    OTHER,
}

object AppCategory {
    fun kind(category: Int): AppCategoryKind {
        return when (category) {
            0 -> AppCategoryKind.GAME
            1 -> AppCategoryKind.AUDIO
            2 -> AppCategoryKind.VIDEO
            3 -> AppCategoryKind.IMAGE
            4 -> AppCategoryKind.SOCIAL
            5 -> AppCategoryKind.NEWS
            6 -> AppCategoryKind.MAPS
            7 -> AppCategoryKind.PRODUCTIVITY
            8 -> AppCategoryKind.ACCESSIBILITY
            else -> AppCategoryKind.OTHER
        }
    }

    fun kindOf(packageName: String, category: Int): AppCategoryKind {
        if (packageName.isBlank()) {
            return AppCategoryKind.OTHER
        }
        return kind(category)
    }

    fun labelKey(kind: AppCategoryKind): String {
        return kind.name.lowercase()
    }
}
