plugins {
    kotlin("jvm") version "1.8.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("info.picocli:picocli:4.6.1")
    implementation("com.andreapivetta.kolor:kolor:1.0.0")
    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.50.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
    implementation("com.h2database:h2:2.1.214")
    implementation("org.slf4j:slf4j-nop:2.0.0")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(16)
}