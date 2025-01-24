plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("com.modrinth.minotaur") version "2.+"
    id("org.openjfx.javafxplugin") version "0.1.0"
}

repositories {
    mavenCentral()
    maven("https://repo.thejocraft.net/releases/") {
        name = "tjcserver"
    }
    maven ("https://maven.maxhenkel.de/repository/public")
    maven ("https://m2.dv8tion.net/releases")
    maven ("https://jitpack.io")
    //maven("https://repo.plasmoverse.com/releases")
    //maven("https://repo.plasmoverse.com/snapshots")
}

dependencies {
    runtimeOnly(project(":platform-paper-1.20"))
    runtimeOnly(project(":platform-paper-1.20.2"))
    runtimeOnly(project(":platform-paper-1.20.3"))
    runtimeOnly(project(":platform-paper-1.20.5"))
    runtimeOnly(project(":platform-paper-1.21.2"))

    implementation("dev.jorel:commandapi-bukkit-shade:9.7.0")
    implementation("de.maxhenkel.voicechat:voicechat-api:2.4.11")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("uk.co.caprica:vlcj:4.8.2")
    implementation("org.freedesktop.gstreamer:gst1-java-core:1.4.0")
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("net.java.dev.jna:jna-platform:5.14.0")
    implementation("io.javalin:javalin:6.4.0")

    implementation("io.netty:netty-buffer:4.1.116.Final")
    implementation("io.netty:netty-codec:4.1.116.Final")

    compileOnlyApi("org.bytedeco:javacv-platform:1.5.11")

    api(project(":platform-common"))

    compileOnly("de.pianoman911:mapengine-api:1.8.7")
    //compileOnly("de.pianoman911:mapengine-mediaext:1.1.3")
    //compileOnly("su.plo.voice.api:server:2.1.0-SNAPSHOT")

}

javafx {
    version = "23.0.1"
    modules = listOf("javafx.controls")
}

tasks {
    shadowJar {
        destinationDirectory.set(rootProject.buildDir.resolve("libs"))
        archiveBaseName.set(rootProject.name)

        relocate("dev.jorel.commandapi", "net.somewhatcity.boiler.commandapi")
        relocate("okhttp3", "net.somewhatcity.boiler.okhttp")
        relocate("kotlin", "net.somewhatcity.boiler.kotlin")
        dependencies {
            exclude(dependency("de.maxhenkel.voicechat:voicechat-api:2.4.11"))
            //exclude(dependency("com.formdev:flatlaf:3.3"))
        }
    }

    assemble {
        dependsOn(shadowJar)
    }
}

bukkit {
    main = "$group.boiler.core.BoilerPlugin"
    apiVersion = "1.20"
    authors = listOf("mrmrmystery")
    name = rootProject.name
    depend = listOf("MapEngine", "FFmpeg")
    softDepend = listOf("PlasmoVoice", "voicechat")
    version = rootProject.version.toString()
    //softDepend = listOf("voicechat")
}

modrinth {
    token.set(System.getenv("MODRINTH_TOKEN"))
    projectId.set("9R8jBgPj")
    versionNumber.set(rootProject.version.toString())
    versionType.set("release")
    uploadFile.set(tasks.shadowJar)
    gameVersions.addAll(listOf("1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4", "1.20.6", "1.21"))
    loaders.addAll(listOf("paper", "purpur"))
    dependencies {
        required.project("fCDPz9mZ")
        required.project("FMg8aS6R")
        optional.project("9eGKb6K1")
        optional.project("1bZhdhsH")
    }
}