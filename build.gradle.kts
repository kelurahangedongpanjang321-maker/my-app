plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.aistudio.simpel.vugtya"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aistudio.simpel.vugtya"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
            storeFile = file(keystorePath)
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = "upload"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isCrunchPngs = false
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            
            // Mengikuti instruksi bawaan dari panduan project Anda
            // Jika nanti release build masih terkendala, baris ini bisa Anda hapus/beri tanda //
            signingConfig = signingConfigs.getByName("release")
        }
        
        debug {
            // Dikosongkan agar otomatis menggunakan debug keystore default bawaan sistem.
            // Ini akan menyelesaikan error 'debug.keystore not found' di GitHub Actions Anda!
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    // Bagian dependencies Anda di bawah (jika ada) tidak perlu diubah, 
    // silakan biarkan tetap seperti aslinya di bawah baris ini.
}
