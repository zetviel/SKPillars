import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0" // Используйте актуальную версию Kotlin
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("xyz.jpenilla.run-paper") version "2.3.1"
}

group = "net.adamcowell14"
version = "1.1.3"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")

    maven("https://maven.enginehub.org/repo/")
    maven("https://jitpack.io")

    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Adventure
    implementation("net.kyori:adventure-text-minimessage:4.20.0")

    implementation(platform("com.intellectualsites.bom:bom-newest:1.52")) // Ref: https://github.com/IntellectualSites/bom
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Bukkit") { isTransitive = false }

    implementation("com.github.Efnilite:neoschematic:v1.2.0")

    compileOnly("me.clip:placeholderapi:2.11.6")
}

tasks {
    runServer {
        minecraftVersion("1.21")
    }

}

tasks.withType<ShadowJar> {
    dependencies {
        exclude(dependency("com.sk89q.worldedit:.*"))
        exclude(dependency("com.fastasyncworldedit:.*"))
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}