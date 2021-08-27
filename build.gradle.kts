buildscript {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { setUrl ("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.21")
        //classpath("org.jetbrains.kotlin:kotlin-serialization:1.5.21")
        classpath("com.android.tools.build:gradle:4.2.2")
    }
}

//jar {
//	baseName = 'KChatServer'
//    version =  '0.1.0'
//    manifest {
//        attributes 'Implementation-Title': 'Gradle Quickstart',
//                   'Implementation-Version': version,
//                   'Main-Class': 'ai.sterling.kchat.server.KChatServer'
//    }
//}

allprojects {
    repositories {
        google()
        mavenCentral()
//        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-js-wrappers")
        maven(url = "https://jitpack.io")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}

tasks.register("projectLint")
tasks.register("projectCodestyle")
tasks.register("projectTest")
