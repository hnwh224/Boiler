rootProject.name = "Boiler"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://papermc.io/repo/repository/maven-public/")
    }
}
include("api")
include("plugin")
