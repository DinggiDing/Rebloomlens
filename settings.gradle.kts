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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Rebloomlens"
include(":app")
//include(":plugins:likert_scale")

include(":common")
include(":manualInput_plugins:text_input")
include(":manualInput_plugins:likert_scale")
include(":sensor_plugins:health_connect")
