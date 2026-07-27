package com.teguholica.chat

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
