package dev.civilized.cbchunkfix.mixin;

import dev.civilized.cbchunkfix.ClientRefresh;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.Collection;
import java.util.function.Consumer;

@Pseudo
@Mixin(targets = "mod.chiselsandbits.client.logic.ChiseledBlockModelUpdateHandler", remap = false)
public abstract class ChiseledBlockModelUpdateHandlerMixin {
    @Redirect(method = "updateAllModelDataInChunk", remap = false, require = 1,
            at = @At(value = "INVOKE", target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V", remap = false))
    private static void cbChunkLoadFix$snapshot(Collection<BlockEntity> liveValues,
                                                Consumer<BlockEntity> originalUpdate,
                                                LevelChunk chunk) {
        ClientRefresh.refresh(chunk, originalUpdate);
    }
}
