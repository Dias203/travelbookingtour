plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}


android {
    namespace = "com.example.travel_app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.travel_app"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.database)
    implementation(libs.firebase.firestore)
    implementation(fileTree(mapOf(
        "dir" to "C:\\Users\\Admin\\Downloads\\zalopaysdklib",
        "include" to listOf("*.aar", "*.jar"),
        "exclude" to listOf("")
    )))
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation ("com.github.ismaeldivita:chip-navigation-bar:1.4.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.stripe:stripe-java:22.2.0")
    implementation("com.stripe:stripe-android:17.2.0")
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")

    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation ("com.google.firebase:firebase-firestore:24.9.1")
    implementation ("com.google.firebase:firebase-storage:20.0.0")
    implementation ("com.google.firebase:firebase-database:20.0.0")
    implementation ("io.github.cdimascio:dotenv-java:2.2.0")
    implementation ("com.google.code.gson:gson:2.10.1")

    implementation("com.squareup.okhttp3:okhttp:4.6.0")
    implementation("commons-codec:commons-codec:1.14")

}