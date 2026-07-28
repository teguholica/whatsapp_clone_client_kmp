package com.teguholica.chat.data.local

import android.content.Context
import android.content.SharedPreferences

class AndroidTokenStorage(context: Context) : TokenStorage {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)

    override fun saveAccessToken(token: String) {
        prefs.edit().putString(StorageKeys.ACCESS_TOKEN, token).apply()
    }

    override fun getAccessToken(): String? =
        prefs.getString(StorageKeys.ACCESS_TOKEN, null)

    override fun saveRefreshToken(token: String) {
        prefs.edit().putString(StorageKeys.REFRESH_TOKEN, token).apply()
    }

    override fun getRefreshToken(): String? =
        prefs.getString(StorageKeys.REFRESH_TOKEN, null)

    override fun saveUserId(id: String) {
        prefs.edit().putString(StorageKeys.USER_ID, id).apply()
    }

    override fun getUserId(): String? =
        prefs.getString(StorageKeys.USER_ID, null)

    override fun savePhone(phone: String) {
        prefs.edit().putString(StorageKeys.PHONE, phone).apply()
    }

    override fun getPhone(): String? =
        prefs.getString(StorageKeys.PHONE, null)

    override fun clear() {
        prefs.edit().clear().apply()
    }
}
