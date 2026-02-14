package com.lucianowayand.connected_signs.mixin.tfc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

/**
 * TFC 1.20.1 (Forge) inherits vanilla StandingSignBlock.canSurvive(), which requires a solid block below,
 * preventing sign stacking. This adds an override to allow standing signs to sit on other standing signs.
 */
@Pseudo
@Mixin(targets = "net.dries007.tfc.common.blocks.wood.TFCStandingSignBlock")
public abstract class TFCStandingSignBlockMixin {
    @SuppressWarnings("deprecation")
    public boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        final BlockState below = level.getBlockState(pos.below());
        return below.isSolid() || below.getBlock() instanceof StandingSignBlock;
    }
}
