package com.teguholica.chat.data.local

expect class TokenStorage {
    fun saveAccessToken(token: String)
    fun getAccessToken(): String?
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun saveUserId(id: String)
    fun getUserId(): String?
    fun savePhone(phone: String)
    fun getPhone(): String?
    fun clear()
}
