package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.FogState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Moves where the terrain fog starts (see FogState).
 * <p>
 * WHY THE CLAMP. setupFog picks one branch per situation -- lava, powder
 * snow, Blindness/Darkness, water, the thick Nether fog, the sky -- and
 * only the last one, ordinary terrain fog, computes
 * {@code Mth.clamp(farPlaneDistance / 10, 4, 64)}: the depth of the fog
 * band, with start = far - depth. Changing that one value changes the
 * start and nothing else, and every other branch is left alone by
 * construction instead of by a list of conditions: the effect fogs, the
 * fluids, the Nether and the sky simply never reach this call. Verified
 * against setupFog in 1.21.1 on both loaders: it is the only clamp in
 * the method.
 * <p>
 * In common/ rather than per loader: the method is the same on NeoForge,
 * whose RenderFog event fires afterwards and so sees the moved start --
 * and the terrain fog toggle (FogHandler) still pushes the whole fog out
 * when it is off. Not to be confused with the Fabric module's own
 * FogRendererMixin, which is that toggle's hook on Fabric.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @ModifyExpressionValue(
            method = "setupFog",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;clamp(FFF)F"))
    private static float borderlesswindow$moveTerrainFogStart(float original,
                                                              @Local(argsOnly = true, ordinal = 0) float farPlaneDistance) {
        int start = FogState.getTerrainStart();
        if (start == FogState.DEFAULT_START) {
            return original;
        }
        return farPlaneDistance * (1.0F - start / 100.0F);
    }
}
