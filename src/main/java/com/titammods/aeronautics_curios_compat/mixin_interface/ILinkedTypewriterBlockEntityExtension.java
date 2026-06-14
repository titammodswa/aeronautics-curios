package com.titammods.aeronautics_curios_compat.mixin_interface;

import com.titammods.aeronautics_curios_compat.util.DummyLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ILinkedTypewriterBlockEntityExtension {
    void saveAdditional(ItemStack stack);
    void loadAdditional(ItemStack stack, Level level, BlockPos pos);
    boolean isRunOnServer(DummyLevel dummyLevel);
    void setClientCheck(boolean clientCheck);
}