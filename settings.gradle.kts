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

// Modul aplikasi bersama (Compose Multiplatform + Navigation3).
include(":composeApp")
// Modul gudang desktop Windows: USB/serial timbangan + printer thermal.
include(":gudang-desktop")
// Modul lapangan mobile Android/iOS: panen, lot, trip.
include(":lapangan-mobile")
