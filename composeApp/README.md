# composeApp — UI Bersama Lumbung

Modul UI bersama berbasis Compose Multiplatform + Navigation3.

## Isi Rencana

- `src/commonMain/kotlin/` — layar bersama: daftar lot, detail timbangan, papan harga, riwayat trip.
- `src/androidMain/`, `src/iosMain/`, `src/desktopMain/` — adaptor platform (izin, printer, serial).
- Navigasi memakai Navigation3 (`navigation3-runtime` + `navigation3-ui` versi `1.0.0`).

## Rute Awal

- `/lot` — daftar lot panen aktif.
- `/lot/{id}` — detail lot + hasil timbangan.
- `/trip` — daftar trip kebun ke gudang.
- `/harga` — papan harga pasar induk.

Setiap layar wajib mendukung mode offline: tulis ke SQLDelight lokal lalu sinkron ke backend.
