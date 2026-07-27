package com.teguholica.chat.data.local

import android.content.Context
import android.content.SharedPreferences

actual class TokenStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("chat_prefs", Context.MODE_PRIVATE)

    actual fun saveAccessToken(token: String) {
        prefs.edit().putString(StorageKeys.ACCESS_TOKEN, token).apply()
    }

    actual fun getAccessToken(): String? =
        prefs.getString(StorageKeys.ACCESS_TOKEN, null)

    actual fun saveRefreshToken(token: String) {
        prefs.edit().putString(StorageKeys.REFRESH_TOKEN, token).apply()
    }

    actual fun getRefreshToken(): String? =
        prefs.getString(StorageKeys.REFRESH_TOKEN, null)

    actual fun saveUserId(id: String) {
        prefs.edit().putString(StorageKeys.USER_ID, id).apply()
    }

    actual fun getUserId(): String? =
        prefs.getString(StorageKeys.USER_ID, null)

    actual fun savePhone(phone: String) {
        prefs.edit().putString(StorageKeys.PHONE, phone).apply()
    }

    actual fun getPhone(): String? =
        prefs.getString(StorageKeys.PHONE, null)

    actual fun clear() {
        prefs.edit().clear().apply()
    }
}
