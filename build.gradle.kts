plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.sqldelight) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
}

// `./gradlew uji`: uji unit shared-core (JVM) + gerbang cakupan >= 85% + pemeriksaan dependensi terlarang.
tasks.register("uji") {
    group = "verification"
    description = "Menjalankan uji unit, gerbang cakupan, dan pemeriksaan dependensi shared-core."
    dependsOn(":shared-core:jvmTest", ":shared-core:koverVerify", ":shared-core:periksaDependensi",
        ":shared-local:jvmTest", ":shared-local:koverVerify",
        ":gudang-desktop:test",
        ":lapangan-mobile:jvmTest")
}
