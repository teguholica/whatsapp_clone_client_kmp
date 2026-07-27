package com.teguholica.chat

import com.teguholica.chat.data.remote.MediaFile
import kotlinx.cinterop.*
import platform.Foundation.NSData
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
actual class MediaPicker {
    private var pendingData: NSData? = null

    fun setPending(data: NSData) {
        pendingData = data
    }

    actual suspend fun pickImage(): MediaFile? {
        val data = pendingData ?: return null
        pendingData = null
        val size = data.length.toInt()
        if (size == 0) return null
        val rawPtr = data.bytes ?: return null
        val result = ByteArray(size)
        result.usePinned { pinned ->
            memcpy(pinned.addressOf(0), rawPtr, size.toULong())
        }
        return MediaFile(fileName = "image.jpg", mimeType = "image/jpeg", bytes = result)
    }

    actual suspend fun pickVideo(): MediaFile? = null
    actual suspend fun pickDocument(): MediaFile? = null
}
