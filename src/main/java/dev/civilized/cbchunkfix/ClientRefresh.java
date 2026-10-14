package dev.civilized.cbchunkfix;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = "cb_chunkload_fix", value = Dist.CLIENT)
public final class ClientRefresh {
    private static final Logger LOG = LogUtils.getLogger();
    private static final ArrayDeque<Retry> RETRIES = new ArrayDeque<>();
    private static boolean announced;
    private record Retry(LevelChunk chunk, Consumer<BlockEntity> update, int attempts) {}

    private ClientRefresh() {}

    public static void refresh(LevelChunk chunk, Consumer<BlockEntity> originalUpdate) {
        Minecraft client = Minecraft.getInstance();
        if (!client.isSameThread()) {
            client.execute(() -> refresh(chunk, originalUpdate));
            return;
        }
        if (!announced) {
            announced = true;
            LOG.info("[CB Chunk Load Fix] Snapshot hook ACTIVE for Chisels & Bits chunk model refreshes.");
        }
        if (client.level == null || chunk.getLevel() != client.level) return;
        if (!tryUpdate(chunk, originalUpdate)) {
            LOG.warn("[CB Chunk Load Fix] Chunk {} changed during snapshot capture; deferring its model refresh.", chunk.getPos());
            RETRIES.addLast(new Retry(chunk, originalUpdate, 0));
        }
    }

    private static boolean tryUpdate(LevelChunk chunk, Consumer<BlockEntity> update) {
        return SnapshotRefresh.tryUpdate(() -> chunk.getBlockEntities().values(),
                entity -> !entity.isRemoved()
                        && entity.getLevel() == chunk.getLevel()
                        && chunk.getBlockEntities().get(entity.getBlockPos()) == entity,
                update);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            RETRIES.clear();
            return;
        }
        // Only process entries present at this tick's start; retries never spin.
        int queued = RETRIES.size();
        for (int i = 0; i < queued; i++) {
            Retry retry = RETRIES.removeFirst();
            LevelChunk chunk = retry.chunk();
            if (chunk.getLevel() != client.level
                    || client.level.getChunkSource().getChunk(chunk.getPos().x, chunk.getPos().z,
                    ChunkStatus.FULL, false) != chunk) continue;
            if (tryUpdate(chunk, retry.update())) continue;
            if (retry.attempts() < 2) {
                RETRIES.addLast(new Retry(chunk, retry.update(), retry.attempts() + 1));
            } else {
                LOG.warn("[CB Chunk Load Fix] Chunk {} stayed unstable after three deferred attempts; skipped this model refresh without interrupting chunk reception.", chunk.getPos());
            }
        }
    }
}
