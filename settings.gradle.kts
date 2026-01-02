pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()

    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "Stable VPN"
include(":app")
include(":app:data")
include(":app:domain")
include(":app:presentation")
include(":countries")
include(":countries:data")
include(":countries:domain")
include(":vpn")
include(":vpn:data")
include(":vpn:domain")
include(":permissions")
include(":permissions:data")
include(":permissions:domain")
include(":advertising")
include(":advertising:domain")
include(":advertising:presentation")
include(":advertising:data")