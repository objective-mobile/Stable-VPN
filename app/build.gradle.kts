import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
} // Load local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.objmobile.stablevpn"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.objmobile.stablevpn"
        minSdk = 23
        targetSdk = 36
        versionCode = 8
        versionName = "2026.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("Boolean", "IS_ADVERTISING", true.toString())
        buildConfigField(
            "String",
            "BANNER_ID",
            "\"${localProperties.getProperty("BANNER_ID") ?: ""}\""
        )
        buildConfigField(
            "String",
            "INTERSTITIAL_ID",
            "\"${localProperties.getProperty("INTERSTITIAL_ID") ?: ""}\""
        )

        manifestPlaceholders["admobApplicationId"] =
            localProperties.getProperty("ADMOB_APPLICATION_ID") ?: ""
    }

    signingConfigs {
        create("release") {
            storeFile = file(project.property("RELEASE_STORE_FILE") as String)
            storePassword = project.property("RELEASE_STORE_PASSWORD") as String
            keyAlias = project.property("RELEASE_KEY_ALIAS") as String
            keyPassword = project.property("RELEASE_KEY_PASSWORD") as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    packaging {
        jniLibs {
            keepDebugSymbols += "**/*.so"
            useLegacyPackaging = true
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":app:presentation"))
    implementation(project(":app:domain"))
    implementation(project(":app:data")) // Permissions module dependencies
    implementation(project(":permissions:domain"))
    implementation(project(":permissions:data"))
    implementation(project(":countries:domain"))
    implementation(project(":countries:data"))
    implementation(project(":advertising:domain"))
    implementation(project(":advertising:presentation"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.0.0")

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.google.admob)
}