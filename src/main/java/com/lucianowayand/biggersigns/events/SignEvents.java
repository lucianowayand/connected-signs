package com.lucianowayand.biggersigns.events;

import com.lucianowayand.biggersigns.util.SignConnectionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Set;

public class SignEvents {
    private static final int MAX_GROUP_SIZE = 256;

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = (Level) event.getLevel();
        if (level.isClientSide()) return;

        BlockPos start = event.getPos();
        BlockState startState = level.getBlockState(start);
        if (!(startState.getBlock() instanceof SignBlock)) return;

        Player player = event.getEntity();

        Set<BlockPos> group = SignConnectionHelper.getConnectedSigns(level, start);
        group.add(start); // Include the starting sign itself

        SignConnectionHelper.SignBounds bounds = SignConnectionHelper.calculateBounds(level, start, group);

        player.sendSystemMessage(Component.literal(
                "Connected sign group size=" + group.size() +
                " bounds=[(" + bounds.minX + "," + bounds.minY + "," + bounds.minZ +
                ") -> (" + bounds.maxX + "," + bounds.maxY + "," + bounds.maxZ + ")]"
        ));
    }
}
