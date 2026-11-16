plugins {
    id("net.fabricmc.fabric-loom") version "1.19-SNAPSHOT"
    id("spectatorplus.platform")
}

description = "A Fabric mod that improves spectator mode by showing the hotbar, health, and held item of the spectated player"

repositories {
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://api.modrinth.com/maven")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        register("spectatorplus") {
            sourceSet(sourceSets.getByName("main"))
            sourceSet(sourceSets.getByName("client"))
        }
    }

    accessWidenerPath = file("src/main/resources/spectatorplus.accesswidener")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    implementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    implementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")

    include(implementation("maven.modrinth:fabric-permissions-api:${property("fabric_permissions_api_version")}")!!)

    // Optional soft deps (compile-only). Clients without them still run; config UI/mod menu are gated in code.
    compileOnly("maven.modrinth:cloth-config:${property("cloth_config_version")}")
    compileOnly("maven.modrinth:modmenu:${property("modmenu_version")}")
}

tasks {
    withType<JavaCompile>().configureEach {
        options.release = 25
    }

    processResources {
        inputs.property("version", project.version)
        inputs.property("description", project.description)
        filesMatching("fabric.mod.json") {
            expand(
                mapOf(
                    "version" to project.version,
                    "description" to project.description,
                )
            )
        }
    }

    jar {
        from("../LICENSE")
    }
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
