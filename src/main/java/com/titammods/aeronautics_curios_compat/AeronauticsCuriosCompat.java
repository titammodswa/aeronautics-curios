package com.titammods.aeronautics_curios_compat;

import com.titammods.aeronautics_curios_compat.network.ModNetworking;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.event.CurioChangeEvent;

import java.lang.reflect.Method;
import java.util.function.Predicate;

@Mod(AeronauticsCuriosCompat.MODID)
public class AeronauticsCuriosCompat {

    public static final String MODID = "aeronautics_curios_compat";

    public AeronauticsCuriosCompat(IEventBus modEventBus) {
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onRegisterPayloads);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            try {
                Class<?> gogglesClass = Class.forName("com.simibubi.create.content.equipment.goggles.GogglesItem");
                Method addPredicateMethod = gogglesClass.getMethod("addIsWearingPredicate", Predicate.class);
                Predicate<Player> pred = player -> {
                    Item goggles = BuiltInRegistries.ITEM.get(
                            ResourceLocation.fromNamespaceAndPath("aeronautics", "aviators_goggles"));
                    if (goggles == null) return false;
                    return CuriosApi.getCuriosInventory(player)
                            .map(inv -> inv.findFirstCurio(goggles).isPresent())
                            .orElse(false);
                };
                addPredicateMethod.invoke(null, pred);
            } catch (Exception e) {
                System.out.println("[Aeronautics Curios Compat] Não foi possível injetar predicate de goggles: " + e.getMessage());
            }
        });
    }

    private void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
        ModNetworking.register(event);
    }

    @EventBusSubscriber(modid = MODID)
    public static class GameEvents {

        @SubscribeEvent
        public static void onCurioEquip(CurioChangeEvent event) {
            ItemStack to = event.getTo();
            ItemStack from = event.getFrom();
            Item goggles = BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath("aeronautics", "aviators_goggles"));
            if (goggles == null) return;
            if (!to.is(goggles) || from.is(goggles)) return;
            LivingEntity entity = event.getEntity();
            if (!entity.level().isClientSide()) {
                entity.level().playSound(null, entity.blockPosition(),
                        SoundEvents.ARMOR_EQUIP_LEATHER.value(),
                        SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
    }
}