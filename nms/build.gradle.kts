plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.4.1"
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

// 仓库由根项目 settings.gradle.kts 统一管理（dependencyResolutionManagement）

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.0")
    api("org.ow2.asm:asm:9.7.1")
    api("org.ow2.asm:asm-tree:9.7.1")
    api("org.ow2.asm:asm-commons:9.7.1")
}

// 生成 shaded jar：将 ASM 嵌入并重定位，避免与宿主环境冲突
tasks.shadowJar {
    archiveClassifier.set("shaded")
    val prefix = "riceear.nms.libs"
    listOf("org.objectweb", "org.objectweb.asm").forEach { pack ->
        relocate(pack, "$prefix.$pack")
    }
}

// 发布双产物：原始 jar（供已有 ASM 环境使用）+ shaded jar（开箱即用）
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
                description.set("NMS Reflection Framework & ASM bytecode toolkit for Minecraft 1.8.8 - Latest")
            }
        }

        register<MavenPublication>("mavenShaded") {
            groupId = project.group.toString()
            artifactId = "riceear-nms"
            version = project.version.toString()
            artifact(tasks.shadowJar) {
                classifier = "shaded"
            }
            pom {
                name.set("Riceear NMS (shaded)")
                description.set("NMS Reflection Framework with relocated ASM bundled")
            }
        }
    }
}
