# Chisels & Bits Chunk Load Fix

A client-side compatibility mod for Minecraft 1.20.1, Forge 47, and Chisels & Bits 20.1.20.

Prevents unsafe block-entity iteration during Chisels & Bits chunk model refreshes. This exception can interrupt incoming chunk processing and leave terrain invisible. The patch targets this specific failure; other rendering problems may have different causes.

## Installation

Place the mod jar in your client's `mods` folder alongside Chisels & Bits, then restart Minecraft. No server installation or configuration is required.

## Building

Requires Java 17.

```sh
./gradlew build
```

On Windows, run `gradlew.bat build`. The built jar is in `build/libs`; regression checks run with the build.

See [development notes](docs/development.md) for implementation details and additional checks, or [the changelog](CHANGELOG.md) for version history.

## Issues

Report bugs through [GitHub Issues](https://github.com/NightQueal/cb-chunkload-fix/issues). Include the mod versions, reproduction steps, and relevant client log.

## License

[MIT](LICENSE). Unofficial compatibility patch; not affiliated with the Chisels & Bits team.
