import java.time.LocalDateTime

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.magnetar.janus"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.magnetar.janus"
        minSdk = 26
        targetSdk = 37
        // yyyy-MM-dd-HH-mm-sss; use -PjanusVersion for reproducible builds.
        val now = LocalDateTime.now()
        val generatedVersion = "%04d-%02d-%02d-%02d-%02d-%03d".format(now.year, now.monthValue, now.dayOfMonth, now.hour, now.minute, now.second)
        versionCode = (System.currentTimeMillis() / 1000L).toInt()
        versionName = providers.gradleProperty("janusVersion").orElse(generatedVersion).get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
