pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

// 统一管理依赖仓库，按可达性优先级排序：
// 1. mavenCentral / 中央仓库镜像 —— 基础库
// 2. maven.fabricmc.net —— net.fabricmc 工件（sponge-mixin、access-widener）
// 3. vendor/maven —— 仓库内置离线工件（org.leavesmc，MIT），防外部源不可达
// 4. repo.leavesmc.org —— 官方源，仅作最后回退
//
// 如需自定义镜像，可在 gradle.properties 中设置：
//   hyacinthusclip.mavenMirror=https://your-mirror.example.com/maven-public/
dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS
    repositories {
        val mirror = providers.gradleProperty("hyacinthusclip.mavenMirror")
        if (mirror.isPresent) {
            maven(mirror.get()) {
                name = "CustomMirror"
                content {
                    includeGroupByRegex(".*")
                }
            }
        }
        mavenCentral()
        maven("https://maven.fabricmc.net/") {
            name = "FabricMC"
            content {
                includeGroup("net.fabricmc")
            }
        }
        // 仓库内置的 vendor 工件（MIT，见 vendor/README.md）：
        // repo.leavesmc.org 在部分网络环境不可达时的离线兜底
        maven(rootDir.toURI().resolve("vendor/maven")) {
            name = "Vendor"
            content {
                includeGroup("org.leavesmc")
            }
        }
        maven("https://maven-central.storage-download.googleapis.com/maven2") {
            name = "CentralMirror"
            content {
                includeGroupByRegex("org\\.ow2.*")
                includeGroupByRegex("org\\.apache.*")
                includeGroup("com.google.code.gson")
                includeGroup("org.jetbrains")
                includeGroup("io.sigpipe")
                includeGroup("net.fabricmc")
                includeGroup("io.github.llamalad7")
            }
        }
        maven("https://repo.leavesmc.org/releases/") {
            name = "LeavesReleases"
            content {
                includeGroup("org.leavesmc")
            }
        }
        maven("https://repo.leavesmc.org/snapshots/") {
            name = "LeavesSnapshots"
            content {
                includeGroup("org.leavesmc")
            }
        }
    }
}

rootProject.name = "riceear"
include("java6", "java25", "nms")
