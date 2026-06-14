package com.titammods.aeronautics_curios_compat.event.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.titammods.aeronautics_curios_compat.AeronauticsCuriosCompat;
import com.titammods.aeronautics_curios_compat.mixin_interface.ILinkedTypewriterBlockEntityExtension;
import com.titammods.aeronautics_curios_compat.network.RemoteUsePacket;
import com.titammods.aeronautics_curios_compat.registry.ModKeyBindings;
import com.titammods.aeronautics_curios_compat.util.CuriosUtils;
import com.titammods.aeronautics_curios_compat.util.DummyLevel;
import com.titammods.aeronautics_curios_compat.util.LinkedTypewriterBlockEntityUtils;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterBlockEntity;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterInteractionHandler;
import dev.simulated_team.simulated.content.blocks.redstone.linked_typewriter.LinkedTypewriterItem;
import dev.simulated_team.simulated.mixin_interface.PlayerTypewriterExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = AeronauticsCuriosCompat.MODID, value = Dist.CLIENT)
public class ClientKeyInputHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (ModKeyBindings.REMOTE_USE.consumeClick() && !isAnyOtherKeyDown()) {
            onRemoteUseKeyPressed();
        }
        LinkedTypewriterInteractionHandler.tick();
    }

    private static boolean isAnyOtherKeyDown() {
        Minecraft mc = Minecraft.getInstance();
        long window = mc.getWindow().getWindow();
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_LAST; key++) {
            if (key == ModKeyBindings.REMOTE_USE.getKey().getValue()) continue;
            if (InputConstants.isKeyDown(window, key)) return true;
        }
        return false;
    }

    private static void onRemoteUseKeyPressed() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.getConnection() == null) return;

        ItemStack stack = CuriosUtils.getEquippedLinkedTypewriter(player);
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof LinkedTypewriterItem)) return;

        LinkedTypewriterBlockEntity lbe =
                LinkedTypewriterBlockEntityUtils.getLinkedTypewriterBlockEntityAt(player.blockPosition(), player);

        if (lbe == null) {
            LinkedTypewriterBlockEntityUtils.VirtualLinkedTypewriterContext ctx =
                    LinkedTypewriterBlockEntityUtils.createVirtualLinkedTypewriterContext(stack, player.level(), player);
            if (ctx == null) return;
            lbe = ctx.blockEntity();
        }

        ((PlayerTypewriterExtension) player).simulated$setCurrentTypewriter(player.blockPosition());
        LinkedTypewriterInteractionHandler.associateTypewriter(lbe);

        DummyLevel dummyLevel = DummyLevel.getDummyLevelFor(player);
        if (dummyLevel != null) {
            dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
        }

        boolean check = lbe.checkAndStartUsing(player.getUUID());
        if (!check) {
            lbe.disconnectUser();
        } else {
            if (dummyLevel != null) {
                ((ILinkedTypewriterBlockEntityExtension) lbe).setClientCheck(true);
                dummyLevel.setBlockEntity(player.blockPosition(), lbe, player);
            }
        }

        PacketDistributor.sendToServer(new RemoteUsePacket());
    }
}