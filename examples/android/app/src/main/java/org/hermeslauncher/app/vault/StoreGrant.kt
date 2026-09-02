package org.hermeslauncher.app.vault

object StoreGrant {
    fun policy(packageName: String, storeContent: Boolean, storeImages: Boolean): AppStorePolicy? {
        if (!storeContent) {
            return null
        }
        return AppStorePolicy(packageName, storeContent = true, storeImages = storeImages)
    }
}
