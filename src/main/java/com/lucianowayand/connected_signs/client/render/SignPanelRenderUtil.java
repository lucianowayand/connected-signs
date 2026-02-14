package com.lucianowayand.connected_signs.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Matrix4f;

public final class SignPanelRenderUtil {

    private static final String AFC_NAMESPACE = "afc";
    private static final float HEIGHT_EPSILON = 0.001f;
    private static final float THICKNESS_EPSILON = 2/24 + 0.001f;
    // Higher = less "zoom" (more tiling). Lower = larger planks (more zoom).
    private static final float UV_SCALE = 1.6f;

    private SignPanelRenderUtil() {
    }

    public static void applySignTransform(final PoseStack poseStack, final BlockState state) {
        if (state.hasProperty(WallSignBlock.FACING)) {
            final float rotation = -state.getValue(WallSignBlock.FACING).toYRot();
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0D, -0.3125D, -0.4375D);
        } else if (state.hasProperty(StandingSignBlock.ROTATION)) {
            final float rotation = -((state.getValue(StandingSignBlock.ROTATION) * 360) / 16.0f);
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0D, -0.3125D, 0D);
        } else {
            // Fallback for modded signs that don't expose the vanilla FACING/ROTATION properties.
            // Render in a sensible default orientation.
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.translate(0D, -0.3125D, 0D);
        }
    }

    /**
     * Returns a planks texture that matches the wood type for the *panel* material.
     *
     * Vanilla uses: minecraft:textures/block/<wood>_planks.png
     * TFC uses:     tfc:textures/block/wood/planks/<wood>.png
     * AFC uses:     afc:textures/block/wood/planks/<wood>.png (but note that AFC's WoodType strings may be namespaced as tfc)
     */
    public static ResourceLocation planksTextureForWoodType(final WoodType woodType) {
        final String woodName = woodType.name();

        final ResourceLocation woodId = woodName.contains(":")
            ? ResourceLocation.tryParse(woodName)
            : ResourceLocation.withDefaultNamespace(woodName);

        if (woodId == null) {
            return ResourceLocation.withDefaultNamespace("textures/block/" + woodName + "_planks.png");
        }

        if (woodId.getNamespace().equals("tfc")) {
            return parse("tfc:textures/block/wood/planks/" + woodId.getPath() + ".png");
        }

        if (woodId.getNamespace().equals("afc")) {
            return parse("afc:textures/block/wood/planks/" + woodId.getPath() + ".png");
        }

        if (woodId.getNamespace().equals("minecraft")) {
            return parse("minecraft:textures/block/" + woodId.getPath() + "_planks.png");
        }

        return parse(woodId.getNamespace() + ":textures/block/" + woodId.getPath() + "_planks.png");
    }

    /**
     * AFC renders planks textures in its own namespace, but its WoodType strings are commonly "tfc:<wood>".
     * This forces the panel to use AFC's planks textures.
     */
    public static ResourceLocation afcPlanksTextureForWoodType(final WoodType woodType) {
        final String woodName = woodType.name();
        final ResourceLocation woodId = ResourceLocation.tryParse(woodName);
        final String woodPath = woodId != null ? woodId.getPath() : woodName;
        return parse("afc:textures/block/wood/planks/" + woodPath + ".png");
    }

    /**
     * Returns the correct panel texture for the given sign state + wood type.
     *
     * AFC signs often report their WoodType as "tfc:<wood>", but the textures live under the "afc" namespace.
     */
    public static ResourceLocation panelTextureForSign(final BlockState state, final WoodType woodType) {
        final ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());
        if (blockId != null && AFC_NAMESPACE.equals(blockId.getNamespace())) {
            return afcPlanksTextureForWoodType(woodType);
        }
        return planksTextureForWoodType(woodType);
    }

    private static ResourceLocation parse(final String id) {
        final ResourceLocation parsed = ResourceLocation.tryParse(id);
        return parsed != null ? parsed : ResourceLocation.withDefaultNamespace("textures/block/oak_planks.png");
    }

    public static void renderExtendedPanel(final PoseStack poseStack,
                                           final BlockState state,
                                           final MultiBufferSource bufferSource,
                                           final int packedLight,
                                           final int packedOverlay,
                                           final ResourceLocation texture,
                                           final int verticalLevels) {

        final VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(texture));
        final Matrix4f matrix = poseStack.last().pose();

        if (state.hasProperty(WallSignBlock.FACING)) {
            renderExtendedWallPanel(matrix, consumer, packedLight, packedOverlay, verticalLevels);
            return;
        }

        // Default to standing-style panel for anything else.
        renderExtendedStandingPanel(matrix, consumer, packedLight, packedOverlay, verticalLevels);
    }

    private static void vertex(final VertexConsumer consumer,
                               final Matrix4f matrix,
                               final float x, final float y, final float z,
                               final float u, final float v,
                               final int packedOverlay,
                               final int packedLight,
                               final float nx, final float ny, final float nz) {

        consumer.vertex(matrix, x, y, z)
            .color(255, 255, 255, 255)
            .uv(u, v)
            .overlayCoords(packedOverlay)
            .uv2(packedLight)
            .normal(nx, ny, nz)
            .endVertex();
    }

    private static void quad(final VertexConsumer consumer,
                             final Matrix4f matrix,
                             final float x1, final float y1, final float z1, final float u1, final float v1,
                             final float x2, final float y2, final float z2, final float u2, final float v2,
                             final float x3, final float y3, final float z3, final float u3, final float v3,
                             final float x4, final float y4, final float z4, final float u4, final float v4,
                             final int packedOverlay,
                             final int packedLight,
                             final float nx, final float ny, final float nz) {

        vertex(consumer, matrix, x1, y1, z1, u1, v1, packedOverlay, packedLight, nx, ny, nz);
        vertex(consumer, matrix, x2, y2, z2, u2, v2, packedOverlay, packedLight, nx, ny, nz);
        vertex(consumer, matrix, x3, y3, z3, u3, v3, packedOverlay, packedLight, nx, ny, nz);
        vertex(consumer, matrix, x4, y4, z4, u4, v4, packedOverlay, packedLight, nx, ny, nz);
    }

    public static void renderExtendedWallPanel(final Matrix4f matrix,
                                               final VertexConsumer consumer,
                                               final int packedLight,
                                               final int packedOverlay,
                                               final int verticalLevels) {

        final float extend = 0.001f;
        final float width = 1.0f + extend * 2;
        final float baseHeight = 0.5f;
        final float height = baseHeight * (verticalLevels + 1);
        final float thickness = 2 / 24f;

        final float faceWidth = width;
        final float faceHeight = height;

        final float x1 = -width / 2;
        final float x2 = width / 2;

        final float y2Base = baseHeight + 2 / 24f;
        final float y1 = verticalLevels == 0 ? (y2Base - baseHeight) : (y2Base - height);
        final float y2 = y2Base + HEIGHT_EPSILON;

        final float zOffset = 1 / 24f + 0.001f;
        final float zBack = zOffset - thickness;
        final float zFront = zOffset;

        final float uMin = 0.0f;
        final float uMax = faceWidth * UV_SCALE;
        final float vMin = 0.0f;
        final float vMax = faceHeight * UV_SCALE;

        // Front (+Z)
        quad(consumer, matrix,
            x1, y1, zFront, uMin, vMax,
            x2, y1, zFront, uMax, vMax,
            x2, y2, zFront, uMax, vMin,
            x1, y2, zFront, uMin, vMin,
            packedOverlay, packedLight,
            0, 0, 1);

        // Back (-Z)
        quad(consumer, matrix,
            x2, y1, zBack, uMin, vMax,
            x1, y1, zBack, uMax, vMax,
            x1, y2, zBack, uMax, vMin,
            x2, y2, zBack, uMin, vMin,
            packedOverlay, packedLight,
            0, 0, -1);

        final float sideUMax = thickness * UV_SCALE;
        final float sideVMax = faceHeight * UV_SCALE;
        final float topVMax = thickness * UV_SCALE;

        // Left (-X)
        quad(consumer, matrix,
            x1, y1, zFront, 0, sideVMax,
            x1, y2, zFront, 0, 0,
            x1, y2, zBack, sideUMax, 0,
            x1, y1, zBack, sideUMax, sideVMax,
            packedOverlay, packedLight,
            -1, 0, 0);

        // Right (+X)
        quad(consumer, matrix,
            x2, y1, zBack, 0, sideVMax,
            x2, y2, zBack, 0, 0,
            x2, y2, zFront, sideUMax, 0,
            x2, y1, zFront, sideUMax, sideVMax,
            packedOverlay, packedLight,
            1, 0, 0);

        // Top (+Y)
        quad(consumer, matrix,
            x1, y2, zBack, 0, 0,
            x2, y2, zBack, faceWidth * UV_SCALE, 0,
            x2, y2, zFront, faceWidth * UV_SCALE, topVMax,
            x1, y2, zFront, 0, topVMax,
            packedOverlay, packedLight,
            0, 1, 0);

        // Bottom (-Y)
        quad(consumer, matrix,
            x1, y1, zFront, 0, 0,
            x2, y1, zFront, faceWidth * UV_SCALE, 0,
            x2, y1, zBack, faceWidth * UV_SCALE, topVMax,
            x1, y1, zBack, 0, topVMax,
            packedOverlay, packedLight,
            0, -1, 0);
    }

    public static void renderExtendedStandingPanel(final Matrix4f matrix,
                                                   final VertexConsumer consumer,
                                                   final int packedLight,
                                                   final int packedOverlay,
                                                   final int verticalLevels) {

        final float extend = 0.001f;
        final float width = 1.0f + extend * 2;
        final float depth = 2 / 24f;
        final float baseHeight = 0.5f;
        final float height = baseHeight * (verticalLevels + 1);

        final float faceWidth = width;
        final float faceHeight = height;

        final float x1 = -width / 2;
        final float x2 = width / 2;
        final float zPad = THICKNESS_EPSILON * 2;
        final float z1 = -depth / 2 - zPad;
        final float z2 = depth / 2 + zPad;

        final float y2Base = baseHeight + 19 / 48f;
        final float y1 = verticalLevels == 0 ? (y2Base - baseHeight) : (y2Base - height);
        final float y2 = y2Base + HEIGHT_EPSILON;

        final float uMin = 0.0f;
        final float uMax = faceWidth * UV_SCALE;
        final float vMin = 0.0f;
        final float vMax = faceHeight * UV_SCALE;

        final float sideUMax = depth * UV_SCALE;
        final float sideVMax = vMax;
        final float topVMax = depth * UV_SCALE;

        // Front (+Z)
        quad(consumer, matrix,
            x1, y1, z2, uMin, vMax,
            x2, y1, z2, uMax, vMax,
            x2, y2, z2, uMax, vMin,
            x1, y2, z2, uMin, vMin,
            packedOverlay, packedLight,
            0, 0, 1);

        // Back (-Z)
        quad(consumer, matrix,
            x2, y1, z1, uMin, vMax,
            x1, y1, z1, uMax, vMax,
            x1, y2, z1, uMax, vMin,
            x2, y2, z1, uMin, vMin,
            packedOverlay, packedLight,
            0, 0, -1);

        // Left (-X)
        quad(consumer, matrix,
            x1, y1, z1, 0, sideVMax,
            x1, y2, z1, 0, 0,
            x1, y2, z2, sideUMax, 0,
            x1, y1, z2, sideUMax, sideVMax,
            packedOverlay, packedLight,
            -1, 0, 0);

        // Right (+X)
        quad(consumer, matrix,
            x2, y1, z2, 0, sideVMax,
            x2, y2, z2, 0, 0,
            x2, y2, z1, sideUMax, 0,
            x2, y1, z1, sideUMax, sideVMax,
            packedOverlay, packedLight,
            1, 0, 0);

        // Top (+Y)
        quad(consumer, matrix,
            x1, y2, z1, 0, 0,
            x2, y2, z1, faceWidth * UV_SCALE, 0,
            x2, y2, z2, faceWidth * UV_SCALE, topVMax,
            x1, y2, z2, 0, topVMax,
            packedOverlay, packedLight,
            0, 1, 0);

        // Bottom (-Y)
        quad(consumer, matrix,
            x1, y1, z2, 0, 0,
            x2, y1, z2, faceWidth * UV_SCALE, 0,
            x2, y1, z1, faceWidth * UV_SCALE, topVMax,
            x1, y1, z1, 0, topVMax,
            packedOverlay, packedLight,
            0, -1, 0);
    }
}
