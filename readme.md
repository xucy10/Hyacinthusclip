Rice ear
=========
A binary patch distribution system for Paper, with NMS reflection framework for Minecraft versions 1.8.8+.

Rice ear is the launcher for the Luminol Minecraft server and provides a comprehensive NMS (net.minecraft.server) 
reflection framework supporting Minecraft versions from 1.8.8 to the latest release. It uses a 
[bsdiff](http://www.daemonology.net/bsdiff/) patch between the vanilla Minecraft server and the modified Paper 
server to generate the Paper Minecraft server immediately upon first run. Once the Paper server is generated it 
loads the patched jar into Rice ear's own class loader, and runs the main class.

This avoids the legal problems of the GPL's linking clause.

The patching overhead is avoided if a valid patched jar is found in the cache directory.
It checks via sha256 so any modification to those jars (or updated launcher) will cause a repatch.

NMS Reflection Framework
------------------------

The `nms` module provides a complete reflection-based wrapper for Minecraft's NMS (net.minecraft.server) classes 
across all versions from 1.8.8 to the latest. This allows server core developers to write version-agnostic code 
without depending on specific Minecraft server implementations.

### Features
- **Version detection**: Automatically detects the running Minecraft server version
- **Reflection wrappers**: Type-safe access to NMS classes, methods, and fields
- **Cross-version support**: Supports Minecraft 1.8.8 through latest versions
- **Packet handling**: Wrappers for common packet types
- **Entity management**: Reflection-based entity and player access
- **World manipulation**: Access to world-level NMS operations

Building
--------

Building Rice ear creates a runnable jar, but the jar will not contain the Rice ear config file or patch data. 
This project consists simply of the launcher itself, the [paperweight Gradle plugin](https://github.com/PaperMC/paperweight) 
generates the patch and config file and inserts it into the jar provided by this project, creating a working runnable jar.
Mili Ecosystem Integration
--------------------------

Hyacinthusclip is the launcher and NMS/ASM toolkit of the Mili ecosystem
(Mili = a Folia/Luminol based high-performance server core).

### Gradle 导入与网络可达性（重要）

本项目的依赖仓库由根目录 `settings.gradle.kts` 统一管理，并按可达性优先级排序：
`mavenCentral` → `repo.menthamc.org`（Luminol 社区仓库，托管全部 `org.leavesmc` 工件）
→ `repo.leavesmc.org`（官方源，仅作最后回退）。

此前若在 IDE 中导入失败（表现为"项目未被识别为 Gradle 工程"），原因是
`repo.leavesmc.org` 在部分网络环境下连接超时，导致依赖解析失败、Gradle 同步中断。
现在 MenthaMC 镜像已作为首选回退，同步可正常完成。

如需使用自定义镜像，请在 `gradle.properties` 中设置：

```properties
hyacinthusclip.mavenMirror=https://your-mirror.example.com/maven-public/
```

### ASM 字节码工具

`nms` 模块新增 `moe.luminolmc.riceear.nms.asm` 包：

- `AsmClassLocator`：不加载类到 JVM 的前提下，从 jar / 目录 / 字节流中用 ASM 解析类元数据，
  支持对 paperweight 产出的 Mojang 映射服务端 jar 建立索引。
- `ClassMetadata`：不可变的类元数据快照（字段/方法/描述符/访问标志）。
- `AsmReflection`：当按名称查找 NMS 成员失败时，按类型回退解析 `Field`/`Method`，
  跨版本安全，全量缓存。

`nms` 模块发布双产物：原始 jar 与 `-shaded` jar（ASM 已重定位到
`riceear.nms.libs`，避免与宿主冲突）。

### 新增 NMS 包装类

- `NmsScheduler`：统一 Bukkit / Folia（Region、GlobalRegion、Async）调度器，
  Mili 核心可直接使用。
- `NmsBossBar`：跨版本 BossBar（`BossBattleServer`）反射包装。

### 与 Mili / paperweight 的联动

在 Mili fork（paperweight-patcher 工程）中使用本启动器：

1. 本地发布：`./gradlew publishToMavenLocal`
2. 在 Mili 构建脚本中将 `PAPERCLIP_CONFIG` 依赖指向本模块，随后运行
   paperweight 的 `createMojmapPaperclipJar` 即可产出内置 Hyacinthusclip 补丁
   数据的可执行启动 jar。
