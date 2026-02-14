package com.lucianowayand.connected_signs.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;

import com.lucianowayand.connected_signs.util.SignConnectionHelper;

public class SignEvents {
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        BlockPos start = event.getPos();
        BlockState startState = level.getBlockState(start);
        if (!(startState.getBlock() instanceof SignBlock)) return;

        Set<BlockPos> group = SignConnectionHelper.getConnectedSigns(level, start);
        group.add(start); // Include the starting sign itself
    }

    @SubscribeEvent
    public void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;
        if (level.isClientSide()) return;

        BlockPos placedPos = event.getPos();
        BlockState placedState = event.getPlacedBlock();

        // Only handle standing signs
        if (!(placedState.getBlock() instanceof StandingSignBlock)) return;

        // Get connected signs
        Set<BlockPos> connectedSigns = SignConnectionHelper.getConnectedSigns(level, placedPos);
        if (connectedSigns.isEmpty()) return;

        // Find the topmost sign
        BlockPos topSign = placedPos;
        for (BlockPos connected : connectedSigns) {
            if (connected.getY() > topSign.getY()) {
                topSign = connected;
            }
        }

        // Get the rotation of the topmost sign
        BlockState topState = level.getBlockState(topSign);
        if (!(topState.getBlock() instanceof StandingSignBlock)) return;

        int topRotation = topState.getValue(StandingSignBlock.ROTATION);

        // Align all signs below to match the top sign's rotation
        for (BlockPos connected : connectedSigns) {
            if (connected.getY() < topSign.getY()) {
                BlockState connectedState = level.getBlockState(connected);
                if (connectedState.getBlock() instanceof StandingSignBlock) {
                    BlockState newState = connectedState.setValue(StandingSignBlock.ROTATION, topRotation);
                    level.setBlock(connected, newState, 3);
                }
            }
        }

        // Also update the placed sign if it's not the topmost
        if (!placedPos.equals(topSign)) {
            BlockState newState = placedState.setValue(StandingSignBlock.ROTATION, topRotation);
            level.setBlock(placedPos, newState, 3);
        }
    }
}
