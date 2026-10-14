> Versi: v1.0.0 | Status: disetujui | Menggantikan: -
> Sumber: Lumbung-TownHall `platform/versions/v1.0.0/` pada commit `673a467`; [townhall-roadmap.md §2](https://github.com/Coding-Skuy/ChefGenie-TownHall/blob/main/townhall-roadmap.md); [KONTEKS §8/§9](https://github.com/Coding-Skuy/ChefGenie-TownHall/blob/main/KONTEKS-LINTAS-DIVISI.md)

# 60 — Blast Radius lumbung-app-kmp

Batas context v1.0.0 Lumbung (townhall-roadmap §2): 3 komoditas pilot, fee Rp1.500/kg dekat dan Rp2.500/kg jauh, radius 25 km; point of concern titik impas 18.000 kg/bulan dan susut <5%.

## Boleh disentuh

- UI + logika klien untuk gudang desktop, mobile sopir, mobile petani.
- Basis lokal SQLDelight dan outbox sinkron; integrasi timbangan USB 150 kg dan printer thermal 80 mm.
- Modul `shared-core` (model, validasi grade +-2%, antrean, rute navigasi).

## Dilarang

- Menyentuh DB logis divisi lain (`pasaree`, `titipo`, `pedaree`, `pawonee`, `titeny`).
- Menambah `event_type` di luar registry KONTEKS §3 tanpa bump versi + 30 hari dual-read.
- Menambah komoditas keempat atau melayani di luar radius 25 km (non-goal PRD/30).
- Menghitung fee, grade, atau papan sebagai angka resmi di klien (selalu angka server).
- Menyimpan token shift di luar PC gudang; membocorkan kredensial perangkat.
- Menambah target Windows untuk peran selain gudang, atau framework UI selain Compose Multiplatform.
- Mengimpor modul Pawonee (`:shared:pantry-resep`); Lumbung tidak mengonsumsinya.

## Kontrak diproduksi

- Bundel MSIX `gudang-desktop` 1.0.0 dan APK/IPA `lapangan-mobile`.
- Request idempoten (UUID klien) ke backend: lot, sortir, keranjang, trip, bukti terima, insiden, sinkron batch.

## Kontrak dikonsumsi

- REST `lumbung-backend-service` (`https://api.lumbung.chefgenie.id/v1`) sesuai OpenAPI.
- Token dari `docs/auth.md` backend: JWT, token shift, device key.
- Token desain dan layar dari `lumbung-design`.

## Aturan perubahan

Perubahan di luar batas ini = PR requirement di Lumbung-TownHall lebih dulu, lalu ADR baru di repo ini. Perubahan angka lintas-divisi wajib memperbarui KONTEKS §terkait + dokumen divisi + CHANGELOG versi dalam PR yang sama (KONTEKS §9).
