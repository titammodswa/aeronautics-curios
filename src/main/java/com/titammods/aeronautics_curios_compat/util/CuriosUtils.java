package com.titammods.aeronautics_curios_compat.util;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.CuriosApi;

public class CuriosUtils {

    @Nullable
    public static ItemStack getEquippedLinkedTypewriter(Player player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler("linked_typewriter"))
                .map(stacks -> stacks.getStacks().getStackInSlot(0))
                .filter(stack -> !stack.isEmpty())
                .orElse(ItemStack.EMPTY);
    }
}