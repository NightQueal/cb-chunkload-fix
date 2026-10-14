package dev.civilized.cbchunkfix;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;

@Mod("cb_chunkload_fix")
public final class ChunkLoadFix {
    public ChunkLoadFix() {
        LogUtils.getLogger().info("[CB Chunk Load Fix] Loaded 0.1.1; the client hook activates when Chisels & Bits refreshes a chunk.");
    }
}
