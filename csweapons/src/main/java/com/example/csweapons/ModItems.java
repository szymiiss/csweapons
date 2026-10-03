package com.example.csweapons;

import java.util.function.Function;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItems {
    public static final Item BULLET = register("bullet", Item::new, new Item.Settings());

    // --- Guns: GunStats(damage, cooldown, range, spread, magSize, reloadTicks, volume, pitch, loud, scoped) ---
    public static final Item GLOCK_18 = gun("glock_18", new GunStats(4.0f, 4, 48.0, 0.02f, 20, 40, 1.0f, 1.7f, false, false));
    public static final Item USP_S = gun("usp_s", new GunStats(4.5f, 5, 80.0, 0.01f, 12, 44, 0.35f, 1.9f, false, false));
    public static final Item DESERT_EAGLE = gun("desert_eagle", new GunStats(9.0f, 14, 96.0, 0.03f, 7, 56, 1.3f, 1.1f, false, false));
    public static final Item MP9 = gun("mp9", new GunStats(3.0f, 2, 40.0, 0.035f, 30, 50, 1.0f, 1.9f, false, false));
    public static final Item AK_47 = gun("ak_47", new GunStats(6.0f, 3, 100.0, 0.03f, 30, 50, 1.2f, 1.2f, false, false));
    public static final Item M4A4 = gun("m4a4", new GunStats(5.0f, 3, 100.0, 0.022f, 30, 60, 1.2f, 1.5f, false, false));
    public static final Item AWP = gun("awp", new GunStats(20.0f, 30, 200.0, 0.12f, 5, 70, 2.0f, 0.8f, true, true));

    // --- Knives (id, bonus damage, attack speed modifier) ---
    public static final Item COMBAT_KNIFE = knife("combat_knife", 2.0f, -1.0f);
    public static final Item KARAMBIT = knife("karambit", 2.0f, -0.7f);
    public static final Item BUTTERFLY_KNIFE = knife("butterfly_knife", 2.5f, -1.0f);
    public static final Item BAYONET = knife("bayonet", 3.0f, -1.4f);

    private static Item gun(String name, GunStats stats) {
        return register(name, s -> new GunItem(s, stats),
                new Item.Settings().maxCount(1).component(ModComponents.AMMO, stats.magSize()));
    }

    private static Item knife(String name, float dmg, float speed) {
        return register(name, Item::new, new Item.Settings().sword(ToolMaterial.IRON, dmg, speed));
    }

    private static Item register(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, Identifier.of(CSWeapons.MOD_ID, name));
        Item item = factory.apply(settings.registryKey(key));
        return Registry.register(Registries.ITEM, key, item);
    }

    public static void init() {
        ItemGroup group = FabricItemGroup.builder()
            .icon(() -> new ItemStack(AK_47))
            .displayName(Text.translatable("itemGroup.csweapons.main"))
            .entries((ctx, entries) -> {
                entries.add(BULLET);
                entries.add(GLOCK_18);
                entries.add(USP_S);
                entries.add(DESERT_EAGLE);
                entries.add(MP9);
                entries.add(AK_47);
                entries.add(M4A4);
                entries.add(AWP);
                entries.add(COMBAT_KNIFE);
                entries.add(KARAMBIT);
                entries.add(BUTTERFLY_KNIFE);
                entries.add(BAYONET);
            })
            .build();
        Registry.register(Registries.ITEM_GROUP, Identifier.of(CSWeapons.MOD_ID, "main"), group);

        // Also list everything in the vanilla Combat tab (and therefore the search tab).
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(entries -> {
            entries.add(BULLET);
            entries.add(GLOCK_18);
            entries.add(USP_S);
            entries.add(DESERT_EAGLE);
            entries.add(MP9);
            entries.add(AK_47);
            entries.add(M4A4);
            entries.add(AWP);
            entries.add(COMBAT_KNIFE);
            entries.add(KARAMBIT);
            entries.add(BUTTERFLY_KNIFE);
            entries.add(BAYONET);
        });
    }

    private ModItems() {}
}
