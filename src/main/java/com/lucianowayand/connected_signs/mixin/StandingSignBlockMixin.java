package com.lucianowayand.connected_signs.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StandingSignBlock.class)
public abstract class StandingSignBlockMixin {

    @Inject(method = "canSurvive", at = @At("HEAD"), cancellable = true)
    private void connectedSigns$allowStandingSignStackingForTfc(final BlockState state,
                                                               final LevelReader level,
                                                               final BlockPos pos,
                                                               final CallbackInfoReturnable<Boolean> cir) {

        final ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId == null) return;

        final String namespace = blockId.getNamespace();
        if (!"tfc".equals(namespace) && !"afc".equals(namespace)) return;

        final BlockState below = level.getBlockState(pos.below());
        if (below.getBlock() instanceof StandingSignBlock) {
            cir.setReturnValue(true);
        }
    }
}
