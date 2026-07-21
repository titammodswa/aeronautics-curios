package com.titammods.aeronautics_curios_compat.client;

import com.titammods.aeronautics_curios_compat.AeronauticsCuriosCompat;
import com.titammods.aeronautics_curios_compat.registry.ModKeyBindings;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

@EventBusSubscriber(modid = AeronauticsCuriosCompat.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AeronauticsCuriosCompatClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Item goggles = BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("aeronautics", "aviators_goggles"));
            if (goggles != null) {
                CuriosRendererRegistry.register(goggles, AviatorsGogglesCurioRenderer::new);
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        ModKeyBindings.register();
        event.register(ModKeyBindings.REMOTE_USE);
    }
}