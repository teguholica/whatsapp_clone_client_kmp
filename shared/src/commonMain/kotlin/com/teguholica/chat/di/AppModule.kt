package com.teguholica.chat.di

import com.teguholica.chat.MediaPicker
import com.teguholica.chat.data.local.TokenStorage
import org.koin.core.context.startKoin
import org.koin.dsl.module

val appModule = module {
    includes(networkModule, repositoryModule)
}

fun initKoin(tokenStorage: TokenStorage, mediaPicker: MediaPicker) {
    startKoin {
        modules(appModule, module {
            single { tokenStorage }
            single { mediaPicker }
        })
    }
}
