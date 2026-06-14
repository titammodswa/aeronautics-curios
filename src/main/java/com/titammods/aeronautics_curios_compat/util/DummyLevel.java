package com.titammods.aeronautics_curios_compat.util;

import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class DummyLevel {
    private final Level realLevel;
    private boolean clientSide;
    private final Map<BlockPos, BlockEntity> virtualBlockEntities = new ConcurrentHashMap<>();
    private static final Map<BlockEntity, DummyLevel> ownerMap_be = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<UUID, DummyLevel> ownerMap_player = Collections.synchronizedMap(new WeakHashMap<>());

    public DummyLevel(Level realLevel) {
        this(realLevel, realLevel.isClientSide);
    }

    public DummyLevel(Level realLevel, boolean clientSide) {
        this.realLevel = realLevel;
        this.clientSide = clientSide;
    }

    public Level getLevel() { return realLevel; }
    public boolean isClientSide() { return clientSide; }

    public void setBlockEntity(BlockPos pos, BlockEntity blockEntity, Player player) {
        virtualBlockEntities.put(pos.immutable(), blockEntity);
        ownerMap_be.put(blockEntity, this);
        ownerMap_player.put(player.getUUID(), this);
    }

    public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
        return virtualBlockEntities.get(pos);
    }

    public void removeBlockEntity(BlockPos pos, Player player) {
        BlockEntity removed = virtualBlockEntities.remove(pos);
        if (removed != null) ownerMap_be.remove(removed);
        if (player != null) ownerMap_player.remove(player.getUUID());
    }

    public @Nullable LinkedTypewriterBlockEntity placeLinkedTypewriter(BlockPos pos, BlockEntity blockEntity, Player player) {
        if (!(blockEntity instanceof LinkedTypewriterBlockEntity lbe)) return null;
        lbe.setLevel(realLevel);
        return lbe;
    }

    public static @Nullable DummyLevel getDummyLevelFor(BlockEntity be) {
        return ownerMap_be.get(be);
    }

    public static @Nullable DummyLevel getDummyLevelFor(Player player) {
        return getDummyLevelFor(player, true);
    }

    public static @Nullable DummyLevel getDummyLevelFor(Player player, boolean sideUpdate) {
        if (player == null) return null;
        if (ownerMap_player.get(player.getUUID()) == null) return null;
        if (sideUpdate) ownerMap_player.get(player.getUUID()).clientSide = player.level().isClientSide;
        return ownerMap_player.get(player.getUUID());
    }
}