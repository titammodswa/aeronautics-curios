package com.titammods.aeronautics_curios_compat.network;

import com.titammods.aeronautics_curios_compat.AeronauticsCuriosCompat;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar reg = event.registrar(AeronauticsCuriosCompat.MODID);
        reg.playToServer(RemoteUsePacket.TYPE, RemoteUsePacket.STREAM_CODEC, RemoteUsePacket::handle);
        reg.playToServer(RemoteTypewriterKeyInteractionPacket.TYPE, RemoteTypewriterKeyInteractionPacket.STREAM_CODEC, RemoteTypewriterKeyInteractionPacket::handle);
    }
}