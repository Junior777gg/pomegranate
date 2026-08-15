import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("plugin.serialization") version "2.4.0"
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    androidLibrary {
       namespace = "org.unstabledev.pomegranate.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_17
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        jvmMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(files("libs/HideP2P.jar"))
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            implementation("com.google.crypto.tink:tink:1.21.0")
            implementation("org.bytedeco:javacv-platform:1.5.10")
        }
        androidMain.dependencies {
            implementation(files("libs/HideP2P.jar"))
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compose.uiToolingPreview)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            val camerax_version = "1.6.1"
            implementation("androidx.camera:camera-core:${camerax_version}")
            implementation("androidx.camera:camera-compose:${camerax_version}")
            implementation("androidx.camera:camera-camera2:${camerax_version}")
            implementation("androidx.camera:camera-lifecycle:${camerax_version}")
            implementation("androidx.camera:camera-video:${camerax_version}")
            implementation("androidx.camera:camera-view:${camerax_version}")
            implementation("androidx.camera:camera-extensions:${camerax_version}")
            implementation("com.google.crypto.tink:tink:1.21.0")
        }
        commonMain.dependencies {
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.5.0")
            implementation("io.coil-kt.coil3:coil-compose:3.5.0")
            implementation("org.kotlincrypto.hash:sha2:0.5.3")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
            implementation(libs.ktor.client.core)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
            implementation("org.jetbrains.androidx.navigation:navigation-compose:2.9.2")
            implementation(compose.materialIconsExtended)
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("io.github.panpf.sketch4:sketch-compose:4.4.0")
            implementation("io.github.panpf.sketch4:sketch-animated-gif:4.4.0")
            implementation("io.github.panpf.sketch4:sketch-animated-webp:4.4.0")
            implementation("com.fleeksoft.ksoup:ksoup:0.2.6")
            implementation("com.fleeksoft.ksoup:ksoup-network:0.2.6")
            implementation("com.mikepenz:multiplatform-markdown-renderer:0.41.0")
            implementation("com.mikepenz:multiplatform-markdown-renderer-m3:0.41.0")
        }
        iosArm64Main.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
room {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    androidRuntimeClasspath(libs.compose.uiTooling)
}