package com.lucianowayand.connected_signs.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.lucianowayand.connected_signs.ConnectedSignsMod;

import net.minecraft.world.level.block.entity.BlockEntityType;

@Mod.EventBusSubscriber(modid = ConnectedSignsMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BlockEntityType.SIGN, JoinedSignRenderer::new);
        event.registerBlockEntityRenderer(BlockEntityType.HANGING_SIGN, JoinedSignRenderer::new);
    }
}
