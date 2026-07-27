import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.kotlin.gradle.dsl.JvmDefaultMode
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.pika.idea"
version = "0.2.0"
val pluginVersion = version.toString()

val defaultIdeaPath = "/Applications/IntelliJ IDEA.app"
val ideaPath = providers.gradleProperty("ideaPath").orElse(defaultIdeaPath)
val targetIdeaVersion = providers.gradleProperty("targetIdeaVersion").orNull
val minimumSupportedIdeaVersion = "2024.2"
val maximumSupportedIdeaVersion = "2026.2"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        if (targetIdeaVersion == null) {
            local(ideaPath)
        } else {
            intellijIdea(targetIdeaVersion)
        }
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.11.4")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_21
        jvmDefault = JvmDefaultMode.NO_COMPATIBILITY
    }
}

intellijPlatform {
    buildSearchableOptions = false
    instrumentCode = true
    projectName = "Pika-IDEA-Control"

    pluginConfiguration {
        name = "Pika Control"
        version = pluginVersion

        ideaVersion {
            sinceBuild = "242"
            untilBuild = "262.*"
        }
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    pluginVerification {
        ides {
            if (targetIdeaVersion == null) {
                local(ideaPath)
            } else {
                create(IntelliJPlatformType.IntellijIdeaUltimate, minimumSupportedIdeaVersion)
            }
            create(IntelliJPlatformType.IntellijIdeaUltimate, maximumSupportedIdeaVersion)
        }
    }
}

tasks {
    processResources {
        from("LICENSE") {
            into("META-INF")
            rename { "LICENSE.txt" }
        }
    }

    test {
        useJUnitPlatform()
        systemProperty("pika.idea.port", "0")
    }
}
