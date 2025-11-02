import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "1.9.24"
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            implementation("io.ktor:ktor-client-core:3.0.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("com.google.auth:google-auth-library-oauth2-http:1.21.0")
            implementation("com.kizitonwose.calendar:compose-multiplatform:2.7.0")
            implementation("dev.gitlive:firebase-auth:2.3.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("com.darkrockstudios:mpfilepicker:3.1.0")
            implementation(kotlin("stdlib"))
            implementation("io.ktor:ktor-client-core:3.0.1")
            implementation("io.ktor:ktor-client-cio:3.0.1")
            implementation("io.ktor:ktor-client-content-negotiation:3.0.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            implementation("io.ktor:ktor-server-core:3.0.1")
            implementation("io.ktor:ktor-server-netty:3.0.1")
            implementation("io.ktor:ktor-server-host-common:3.0.1")
            implementation("io.ktor:ktor-server-cors:3.0.1")
            implementation("io.ktor:ktor-server-call-logging:3.0.1")
            implementation("io.ktor:ktor-server-content-negotiation:3.0.1")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.1")
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.10.2")
            implementation("com.google.auth:google-auth-library-oauth2-http:1.21.0")
            implementation("com.squareup.okhttp3:okhttp:4.12.0")
            implementation("com.google.code.gson:gson:2.10.1")
            implementation("org.json:json:20231013")
            implementation("dev.gitlive:firebase-app-jvm:2.3.0")
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.nextlevelprogrammers.elearn.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.nextlevelprogrammers.elearn"
            packageVersion = "1.0.5"  // Incremented version

            // Add required modules for authentication libraries
            modules("java.sql", "java.naming", "jdk.unsupported", "java.management")

            // Configure JVM arguments for Google Auth, Firebase, and Skiko rendering
            jvmArgs += listOf(
                "-Xmx2048m",
                "-Dskiko.renderApi=SOFTWARE",  // Fix for DirectX12 rendering issue
                "--add-opens", "java.base/java.lang=ALL-UNNAMED",
                "--add-opens", "java.base/java.net=ALL-UNNAMED",
                "--add-opens", "java.base/java.nio=ALL-UNNAMED",
                "--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
                "--add-opens", "java.base/java.util=ALL-UNNAMED",
                "--add-opens", "java.base/sun.security.ssl=ALL-UNNAMED",
                "--add-opens", "java.base/java.io=ALL-UNNAMED",
                "--add-opens", "java.base/sun.security.x509=ALL-UNNAMED",
                "--add-opens", "java.management/java.lang.management=ALL-UNNAMED"
            )

            windows {
                console = false  // Changed back to false - no need for console now
                dirChooser = true
                perUserInstall = true
                menuGroup = "NextLevel Programmers"
                upgradeUuid = "a4f9c5d8-6b7e-4f3a-9c2d-1e8a7b6c5d4f"
            }
        }
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlinx" && requested.name.startsWith("kotlinx-coroutines")) {
            useVersion("1.10.2")
            because("Avoid NoSuchMethodError from Ktor/Compose coroutine mismatch")
        }
    }
}