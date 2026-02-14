package com.lucianowayand.connected_signs.integration;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.item.SignItem;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;

/**
 * Isolated integration helpers for TerraFirmaCraft (TFC).
 * This class will perform any TFC-specific registration only when the TFC mod is present.
 */
public final class TfcIntegration {
    private static final Logger LOGGER = LogUtils.getLogger();

    private TfcIntegration() {}

    /**
     * Register TFC integration listeners on the provided mod event bus.
     * Call this from your mod constructor with the constructor-provided
     * `FMLJavaModLoadingContext.getModEventBus()` to avoid the deprecated
     * `FMLJavaModLoadingContext.get()` helper.
     */
    public static void register(final IEventBus modEventBus) {
        // Check for common TFC mod IDs; be permissive in matching
        boolean tfcPresent = ModList.get().isLoaded("tfc") || ModList.get().isLoaded("terrafirmacraft");

        if (!tfcPresent) {
            LOGGER.debug("TFC not detected; skipping TFC integration registration.");
            return;
        }

        LOGGER.info("TFC detected — registering TFC integration listeners.");

        // Register a common-setup listener so TFC-specific setup happens during the normal lifecycle.
        modEventBus.addListener(TfcIntegration::onCommonSetup);
    }

    private static void onCommonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Running TFC-specific common setup.");
        // Register a high-priority Forge event listener to allow placing standing signs on top of existing standing signs.
        // This runs early so it can override TFC placement restrictions in a development environment when the TFC mod is present.
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, TfcIntegration::onRightClickBlock);
    }

    private static void onRightClickBlock(final PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        // Only consider sign items
        if (!(event.getItemStack().getItem() instanceof SignItem)) return;

        Direction face = event.getFace();
        if (face != Direction.UP) return;

        Level level = (Level) event.getLevel();
        BlockPos clicked = event.getPos();

        // If the clicked block is a standing sign, allow placing another standing sign on top of it
        if (level.getBlockState(clicked).getBlock() instanceof StandingSignBlock) {
            // Ensure placement is permitted by forcing the event not to be canceled here
            if (event.isCanceled()) {
                event.setCanceled(false);
            }
            // Also allow using the item so vanilla placement proceeds
            event.setUseItem(net.minecraftforge.eventbus.api.Event.Result.ALLOW);
        }
    }
}
