package com.example.csweapons;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

/** Server-side timed reloads. Bullets are only taken from the inventory when the reload finishes. */
public final class Reloader {
    private static final Map<UUID, State> ACTIVE = new HashMap<>();

    private static final class State {
        final ItemStack stack;
        final int total;
        int left;

        State(ItemStack stack, int total) {
            this.stack = stack;
            this.total = total;
            this.left = total;
        }
    }

    public static boolean isReloading(PlayerEntity p) {
        return ACTIVE.containsKey(p.getUuid());
    }

    public static void start(ServerPlayerEntity p) {
        ItemStack stack = p.getMainHandStack();
        if (!(stack.getItem() instanceof GunItem gun) || isReloading(p)) return;

        if (GunItem.getAmmo(stack) >= gun.stats().magSize()) {
            p.sendMessage(Text.literal("Magazine full"), true);
            return;
        }
        if (!p.isCreative() && countBullets(p) == 0) {
            p.sendMessage(Text.literal("No bullets left"), true);
            p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                    SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.PLAYERS, 1.0f, 1.4f);
            return;
        }
        ACTIVE.put(p.getUuid(), new State(stack, gun.stats().reloadTicks()));
        p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.BLOCK_IRON_TRAPDOOR_OPEN, SoundCategory.PLAYERS, 0.8f, 1.6f);
    }

    public static void tick(MinecraftServer server) {
        Iterator<Map.Entry<UUID, State>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, State> e = it.next();
            State s = e.getValue();
            ServerPlayerEntity p = server.getPlayerManager().getPlayer(e.getKey());

            if (p == null || !p.isAlive() || p.getMainHandStack() != s.stack) {
                if (p != null) p.sendMessage(Text.literal("Reload cancelled"), true);
                it.remove();
                continue;
            }
            s.left--;
            if (s.left <= 0) {
                finish(p, s.stack);
                it.remove();
            } else if (s.left % 2 == 0) {
                int filled = Math.round(10f * (s.total - s.left) / s.total);
                p.sendMessage(Text.literal("Reloading [" + "|".repeat(filled) + ".".repeat(10 - filled) + "]"), true);
            }
        }
    }

    private static void finish(ServerPlayerEntity p, ItemStack stack) {
        GunItem gun = (GunItem) stack.getItem();
        int need = gun.stats().magSize() - GunItem.getAmmo(stack);
        int taken = p.isCreative() ? need : takeBullets(p, need);
        GunItem.setAmmo(stack, GunItem.getAmmo(stack) + taken);
        p.getWorld().playSound(null, p.getX(), p.getY(), p.getZ(),
                SoundEvents.BLOCK_IRON_TRAPDOOR_CLOSE, SoundCategory.PLAYERS, 0.8f, 1.8f);
        p.sendMessage(Text.literal("Reloaded  " + GunItem.getAmmo(stack) + "/" + gun.stats().magSize()), true);
    }

    public static int countBullets(PlayerEntity p) {
        int n = 0;
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.size(); i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(ModItems.BULLET)) n += s.getCount();
        }
        return n;
    }

    private static int takeBullets(PlayerEntity p, int amount) {
        int taken = 0;
        PlayerInventory inv = p.getInventory();
        for (int i = 0; i < inv.size() && taken < amount; i++) {
            ItemStack s = inv.getStack(i);
            if (s.isOf(ModItems.BULLET)) {
                int t = Math.min(s.getCount(), amount - taken);
                s.decrement(t);
                taken += t;
            }
        }
        return taken;
    }

    private Reloader() {}
}
