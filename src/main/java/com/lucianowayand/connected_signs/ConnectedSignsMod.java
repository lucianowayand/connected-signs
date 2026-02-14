package com.lucianowayand.connected_signs;

import com.lucianowayand.connected_signs.events.SignEvents;
import com.lucianowayand.connected_signs.integration.TfcIntegration;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(ConnectedSignsMod.MODID)
public class ConnectedSignsMod
{
    public static final String MODID = "connected_signs";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ConnectedSignsMod(FMLJavaModLoadingContext context)
    {
        context.getModEventBus().addListener(this::commonSetup);
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(new SignEvents());

        // Pass the mod event bus into the integration layer so it can register listeners
        // without using the deprecated FMLJavaModLoadingContext.get() helper.
        TfcIntegration.register(context.getModEventBus());
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Connected Signs: common setup complete");
    }
}
