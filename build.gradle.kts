plugins {
    kotlin("jvm") version "1.9.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("io.papermc.paperweight.userdev") version "1.5.5"
    application
}

apply(plugin = "io.papermc.paperweight.userdev")

group = "dev.osmii"
version = "1.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://oss.sonatype.org/content/repositories/central")
    maven("https://repo.papermc.io/repository/maven-public/")
    mavenLocal()
}

dependencies {
    paperweight.paperDevBundle("1.20.1-R0.1-SNAPSHOT")
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib"))
    compileOnly("com.comphenix.protocol:ProtocolLib:5.1.0")
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
    dependencies {
        implementation(kotlin("stdlib"))
    }
}

application {
    mainClass.set("dev.osmii.shadow.Shadow")
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    build {
        dependsOn("shadowJar")
    }
    shadowJar {
        archiveClassifier.set("")
        destinationDirectory.set(layout.buildDirectory.dir("C:/Users/osmii/Desktop/Programming/Personal/Minecraft/Server/plugins"))
    }
    jar {
    }
}