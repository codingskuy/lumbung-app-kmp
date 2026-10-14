# AGENTS.md — lumbung-app-kmp

Aplikasi Kotlin Multiplatform + Compose Multiplatform Lumbung: desktop Windows gudang (hari-1, MSIX) dan mobile lapangan (sopir, petani) offline-first.

## Sumber kebenaran

Requirement hidup di [Lumbung-TownHall](https://github.com/Coding-Skuy/Lumbung-TownHall), bukan di repo ini. Dilarang menduplikasi requirement di repo kode; requirement berubah = PR di Lumbung-TownHall dulu (versi beku `v1.0.0` tidak diedit; perubahan masuk v1.1.0/v2.0.0).

| Dokumen | Isi yang berlaku untuk repo ini |
|---|---|
| [`platform/versions/v1.0.0/FSD/10-alur.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/platform/versions/v1.0.0/FSD/10-alur.md) | Tulis lokal dahulu, outbox FIFO, nomor TMP, urutan layar gudang/sopir/petani |
| [`platform/versions/v1.0.0/FSD/30-kontrak.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/platform/versions/v1.0.0/FSD/30-kontrak.md) | Kontrak per konsumen, idempotensi UUID, galat baku |
| [`platform/versions/v1.0.0/FSD/20-model-data.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/platform/versions/v1.0.0/FSD/20-model-data.md) | Entitas dan enum (gram/rupiah integer) |
| [`platform/versions/v1.0.0/PRD/20-alur.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/platform/versions/v1.0.0/PRD/20-alur.md) | Alur pengguna |
| [`platform/versions/v1.0.0/PRD/30-kriteria.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/platform/versions/v1.0.0/PRD/30-kriteria.md) | US-001..US-006, Non-Goals |
| [`platform/versions/v1.0.0/FRD/10-fungsional.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/platform/versions/v1.0.0/FRD/10-fungsional.md) | FR agregasi/distribusi/keuangan |
| [`produk/31-kontrak-api-desktop-windows.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/produk/31-kontrak-api-desktop-windows.md) | Kontrak gudang desktop (7 endpoint) |
| [`produk/30-kontrak-api-KMP-mobile.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/produk/30-kontrak-api-KMP-mobile.md) | Kontrak mobile (sopir 5, petani 4) |
| [`produk/40-modul-KMP-bersama.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/produk/40-modul-KMP-bersama.md) | Modul `shared-core` (core.model/pricing/grading/sync/nav) |
| [`produk/50-autentikasi-perangkat.md`](https://github.com/Coding-Skuy/Lumbung-TownHall/blob/main/produk/50-autentikasi-perangkat.md) | Token perangkat 30 hari, token shift 12 jam, PIN |
| [`KONTEKS-LINTAS-DIVISI.md`](https://github.com/Coding-Skuy/ChefGenie-TownHall/blob/main/KONTEKS-LINTAS-DIVISI.md) (ChefGenie-TownHall) | Event registry §3, komoditas pilot §4, grade §5, satuan/ID/uang §6 |
| [`townhall-roadmap.md`](https://github.com/Coding-Skuy/ChefGenie-TownHall/blob/main/townhall-roadmap.md) (ChefGenie-TownHall) | Batas context §2, Definisi Selesai §6 |

Angka terkunci v1.0.0 yang berlaku di seluruh repo Lumbung:

- 3 komoditas pilot: cabai, bawang, kangkung (KONTEKS §4; dilarang mencampur angka 3 dan 12 komoditas tanpa label fase).
- Fee logistik murni: Dekat (<15 km) Rp1.500/kg, Jauh (15-25 km) Rp2.500/kg; radius layanan 25 km (BRD/30 BR-301).
- Uang integer rupiah, berat integer gram; pembayaran petani H+1 dari berat bersih per grade x harga papan.
- Papan harga dihitung Jumat 15.00 (median 3 referensi - 10%, bulatkan ke bawah kelipatan Rp500), publikasi maksimal 17.00.
- Titik impas 18.000 kg/bulan (600 kg/hari); target 2 ton/hari; susut total <5%.

## Tech stack terkunci

| Lapisan | Pilihan terkunci |
|---|---|
| Bahasa/UI | Kotlin `2.2.20`, Compose Multiplatform `1.8.2`, Navigation3 `1.0.0` |
| Jaringan | Ktor Client `3.1.3` (OkHttp Android, Darwin iOS, WinHttp Windows) |
| Lokal | SQLDelight `2.1.0` (offline-first + outbox FIFO + retry) |
| Target | Android, iOS, Windows desktop (hanya `:gudang-desktop`, MSIX x64) |
| Modul | `:composeApp`, `:gudang-desktop`, `:lapangan-mobile`, `shared-core` |

Larangan: framework UI selain Compose Multiplatform, Next.js/React/Flutter, logika fee/grade/papan di luar `shared-core` (klien tidak menghitung angka bisnis; angka resmi dari server), auto-update MSIX (non-goal), dependensi Compose/SQLDelight/Ktor di `shared-core`.

## Aturan kerja AI

1. **Baca dulu.** Baca `AGENTS.md` ini dan seluruh ADR di `docs/decisions/` sebelum menulis kode.
2. **TDD merah-hijau.** Tulis tes gagal dari acceptance criteria tiket, lalu implementasi. Rilis diblokir bila QA gate gagal (SRS `40-acceptance-criteria.md`: QA-001..QA-004; tidak ada tes yang di-skip).
3. **Kontrak berversi.** Kontrak API/event hanya diubah lewat bump versi + ADR baru. Perubahan requirement dimulai dari PR di Lumbung-TownHall, bukan dari repo ini.
4. **Commit.** Awalan `docs:` / `feat:` / `fix:`; setiap PR wajib memperbarui `CHANGELOG.md` repo ini.
5. **ADR immutable.** Keputusan arsitektur baru = ADR bernomor berikutnya di `docs/decisions/`; ADR lama tidak diedit (diganti dengan ADR baru berstatus "Menggantikan ADR-NNNN"), sama seperti aturan versi beku TownHall.
6. **DB per service.** Dilarang menyentuh basis data divisi lain. Basis data logis hanya `lumbung`; Titeny hanya membaca (read-only) ke DB sumber lewat kontrak event, bukan lewat akses langsung dari repo ini.
7. **Angka dari dokumen.** Angka bisnis (tarif, ambang, batas) hanya dari dokumen TownHall; jangan mengarang atau menghitung ulang di tempat yang dilarang.
8. **Offline-first.** Setiap aksi ditulis lokal dahulu (<200 ms, UUID v4) lalu antre di outbox FIFO; UI tidak menunggu server.
9. **Shared-core.** Logika harga/grade hanya di `shared-core` (cakupan uji min. 85%, `FeeCalculator` 100% branch); klien tidak menghitung ulang angka resmi server.

## Definisi selesai

Dari [townhall-roadmap.md §6](https://github.com/Coding-Skuy/ChefGenie-TownHall/blob/main/townhall-roadmap.md), berlaku per milestone:

- [ ] Dokumen versi lengkap (BRD/FRD/FSD/PRD/SRS) + `SNAPSHOT-ROADMAP.md` di Lumbung-TownHall.
- [ ] QA gate berbasis SRS lulus (TDD merah-hijau); rilis diblokir bila gagal.
- [ ] Kontrak API terbit; audit keamanan bersih (JWT audiens `lumbung` + DB per service).
- [ ] Bukti metrik lintas-divisi (KONTEKS §7) tercatat.
- [ ] CHANGELOG versi diperbarui; dokumen beku tidak diedit.
