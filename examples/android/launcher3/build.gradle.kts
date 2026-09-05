plugins {
    id("com.android.library")
}

android {
    namespace = "com.android.launcher3"
    compileSdk = 37
    defaultConfig {
        minSdk = 26
        buildConfigField("String", "APPLICATION_ID", "\"org.hermeslauncher.app\"")
        buildConfigField("boolean", "IS_STUDIO_BUILD", "true")
        buildConfigField("boolean", "QSB_ON_FIRST_SCREEN", "true")
        buildConfigField("boolean", "IS_DEBUG_DEVICE", "false")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { buildConfig = true }
    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
    sourceSets {
        getByName("main") {
            java.setSrcDirs(
                listOf(
                    "../third_party/launcher3/src",
                    "../third_party/launcher3/src_plugins",
                    "../third_party/launcher3/src_shortcuts_overrides",
                    "../third_party/launcher3/src_ui_overrides",
                    "src/stubs",
                    "src/generated",
                ),
            )
            kotlin.setSrcDirs(
                listOf(
                    "../third_party/launcher3/src",
                    "../third_party/launcher3/src_plugins",
                    "../third_party/launcher3/src_shortcuts_overrides",
                    "../third_party/launcher3/src_ui_overrides",
                    "src/stubs",
                ),
            )
            res.setSrcDirs(listOf("../third_party/launcher3/res"))
            manifest.srcFile("src/main/AndroidManifest.xml")
        }
    }
}

dependencies {
    api(project(":iconloader"))
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("androidx.dynamicanimation:dynamicanimation:1.1.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.slice:slice-view:1.1.0-alpha02")
    implementation("androidx.slice:slice-core:1.1.0-alpha02")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("com.google.protobuf:protobuf-javalite:3.25.5")
}

tasks.matching { it.name.startsWith("extract") && it.name.endsWith("Annotations") }
    .configureEach { enabled = false }

// AGP still requires typedefs.txt on sync*LibJars even when extract*Annotations is skipped.
tasks.matching { it.name.startsWith("sync") && it.name.endsWith("LibJars") }.configureEach {
    doFirst {
        val cap = name.removePrefix("sync").removeSuffix("LibJars")
        val variant = cap.replaceFirstChar { it.lowercase() }
        val typedefs = layout.buildDirectory.get().asFile.resolve(
            "intermediates/annotations_typedef_file/$variant/extract${cap}Annotations/typedefs.txt",
        )
        typedefs.parentFile.mkdirs()
        if (!typedefs.exists()) {
            typedefs.writeText("")
        }
    }
}
