package com.teguholica.chat.data.local

import platform.Foundation.NSUserDefaults

class IosTokenStorage : TokenStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun saveAccessToken(token: String) {
        defaults.setObject(token, forKey = StorageKeys.ACCESS_TOKEN)
    }

    override fun getAccessToken(): String? =
        defaults.stringForKey(StorageKeys.ACCESS_TOKEN)

    override fun saveRefreshToken(token: String) {
        defaults.setObject(token, forKey = StorageKeys.REFRESH_TOKEN)
    }

    override fun getRefreshToken(): String? =
        defaults.stringForKey(StorageKeys.REFRESH_TOKEN)

    override fun saveUserId(id: String) {
        defaults.setObject(id, forKey = StorageKeys.USER_ID)
    }

    override fun getUserId(): String? =
        defaults.stringForKey(StorageKeys.USER_ID)

    override fun savePhone(phone: String) {
        defaults.setObject(phone, forKey = StorageKeys.PHONE)
    }

    override fun getPhone(): String? =
        defaults.stringForKey(StorageKeys.PHONE)

    override fun clear() {
        defaults.removeObjectForKey(StorageKeys.ACCESS_TOKEN)
        defaults.removeObjectForKey(StorageKeys.REFRESH_TOKEN)
        defaults.removeObjectForKey(StorageKeys.USER_ID)
        defaults.removeObjectForKey(StorageKeys.PHONE)
    }
}
