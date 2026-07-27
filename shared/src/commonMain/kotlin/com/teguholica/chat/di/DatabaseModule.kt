package com.teguholica.chat.di

import com.teguholica.chat.data.local.ChatDatabase
import com.teguholica.chat.data.local.DatabaseDriverFactory
import com.teguholica.chat.data.repository.ContactRepositoryImpl
import com.teguholica.chat.domain.repository.ContactRepository
import org.koin.dsl.module

val databaseModule = module {
    single { get<DatabaseDriverFactory>().createDriver() }
    single { ChatDatabase(get()) }
    single<ContactRepository> { ContactRepositoryImpl(get()) }
}
