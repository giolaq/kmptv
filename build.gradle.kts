plugins {
    kotlin("multiplatform") version "1.9.24" apply false
    kotlin("android") version "1.9.24" apply false
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
}

allprojects {
    group = "com.kmptv"
    version = "0.1.0"
}

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}