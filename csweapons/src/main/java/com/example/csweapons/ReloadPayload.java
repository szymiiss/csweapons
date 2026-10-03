package com.example.csweapons;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** Sent from client to server when the reload key is pressed. */
public record ReloadPayload() implements CustomPayload {
    public static final CustomPayload.Id<ReloadPayload> ID =
            new CustomPayload.Id<>(Identifier.of(CSWeapons.MOD_ID, "reload"));
    public static final PacketCodec<RegistryByteBuf, ReloadPayload> CODEC = PacketCodec.unit(new ReloadPayload());

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}
