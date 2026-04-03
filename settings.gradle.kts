pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Object Detection Lab"
include(":app")
include(":core:ml")
include(":core:cpp")
include(":core:camera")
include(":core:ui")
include(":core:designsystem")
include(":core:dispatchers")
include(":core:common")
include(":feature:detection")
include(":benchmark:macrobenchmark")
