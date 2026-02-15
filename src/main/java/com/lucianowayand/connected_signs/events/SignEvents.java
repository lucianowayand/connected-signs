package com.lucianowayand.connected_signs.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

import com.lucianowayand.connected_signs.util.SignConnectionHelper;

public class SignEvents {
    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        final ItemStack stack = event.getItemStack();
        final String itemName = stack.isEmpty() ? "" : stack.getHoverName().getString().toLowerCase();
        final boolean isShearsNamed = !itemName.isEmpty() && itemName.contains("shears");
        if (!isShearsNamed) return;

        BlockPos start = event.getPos();
        BlockState startState = level.getBlockState(start);
        if (!(startState.getBlock() instanceof SignBlock)) return;

        final Player player = event.getEntity();
        final boolean wasDisconnected = SignConnectionHelper.isSignDisconnected(level, start);
        final boolean nowDisconnected = !wasDisconnected;
        SignConnectionHelper.setSignDisconnected(level, start, nowDisconnected);

        if (nowDisconnected) {
            // If this is a standing sign and it isn't the bottom sign in a vertical stack,
            // break it (standing sign stacking is only possible for some mods, but this rule applies whenever it happens).
            if (startState.getBlock() instanceof StandingSignBlock) {
                final BlockState belowState = level.getBlockState(start.below());
                if (belowState.getBlock() instanceof StandingSignBlock) {
                    level.destroyBlock(start, true, player);
                }
            }

            player.displayClientMessage(Component.literal("Sign disconnected"), true);
        } else {
            player.displayClientMessage(Component.literal("Sign connected"), true);
        }

        // Consume the interaction so vanilla sign editing / other interactions don't fire.
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setUseBlock(Event.Result.DENY);
        event.setUseItem(Event.Result.DENY);
        return;
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
