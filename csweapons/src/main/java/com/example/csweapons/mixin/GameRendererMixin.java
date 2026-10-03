package com.example.csweapons.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.example.csweapons.client.CSWeaponsClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;

/** Narrows the field of view while scoped (about 5x zoom). */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D",
            at = @At("RETURN"), cancellable = true, require = 0)
    private void csweapons$scopeZoom(Camera camera, float tickDelta, boolean changingFov,
                                     CallbackInfoReturnable<Double> cir) {
        if (changingFov && CSWeaponsClient.isScoped(MinecraftClient.getInstance())) {
            cir.setReturnValue(cir.getReturnValue() * 0.2);
        }
    }
}
