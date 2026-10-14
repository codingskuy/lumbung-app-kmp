# ADR-0001 — Fondasi teknis lumbung-app-kmp

- Status: Diterima
- Tanggal: 2026-10-14

## Konteks

lumbung-app-kmp memuat dua aplikasi: gudang desktop Windows (wajib hari-1, MSIX 1.0.0, USB/serial + printer thermal 80 mm) dan mobile lapangan Android/iOS (sopir, petani). Jaringan lapangan tidak andal.

## Keputusan

1. Stack: Kotlin Multiplatform + Compose Multiplatform + Navigation3; versi exact di `gradle/libs.versions.toml`. Dilarang framework UI lain.
2. Windows desktop hanya untuk `:gudang-desktop` (gudang); mobile (`:lapangan-mobile`) Android + iOS untuk sopir dan petani. Tidak ada desktop untuk peran lain.
3. Offline-first: SQLDelight lokal, outbox FIFO, retry 1/5/15 menit, nomor sementara `TMP-{uuid8}` dipetakan ke nomor resmi (`SJ-YYYYMMDD-NNN`, `L-NNNN`) saat catch-up maks 200 rekaman per request.
4. Kredensial perangkat: API key per perangkat gudang + PIN operator per shift (token shift 12 jam); mobile memakai token perangkat 30 hari; JWT akses 15 menit dengan refresh otomatis 1 kali.
5. Konflik: gudang menang untuk berat dan grade, lapangan menang untuk bukti terima dan posisi; konflik dicatat dan pihak kalah diberi tahu.
6. Klien tidak menghitung fee/grade/papan sebagai angka resmi; `shared-core` hanya untuk pratinjau konsisten dan validasi (total sortir +-2%) dengan hasil identik server.
7. Rilis: Windows MSIX/EXE; mobile Play Store + APK sideload + TestFlight.

## Konsekuensi

- Tiap fitur lapangan/gudang wajib punya tes antrean offline dan pemetaan TMP.
- Offline >3x24 jam mengunci penerbitan surat jalan baru; timbang dan bukti tetap jalan; backup SQLite ke flashdisk tiap Jumat sore.
- Perubahan formula `shared-core` menaikkan MINOR dan dicatat di `CHANGELOG-shared-core.md`.
