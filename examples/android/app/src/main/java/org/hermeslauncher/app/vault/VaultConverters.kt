package org.hermeslauncher.app.vault

import androidx.room.TypeConverter

class VaultConverters {
    @TypeConverter
    fun toItemType(raw: String): VaultItemType {
        return VaultItemType.valueOf(raw)
    }

    @TypeConverter
    fun fromItemType(type: VaultItemType): String {
        return type.name
    }
}
