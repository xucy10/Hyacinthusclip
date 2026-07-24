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