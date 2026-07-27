package com.teguholica.chat

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.teguholica.chat.data.remote.MediaFile

actual class MediaPicker(private val context: Context) {
    private var pending: Triple<ByteArray, String, String>? = null

    fun setPending(uri: Uri) {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val name = if (nameIdx >= 0) it.getString(nameIdx) else "file"
                pendingMime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val stream = context.contentResolver.openInputStream(uri)
                val bytes = stream?.readBytes()
                stream?.close()
                if (bytes != null) pending = Triple(bytes, name, pendingMime)
            }
        }
    }

    private var pendingMime = "application/octet-stream"

    actual suspend fun pickImage(): MediaFile? = takePending("image")
    actual suspend fun pickVideo(): MediaFile? = takePending("video")
    actual suspend fun pickDocument(): MediaFile? = takePending("document")

    private fun takePending(expectedType: String): MediaFile? {
        val (bytes, name, mime) = pending ?: return null
        pending = null
        val typePrefix = mime.substringBefore("/")
        if (typePrefix != expectedType && typePrefix != "application") return null
        return MediaFile(fileName = name, mimeType = mime, bytes = bytes)
    }
}
