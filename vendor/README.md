# Vendor Maven Artifacts

本目录存放构建所需的第三方工件的离线副本，避免因外部 Maven 仓库不可达
（如 `repo.leavesmc.org` 连接超时）导致 Gradle 同步/构建失败。

| 工件 | 来源 | 许可证 |
|------|------|--------|
| `org/leavesmc/leaves-plugin-mixin-condition/1.0.0` | [LeavesMC/leaves-plugin-mixin-condition](https://github.com/LeavesMC/leaves-plugin-mixin-condition)（v1.0.0 官方源码构建产物） | MIT |

`settings.gradle.kts` 已将该目录注册为 `org.leavesmc` 组的兜底 Maven 仓库；
若官方仓库可达，Gradle 会优先使用 `mavenCentral` / `repo.leavesmc.org` 等在线源。

如需更新工件版本：克隆上游仓库，执行 `./gradlew publishToMavenLocal`，
再把 `~/.m2/repository/org/leavesmc/...` 下的 jar/pom/module 复制到本目录对应坐标。
