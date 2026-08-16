import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kover)
}

// Carrega credenciais de assinatura de `keystore.properties` (nunca commitado — está no
// .gitignore) ou de variáveis de ambiente, para funcionar tanto localmente quanto em CI/CD
// sem expor segredos no repositório.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(keystorePropertiesFile.inputStream())
    }
}

fun signingProperty(key: String, envVar: String): String? =
    keystoreProperties.getProperty(key) ?: System.getenv(envVar)

android {
    namespace = "com.taskflow.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.taskflow.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
    }

    sourceSets {
        // Expõe os JSONs de schema exportados (`room.schemaLocation` abaixo) como
        // asset do androidTest — é o que `MigrationTestHelper` usa para validar as
        // migrations contra o schema real de cada versão. Só funciona em testes
        // instrumentados de verdade: sob Robolectric, os asset "test_config.properties"
        // gerado pelo AGP aponta para os assets da variante `debug` normal, não para um
        // diretório de assets exclusivo de `test` — por isso o MigrationTest mora em
        // `androidTest`, não em `test`.
        getByName("androidTest") {
            assets.srcDirs("$projectDir/schemas")
        }
    }

    signingConfigs {
        create("release") {
            val storeFilePath = signingProperty("storeFile", "TASKFLOW_STORE_FILE")
            // Só configura a assinatura de release se as credenciais existirem — permite
            // que `assembleDebug`, testes e CI de PR continuem funcionando sem keystore.
            if (storeFilePath != null) {
                storeFile = file(storeFilePath)
                storePassword = signingProperty("storePassword", "TASKFLOW_STORE_PASSWORD")
                keyAlias = signingProperty("keyAlias", "TASKFLOW_KEY_ALIAS")
                keyPassword = signingProperty("keyPassword", "TASKFLOW_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (keystorePropertiesFile.exists() || System.getenv("TASKFLOW_STORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api"
        )
    }

    kotlin {
        jvmToolchain(17)
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    lint {
        abortOnError = true
        warningsAsErrors = false
        checkDependencies = true
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    autoCorrect = false
}

ksp {
    // Necessário para o Room exportar o schema JSON usado por `MigrationTestHelper`
    // e para detectar, em tempo de build, mudanças de schema sem uma Migration.
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.activity.compose)

    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.compose.animation)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)

    implementation(libs.navigation.compose)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.workmanager)

    implementation(libs.coroutines.android)

    implementation(libs.work.runtime.ktx)

    implementation(libs.datastore.preferences)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.room.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.room.testing)
    debugImplementation(libs.compose.ui.test.manifest)
}
