
import org.jetbrains.kotlin.storage.CacheResetOnProcessCanceled.enabled




plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("kotlin-android")

}



android {
    namespace = "com.example.whisper_waves"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.whisper_waves"
        minSdk = 26
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

    packagingOptions {
        exclude("META-INF/AL2.0")
        exclude("META-INF/LGPL2.1")
        exclude("META-INF/DEPENDENCIES")
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/NOTICE.md")
        // Add more exclude statements as needed
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }


}


configurations {
    all {
        exclude( group= "javax.activation", module= "javax.activation-api")
    }
}
configurations {
    all {
        exclude (group= "javax.activation", module= "javax.activation-api")
        exclude (group= "com.sun.activation", module= "javax.activation")
    }
}


dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.5.21")
    implementation ("androidx.core:core-ktx:1.3.1")
    implementation ("androidx.appcompat:appcompat:1.2.0")
    implementation ("com.google.android.material:material:1.2.0")
    implementation ("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("androidx.navigation:navigation-fragment:2.3.0")
    implementation ("androidx.navigation:navigation-ui:2.3.0")
    implementation ("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.3.0")
    implementation ("androidx.navigation:navigation-ui-ktx:2.3.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.2.0")
    implementation ("com.google.firebase:firebase-database:19.3.1")
    implementation ("com.google.firebase:firebase-auth:19.3.2")
    implementation ("com.google.firebase:firebase-storage:19.1.1")
    implementation ("com.squareup.picasso:picasso:2.71828")



    implementation ("com.github.bumptech.glide:glide:4.12.0")
    // implementation ("com.sun.activation:javax.activation:1.2.0") {
    //     exclude (group= "jakarta.activation", module= "jakarta.activation-api")
    // }
    implementation ("jakarta.activation:jakarta.activation-api:1.2.1") {
        ///  exclude( group= "com.sun.activation", module= "javax.activation")
    }
    implementation("com.google.firebase:firebase-messaging-ktx:23.4.1")
    kapt ("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation ("junit:junit:4.13")
    androidTestImplementation ("androidx.test.ext:junit:1.1.1")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.2.0")
    implementation ("com.android.tools.build:gradle:4.2.2")
    implementation ("com.android.tools.build:bundletool:1.15.6")
}