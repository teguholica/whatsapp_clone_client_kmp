package com.teguholica.chat.di

import com.teguholica.chat.data.remote.AuthApi
import com.teguholica.chat.data.remote.ConversationApi
import com.teguholica.chat.data.remote.GroupApi
import com.teguholica.chat.data.remote.HealthApi
import com.teguholica.chat.data.remote.MediaUploadApi
import com.teguholica.chat.data.remote.MessageApi
import com.teguholica.chat.data.remote.UserApi
import com.teguholica.chat.data.remote.ws.WsClient
import org.koin.dsl.module

val networkModule = module {
    single { AuthApi() }
    single { ConversationApi() }
    single { GroupApi() }
    single { HealthApi() }
    single { MessageApi() }
    single { MediaUploadApi() }
    single { UserApi() }
    single { WsClient() }
}
