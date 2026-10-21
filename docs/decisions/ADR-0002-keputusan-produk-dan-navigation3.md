# ADR-0002 — Keputusan produk siklus 1 dan penguncian Navigation3

- Status: Diterima (keputusan produk disampaikan oleh manajer divisi; lihat PR #14)
- Tanggal: 2026-10-21
- Menggantikan: bagian "Tech stack" `AGENTS.md` yang menyebut Compose Multiplatform `1.8.2` dan Navigation3 `1.0.0` (versi itu tidak dapat diunduh)

## Konteks

Siklus 1 menemukan beberapa ketidaksesuaian antara kode, kontrak backend (`docs/api/openapi.yaml`, branch `feat/lmb-be-005-trip-sinkron`), dan dokumen beku. Keputusan berikut sudah disetujui dan diterapkan.

## Keputusan

1. **Status pembayaran** = `MENUNGGU`, `DIBAYAR`, `TERLAMBAT` (sama dengan kontrak backend). `core.model.StatusPembayaran` diubah.
2. **Harga ke petani per grade**: A 100% papan, B 85% papan (dibulatkan ke bawah kelipatan Rp500), C ditolak (BRD BR-002, beku). Diimplementasikan di `PapanHarga.hargaGrade` dan `PembayaranPetani.nilai`; klien hanya pratinjau, angka resmi dari server. Contoh papan: cabai A 31.500 -> B 26.500; bawang A 28.000 -> B 23.500; kangkung A 6.000 -> B 5.000.
3. **Berat selalu gram bilangan bulat** (`Long`), uang rupiah `Long`; tidak ada desimal di model, DTO, maupun kalkulasi. Pecahan rupiah dibulatkan ke bawah.
4. **Navigation3 tetap dipakai**, versi dikunci persis di `gradle/libs.versions.toml`:
   - `org.jetbrains.androidx.navigation3:navigation3-ui` **1.1.2** (stabil terbaru; Maven Central). Versi 1.0.0 dari koordinat yang sama tidak pernah terbit (hanya alpha01-06), sedangkan 1.2.0-rc01 masih kandidat rilis.
   - `androidx.navigation3:navigation3-runtime` **1.1.7** (Google Maven `dl.google.com/dl/android/maven2`; dibutuhkan transitif oleh `navigation3-ui` 1.1.2 dan dipin eksplisit). Koordinat JetBrains `navigation3-runtime` tidak ada.
   - Konsekuensi: `navigation3-ui` 1.1.2 mensyaratkan Compose runtime/ui `1.10.0`, sehingga plugin Compose Multiplatform dinaikkan dari `1.8.2` ke **1.10.0** (Kotlin tetap `2.2.20`). Konsekuensi lanjutan: `material3` dari plugin menarik `kotlinx-datetime` 0.7.x, jadi `kotlinx-datetime` dinaikkan ke **0.7.1** (Instant pindah ke `kotlin.time`; opt-in `ExperimentalTime` dan serializer ISO 8601 sendiri `core.model.InstantIso`).
   - Repositori sumber: Maven Central (`org.jetbrains.*`, `org.jetbrains.kotlinx.*`), Google Maven (`androidx.*`), Gradle Plugin Portal (plugin).
5. **Hak admin minimum**: peran admin hanya menerbitkan papan harga dan mengubah tarif fee; tidak ada operasi admin lain di aplikasi ini (aplikasi tidak memuat layar admin). Alat gudang tetap memakai kunci perangkat `X-Device-Key` untuk 7 endpoint gudang (tulis `timbangan`, `fee.status_cetak`) ditambah token shift 12 jam; tidak ada hak baca data pengguna.

## Konsekuensi

- Tech stack di `AGENTS.md` perlu diselaraskan oleh pemilik dokumen (Compose 1.10.0, Navigation3 1.1.2/1.1.7, kotlinx-datetime 0.7.1); AGENTS.md tidak diedit dalam PR ini karena ADR lama bersifat immutable dan pengubahan stack lewat ADR baru ini.
- Dengan Compose 1.10.0, target Android perlu diverifikasi ulang (compileSdk/AGP) saat shell aplikasi Android dibuat.
