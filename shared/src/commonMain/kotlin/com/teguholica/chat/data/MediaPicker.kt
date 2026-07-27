package com.teguholica.chat

import com.teguholica.chat.data.remote.MediaFile

expect class MediaPicker {
    suspend fun pickImage(): MediaFile?
    suspend fun pickVideo(): MediaFile?
    suspend fun pickDocument(): MediaFile?
}
