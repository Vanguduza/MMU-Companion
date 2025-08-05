plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.aeci.mmucompanion"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.aeci.mmucompanion"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false  // Temporarily disabled due to POI + R8 conflicts
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("debug")
        }
        debug {
            isMinifyEnabled = false
            isDebuggable = true
        }
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    
    kotlinOptions {
        jvmTarget = "17"
    }
    
    ksp {
        arg("dagger.hilt.shareTestComponents", "false")
        arg("dagger.formatGeneratedSource", "disabled")
    }
    
    buildFeatures {
        compose = true
        viewBinding = true
    }
    
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/DEPENDENCIES"
            excludes += "/META-INF/LICENSE"
            excludes += "/META-INF/LICENSE.txt"
            excludes += "/META-INF/NOTICE"
            excludes += "/META-INF/NOTICE.txt"
            excludes += "/META-INF/services/javax.xml.stream.*"
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    
    // Material3 additional components
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.material.icons.extended)
    
    // Debug dependencies (these are needed but might be missing)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    
    // Navigation
    implementation(libs.androidx.navigation.compose)
    
    // ViewModel & LiveData
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    
    // Hilt for Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    
    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)
    
    // PDF Generation
    implementation(libs.itext.core)
    implementation(libs.itext.html2pdf)
    
    // Excel Export - with exclusions to avoid AWT dependencies
    implementation(libs.poi) {
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
    }
    implementation(libs.poi.ooxml) {
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
        exclude(group = "org.apache.santuario", module = "xmlsec")
    }
    implementation(libs.poi.scratchpad)
    implementation(libs.xmlbeans) {
        exclude(group = "org.apache.ant", module = "ant")
    }
    implementation(libs.xmlsec) {
        exclude(group = "org.apache.xalan", module = "xalan")
        exclude(group = "org.apache.xerces", module = "xercesImpl")
    }
    implementation(libs.commons.compress)
    implementation(libs.commons.codec)
    
    // Image Processing
    implementation(libs.glide)
    ksp(libs.glide.compiler)
    
    // Camera & Gallery
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)
    
    // Signature Capture
    implementation(libs.signature.pad)
    
    // Date/Time Picker
    implementation(libs.material.dialogs.datetime)
    
    // Permissions
    implementation(libs.accompanist.permissions)
    
    // WorkManager for Background Tasks
    implementation(libs.work.runtime.ktx)
    implementation(libs.hilt.work)
    
    // Security
    implementation(libs.security.crypto)
    
    // Biometric Authentication
    implementation(libs.biometric)
    
    // Image Loading and Processing
    implementation(libs.coil.compose)
    
    // Activity Result APIs
    implementation(libs.androidx.activity.compose.latest)
    
    // JSON Processing
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)
    
    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}