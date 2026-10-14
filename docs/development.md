# Development

## Target and implementation

The patch targets `ChiseledBlockModelUpdateHandler.updateAllModelDataInChunk` in Chisels & Bits 20.1.20. Its iteration over the live `chunk.getBlockEntities().values()` collection can throw `ConcurrentModificationException` if the collection changes during callbacks.

A client-only Mixin redirects the single `Collection.forEach` call. It captures entries before running the original callbacks and skips removed or replaced block entities. Capture races defer the refresh for up to three client ticks. Pending retries are discarded when the world changes or the chunk unloads or is replaced.

Callback exceptions are not broadly swallowed. The injection requires exactly one matching call. The pseudo Mixin is inert when Chisels & Bits is absent; when present, compatibility is restricted to 20.1.20.

## Tests

Use Java 17:

```sh
./gradlew build
./gradlew auditTarget -PcbJar="path/to/chisels-and-bits-forge-20.1.20.jar"
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

The regression checks cover live-map invalidation, callback preservation, snapshot behavior, removed or replaced entries, capture deferral and recovery, null entries, and unrelated callback exceptions.

The target audit checks the matching injection site and the redirect's static method and operand-stack contract using ASM. These checks do not replace testing the Mixin in a running Minecraft client.

## Runtime verification

Visit an affected area and check the client log for:

```text
[CB Chunk Load Fix] Snapshot hook ACTIVE for Chisels & Bits chunk model refreshes.
```

This confirms the redirected code ran. The startup `Loaded` message alone confirms only initialization. Check that the previous chunk-refresh exception no longer occurs and that terrain renders correctly. Repeated retry warnings warrant further investigation.

No Chisels & Bits classes are bundled or redistributed. Removing the compatibility jar restores the original behavior.
