# Changelog

Semua perubahan penting lumbung-app-kmp dicatat di sini. Format mengikuti Keep a Changelog; versi mengikuti versi milestone Lumbung.

## [Belum dirilis]

### Ditambahkan
- Irisan petani di `lapangan-mobile` (LMB-KMP-006, kerangka): klien `auth/masuk`, `kontrak-saya`, `pembayaran-saya`, `papan-harga` (baca saja), `rencana-setoran`, tulis lokal + pengirim outbox, aturan PIN/token perangkat 30 hari/maks 2 perangkat, kerangka 4 layar.
- Modul `lapangan-mobile` (LMB-KMP-005, sopir, kerangka): klien `trip-hari-ini`/`titik/{id}/tiba|berangkat`/`bukti-terima`/`insiden`, tulis lokal ke outbox, pengirim FIFO, aturan GPS 5 menit, validasi 2 foto <=500 KB, filter trip hari ini, kerangka 4 layar.
- Modul `gudang-desktop` (LMB-KMP-004, kerangka): timbang ganda, label BR-105, kunci PIN 3x/15 menit, token shift 12 jam, klien `POST /gudang/masuk|keluar`, kerangka 7 layar Compose Desktop dengan bilah Online/Offline + antrean.
- Modul `shared-local` (LMB-KMP-003): skema SQLDelight (outbox FIFO, lot, nomor_map, meta), `Outbox` (retry 1/5/15 menit, batas 200, foto menunggu WiFi), `TulisLokal` (tulis lokal + antre satu transaksi, catch-up TMP->resmi maks 200, kunci offline >3x24 jam).
- Modul `shared-core` (LMB-KMP-002): `core.model` (8 entitas + enum), `core.pricing` (`FeeCalculator`, `PapanHarga`), `core.grading` (`AturanGrade`, `ValidasiSortir` +-2%), `core.sync` (UUID v4, nomor TMP, kebijakan retry/offline), `core.nav` (`Route`); bootstrap Gradle wrapper 8.14.3, Kover gerbang 85%, tugas `./gradlew uji`.
- Fondasi AI-dev v1.0.0: `AGENTS.md`, `CLAUDE.md`, `docs/decisions/` (ADR-0001), `versions/v1.0.0/docs/40-TECHSTACK.md`, `versions/v1.0.0/docs/60-BLAST-RADIUS.md` (tiket LMB-KMP-001, 2026-10-14).
