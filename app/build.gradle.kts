plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.hilt)
    id("com.google.android.gms.oss-licenses-plugin")
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

protobuf {
    protoc {
        artifact = libs.protoc.get().toString()
    }
    // https://github.com/google/protobuf-gradle-plugin/issues/518#issuecomment-1273099797
    // https://github.com/zhaobozhen/LibChecker/blob/c0c3bc7c661fe45cc44d5c6ab0202764652e0b7e/app/build.gradle.kts#L197
    generateProtoTasks {
        all().forEach {
            it.builtins {
                create("java") {
                    option("lite")
                }
                create("kotlin") {
                    option("lite")
                }
            }
        }
    }
}

// Updated by roar
val vName = "0.2.0"

// Compute versionCode based on versionName, by padding the segments in thousands groups,
// allowing for up to 1000 patch versions per major/minor.
// v0 will be codes less than 1_000_000.
// https://pl.kotl.in/LUdtQtZh6
val (vMajor, vMinor, vPatch) = vName
    .split(".")
    .map { s -> s.toInt(radix = 10).also { require(it < 1000) } }
val vCode = "%d%03d%03d".format(vMajor, vMinor, vPatch)
    .toInt(radix = 10)
    .also { require(it < 2100000000) { "Exceeded greatest value for Google Play: $it" } }

android {
    namespace = "io.github.janmalch.simplerssreader"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.janmalch.simplerssreader"
        minSdk = 26
        targetSdk = 36
        versionName = vName
        versionCode = vCode

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        val release = getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.create("release") {
                storeFile = file("keystore/android_keystore.jks")
                storePassword = System.getenv("SIGNING_STORE_PASSWORD")
                keyAlias = System.getenv("SIGNING_KEY_ALIAS")
                keyPassword = System.getenv("SIGNING_KEY_PASSWORD")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        create("releaseSignDebug") {
            initWith(release)
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // https://github.com/Kotlin/kotlinx.coroutines#avoiding-including-the-debug-infrastructure-in-the-resulting-apk
            excludes += "DebugProbesKt.bin"
        }
    }
}

dependencies {
    implementation(libs.slf4j.nop) // TODO: write Timber adapter?
    implementation(libs.timber)
    implementation(libs.shed)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.ui.icons)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)

    implementation(libs.androidx.work)

    implementation(libs.accompanist.permissions)

    implementation(libs.hilt.android)
    implementation(libs.hilt.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.hilt.compiler)

    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)
    implementation(libs.kotlinx.serialization.core)

    implementation(libs.androidx.core.i18n)

    implementation(libs.datetime)

    implementation(libs.bundles.datastore)

    implementation(libs.room.runtime)
    implementation(libs.room.paging)
    ksp(libs.room.compiler)

    implementation(libs.coil.compose)
    implementation(libs.coil.ktor)
    implementation(libs.coil.cache)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)

    implementation(ktorLibs.client.android)
    implementation(ktorLibs.client.core)
    implementation(ktorLibs.client.cio)
    implementation(ktorLibs.client.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.xml)
    testImplementation(ktorLibs.client.mock)

    implementation(libs.coroutines.android)
    testImplementation(libs.coroutines.test)

    implementation(libs.google.oss.licenses) {
        exclude(group = "androidx.appcompat")
    }

    testImplementation(kotlin("test"))
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
