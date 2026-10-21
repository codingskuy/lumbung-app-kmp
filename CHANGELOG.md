# Changelog

Semua perubahan penting lumbung-app-kmp dicatat di sini. Format mengikuti Keep a Changelog; versi mengikuti versi milestone Lumbung.

## [Belum dirilis]

### Ditambahkan
- Modul `shared-local` (LMB-KMP-003): skema SQLDelight (outbox FIFO, lot, nomor_map, meta), `Outbox` (retry 1/5/15 menit, batas 200, foto menunggu WiFi), `TulisLokal` (tulis lokal + antre satu transaksi, catch-up TMP->resmi maks 200, kunci offline >3x24 jam).
- Modul `shared-core` (LMB-KMP-002): `core.model` (8 entitas + enum), `core.pricing` (`FeeCalculator`, `PapanHarga`), `core.grading` (`AturanGrade`, `ValidasiSortir` +-2%), `core.sync` (UUID v4, nomor TMP, kebijakan retry/offline), `core.nav` (`Route`); bootstrap Gradle wrapper 8.14.3, Kover gerbang 85%, tugas `./gradlew uji`.
- Fondasi AI-dev v1.0.0: `AGENTS.md`, `CLAUDE.md`, `docs/decisions/` (ADR-0001), `versions/v1.0.0/docs/40-TECHSTACK.md`, `versions/v1.0.0/docs/60-BLAST-RADIUS.md` (tiket LMB-KMP-001, 2026-10-14).
