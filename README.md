# Player Radar Packet Test — Minecraft 1.21.4

Source project for Fabric; **not a compiled/tested JAR**. Java 21 is required. Sodium can be installed alongside it.

- Press **8** to open the GUI.
- The GUI button or **P** starts/stops a custom-payload test.
- Rate is deliberately capped at **10000 packets per second** and restricted to an integrated singleplayer world.
- The mod must be installed on the server side too so the payload type is registered.

Build: install JDK 21 and Gradle, then run `gradle build` in this folder. The JAR should appear in `build/libs/`. This environment could not compile/test it, so compilation may require API/mapping adjustments.
