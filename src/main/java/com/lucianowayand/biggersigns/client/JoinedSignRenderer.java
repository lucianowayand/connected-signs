package com.lucianowayand.biggersigns.client;

import com.lucianowayand.biggersigns.util.SignConnectionHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Matrix4f;

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

        // Calculate bounds of the entire connected group
        SignConnectionHelper.SignBounds bounds = SignConnectionHelper.calculateBounds(level, pos, connectedSigns);

        // Check if there's a sign directly below or above this one
        BlockPos posBelow = pos.below();
        BlockPos posAbove = pos.above();
        boolean hasSignBelow = connectedSigns.contains(posBelow);
        boolean hasSignAbove = connectedSigns.contains(posAbove);

        // Render panel FIRST (so text appears on top)
        // Render if part of a vertical stack (has sign above or below)
        if (hasSignBelow || hasSignAbove) {
            poseStack.pushPose();

            // Apply sign transformations (rotation, positioning)
            applySignTransform(poseStack, state);

            // Render extended wood panel
            // If has sign below, extend down; otherwise just render contained panel
            renderExtendedPanel(poseStack, bufferSource, packedLight, packedOverlay,
                               signEntity, state, hasSignBelow ? 1 : 0);

            poseStack.popPose();
        }

        // Render text and sign normally on top of the panel
        super.render(signEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void applySignTransform(PoseStack poseStack, BlockState state) {
        if (state.getBlock() instanceof WallSignBlock) {
            // Wall sign transformations
            float rotation = -state.getValue(WallSignBlock.FACING).toYRot();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0D, -0.3125D, -0.4375D);
        } else if (state.getBlock() instanceof StandingSignBlock) {
            // Standing sign transformations
            float rotation = -((state.getValue(StandingSignBlock.ROTATION) * 360) / 16.0f);
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0D, -0.3125D, 0D);
        }
    }

    private void renderExtendedPanel(PoseStack poseStack, MultiBufferSource bufferSource,
                                    int packedLight, int packedOverlay,
                                    SignBlockEntity signEntity, BlockState state,
                                    int verticalLevels) {

        // Get wood texture
        WoodType woodType = ((SignBlock) state.getBlock()).type();
        ResourceLocation texture = getWoodTexture(woodType);

        // Use cutout for proper lighting
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        Matrix4f matrix = poseStack.last().pose();

        if (state.getBlock() instanceof WallSignBlock) {
            renderExtendedWallPanel(matrix, consumer, packedLight, packedOverlay, verticalLevels);
        } else {
            renderExtendedStandingPanel(matrix, consumer, packedLight, packedOverlay, verticalLevels);
        }
    }

    private void renderExtendedWallPanel(Matrix4f matrix, VertexConsumer consumer,
                                        int packedLight, int packedOverlay, int verticalLevels) {
        // Panel dimensions - make it taller based on vertical levels (Y distance)
        // Sign is 8 pixels tall, block is 16 pixels, so each level is 0.5 units
        float extend = 0.001f; // Tiny extension to prevent z-fighting
        float width = 1.0f + extend * 2; // Extend on both sides
        float baseHeight = 0.5f; // 8 pixels / 16 pixels = 0.5 block height per sign
        float height = baseHeight * (verticalLevels + 1); // Height based on Y levels
        float thickness = 2/24f; // 4 voxels thick

        float x1 = -width / 2;
        float x2 = width / 2;

        // Position panel - for bottom sign (verticalLevels=0), stay contained; otherwise extend down
        float y2 = baseHeight + 2/24f;  // Top of panel
        float y1 = verticalLevels == 0 ? (y2 - baseHeight) : (y2 - height);  // Bottom: contained or extended

        // Position panel slightly forward
        float zOffset = 1/24f + 0.001f;
        float zBack = zOffset - thickness;
        float zFront = zOffset;

        // UV coordinates - repeat texture every 8 pixels (0.5 blocks = 1 sign height)
        float uMin = 0.0f;
        float uMax = 1.0f;
        float vMin = 0.0f;
        // For bottom sign, use 1.0 to fit texture in its own space; otherwise repeat based on height
        float vMax = verticalLevels == 0 ? 1.0f : (height / baseHeight);

        // Front face (towards player)
        consumer.vertex(matrix, x1, y1, zFront).color(255, 255, 255, 255)
            .uv(uMin, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, x2, y1, zFront).color(255, 255, 255, 255)
            .uv(uMax, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, x2, y2, zFront).color(255, 255, 255, 255)
            .uv(uMax, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, x1, y2, zFront).color(255, 255, 255, 255)
            .uv(uMin, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();

        // Back face
        consumer.vertex(matrix, x2, y1, zBack).color(255, 255, 255, 255)
            .uv(uMin, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, -1).endVertex();
        consumer.vertex(matrix, x1, y1, zBack).color(255, 255, 255, 255)
            .uv(uMax, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, -1).endVertex();
        consumer.vertex(matrix, x1, y2, zBack).color(255, 255, 255, 255)
            .uv(uMax, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, -1).endVertex();
        consumer.vertex(matrix, x2, y2, zBack).color(255, 255, 255, 255)
            .uv(uMin, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, -1).endVertex();

        // Calculate UV for sides - use actual pixel dimensions to avoid stretching
        // Side thickness is 2 pixels (thickness = 2/24 ≈ 0.083 blocks)
        float sideUMax = thickness; // Map to actual thickness in texture space
        float sideVMax = verticalLevels == 0 ? 1.0f : (height / baseHeight);

        // Left side (viewed from left)
        consumer.vertex(matrix, x1, y1, zFront).color(255, 255, 255, 255)
            .uv(0, sideVMax).overlayCoords(packedOverlay).uv2(packedLight).normal(-1, 0, 0).endVertex();
        consumer.vertex(matrix, x1, y2, zFront).color(255, 255, 255, 255)
            .uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(-1, 0, 0).endVertex();
        consumer.vertex(matrix, x1, y2, zBack).color(255, 255, 255, 255)
            .uv(sideUMax, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(-1, 0, 0).endVertex();
        consumer.vertex(matrix, x1, y1, zBack).color(255, 255, 255, 255)
            .uv(sideUMax, sideVMax).overlayCoords(packedOverlay).uv2(packedLight).normal(-1, 0, 0).endVertex();

        // Right side (viewed from right)
        consumer.vertex(matrix, x2, y1, zBack).color(255, 255, 255, 255)
            .uv(0, sideVMax).overlayCoords(packedOverlay).uv2(packedLight).normal(1, 0, 0).endVertex();
        consumer.vertex(matrix, x2, y2, zBack).color(255, 255, 255, 255)
            .uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(1, 0, 0).endVertex();
        consumer.vertex(matrix, x2, y2, zFront).color(255, 255, 255, 255)
            .uv(sideUMax, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(1, 0, 0).endVertex();
        consumer.vertex(matrix, x2, y1, zFront).color(255, 255, 255, 255)
            .uv(sideUMax, sideVMax).overlayCoords(packedOverlay).uv2(packedLight).normal(1, 0, 0).endVertex();

        // Top side (viewed from above)
        consumer.vertex(matrix, x1, y2, zBack).color(255, 255, 255, 255)
            .uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x2, y2, zBack).color(255, 255, 255, 255)
            .uv(1, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x2, y2, zFront).color(255, 255, 255, 255)
            .uv(1, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x1, y2, zFront).color(255, 255, 255, 255)
            .uv(0, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();

        // Bottom side (viewed from below)
        consumer.vertex(matrix, x1, y1, zFront).color(255, 255, 255, 255)
            .uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, -1, 0).endVertex();
        consumer.vertex(matrix, x2, y1, zFront).color(255, 255, 255, 255)
            .uv(1, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, -1, 0).endVertex();
        consumer.vertex(matrix, x2, y1, zBack).color(255, 255, 255, 255)
            .uv(1, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, -1, 0).endVertex();
        consumer.vertex(matrix, x1, y1, zBack).color(255, 255, 255, 255)
            .uv(0, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, -1, 0).endVertex();
    }

    private void renderExtendedStandingPanel(Matrix4f matrix, VertexConsumer consumer,
                                            int packedLight, int packedOverlay, int verticalLevels) {
        // Panel dimensions - same logic as wall signs
        // Sign is 8 pixels tall, block is 16 pixels, so each level is 0.5 units
        float extend = 0.001f;
        float width = 1.0f + extend * 2;
        float depth = 2/24f; // Standing sign depth
        float baseHeight = 0.5f; // 8 pixels / 16 pixels = 0.5 block height per sign
        float height = baseHeight * (verticalLevels + 1);

        float x1 = -width / 2;
        float x2 = width / 2;
        float z1 = -depth / 2 - 0.001f;  // Add offset to prevent z-fighting
        float z2 = depth / 2 + 0.001f;   // Add offset to prevent z-fighting

        // Position panel - adjust for standing sign coordinate system
        // Standing signs have a -0.3125 Y offset in their transform, so use lower y2
        float y2 = baseHeight + 19/48f;  // Top of panel
        float y1 = verticalLevels == 0 ? (y2 - baseHeight) : (y2 - height);  // Bottom: contained or extended

        // UV coordinates - repeat texture every 8 pixels (0.5 blocks = 1 sign height)
        float uMin = 0.0f;
        float uMax = 1.0f;
        float vMin = 0.0f;
        float vMax = verticalLevels == 0 ? 1.0f : (height / baseHeight);

        // Calculate UV for sides - use actual pixel dimensions to avoid stretching
        float sideUMax = depth; // Map to actual depth in texture space
        float sideVMax = vMax;

        // Front face (towards +Z)
        consumer.vertex(matrix, x1, y1, z2).color(255, 255, 255, 255)
            .uv(uMin, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, x2, y1, z2).color(255, 255, 255, 255)
            .uv(uMax, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(255, 255, 255, 255)
            .uv(uMax, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();
        consumer.vertex(matrix, x1, y2, z2).color(255, 255, 255, 255)
            .uv(uMin, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, 1).endVertex();

        // Back face (towards -Z)
        consumer.vertex(matrix, x2, y1, z1).color(255, 255, 255, 255)
            .uv(uMin, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, -1).endVertex();
        consumer.vertex(matrix, x1, y1, z1).color(255, 255, 255, 255)
            .uv(uMax, vMax).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, -1).endVertex();
        consumer.vertex(matrix, x1, y2, z1).color(255, 255, 255, 255)
            .uv(uMax, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, -1).endVertex();
        consumer.vertex(matrix, x2, y2, z1).color(255, 255, 255, 255)
            .uv(uMin, vMin).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 0, -1).endVertex();

        // Left side (towards -X)
        consumer.vertex(matrix, x1, y1, z1).color(255, 255, 255, 255)
            .uv(0, sideVMax).overlayCoords(packedOverlay).uv2(packedLight).normal(-1, 0, 0).endVertex();
        consumer.vertex(matrix, x1, y2, z1).color(255, 255, 255, 255)
            .uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(-1, 0, 0).endVertex();
        consumer.vertex(matrix, x1, y2, z2).color(255, 255, 255, 255)
            .uv(sideUMax, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(-1, 0, 0).endVertex();
        consumer.vertex(matrix, x1, y1, z2).color(255, 255, 255, 255)
            .uv(sideUMax, sideVMax).overlayCoords(packedOverlay).uv2(packedLight).normal(-1, 0, 0).endVertex();

        // Right side (towards +X)
        consumer.vertex(matrix, x2, y1, z2).color(255, 255, 255, 255)
            .uv(0, sideVMax).overlayCoords(packedOverlay).uv2(packedLight).normal(1, 0, 0).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(255, 255, 255, 255)
            .uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(1, 0, 0).endVertex();
        consumer.vertex(matrix, x2, y2, z1).color(255, 255, 255, 255)
            .uv(sideUMax, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(1, 0, 0).endVertex();
        consumer.vertex(matrix, x2, y1, z1).color(255, 255, 255, 255)
            .uv(sideUMax, sideVMax).overlayCoords(packedOverlay).uv2(packedLight).normal(1, 0, 0).endVertex();

        // Top side (viewed from above)
        consumer.vertex(matrix, x1, y2, z1).color(255, 255, 255, 255)
            .uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x2, y2, z1).color(255, 255, 255, 255)
            .uv(1, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x2, y2, z2).color(255, 255, 255, 255)
            .uv(1, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();
        consumer.vertex(matrix, x1, y2, z2).color(255, 255, 255, 255)
            .uv(0, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, 1, 0).endVertex();

        // Bottom side (viewed from below)
        consumer.vertex(matrix, x1, y1, z2).color(255, 255, 255, 255)
            .uv(0, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, -1, 0).endVertex();
        consumer.vertex(matrix, x2, y1, z2).color(255, 255, 255, 255)
            .uv(1, 0).overlayCoords(packedOverlay).uv2(packedLight).normal(0, -1, 0).endVertex();
        consumer.vertex(matrix, x2, y1, z1).color(255, 255, 255, 255)
            .uv(1, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, -1, 0).endVertex();
        consumer.vertex(matrix, x1, y1, z1).color(255, 255, 255, 255)
            .uv(0, 1).overlayCoords(packedOverlay).uv2(packedLight).normal(0, -1, 0).endVertex();
    }

    private ResourceLocation getWoodTexture(WoodType woodType) {
        // Get the planks texture for the wood type
        String woodName = woodType.name();
        return ResourceLocation.withDefaultNamespace("textures/block/" + woodName + "_planks.png");
    }
}
