# Chisels & Bits Chunk Load Fix 0.1.1

Client-side Forge 1.20.1 compatibility mod targeting **Chisels & Bits 20.1.20**. Install only on the affected player's client. The server does not need this mod. It requires no configuration, scripts or world changes.

## Failure addressed

The installed C&B handler `ChiseledBlockModelUpdateHandler.updateAllModelDataInChunk` calls `forEach` on the live `chunk.getBlockEntities().values()` view. A structural change to that HashMap during the callbacks invalidates iteration and throws `ConcurrentModificationException`. In the observed client log, this exception escaped the chunk-load event and interrupted processing of `ClientboundLevelChunkWithLightPacket`.

The original log alone does not identify every writer or prove whether the mutation was reentrant or off-thread. This mod fixes the unsafe iteration point; an in-game reconnect is still needed to confirm that fixing this exception resolves the visible missing terrain.

## Patch

- A client-only Mixin redirects the handler's single `Collection.forEach` call.
- All entries are captured into an array before any original C&B model callback runs.
- The original callback is preserved. Removed or replaced block entities are skipped.
- If capture itself encounters concurrent modification or a concurrent size-copy race, the refresh is deferred to client ticks, with at most three deferred attempts.
- Retries are discarded if the world changes or the chunk unloads/is replaced.
- Exceptions from the actual model callbacks are not broadly swallowed.
- The injection requires one matching target call. An incompatible target fails visibly rather than silently pretending to apply.
- With C&B absent, the pseudo Mixin is inert. If C&B is present, this release restricts compatibility to 20.1.20.

## Installation and live verification

1. Fully close Minecraft.
2. Put `cb-chunkload-fix-1.20.1-0.1.1.jar` in the client's `mods` folder.
3. Start Minecraft and reconnect to the affected area.
4. Check `logs/latest.log` for:

   `[CB Chunk Load Fix] Snapshot hook ACTIVE for Chisels & Bits chunk model refreshes.`

5. Check whether the previous `HashMap$Values.forEach` / `ChiseledBlockModelUpdateHandler` exception is absent, and whether the terrain now renders correctly.

The earlier `Loaded 0.1.1` line only confirms the mod initialized. **The `Snapshot hook ACTIVE` line confirms that the redirected C&B code actually ran.** Repeated snapshot retry warnings indicate an additional off-thread mutation issue that may need investigation.

Version 0.1.1 adds the missing Minecraft 1.20.1 resource-pack metadata (`pack.mcmeta`, format 15), correcting the Forge loading warning from 0.1.0. The snapshot patch itself is unchanged.

For rollback, close Minecraft and remove this compatibility jar. The original C&B jar and its data are unchanged.

## Checks performed

`regressionTest` reproduces the original live-map exception and checks callback preservation, snapshot behavior, removed/replaced entries, capture deferral/recovery, null entries, and visibility of unrelated callback exceptions. `auditTarget` reads the actual installed C&B jar, requires exactly one matching injection site and verifies the redirect's static method/operand-stack contract with ASM. It does not modify the target jar or claim to replace a full Minecraft Mixin launch test.

Build offline against the existing Forge cache:

```
gradlew.bat build --offline
gradlew.bat auditTarget --offline -PcbJar="path/to/chisels-and-bits-forge-20.1.20.jar"
```

The project uses Java 17 bytecode and Forge 47 APIs. No C&B classes are bundled, replaced or redistributed.
