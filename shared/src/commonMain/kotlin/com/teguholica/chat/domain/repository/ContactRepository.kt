package com.teguholica.chat.domain.repository

import com.teguholica.chat.domain.model.Contact
import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    suspend fun getAll(): List<Contact>
    suspend fun getById(id: String): Contact?
    suspend fun save(contact: Contact)
    suspend fun delete(id: String)
    fun observeAll(): Flow<List<Contact>>
}
