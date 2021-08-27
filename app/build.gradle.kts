plugins {
    application
    kotlin("jvm")
}

group = "ai.sterling.kchat.server"
version = "1.0-SNAPSHOT"

//mainClassName = 'ai.sterling.kchat.server.ChatApplication'
//mainClassName = "io.ktor.server.netty.EngineMain"
//mainClassName = "io.ktor.server.netty.DevelopmentEngine"
application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

val ktorVersion = "1.6.2"
dependencies {
    implementation(project(":domain"))
    implementation(project(":logging:logger"))
    implementation(project(":logging:platformlogger"))

    implementation(kotlin("stdlib"))

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")

    implementation("com.google.code.gson:gson:2.8.7")

    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation("io.ktor:ktor-network:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:1.2.5")
    implementation("io.ktor:ktor-websockets:$ktorVersion")

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-sessions:$ktorVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")

    // Use JUnit test framework
    testImplementation("junit:junit:4.13.2")
}
