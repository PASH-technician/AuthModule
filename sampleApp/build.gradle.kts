import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.ksp)
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SampleApp"
            isStatic = true
        }
    }

    jvm("desktop")

    sourceSets {
        commonMain.dependencies {
            //Project dependencies
            implementation(project(":oauth_2_0:impl"))
            implementation(project(":oauth_2_0:ktor"))

            implementation(project(":sampleApp:uikit"))

            //UI (compose)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            //Network (ktor)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.logging)

            //DI (koin)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(project.dependencies.platform(libs.koin.annotations.bom))
            api(libs.koin.annotations)

            //mvikotlin
            implementation(libs.mvikotlin)
            implementation(libs.mvikotlin.main)
            implementation(libs.mvikotlin.logging)
            implementation(libs.mvikotlin.extensions.coroutines)

            //Navigation (decompose)
            implementation(libs.decompose)
            implementation(libs.decompose.compose)

            //essenty
            implementation(libs.essenty.lifecycle.coroutines)
        }

        val desktopMain by getting

        androidMain.dependencies {
            //Network (ktor)
            implementation(libs.ktor.client.okhttp)

            //Platform SDK + Jetpack
            implementation(libs.androidx.ui)
            implementation(libs.androidx.ui.tooling)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.foundation.layout.android)

            //Di (koin)
            implementation(libs.koin.android)

            //coroutine
            implementation(libs.kotlinx.coroutines.android)
        }

        desktopMain.dependencies {
            //UI (compose)
            implementation(compose.desktop.currentOs)

            //coroutine
            implementation(libs.kotlinx.coroutines.swing)
        }
    }

    sourceSets.named("commonMain").configure {
        kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)

    add("kspAndroid", libs.koin.ksp.compiler)

    add("kspIosX64", libs.koin.ksp.compiler)
    add("kspIosArm64", libs.koin.ksp.compiler)
    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)

    add("kspDesktop", libs.koin.ksp.compiler)
}

project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

android {
    namespace = property("group").toString() + ".sample"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = property("group").toString() + ".sample"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose {
    resources {
        publicResClass = false
        generateResClass = always
    }
    desktop {
        application {
            mainClass = property("group").toString() + ".sample.MainKt"

            nativeDistributions {
                targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                packageName = property("group").toString() + ".sample"
                packageVersion = "1.0.0"
            }
        }
    }
}