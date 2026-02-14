package com.lucianowayand.connected_signs.mixin.client;

import com.lucianowayand.connected_signs.client.render.SignPanelRenderUtil;
import com.lucianowayand.connected_signs.util.SignConnectionHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

/**
 * Renders the connected_signs panel for TFC sign blocks regardless of what block-entity renderer TFC uses.
 *
 * This avoids version-specific injections into TFC's renderer method signatures.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class TFCSignPanelDispatcherMixin {

    private static final String TFC_NAMESPACE = "tfc";

    @Inject(
        method = "render(Lnet/minecraft/world/level/block/entity/BlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
        at = @At("HEAD")
    )
    private void connectedSigns$renderTfcPanel(final BlockEntity blockEntity,
                                              final float partialTick,
                                              final PoseStack poseStack,
                                              final MultiBufferSource bufferSource,
                                              final CallbackInfo ci) {

        final Level level = blockEntity.getLevel();
        if (level == null) return;

        final BlockPos pos = blockEntity.getBlockPos();
        final BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof SignBlock)) return;

        final ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId == null || !TFC_NAMESPACE.equals(blockId.getNamespace())) return;

        final Set<BlockPos> connectedSigns = SignConnectionHelper.getConnectedSigns(level, pos);
        if (connectedSigns.isEmpty()) return;
        connectedSigns.add(pos);

        final boolean hasSignBelow = connectedSigns.contains(pos.below());
        final boolean hasSignAbove = connectedSigns.contains(pos.above());
        if (!hasSignBelow && !hasSignAbove) return;

        final int verticalLevels = hasSignBelow ? 1 : 0;

        poseStack.pushPose();
        SignPanelRenderUtil.applySignTransform(poseStack, state);

        final WoodType woodType = ((SignBlock) state.getBlock()).type();
        final ResourceLocation texture = SignPanelRenderUtil.panelTextureForSign(state, woodType);

        final int packedLight = LevelRenderer.getLightColor(level, pos);
        final int packedOverlay = OverlayTexture.NO_OVERLAY;

        SignPanelRenderUtil.renderExtendedPanel(poseStack, state, bufferSource, packedLight, packedOverlay, texture, verticalLevels);
        poseStack.popPose();
    }
}
