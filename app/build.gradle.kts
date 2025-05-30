plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.baselineprofile)
}

android {
  namespace = "id.tiooooo.testbaseline"
  compileSdk = 35

  defaultConfig {
    applicationId = "id.tiooooo.testbaseline"
    minSdk = 24
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
    create("benchmark") {
      initWith(buildTypes.getByName("release"))
      signingConfig = signingConfigs.getByName("debug")
      matchingFallbacks += listOf("release")
      isDebuggable = false
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
  }
}

dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.ui)
  implementation(libs.androidx.ui.graphics)
  implementation(libs.androidx.ui.tooling.preview)
  implementation(libs.androidx.material3)
  implementation(libs.androidx.profileinstaller)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.ui.test.junit4)
  "baselineProfile"(project(":baselineprofile"))
  debugImplementation(libs.androidx.ui.tooling)
  debugImplementation(libs.androidx.ui.test.manifest)
}

tasks.register<Delete>("cleanBaselineProfile") {
  val baselineFile = file("src/release/generated/baselineProfiles/baseline-prof.txt")
  val startupFile = file("src/release/generated/baselineProfiles/startup-prof.txt")
  delete(baselineFile, startupFile)

  doFirst {
    println("🧹 Cleaning old baseline profile files...")
  }
}

tasks.register("generateBaselineProfileFull") {
  group = "build"
  description = "Clean baseline profile files and generate new baseline profile"

  dependsOn("cleanBaselineProfile", ":app:generateBaselineProfile")

  doLast {
    println("✅ Baseline profile generated.")
  }
}
