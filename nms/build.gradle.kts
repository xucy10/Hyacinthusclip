plugins {
    java
    `maven-publish`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(21)
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.0")
}

publishing {
    publications {
        register<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "riceear-nms"
            version = project.version.toString()

            from(components["java"])
            withoutBuildIdentifier()

            pom {
                name.set("Riceear NMS")
                description.set("NMS Reflection Framework for Minecraft 1.8.8 - Latest")
            }
        }
    }
}