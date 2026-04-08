import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.kotlin.kapt)
    alias(libs.plugins.hilt.android)
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use(::load)
    }
}

fun propertyOrEnv(name: String, fallback: String): String {
    val localValue = localProperties.getProperty(name)
    if (!localValue.isNullOrBlank()) return localValue

    val gradleValue = providers.gradleProperty(name).orNull
    if (!gradleValue.isNullOrBlank()) return gradleValue

    val envName = name.replace('.', '_').replace('-', '_').uppercase()
    val envValue = System.getenv(envName)
    if (!envValue.isNullOrBlank()) return envValue

    return fallback
}

fun escapeBuildConfig(value: String): String = value
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")

fun websocketFallbackFrom(apiBaseUrl: String): String = when {
    apiBaseUrl.startsWith("https://") -> apiBaseUrl.replaceFirst("https://", "wss://")
    apiBaseUrl.startsWith("http://") -> apiBaseUrl.replaceFirst("http://", "ws://")
    else -> apiBaseUrl
}

val debugApiBaseUrl = propertyOrEnv("vzaimno.apiBaseUrl.debug", "http://10.0.2.2:8000/")
val releaseApiBaseUrl = propertyOrEnv("vzaimno.apiBaseUrl.release", "https://api.vzaimno.app/")
val debugWebSocketBaseUrl = propertyOrEnv(
    "vzaimno.wsBaseUrl.debug",
    websocketFallbackFrom(debugApiBaseUrl)
)
val releaseWebSocketBaseUrl = propertyOrEnv(
    "vzaimno.wsBaseUrl.release",
    websocketFallbackFrom(releaseApiBaseUrl)
)
val debugEnvironmentName = propertyOrEnv("vzaimno.environment.debug", "local")
val releaseEnvironmentName = propertyOrEnv("vzaimno.environment.release", "production")
val debugAuthEnabled = propertyOrEnv("vzaimno.authEnabled.debug", "true").toBooleanStrictOrNull() ?: true
val releaseAuthEnabled = propertyOrEnv("vzaimno.authEnabled.release", "true").toBooleanStrictOrNull() ?: true

android {
    namespace = "com.vzaimno.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vzaimno.app"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            buildConfigField("String", "APP_ENVIRONMENT", "\"${escapeBuildConfig(debugEnvironmentName)}\"")
            buildConfigField("String", "API_BASE_URL", "\"${escapeBuildConfig(debugApiBaseUrl)}\"")
            buildConfigField("String", "WEBSOCKET_BASE_URL", "\"${escapeBuildConfig(debugWebSocketBaseUrl)}\"")
            buildConfigField("boolean", "AUTH_ENABLED", debugAuthEnabled.toString())
            manifestPlaceholders["usesCleartextTraffic"] = true
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "APP_ENVIRONMENT", "\"${escapeBuildConfig(releaseEnvironmentName)}\"")
            buildConfigField("String", "API_BASE_URL", "\"${escapeBuildConfig(releaseApiBaseUrl)}\"")
            buildConfigField("String", "WEBSOCKET_BASE_URL", "\"${escapeBuildConfig(releaseWebSocketBaseUrl)}\"")
            buildConfigField("boolean", "AUTH_ENABLED", releaseAuthEnabled.toString())
            manifestPlaceholders["usesCleartextTraffic"] = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.material)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.hilt.android)
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.coil.compose)
    implementation(libs.androidx.security.crypto)

    kapt(libs.hilt.compiler)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
