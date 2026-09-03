import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.zuozhi.ideaannotation"
version = "1.3.9"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        val localIdeaHome = providers.environmentVariable("IDEA_HOME").orNull
        if (localIdeaHome == null) {
            intellijIdea("2026.2.0.1")
        } else {
            local(localIdeaHome)
        }
        testFramework(TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

intellijPlatform {
    pluginConfiguration {
        changeNotes = providers.fileContents(layout.projectDirectory.file("RELEASE_NOTES.md")).asText
        ideaVersion {
            sinceBuild = "262"
            untilBuild = provider { null }
        }
    }

    signing {
        certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN")
        privateKey = providers.environmentVariable("PRIVATE_KEY")
        password = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
    }

    publishing {
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }
}
