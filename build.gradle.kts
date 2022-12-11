import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.21"
    application
}

group = "mpeciakk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://repo.peciak.xyz/snapshots")
    maven("https://oss.sonatype.org/content/repositories/snapshots")
}

val lwjglVersion = "3.3.1"

val lwjglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->
            "natives-linux"
        arrayOf("Windows").any { name.startsWith(it) }                           ->
            "natives-windows"
        else -> throw Error("Unrecognized or unsupported platform. Please set \"lwjglNatives\" manually")
    }
}

dependencies {
    implementation("mpeciakk:Ain:1.0.2-SNAPSHOT")
    implementation("mpeciakk:Aries:1.0-SNAPSHOT")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")

    implementation("de.javagl:obj:0.3.0")

//    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
//
//    implementation("org.joml:joml:1.9.2")
//    implementation("org.lwjgl", "lwjgl")
//    implementation("org.lwjgl", "lwjgl-glfw")
//    implementation("org.lwjgl", "lwjgl-opengl")
//    runtimeOnly("org.lwjgl", "lwjgl", classifier = lwjglNatives)
//    runtimeOnly("org.lwjgl", "lwjgl-glfw", classifier = lwjglNatives)
//    runtimeOnly("org.lwjgl", "lwjgl-opengl", classifier = lwjglNatives)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

application {
    mainClass.set("MainKt")
}