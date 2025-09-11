pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Required for Kotlin Native compiler downloads
        maven("https://download.jetbrains.com/kotlin/native/builds/releases/")
    }
}

rootProject.name = "kmptv"

include(":shared-core")
include(":androidtv-app")