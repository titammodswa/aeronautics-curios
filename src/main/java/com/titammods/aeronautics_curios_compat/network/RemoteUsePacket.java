package com.titammods.aeronautics_curios_compat.network;

import com.simibubi.create.AllSoundEvents;
import com.titammods.aeronautics_curios_compat.AeronauticsCuriosCompat;
import com.titammods.aeronautics_curios_compat.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.titammods.aeronautics_curios_compat.util.CuriosUtils;
import com.titammods.aeronautics_curios_compat.util.DummyLevel;
import com.titammods.aeronautics_curios_compat.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import dev.simulated_team.simulated.data.SimLang;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record RemoteUsePacket() implements CustomPacketPayload {

    public static final Type<RemoteUsePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(AeronauticsCuriosCompat.MODID, "remote_use"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RemoteUsePacket> STREAM_CODEC = StreamCodec.of(
            (buf, packet) -> {},
            buf -> new RemoteUsePacket());

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(RemoteUsePacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            ItemStack stack = CuriosUtils.getEquippedLinkedTypewriter(player);
            if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof LinkedTypewriterItem)) return;

            LinkedTypewriterBlockEntity lbe =
                    LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(player.blockPosition(), player);

            if (lbe == null) {
                LinkedTypewriterBlockEntityUtils.VirtualLinkedTypewriterContext ctx2 =
                        LinkedTypewriterBlockEntityUtils.createVirtualLinkedTypewriterContext(stack, player.level(), player);
                if (ctx2 == null) return;
                lbe = ctx2.blockEntity();
            }

            DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player);
            if (dummyLevel != null) dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);

            boolean check = lbe.checkAndStartUsing(player.getUUID());
            if (!check) {
                lbe.disconnectUser();
            } else {
                if (dummyLevel != null) dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);

                dummyLevel.getLevel().playSound(null, player.blockPosition(), AllSoundEvents.CONTROLLER_PUT.getMainEvent(), SoundSource.BLOCKS, 1.0F, 0.95F + 0.1F * dummyLevel.getLevel().getRandom().nextFloat());

                final Component customName = lbe.components().getOrDefault(DataComponents.CUSTOM_NAME, SimLang.translate("linked_typewriter.title").component());
                player.displayClientMessage(SimLang.translate("linked_typewriter.start_controlling", customName.getString()).component(), true);
            }

            ((ILinkedTypewriterBlockEntityExtension) lbe).saveAdditional(stack);
        });
    }
}