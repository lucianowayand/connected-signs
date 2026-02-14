package com.lucianowayand.connected_signs.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class SignConnectionHelper {

    /**
    * Gets all signs connected to the given position.
    *
    * Note: This checks all 6 directions (including vertical) so stacked signs are considered connected.
     */
    public static Set<BlockPos> getConnectedSigns(Level level, BlockPos centerPos) {
        Set<BlockPos> connected = new HashSet<>();
        Set<BlockPos> visited = new HashSet<>();
        Queue<BlockPos> toCheck = new LinkedList<>();

        BlockState centerState = level.getBlockState(centerPos);
        if (!(centerState.getBlock() instanceof SignBlock)) {
            return connected;
        }

        toCheck.add(centerPos);
        visited.add(centerPos);

        while (!toCheck.isEmpty()) {
            BlockPos current = toCheck.poll();

            // Check all 6 directions for adjacent signs
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);

                if (visited.contains(neighbor)) {
                    continue;
                }

                visited.add(neighbor);
                BlockState neighborState = level.getBlockState(neighbor);

                // Check if neighbor is a sign and has the same orientation
                if (neighborState.getBlock() instanceof SignBlock) {
                    if (areSignsCompatible(centerState, neighborState)) {
                        connected.add(neighbor);
                        toCheck.add(neighbor);
                    }
                }
            }
        }

        return connected;
    }

    /**
     * Checks if two sign states are compatible for connection
     * Signs connect if they are both wall signs facing the same direction,
     * or both standing signs (regardless of rotation or wood type)
     */
    private static boolean areSignsCompatible(BlockState state1, BlockState state2) {
        boolean isWall1 = state1.hasProperty(net.minecraft.world.level.block.WallSignBlock.FACING);
        boolean isWall2 = state2.hasProperty(net.minecraft.world.level.block.WallSignBlock.FACING);
        boolean isStanding1 = state1.hasProperty(net.minecraft.world.level.block.StandingSignBlock.ROTATION);
        boolean isStanding2 = state2.hasProperty(net.minecraft.world.level.block.StandingSignBlock.ROTATION);

        // Both must be the same type (both wall or both standing)
        if (isWall1 != isWall2) {
            return false;
        }

        // For wall signs, must have same facing
        if (isWall1 && isWall2) {
            return state1.getValue(net.minecraft.world.level.block.WallSignBlock.FACING)
                    .equals(state2.getValue(net.minecraft.world.level.block.WallSignBlock.FACING));
        }

        // For standing signs, all connect regardless of rotation or wood type
        if (isStanding1 && isStanding2) {
            return true;
        }

        return false;
    }

    /**
     * Calculates the bounding box of all connected signs
     */
    public static SignBounds calculateBounds(Level level, BlockPos centerPos, Set<BlockPos> connectedSigns) {
        int minX = centerPos.getX();
        int maxX = centerPos.getX();
        int minY = centerPos.getY();
        int maxY = centerPos.getY();
        int minZ = centerPos.getZ();
        int maxZ = centerPos.getZ();

        for (BlockPos pos : connectedSigns) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        return new SignBounds(minX, maxX, minY, maxY, minZ, maxZ);
    }

    public static class SignBounds {
        public final int minX, maxX, minY, maxY, minZ, maxZ;

        public SignBounds(int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
        }

        public int getWidth() {
            return maxX - minX + 1;
        }

        public int getHeight() {
            return maxY - minY + 1;
        }

        public int getDepth() {
            return maxZ - minZ + 1;
        }
    }
}
