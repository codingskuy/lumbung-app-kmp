plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kover)
}

kotlin {
    explicitApi()
    jvmToolchain(17)
    // kotlinx-datetime 0.7 memakai kotlin.time.Instant (eksperimental di Kotlin 2.2).
    compilerOptions { optIn.add("kotlin.time.ExperimentalTime") }

    jvm()
    androidTarget()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // Aturan dependensi (produk/40-modul-KMP-bersama.md): hanya tiga ini.
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }
        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}

android {
    namespace = "id.codingskuy.lumbung.core"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kover {
    reports {
        verify {
            rule {
                // Target mutu modul: cakupan uji unit minimal 85%.
                minBound(85)
            }
        }
    }
}

// Pelanggaran aturan dependensi shared-core gagal di check build.
val periksaDependensi by tasks.registering {
    group = "verification"
    description = "Gagal bila commonMain bergantung pada Compose, SQLDelight, atau Ktor."
    val terlarang = listOf("compose", "sqldelight", "ktor", "androidx")
    val daftar = provider {
        listOf("commonMainImplementation", "commonMainApi")
            .mapNotNull { configurations.findByName(it) }
            .flatMap { c -> c.allDependencies.map { "${it.group}:${it.name}" } }
    }
    doLast {
        val pelanggar = daftar.get().filter { d -> terlarang.any { d.lowercase().contains(it) } }
        check(pelanggar.isEmpty()) { "shared-core dilarang bergantung pada: $pelanggar" }
    }
}
tasks.named("check") { dependsOn(periksaDependensi) }
