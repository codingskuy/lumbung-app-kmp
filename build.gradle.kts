plugins {
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kover) apply false
}

// `./gradlew uji`: uji unit shared-core (JVM) + gerbang cakupan >= 85% + pemeriksaan dependensi terlarang.
tasks.register("uji") {
    group = "verification"
    description = "Menjalankan uji unit, gerbang cakupan, dan pemeriksaan dependensi shared-core."
    dependsOn(":shared-core:jvmTest", ":shared-core:koverVerify", ":shared-core:periksaDependensi")
}
