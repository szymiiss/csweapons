package com.example.csweapons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class CSWeapons implements ModInitializer {
    public static final String MOD_ID = "csweapons";

    @Override
    public void onInitialize() {
        ModComponents.init();
        ModItems.init();

        PayloadTypeRegistry.playC2S().register(ReloadPayload.ID, ReloadPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ReloadPayload.ID,
                (payload, context) -> Reloader.start(context.player()));
        ServerTickEvents.END_SERVER_TICK.register(Reloader::tick);
    }
}
