package com.titammods.aeronautics_curios_compat.network;

import com.titammods.aeronautics_curios_compat.AeronauticsCuriosCompat;
import com.titammods.aeronautics_curios_compat.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.titammods.aeronautics_curios_compat.util.CuriosUtils;
import com.titammods.aeronautics_curios_compat.util.DummyLevel;
import com.titammods.aeronautics_curios_compat.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RemoteTypewriterKeyInteractionPacket(
        BlockPos interactionPos,
        int key,
        int scanCode,
        int action
) implements CustomPacketPayload {

    public static final Type<RemoteTypewriterKeyInteractionPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AeronauticsCuriosCompat.MODID, "key_interaction"));

    public static final StreamCodec<ByteBuf, RemoteTypewriterKeyInteractionPacket> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, RemoteTypewriterKeyInteractionPacket::interactionPos,
                    ByteBufCodecs.INT,     RemoteTypewriterKeyInteractionPacket::key,
                    ByteBufCodecs.INT,     RemoteTypewriterKeyInteractionPacket::scanCode,
                    ByteBufCodecs.INT,     RemoteTypewriterKeyInteractionPacket::action,
                    RemoteTypewriterKeyInteractionPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoteTypewriterKeyInteractionPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer serverPlayer)) return;

        Player player = serverPlayer;
        ItemStack linkedTypewriterStack = CuriosUtils.getEquippedLinkedTypewriter(player);

        if (linkedTypewriterStack == null || !(linkedTypewriterStack.getItem() instanceof LinkedTypewriterItem))
            return;

        LinkedTypewriterBlockEntity lbe =
                LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(player.blockPosition(), player);

        CustomData blockEntityData = linkedTypewriterStack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (lbe != null && blockEntityData != null) {
            lbe.onKeyInteraction(player.getUUID(), null, packet.key, packet.action == 1);

            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                server.execute(() -> {
                    DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player);
                    if (dummyLevel != null) {
                        dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
                    }
                    lbe.getTypewriterEntries().updateNetworks(player.level());
                    ((ILinkedTypewriterBlockEntityExtension) lbe).saveAdditional(linkedTypewriterStack);
                });
            }
        }
    }
}