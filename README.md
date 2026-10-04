# lumbung-app-kmp

Aplikasi multiplatform Lumbung untuk rantai pasok hortikultura: gudang dan lapangan dalam satu basis kode.

## Teknologi

- Kotlin Multiplatform (KMP) + Compose Multiplatform (CMP) + Navigation3
- Kotlin `2.2.20` (pin exact, lihat `gradle/libs.versions.toml`)
- Compose Multiplatform `1.8.2`
- Navigation3 `1.0.0`
- Ktor Client `3.1.3`
- SQLDelight `2.1.0`

## Target Platform

- `android` — aplikasi lapangan dan gudang mobile (scan, timbang, trip)
- `ios` — aplikasi lapangan (iPhone petani/koordinator)
- `windows` — aplikasi gudang desktop (timbang USB/serial + cetak thermal)

## Struktur Modul

```text
lumbung-app-kmp/
├── composeApp/            # UI bersama (CMP + Navigation3)
│   └── README.md
├── gudang-desktop/        # :gudang-desktop — USB/serial timbangan + printer thermal (Windows)
├── lapangan-mobile/       # :lapangan-mobile — alur lapangan Android/iOS (panen, lot, trip)
├── gradle/
│   └── libs.versions.toml # katalog versi (pin exact)
└── settings.gradle.kts
```

- Modul `:gudang-desktop`: integrasi timbangan via USB/serial, cetak struk fee via printer thermal, mode offline-antre.
- Modul `:lapangan-mobile`: catat panen, lot, dan trip dari kebun ke gudang; sinkron ke `lumbung-backend-service`.

## Prasyarat

- JDK 17
- Gradle wrapper (dibuat via `gradle wrapper` pada mesin pengembang)
- Android SDK untuk target `android`, Xcode untuk target `ios`

## Cara Jalan (ringkas)

```bash
./gradlew :composeApp:assembleDebug
./gradlew :lapangan-mobile:assembleDebug
./gradlew :gudang-desktop:run
```

## Keterkaitan

- Platform: [Lumbung-TownHall](https://github.com/Coding-Skuy/Lumbung-TownHall)
- Backend: [lumbung-backend-service](https://github.com/Coding-Skuy/lumbung-backend-service)
- Web ops: [lumbung-web](https://github.com/Coding-Skuy/lumbung-web)
