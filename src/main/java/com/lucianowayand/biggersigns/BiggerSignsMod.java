package com.lucianowayand.biggersigns;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import com.lucianowayand.biggersigns.events.SignEvents;

@Mod(BiggerSignsMod.MODID)
public class BiggerSignsMod
{
    public static final String MODID = "bigger_signs";
    private static final Logger LOGGER = LogUtils.getLogger();

    public BiggerSignsMod(FMLJavaModLoadingContext context)
    {
        context.getModEventBus().addListener(this::commonSetup);
        context.registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, Config.SPEC);
        MinecraftForge.EVENT_BUS.register(new SignEvents());
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        LOGGER.info("Bigger Signs: common setup complete");
    }
}
