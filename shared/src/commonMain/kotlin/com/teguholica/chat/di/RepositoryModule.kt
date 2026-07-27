package com.teguholica.chat.di

import com.teguholica.chat.data.local.TokenStorage
import com.teguholica.chat.data.remote.AuthApi
import com.teguholica.chat.data.remote.ConversationApi
import com.teguholica.chat.data.remote.GroupApi
import com.teguholica.chat.data.remote.MessageApi
import com.teguholica.chat.data.repository.AuthRepositoryImpl
import com.teguholica.chat.data.repository.ChatRepositoryImpl
import com.teguholica.chat.data.remote.ws.WsClient
import com.teguholica.chat.domain.repository.AuthRepository
import com.teguholica.chat.domain.repository.ChatRepository
import com.teguholica.chat.ui.auth.AuthViewModel
import com.teguholica.chat.ui.chatdetail.ChatDetailViewModel
import com.teguholica.chat.ui.chatlist.ChatListViewModel
import com.teguholica.chat.ui.creategroup.CreateGroupViewModel
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            tokenStorage = get(),
        )
    }

    single<ChatRepository> {
        ChatRepositoryImpl(
            conversationApi = get(),
            authRepository = get(),
        )
    }

    factory { AuthViewModel(get()) }
    factory { ChatListViewModel(get(), get(), get()) }
    factory { ChatDetailViewModel(get(), get(), get()) }
    factory { CreateGroupViewModel(get(), get()) }
}
