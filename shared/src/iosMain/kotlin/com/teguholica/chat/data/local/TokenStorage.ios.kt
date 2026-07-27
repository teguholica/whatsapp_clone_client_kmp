package com.teguholica.chat.data.local

import platform.Foundation.NSUserDefaults

actual class TokenStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun saveAccessToken(token: String) {
        defaults.setObject(token, forKey = StorageKeys.ACCESS_TOKEN)
    }

    actual fun getAccessToken(): String? =
        defaults.stringForKey(StorageKeys.ACCESS_TOKEN)

    actual fun saveRefreshToken(token: String) {
        defaults.setObject(token, forKey = StorageKeys.REFRESH_TOKEN)
    }

    actual fun getRefreshToken(): String? =
        defaults.stringForKey(StorageKeys.REFRESH_TOKEN)

    actual fun saveUserId(id: String) {
        defaults.setObject(id, forKey = StorageKeys.USER_ID)
    }

    actual fun getUserId(): String? =
        defaults.stringForKey(StorageKeys.USER_ID)

    actual fun savePhone(phone: String) {
        defaults.setObject(phone, forKey = StorageKeys.PHONE)
    }

    actual fun getPhone(): String? =
        defaults.stringForKey(StorageKeys.PHONE)

    actual fun clear() {
        defaults.removeObjectForKey(StorageKeys.ACCESS_TOKEN)
        defaults.removeObjectForKey(StorageKeys.REFRESH_TOKEN)
        defaults.removeObjectForKey(StorageKeys.USER_ID)
        defaults.removeObjectForKey(StorageKeys.PHONE)
    }
}
