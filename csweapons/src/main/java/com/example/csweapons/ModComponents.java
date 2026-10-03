package com.example.csweapons;

import com.mojang.serialization.Codec;

import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public final class ModComponents {
    /** Rounds currently loaded in the gun's magazine. */
    public static final ComponentType<Integer> AMMO = Registry.register(
            Registries.DATA_COMPONENT_TYPE,
            Identifier.of(CSWeapons.MOD_ID, "ammo"),
            ComponentType.<Integer>builder().codec(Codec.INT).packetCodec(PacketCodecs.VAR_INT).build());

    public static void init() {}

    private ModComponents() {}
}
