import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// --- Configuración local (estos archivos no se suben a git, salvo el .jks) ---
// local.properties    -> backend.host=192.168.1.50  (IP del PC si pruebas en teléfono físico)
// keystore.properties -> datos del .jks para firmar el APK release
fun cargarProps(nombre: String) = Properties().apply {
    val archivo = rootProject.file(nombre)
    if (archivo.exists()) archivo.inputStream().use { load(it) }
}
val localProps = cargarProps("local.properties")
val keystoreProps = cargarProps("keystore.properties")
// 10.0.2.2 = el PC visto desde el emulador de Android
val backendHost: String = localProps.getProperty("backend.host", "10.0.2.2")

android {
    namespace = "com.aprendoiasantiago"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.aprendoiasantiago"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URLs base de los microservicios Spring Boot (deben terminar en "/")
        buildConfigField("String", "BASE_URL_PUNTOS", "\"http://$backendHost:8081/\"")
        buildConfigField("String", "BASE_URL_JUGADORES", "\"http://$backendHost:8082/\"")
    }

    signingConfigs {
        create("release") {
            if (keystoreProps.containsKey("storeFile")) {
                storeFile = rootProject.file(keystoreProps.getProperty("storeFile"))
                storePassword = keystoreProps.getProperty("storePassword")
                keyAlias = keystoreProps.getProperty("keyAlias")
                keyPassword = keystoreProps.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        debug {
            // Reporte de cobertura (JaCoCo) para las pruebas unitarias
            enableUnitTestCoverage = true
        }
        release {
            optimization {
                enable = false
            }
            // Si no existe keystore.properties, el APK release sale sin firmar
            if (keystoreProps.containsKey("storeFile")) {
                signingConfig = signingConfigs.getByName("release")
            }
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
    testOptions {
        unitTests {
            // Las clases de android.* devuelven valores por defecto en pruebas JVM
            isReturnDefaultValues = true
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.mlkit.vision)
    implementation(libs.mlkit.barcode.scanning)
    implementation("com.google.mlkit:face-detection:16.1.7")

    // Conexión con los microservicios y APIs externas
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp)

    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.osmdroid.android)
    implementation(libs.coil.compose)

    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.media3:media3-exoplayer:1.11.1")
    implementation("androidx.media3:media3-ui:1.11.1")
}