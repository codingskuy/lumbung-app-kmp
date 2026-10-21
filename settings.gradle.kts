pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "lumbung-app-kmp"

// Logika bisnis bersama (fee, grading, sinkron, rute). Hanya stdlib + kotlinx-datetime + kotlinx-serialization.
include(":shared-core")
// Penyimpanan lokal offline-first (SQLDelight): skema, outbox FIFO, pemetaan nomor TMP (LMB-KMP-003).
include(":shared-local")
// Gudang desktop Windows (Compose Desktop, JVM): timbang ganda, sortir, label, surat jalan (LMB-KMP-004).
include(":gudang-desktop")
// Mobile lapangan Android/iOS (KMP library: sopir #5, petani #6); shell aplikasi menyusul.
include(":lapangan-mobile")
// Modul aplikasi (composeApp, gudang-desktop, lapangan-mobile) ditambahkan di tiket LMB-KMP-003..006
// saat direktori dan build file-nya dibuat.
