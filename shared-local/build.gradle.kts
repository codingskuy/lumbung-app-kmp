plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.sqldelight)
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
            api(project(":shared-core"))
            implementation(libs.sqldelight.runtime)
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)
        }
        androidMain.dependencies { implementation(libs.sqldelight.driver.android) }
        iosMain.dependencies { implementation(libs.sqldelight.driver.native) }
        jvmMain.dependencies { implementation(libs.sqldelight.driver.jvm) }
        jvmTest.dependencies { implementation(kotlin("test")) }
    }
}

android {
    namespace = "id.codingskuy.lumbung.local"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

sqldelight {
    databases {
        create("LumbungDb") {
            packageName.set("local.db")
        }
    }
}

kover {
    reports {
        filters { excludes { packages("local.db") } }
        verify { rule { minBound(85) } }
    }
}
