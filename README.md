<div align="center">
  <h1>Chat</h1>
  <p>WhatsApp Clone — KMP (Compose Multiplatform)</p>

  ![Platform](https://img.shields.io/badge/platform-Android%20%7C%20iOS-lightgrey)
  ![Kotlin](https://img.shields.io/badge/Kotlin-2.4.10-blue)
  ![Compose Multiplatform](https://img.shields.io/badge/Compose%20Multiplatform-1.11.1-orange)
</div>

---

## Requirements

| Dependency | Version |
|---|---|
| JDK | 17 |
| Android SDK | API 24+ (Min), 36 (Target) |
| iOS Deployment Target | 16.0+ |
| Xcode | Latest (for iOS build) |
| Kotlin | 2.4.10 |

Backend server berjalan di `localhost:3000`. Lihat [Integration Guide](docs/integration-guide.md) untuk detail API.

---

## Instalasi

```bash
git clone git@github.com:teguholica/whatsapp_clone_client_kmp.git
cd whatsapp_clone_client_kmp
```

### Menjalankan Aplikasi

- **Android**: `./gradlew :androidApp:assembleDebug`
  Base URL default: `http://10.0.2.2:3000` (emulator → host).
- **iOS**: Buka folder `/iosApp` di Xcode dan jalankan dari sana.
  Base URL default: `http://localhost:3000`.

Konfigurasi base URL bisa diubah di `shared/src/commonMain/kotlin/com/teguholica/chat/data/remote/ApiClient.kt`:

```kotlin
object ApiConfig {
    var baseUrl = "http://<host>:3000"
}
```

---

## Domain Model

| Model | Deskripsi |
|-------|-----------|
| **User** | Akun terautentikasi (`id`, `phone`, `displayName`, `avatarUrl`, `presence`) |
| **Chat** | Percakapan 1-on-1 atau grup (`PERSONAL` / `GROUP`) |
| **Message** | Pesan text/image/video/document, status `SENT` → `DELIVERED` → `READ` |
| **Media** | File terupload (`id`, `url`, `mimeType`, `fileSize`) |
| **Presence** | Online/offline + `lastSeenAt` |

---

## Fitur MVP

| Fitur | Status |
|-------|--------|
| Auth OTP (register → verify → JWT) | ✅ |
| Chat List (avatar, preview, badge, presence dot) | ✅ |
| Chat Detail (bubble hijau/putih, centang ✓/✓✓/✓✓biru) | ✅ |
| Real-time Messaging via WebSocket | ✅ |
| Delivery Status (sent → delivered → read) | ✅ |
| Typing Indicator | ✅ |
| Group Chat (create, sender name, group typing) | ✅ |
| Media Upload (image preview via Coil) | ✅ |
| Auto-reconnect WebSocket (exponential backoff) | ✅ |
| Dark Mode (tema hijau gelap WhatsApp) | ✅ |

---

## Architecture & Project Structure

MVVM + Repository. Package by feature untuk UI, by layer untuk data/domain.

```
shared/src/commonMain/kotlin/com/teguholica/chat/
├── di/              # Koin modules
├── data/
│   ├── local/       # TokenStorage (SharedPref Android / NSUserDefaults iOS), SQLDelight schema
│   ├── remote/      # Ktor API clients (Auth, Conversation, Message, Group, MediaUpload), WebSocket, DTO
│   └── repository/  # Implementasi repository
├── domain/
│   ├── model/       # Data class: User, Chat, Message, Media, Presence
│   └── repository/  # Interface repository
└── ui/
    ├── auth/        # Login OTP (input phone → input OTP)
    ├── chatlist/    # Daftar chat
    ├── chatdetail/  # Bubble chat real-time
    └── creategroup/ # Buat grup
```

### Tech Stack

| Layer | Pilihan |
|-------|---------|
| UI | Compose Multiplatform + Material 3 |
| HTTP + WebSocket | Ktor Client |
| Serialization | kotlinx.serialization |
| DI | Koin |
| Database | SQLDelight |
| Preferences | SharedPreferences (Android) / NSUserDefaults (iOS) |
| Image Loading | Coil 3 |
| Navigation | Compose Navigation multiplatform |
| Architecture | MVVM + Repository |

---

## API Endpoints

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

---

## WebSocket Events

**Client → Server:**

| Event | Data | Deskripsi |
|-------|------|-----------|
| `room:join` | `{ conversationId }` | Join room |
| `room:leave` | `{ conversationId }` | Leave room |
| `message:read` | `{ messageId }` | Mark as read (1-on-1) |
| `typing:start` | `{ conversationId }` | Mulai ngetik |
| `typing:stop` | `{ conversationId }` | Berhenti ngetik |
| `presence:online` | `{}` | Online |

**Server → Client:**

| Event | Data | Deskripsi |
|-------|------|-----------|
| `message:new` | `{ id, conversationId, senderId, type, content, createdAt }` | Pesan baru |
| `message:status` | `{ messageId, userId, status }` | Update status (delivered/read) |
| `message:deleted` | `{ messageId, mode }` | Pesan dihapus |
| `typing` | `{ conversationId, userId? }` | Seseorang ngetik |
| `typing:stop` | `{ conversationId, userId }` | Berhenti ngetik |
| `presence` | `{ userId, status, lastSeenAt? }` | Online/offline |

---

## Running Tests

```bash
./gradlew :shared:check
```

---

## Resources & Documentation

| Resource | Link |
|----------|------|
| Integration Guide | [Integration Guide](docs/integration-guide.md) |
| Spec V1 | [Spec V1](docs/spec-v1.md) |

---

## Development Workflow

Branch naming:
- `feat/` — fitur baru
- `fix/` — bug fix
- `refactor/` — refactoring
- `chore/` — update dependency, konfigurasi
