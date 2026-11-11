package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.FogHandler;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.level.material.FogType;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The fog toggles and the fog start (see FogHandler).
 * <p>
 * WHERE. setupFog lets the camera's environment fill a FogData, then sets
 * the render distance fog last ({@code renderDistanceEnd = far}) and
 * returns it. Right after that assignment every value is final and none
 * has been read yet. Since 26.1 the FogData travels in the frame's camera
 * state and GameRenderer uploads it later (updateBuffer). Verified against
 * setupFog in 26.1.2 on both loaders: one assignment to renderDistanceEnd,
 * one FogData and one FogType local.
 * <p>
 * In common/ for both loaders: NeoForge's own fog event fires right after
 * this point and sees our values, so a mod listening to it still gets the
 * last word.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(
            method = "setupFog",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/renderer/fog/FogData;renderDistanceEnd:F",
                    opcode = Opcodes.PUTFIELD,
                    shift = At.Shift.AFTER))
    private void borderlesswindow$applyFogSettings(CallbackInfoReturnable<FogData> cir,
                                                   @Local(argsOnly = true) Camera camera,
                                                   @Local(argsOnly = true) int renderDistance,
                                                   @Local FogData fogData,
                                                   @Local FogType fogType) {
        FogHandler.apply(fogData, fogType, camera, renderDistance * 16.0F);
    }
}
