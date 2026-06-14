package com.titammods.aeronautics_curios_compat.util;

import com.titammods.aeronautics_curios_compat.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

public class LinkedTypewriterBlockEntityUtils {

    public record VirtualLinkedTypewriterContext(DummyLevel dummyLevel, LinkedTypewriterBlockEntity blockEntity, BlockPos position) {}

    public static VirtualLinkedTypewriterContext createVirtualLinkedTypewriterContext(ItemStack stack, Level level, Player player) {
        return createVirtualLinkedTypewriterContextAt(stack, level, player, player.blockPosition());
    }

    public static VirtualLinkedTypewriterContext createVirtualLinkedTypewriterContextAt(ItemStack stack, Level level, Player player, BlockPos dummyPos) {
        if (level == null) return null;
        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player) != null ? DummyLevel.getDummyLevelFor(player) : new DummyLevel(level);
        if (dummyLevel == null) return null;
        if (dummyLevel.getBlockEntity(dummyPos) instanceof LinkedTypewriterBlockEntity placed) {
            return new VirtualLinkedTypewriterContext(dummyLevel, placed, dummyPos);
        }

        CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null || !(stack.getItem() instanceof BlockItem blockItem)) return null;

        Block block = blockItem.getBlock();
        if (!(block instanceof EntityBlock entityBlock)) return null;

        BlockEntity be = entityBlock.newBlockEntity(dummyPos, block.defaultBlockState());
        if (!(be instanceof LinkedTypewriterBlockEntity lbe)) return null;

        LinkedTypewriterBlockEntity placed = dummyLevel.placeLinkedTypewriter(dummyPos, lbe, player);
        if (placed == null) return null;
        placed.loadWithComponents(blockEntityData.copyTag(), level.registryAccess());
        ((ILinkedTypewriterBlockEntityExtension) placed).loadAdditional(stack, level, dummyPos);
        dummyLevel.setBlockEntity(dummyPos, placed, player);
        return new VirtualLinkedTypewriterContext(dummyLevel, placed, dummyPos);
    }

    public static LinkedTypewriterBlockEntity getLinkedTypewriterBlockEntityAt(BlockPos pos, Player player) {
        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player);
        LinkedTypewriterBlockEntity lbe = getLinkedTypewriterBlockEntityAt(pos, dummyLevel);
        if (lbe == null) return null;
        lbe.setLevel(player.level());
        return lbe;
    }

    public static LinkedTypewriterBlockEntity getLinkedTypewriterBlockEntityAt(BlockPos pos, DummyLevel dummyLevel) {
        return (dummyLevel != null && dummyLevel.getBlockEntity(pos) instanceof LinkedTypewriterBlockEntity lbe) ? lbe : null;
    }
}