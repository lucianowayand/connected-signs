package com.lucianowayand.biggersigns.client;

import com.lucianowayand.biggersigns.BiggerSignsMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;

@Mod.EventBusSubscriber(modid = BiggerSignsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityType.SIGN, JoinedSignRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityType.HANGING_SIGN, JoinedSignRenderer::new);
    }
}
