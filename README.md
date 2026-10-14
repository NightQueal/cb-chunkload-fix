# Chisels & Bits Chunk Load Fix

A small **client-only Forge 1.20.1** compatibility patch for **Chisels & Bits 20.1.20**.

Fixes unsafe iteration of a chunk's block-entity collection during C&B model refreshes. The resulting `ConcurrentModificationException` can interrupt incoming chunk processing and leave terrain invisible.

Install the compatibility jar in your client's `mods` folder alongside Chisels & Bits, then restart Minecraft. No server installation or configuration is needed. This targets one specific exception, not every missing-chunk issue.

## Build

Use Java 17 and run `gradlew build` (`gradlew.bat build` on Windows). The jar is produced in `build/libs`. Regression checks run as part of the build.

See [DEVELOPMENT.md](DEVELOPMENT.md) for implementation and live verification details, and [CURSEFORGE.md](CURSEFORGE.md) for the upload listing.

MIT licensed. Unofficial patch; not affiliated with the Chisels & Bits team.
