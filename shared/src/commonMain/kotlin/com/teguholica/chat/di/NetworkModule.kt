package com.teguholica.chat.di

import com.teguholica.chat.data.remote.AuthApi
import org.koin.dsl.module

val networkModule = module {
    single { AuthApi() }
}
