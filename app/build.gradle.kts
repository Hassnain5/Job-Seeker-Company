plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.companyapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.companyapp"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX Core
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Corrected version

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.9")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.9")

    // Firebase
    implementation("com.google.firebase:firebase-database:20.3.0") // Corrected version
    implementation("com.google.firebase:firebase-storage:20.3.0") // Corrected version
    implementation("com.google.firebase:firebase-auth-ktx:23.0.0")

    // Credentials
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
    implementation("androidx.activity:activity:1.10.1")
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0") // Verify availability

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Circular ImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // Onboarding Dots Indicator
    implementation("com.tbuonomo:dotsindicator:4.3") // Corrected to stable version

    // Glide (Image Loading)
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Country Code Picker
    implementation("com.hbb20:ccp:2.7.3")

    // Gson (JSON Parsing)
    implementation("com.google.code.gson:gson:2.11.0")

    // Lottie Animation
    implementation("com.airbnb.android:lottie:6.5.2")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation ("com.google.android.material:material:1.6.0")
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")
}