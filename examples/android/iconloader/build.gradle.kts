plugins {
    id("com.android.library")
}

android {
    namespace = "com.android.launcher3.icons"
    compileSdk = 37
    defaultConfig { minSdk = 26 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    lint { abortOnError = false }
    sourceSets {
        getByName("main") {
            java.setSrcDirs(
                listOf(
                    "../third_party/systemui/iconloaderlib/src",
                    "src/compat",
                ),
            )
            res.setSrcDirs(listOf("../third_party/systemui/iconloaderlib/res"))
            manifest.srcFile("../third_party/systemui/iconloaderlib/AndroidManifest.xml")
        }
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.core:core:1.19.0")
}
