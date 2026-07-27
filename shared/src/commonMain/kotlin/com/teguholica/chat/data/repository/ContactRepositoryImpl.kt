package com.teguholica.chat.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.db.SqlDriver
import com.teguholica.chat.data.local.ChatDatabase
import com.teguholica.chat.domain.model.Contact
import com.teguholica.chat.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ContactRepositoryImpl(driver: SqlDriver) : ContactRepository {

    private val db = ChatDatabase(driver)
    private val queries = db.chatDatabaseQueries

    override suspend fun getAll(): List<Contact> {
        return queries.selectAllContacts().executeAsList().map { it.toContact() }
    }

    override suspend fun getById(id: String): Contact? {
        return queries.selectContactById(id).executeAsOneOrNull()?.toContact()
    }

    override suspend fun save(contact: Contact) {
        queries.insertContact(
            id = contact.id,
            phone = contact.phone,
            displayName = contact.displayName,
            avatarUrl = contact.avatarUrl,
            createdAt = contact.createdAt,
        )
    }

    override suspend fun delete(id: String) {
        queries.deleteContact(id)
    }

    override fun observeAll(): Flow<List<Contact>> {
        return queries.selectAllContacts().asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toContact() }
        }
    }

    private fun com.teguholica.chat.data.local.ContactEntity.toContact(): Contact {
        return Contact(
            id = id,
            phone = phone,
            displayName = displayName,
            avatarUrl = avatarUrl,
            createdAt = createdAt,
        )
    }
}
