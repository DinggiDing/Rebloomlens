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

// Automatic Inclusion of Plugin Modules
file("plugins").listFiles()?.forEach { dir ->
    if (dir.isDirectory) {
        include(":plugins:${dir.name}")
    }
}
include(":common")
include(":plugins:aaa")
include(":plugins:likert_scale")
