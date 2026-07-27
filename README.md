# Chat — WhatsApp Clone KMP

Klien WhatsApp clone untuk Android + iOS menggunakan Kotlin Multiplatform + Compose Multiplatform. UI/UX mengikuti WhatsApp original.

## Tech Stack

| Layer | Pilihan |
|-------|---------|
| UI | Compose Multiplatform + Material 3 |
| HTTP + WebSocket | Ktor Client |
| Serialization | kotlinx.serialization |
| DI | Koin |
| Database | SQLDelight |
| Image Loading | Coil 3 |
| Navigation | Compose Navigation multiplatform |
| Architecture | MVVM + Repository |

## Arsitektur

```
shared/src/commonMain/kotlin/com/teguholica/chat/
├── di/          # Koin modules
├── data/
│   ├── local/   # TokenStorage, SQLDelight schema
│   ├── remote/  # Ktor API clients, WebSocket, DTO
│   └── repository/  # Implementasi repository
├── domain/
│   ├── model/   # Data class: User, Chat, Message, Media, Presence
│   └── repository/  # Interface repository
└── ui/
    ├── auth/        # Login OTP (phone input → OTP input)
    ├── chatlist/    # Daftar chat (avatar, preview, badge, presence dot)
    ├── chatdetail/  # Bubble chat, kirim/terima real-time, media
    └── creategroup/ # Buat grup (pilih kontak → set nama)
```

## Domain Model

- **User** — akun terautentikasi (`id`, `phone`, `displayName`, `avatarUrl`, `presence`)
- **Chat** — percakapan 1-on-1 atau grup (`PERSONAL` / `GROUP`)
- **Message** — pesan text/image/video/document (`SENT` → `DELIVERED` → `READ`)
- **Media** — file terupload (`id`, `url`, `mimeType`, `fileSize`)
- **Presence** — online/offline + `lastSeenAt`

## Fitur MVP

- [x] Auth OTP (register → verify → JWT token)
- [x] Chat list (avatar, last message preview, unread badge, presence dot)
- [x] Chat detail (bubble hijau/putih, centang ✓/✓✓/✓✓biru, real-time)
- [x] Group chat (create, sender name di bubble, typing indicator)
- [x] Media upload (image preview via Coil, attach button 📷📎)
- [x] WebSocket real-time (message, typing, presence, delivery status)
- [x] Auto-reconnect (exponential backoff 1s → 30s)
- [x] Dark mode (tema hijau gelap WhatsApp)

## Backend

Butuh server backend berjalan di `localhost:3000`. Backend API:

| Endpoint | Method | Deskripsi |
|----------|--------|-----------|
| `/api/auth/register` | POST | Request OTP |
| `/api/auth/verify` | POST | Verify OTP → JWT |
| `/api/conversations` | GET | List chat |
| `/api/conversations/:id` | GET | Detail chat |
| `/api/messages/:conversationId` | GET | Pesan (cursor pagination) |
| `/api/messages/:conversationId` | POST | Kirim pesan |
| `/api/groups` | POST | Buat grup |
| `/api/groups/:id` | GET | Info grup |
| `/api/media/upload` | POST | Upload file (multipart) |
| `ws://host:3000?token=` | WS | Real-time events |

## Cara Run

### Android

```bash
./gradlew :androidApp:assembleDebug
```

Base URL default: `http://10.0.2.2:3000` (Android emulator → host localhost).

### iOS

Buka `iosApp/` di Xcode, run target `iosApp`.

Base URL default: `http://localhost:3000`.

### Konfigurasi Base URL

Ubah di `shared/.../data/remote/ApiConfig.kt`:

```kotlin
object ApiConfig {
    var baseUrl = "http://<host>:3000"
}
```

Atau set `ws://<host>:3000` otomatis dari `baseUrl.replace("http", "ws")`.

## Test

```bash
./gradlew :shared:check
```
