plugins {
    alias(libs.plugins.android.application)
    id("com.google.protobuf") version "0.9.2"}


android {
    namespace = "ma.projet.grcp"
    compileSdk = 34

    defaultConfig {
        applicationId = "ma.projet.grcp"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("io.grpc:grpc-okhttp:1.54.0")
    implementation("io.grpc:grpc-protobuf-lite:1.54.0")
    implementation("io.grpc:grpc-stub:1.54.0")
    implementation("javax.annotation:javax.annotation-api:1.3.2")
    implementation ("androidx.cardview:cardview:1.0.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.22.2"
    }
    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.54.0"
        }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                maybeCreate("java").apply {
                    option("lite")
                }
            }
            task.plugins {
                maybeCreate("grpc").apply {
                    option("lite")
                }
            }
        }
    }
}

