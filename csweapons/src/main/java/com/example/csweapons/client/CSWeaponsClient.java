package com.example.csweapons.client;

import org.lwjgl.glfw.GLFW;

import com.example.csweapons.GunItem;
import com.example.csweapons.ModItems;
import com.example.csweapons.ReloadPayload;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class CSWeaponsClient implements ClientModInitializer {
    private static KeyBinding reloadKey;

    @Override
    public void onInitializeClient() {
        reloadKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.csweapons.reload", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.categories.csweapons"));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (reloadKey.wasPressed()) {
                if (client.player != null && client.player.getMainHandStack().getItem() instanceof GunItem) {
                    ClientPlayNetworking.send(new ReloadPayload());
                }
            }
        });

        HudRenderCallback.EVENT.register(CSWeaponsClient::renderHud);
    }

    /** Scoped = first person, holding a scoped gun (AWP) and sneaking. */
    public static boolean isScoped(MinecraftClient client) {
        PlayerEntity p = client.player;
        if (p == null || !client.options.getPerspective().isFirstPerson()) return false;
        return p.getMainHandStack().getItem() instanceof GunItem g && g.stats().scoped() && p.isSneaking();
    }

    private static void renderHud(DrawContext ctx, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity p = client.player;
        if (p == null || client.options.hudHidden) return;

        int w = ctx.getScaledWindowWidth();
        int h = ctx.getScaledWindowHeight();

        if (isScoped(client)) drawScope(ctx, w, h);

        ItemStack held = p.getMainHandStack();
        if (held.getItem() instanceof GunItem gun) {
            int reserve = 0;
            for (int i = 0; i < p.getInventory().size(); i++) {
                ItemStack s = p.getInventory().getStack(i);
                if (s.isOf(ModItems.BULLET)) reserve += s.getCount();
            }
            TextRenderer tr = client.textRenderer;
            Text name = held.getName();
            Text ammo = Text.literal(GunItem.getAmmo(held) + " / " + gun.stats().magSize()
                    + "   [" + (p.isCreative() ? "INF" : String.valueOf(reserve)) + "]");
            ctx.drawTextWithShadow(tr, name, w - tr.getWidth(name) - 10, h - 34, 0xFFFFFF);
            int color = GunItem.getAmmo(held) == 0 ? 0xFF5555 : 0xFFD24A;
            ctx.drawTextWithShadow(tr, ammo, w - tr.getWidth(ammo) - 10, h - 22, color);
        }
    }

    private static void drawScope(DrawContext ctx, int w, int h) {
        int black = 0xFF000000;
        int cx = w / 2, cy = h / 2;
        int r = (int) (h * 0.46);

        ctx.fill(0, 0, w, cy - r, black);          // above the lens
        ctx.fill(0, cy + r, w, h, black);          // below the lens
        for (int y = cy - r; y < cy + r; y++) {    // left/right of the circle
            int dy = y - cy;
            int half = (int) Math.sqrt((double) r * r - (double) dy * dy);
            ctx.fill(0, y, cx - half, y + 1, black);
            ctx.fill(cx + half, y, w, y + 1, black);
        }
        ctx.fill(cx - r, cy, cx + r, cy + 1, black);   // crosshair
        ctx.fill(cx, cy - r, cx + 1, cy + r, black);
        ctx.fill(cx - 1, cy - 1, cx + 2, cy + 2, 0xFFFF2020); // red dot
    }
}
