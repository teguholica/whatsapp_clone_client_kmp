package com.teguholica.chat.di

import com.teguholica.chat.data.local.TokenStorage
import com.teguholica.chat.data.remote.AuthApi
import com.teguholica.chat.data.repository.AuthRepositoryImpl
import com.teguholica.chat.domain.repository.AuthRepository
import com.teguholica.chat.ui.auth.AuthViewModel
import org.koin.dsl.module

val repositoryModule = module {
    single<AuthRepository> {
        AuthRepositoryImpl(
            authApi = get(),
            tokenStorage = get(),
        )
    }

    factory { AuthViewModel(get()) }
}
