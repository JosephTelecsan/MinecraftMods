pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "spectatorplus"

includeBuild("build-logic")

// Fabric-only for Minecraft 26.3 manhunt port.
// Paper module retained in tree but not built (no Paper 26.3 target for this workspace).
setupSubproject("fabric")

fun setupSubproject(moduleName: String) {
    val name = "spectatorplus-$moduleName"
    include(name)
    val proj = project(":$name")
    proj.projectDir = file(moduleName)
}
