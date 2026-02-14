package com.lucianowayand.connected_signs.client;

import com.lucianowayand.connected_signs.client.render.SignPanelRenderUtil;
import com.lucianowayand.connected_signs.util.SignConnectionHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Set;

public class JoinedSignRenderer extends SignRenderer {

    public JoinedSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SignBlockEntity signEntity, float partialTick, PoseStack poseStack,
                      MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        Level level = signEntity.getLevel();
        if (level == null) {
            super.render(signEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }

        BlockPos pos = signEntity.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof SignBlock)) {
            super.render(signEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }

        // Get connected signs
        Set<BlockPos> connectedSigns = SignConnectionHelper.getConnectedSigns(level, pos);

        if (connectedSigns.isEmpty()) {
            // No connections, render normally
            super.render(signEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
            return;
        }

        connectedSigns.add(pos);

        final boolean hasSignBelow = connectedSigns.contains(pos.below());
        final boolean hasSignAbove = connectedSigns.contains(pos.above());
        if (hasSignBelow || hasSignAbove) {
            poseStack.pushPose();

            SignPanelRenderUtil.applySignTransform(poseStack, state);

            // If there is a sign below, extend just enough to cover the gap between signs.
            // Otherwise render only this sign's panel.
            final int verticalLevels = hasSignBelow ? 1 : 0;
            renderExtendedPanel(poseStack, bufferSource, packedLight, packedOverlay,
                               signEntity, state, verticalLevels);

            poseStack.popPose();
        }

        // Render text and sign normally on top of the panel
        super.render(signEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderExtendedPanel(PoseStack poseStack, MultiBufferSource bufferSource,
                                    int packedLight, int packedOverlay,
                                    SignBlockEntity signEntity, BlockState state,
                                    int verticalLevels) {

        // Get wood texture
        WoodType woodType = ((SignBlock) state.getBlock()).type();
        ResourceLocation texture = SignPanelRenderUtil.panelTextureForSign(state, woodType);

        // Use cutout for proper lighting
        SignPanelRenderUtil.renderExtendedPanel(poseStack, state, bufferSource, packedLight, packedOverlay, texture, verticalLevels);
    }
}
