plugins {
    kotlin(kapt())
}

val kotlinVersion = "1.5.21"
val coroutineVersion = "1.5.1"
dependencies {
    implementation project(':logging:logger')

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutineVersion")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:$kotlinVersion")

    implementation("com.google.dagger:dagger:2.38.1")
    kapt("com.google.dagger:dagger-compiler:2.38.1")

    // Use JUnit test framework
    testImplementation("junit:junit:4.13.2")
}
