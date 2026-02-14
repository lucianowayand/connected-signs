package com.lucianowayand.connected_signs.mixin.tfc.client;

import com.lucianowayand.connected_signs.client.render.SignPanelRenderUtil;
import com.lucianowayand.connected_signs.util.SignConnectionHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Injects connected_signs extended-panel rendering into TFC's sign renderer,
 * so TFC signs behave like vanilla signs under connected_signs.
 */
@Pseudo
@Mixin(targets = "net.dries007.tfc.client.render.blockentity.TFCSignBlockEntityRenderer")
public abstract class TFCSignBlockEntityRendererMixin {

    @Inject(
        method = "render(Lnet/minecraft/world/level/block/entity/SignBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At("HEAD")
    )
    private void connected_signs$renderExtendedPanel(final SignBlockEntity signEntity,
                                                final float partialTick,
                                                final PoseStack poseStack,
                                                final MultiBufferSource bufferSource,
                                                final int packedLight,
                                                final int packedOverlay,
                                                final CallbackInfo ci) {

        final Level level = signEntity.getLevel();
        if (level == null) return;

        final BlockPos pos = signEntity.getBlockPos();
        final BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof SignBlock)) return;

        final Set<BlockPos> connectedSigns = SignConnectionHelper.getConnectedSigns(level, pos);
        if (connectedSigns.isEmpty()) return;

        connectedSigns.add(pos);

        final boolean hasSignBelow = connectedSigns.contains(pos.below());
        final boolean hasSignAbove = connectedSigns.contains(pos.above());
        if (!hasSignBelow && !hasSignAbove) return;

        // Extend only into the gap to the sign below.
        final int verticalLevels = hasSignBelow ? 1 : 0;

        poseStack.pushPose();
        SignPanelRenderUtil.applySignTransform(poseStack, state);

        final WoodType woodType = ((SignBlock) state.getBlock()).type();
        final ResourceLocation texture = SignPanelRenderUtil.planksTextureForWoodType(woodType);

        SignPanelRenderUtil.renderExtendedPanel(poseStack, state, bufferSource, packedLight, packedOverlay, texture, verticalLevels);

        poseStack.popPose();
    }
}
