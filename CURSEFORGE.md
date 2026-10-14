# CurseForge upload

**Name:** Chisels & Bits Chunk Load Fix

**Summary:** Fixes a Chisels & Bits chunk-loading exception that can leave terrain invisible.

**Category:** Bug Fixes

**Minecraft:** 1.20.1

**Mod loader:** Forge

**Environment:** Client only

**License:** MIT

**Required dependency:** Chisels & Bits - For Forge (20.1.20)

**File:** cb-chunkload-fix-1.20.1-0.1.1.jar

**Display name:** Chisels & Bits Chunk Load Fix 0.1.1 — Forge 1.20.1

**Release type:** Beta

**Logo:** icon.png

**Source:** https://github.com/NightQueal/cb-chunkload-fix

**Issues:** https://github.com/NightQueal/cb-chunkload-fix/issues

## Description

A small client-side fix for **Chisels & Bits 20.1.20 on Forge 1.20.1**.

Prevents a `ConcurrentModificationException` in Chisels & Bits' chunk-load model refresh, which can interrupt incoming chunk processing and leave terrain invisible.

- Preserves normal chiseled-block model updates.
- No configuration or server installation required.
- Install alongside Chisels & Bits, then restart Minecraft.

Targets this specific exception; it is not a general fix for every missing-chunk issue. Unofficial compatibility patch, not affiliated with the Chisels & Bits team.

## Changelog

- Snapshot-based chunk model refresh, with bounded retries for unstable captures.
- Skips removed or replaced block entities.
- Includes the resource-pack metadata required by Forge.
