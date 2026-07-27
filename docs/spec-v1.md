# Spec V1: WhatsApp Clone Client — KMP (Compose Multiplatform)

## Problem Statement

Aplikasi WhatsApp clone dengan backend yang sudah siap (REST + WebSocket) tapi belum punya client mobile. Backend menyediakan API untuk auth (OTP), messaging (text + media), grup, real-time event (WebSocket), typing indicator, delivery status, dan presence. Client perlu dibangun dari nol untuk Android dan iOS menggunakan Kotlin Multiplatform dengan Compose Multiplatform.

## Solution

Membangun client WhatsApp clone native untuk Android dan iOS menggunakan KMP + Compose Multiplatform. Semua logic bisnis dan UI di shared module, dengan platform-specific code minimal. UI/UX mengikuti WhatsApp original (bubble chat hijau/putih, centang delivery status, dll).

## User Stories

1. Sebagai pengguna baru, saya ingin mendaftar dengan nomor telepon via OTP, agar bisa menggunakan aplikasi
2. Sebagai pengguna terdaftar, saya ingin login dengan OTP, agar bisa mengakses chat saya
3. Sebagai pengguna, saya ingin token saya disimpan secara lokal, agar tidak perlu login ulang setiap buka aplikasi
4. Sebagai pengguna, saya ingin melihat daftar chat saya, agar bisa memilih percakapan yang ingin dibuka
5. Sebagai pengguna, saya ingin melihat preview pesan terakhir di daftar chat, agar tahu isi chat tanpa membukanya
6. Sebagai pengguna, saya ingin melihat jumlah pesan belum terbaca di daftar chat, agar tahu ada pesan baru
7. Sebagai pengguna, saya ingin melihat status online/offline kontak di daftar chat, agar tahu apakah mereka sedang aktif
8. Sebagai pengguna, saya ingin membuka chat 1-on-1, agar bisa ngobrol dengan kontak saya
9. Sebagai pengguna, saya ingin mengirim pesan text, agar bisa berkomunikasi dengan lawan bicara
10. Sebagai pengguna, saya ingin menerima pesan text secara real-time, agar tidak perlu refresh manual
11. Sebagai pengguna, saya ingin melihat bubble chat: hijau untuk pesan saya, putih untuk pesan lawan, seperti WhatsApp asli
12. Sebagai pengguna, saya ingin melihat status pengiriman pesan (sent ✓, delivered ✓✓, read ✓✓ biru), agar tahu status pesan saya
13. Sebagai pengguna, saya ingin mendapat notifikasi ketika lawan bicara sedang mengetik, agar tahu mereka akan membalas
14. Sebagai pengguna, saya ingin membuat grup chat dengan multiple peserta, agar bisa ngobrol bareng
15. Sebagai pengguna, saya ingin mengirim dan menerima gambar di chat, agar bisa berbagi foto
16. Sebagai pengguna, saya ingin mengirim dan menerima video di chat, agar bisa berbagi video
17. Sebagai pengguna, saya ingin mengirim dan menerima file PDF/doc di chat, agar bisa berbagi dokumen
18. Sebagai pengguna, saya ingin pesan saya tetap terkirim meskipun lawan bicara sedang offline — pesan terkirim saat mereka online dan join room
19. Sebagai pengguna, saya ingin koneksi WebSocket terhubung otomatis saat aplikasi dibuka
20. Sebagai pengguna, saya ingin koneksi WebSocket ter-reconnect otomatis jika putus, agar tidak kehilangan pesan real-time
21. Sebagai pengguna, saya ingin aplikasi tetap berfungsi jika token JWT expired — auto-refresh atau re-auth
22. Sebagai pengguna, saya ingin melihat daftar kontak saya, agar bisa memulai chat dengan mereka
23. Sebagai pengguna, saya ingin aplikasi support dark mode, agar nyaman dipakai di malam hari
24. Sebagai pengguna, saya ingin aplikasi berjalan di Android dan iOS dengan UI dan fungsionalitas yang sama

## Implementation Decisions

### Architecture

- **MVVM + Repository** — ViewModel per screen, StateFlow untuk UI state. Repository sebagai single source truth antara remote (Ktor) dan local (SQLDelight + DataStore).
- **Package by feature** untuk UI screen, **by layer** untuk data/domain.

### Technology Stack

| Layer | Pilihan | Alasan |
|-------|---------|--------|
| UI Framework | Compose Multiplatform | Shared UI untuk Android + iOS |
| HTTP Client | Ktor Client | Multiplatform native, support WebSocket built-in |
| Serialization | kotlinx.serialization | Plugin compiler, integrasi Ktor |
| DI | Koin | DI framework KMP-native, ringan |
| Database | SQLDelight | SQL lokal multiplatform, type-safe |
| Preferences | DataStore | Simpan token, settings — ringan |
| Image Loading | Coil 3 | Multiplatform, cache, integrasi Compose |
| Navigation | Compose Navigation multiplatform | Resmi dari JetBrains, API standar |
| Architecture | MVVM + Repository | Standar KMP, cocok lifecycle-viewmodel-compose |

### API Base URL

- Android emulator: `http://10.0.2.2:3000`
- iOS simulator: `http://localhost:3000`
- Base URL di-config via Koin module, disesuaikan per platform

### Theme

- Material 3 components dengan WhatsApp color palette
- Primary: `#075E54` (hijau WhatsApp)
- Sent message bubble: hijau muda background
- Received message bubble: putih/abu-abu muda background
- Dark mode: tema hijau gelap sesuai WhatsApp

### State Management

- ViewModel + StateFlow (dari `lifecycle-viewmodel-compose` KMP)
- Setiap screen punya `UiState` sealed class: `Loading`, `Success(data)`, `Error(message)`
- WebSocket events diteruskan ke ViewModel via shared `Flow` di repository

### Key Domain Models

```kotlin
data class User(
    val id: String,
    val phone: String,
    val displayName: String?,
    val avatarUrl: String?,
    val presence: Presence?,
)

data class Chat(
    val id: String,
    val type: ChatType, // PERSONAL, GROUP
    val name: String,
    val avatarUrl: String?,
    val lastMessage: Message?,
    val unreadCount: Int,
    val participants: List<User>,
    val createdAt: String,
)

enum class ChatType { PERSONAL, GROUP }

data class Message(
    val id: String,
    val chatId: String,
    val senderId: String,
    val type: MessageType, // TEXT, IMAGE, VIDEO, DOCUMENT
    val content: String, // text or JSON with media reference
    val media: Media?,
    val status: MessageStatus,
    val createdAt: String,
)

enum class MessageType { TEXT, IMAGE, VIDEO, DOCUMENT }
enum class MessageStatus { SENT, DELIVERED, READ }

data class Media(
    val id: String,
    val url: String,
    val mimeType: String,
    val fileSize: Long,
)

data class Presence(
    val userId: String,
    val status: PresenceStatus, // ONLINE, OFFLINE
    val lastSeenAt: String?,
)

enum class PresenceStatus { ONLINE, OFFLINE }
```

### Seams & Modularity

Keseluruhan fitur dipecah menjadi 9 langkah tracer bullet:

1. **Dependencies** — Konfigurasi build files (version catalog, plugins, dependencies)
2. **Domain Models** — Data class, SQLDelight schema, DataStore keys
3. **Auth** — Register OTP → verify → simpan token → Koin module
4. **WebSocket** — Koneksi Ktor WS → room:join/leave → event listener
5. **Chat List** — Screen daftar chat, unread badge, last message, online status
6. **Chat Detail** — Real-time messaging, bubble UI, delivery status
7. **Group Chat** — Create grup, multiple participants, typing indicator group
8. **Media Upload** — Picker → multipart upload → attach ke message
9. **Polish** — Typing indicator, presence indicator, auto-reconnect, error handling, dark mode

## Testing Decisions

- **Unit test** di `commonTest` untuk ViewModel + Repository logic (menggunakan fake repository)
- **Integration test** via connect ke server development (manual untuk MVP)
- Testing ditambahkan sebagai langkah terakhir setelah implementasi
- Prior art: test template sudah ada di `commonTest/`, `androidHostTest/`, `iosTest/`

## Out of Scope

- End-to-end encryption (UI/UX only — WhatsApp palsu)
- Push notifications (MVP menggunakan reconnect/foreground refresh)
- Voice note recording
- Panggilan suara/video
- Story/Status
- Location sharing
- Contact sync dari phonebook
- Desktop/web target (hanya Android + iOS)

## Further Notes

- Development menggunakan server lokal di `localhost:3000`
- Semua UI text dalam Bahasa Indonesia
- UI mengacu ke WhatsApp original — tata letak, warna bubble, ikon centang, dll
- Proyek root name: `Chat`, package: `com.teguholica.chat`
- Android namespace: `com.teguholica.chat`, API 24+ (minimum), target SDK 36
- iOS minimum: iOS 16+ (standar Compose Multiplatform)
