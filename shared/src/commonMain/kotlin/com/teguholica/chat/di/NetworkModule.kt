package com.teguholica.chat.di

import com.teguholica.chat.data.remote.AuthApi
import com.teguholica.chat.data.remote.ConversationApi
import com.teguholica.chat.data.remote.ws.WsClient
import org.koin.dsl.module

val networkModule = module {
    single { AuthApi() }
    single { ConversationApi() }
    single { WsClient() }
}
