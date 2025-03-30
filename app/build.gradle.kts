plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.sqltp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sqltp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    // Use version catalog references (from libs.versions.toml)
    implementation(libs.material) {
        exclude(group = "androidx.lifecycle", module = "lifecycle-viewmodel-ktx")
    }
    implementation(libs.appcompat)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Third-party library not in version catalog
    implementation("de.hdodenhof:circleimageview:3.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}