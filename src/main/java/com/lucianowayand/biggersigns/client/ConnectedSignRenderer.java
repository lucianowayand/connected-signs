package com.lucianowayand.biggersigns.client;

import com.lucianowayand.biggersigns.util.SignConnectionHelper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Matrix4f;

import java.util.Set;

public class ConnectedSignRenderer extends SignRenderer {

    public ConnectedSignRenderer(BlockEntityRendererProvider.Context context) {
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

        // Render the sign with extended background for connected signs
        poseStack.pushPose();

        // Calculate bounds
        SignConnectionHelper.SignBounds bounds = SignConnectionHelper.calculateBounds(level, pos, connectedSigns);

        // Render custom background
        renderConnectedBackground(poseStack, bufferSource, packedLight, packedOverlay, bounds, signEntity);

        poseStack.popPose();

        // Render text normally
        super.render(signEntity, partialTick, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderConnectedBackground(PoseStack poseStack, MultiBufferSource bufferSource,
                                          int packedLight, int packedOverlay,
                                          SignConnectionHelper.SignBounds bounds,
                                          SignBlockEntity signEntity) {

        BlockState state = signEntity.getLevel().getBlockState(signEntity.getBlockPos());

        // Get the relative position of this sign in the connected group
        BlockPos signPos = signEntity.getBlockPos();
        float relX = (signPos.getX() - bounds.minX);
        float relY = (signPos.getY() - bounds.minY);
        float relZ = (signPos.getZ() - bounds.minZ);

        // Calculate the total size of the connected sign group
        float width = bounds.getWidth();
        float height = bounds.getHeight();
        float depth = bounds.getDepth();

        // Render a visual indicator (colored overlay) showing the connection
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.debugLineStrip(2.0));
        Matrix4f matrix = poseStack.last().pose();

        // Draw lines from this sign to the bounds of the connected group
        // This creates a visible frame around the entire connected sign structure

        if (state.getBlock() instanceof WallSignBlock) {
            // For wall signs, draw outline on the wall
            float x1 = -relX;
            float x2 = x1 + width;
            float y1 = -relY;
            float y2 = y1 + height;
            float z = 0.0F;

            // Draw rectangle outline
            consumer.vertex(matrix, x1, y1, z).color(255, 215, 0, 255).endVertex();
            consumer.vertex(matrix, x2, y1, z).color(255, 215, 0, 255).endVertex();
            consumer.vertex(matrix, x2, y2, z).color(255, 215, 0, 255).endVertex();
            consumer.vertex(matrix, x1, y2, z).color(255, 215, 0, 255).endVertex();
            consumer.vertex(matrix, x1, y1, z).color(255, 215, 0, 255).endVertex();
        } else if (state.getBlock() instanceof StandingSignBlock) {
            // For standing signs, draw outline on the ground
            float x1 = -relX;
            float x2 = x1 + width;
            float z1 = -relZ;
            float z2 = z1 + depth;
            float y = 0.0F;

            // Draw rectangle outline
            consumer.vertex(matrix, x1, y, z1).color(255, 215, 0, 255).endVertex();
            consumer.vertex(matrix, x2, y, z1).color(255, 215, 0, 255).endVertex();
            consumer.vertex(matrix, x2, y, z2).color(255, 215, 0, 255).endVertex();
            consumer.vertex(matrix, x1, y, z2).color(255, 215, 0, 255).endVertex();
            consumer.vertex(matrix, x1, y, z1).color(255, 215, 0, 255).endVertex();
        }
    }
}
