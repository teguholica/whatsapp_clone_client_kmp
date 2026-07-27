# Chat (WhatsApp Clone Client) — Domain Glossary

## Language

**Percakapan (Conversation)**:
Ruang chat antara dua user (personal) atau lebih (grup).
_Avoid_: Chat, room, dialog

**Kontak (Contact)**:
User yang saling kenal atau pernah berinteraksi, bisa diajak chat personal.
_Avoid_: Participant (participant = user dalam konteks percakapan tertentu)

**Personal Chat**:
Percakapan 1-on-1 antara dua user. Dibuat explicit via `POST /api/conversations`.
_Avoid_: Private chat, direct message

**Grup (Group)**:
Percakapan dengan tiga atau lebih participant. Dibuat via `POST /api/groups`.

**New Chat**:
Aksi memulai personal chat dengan kontak — explicit create via API, bukan implicit.
_Avoid_: New conversation

**New Group**:
Aksi membuat grup baru — pilih nama grup + pilih peserta.

**Pesan (Message)**:
Unit komunikasi dalam percakapan — TEXT, IMAGE, VIDEO, atau DOCUMENT.
