import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
}

kotlin {
    jvmToolchain(17)
    // kotlinx-datetime 0.7 memakai kotlin.time.Instant (eksperimental di Kotlin 2.2).
    compilerOptions { optIn.add("kotlin.time.ExperimentalTime") }
}

dependencies {
    implementation(project(":shared-core"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.client.mock)
}

compose.desktop {
    application {
        mainClass = "gudang.MainKt"
        nativeDistributions {
            // ASUMSI: plugin Compose hanya menghasilkan Msi/Exe; paket MSIX (makeappx) butuh Windows
            // dan dikerjakan terpisah. Tanpa auto-update (non-goal PRD/30).
            targetFormats(TargetFormat.Msi)
            packageName = "LumbungGudang"
            packageVersion = "1.0.0"
        }
    }
}
