package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Splits vanilla's single Distortion Effects slider by source.
 * <p>
 * Since 26.3 the frame's extract step reads the two sources apart into
 * the player's render state -- the portal's intensity with this Mth.lerp
 * (the only one in extractPlayerState), Nausea's with getEffectBlendFactor
 * -- and GameRenderer.renderLevel takes the larger and multiplies it by
 * the squared vanilla scale. Scaling each source before that max is all
 * the split needs. (Up to 26.2 renderLevel read both itself.)
 * <p>
 * Squaring our own factor matches vanilla's scale * scale, so 50% here
 * feels like 50% on Minecraft's slider.
 * <p>
 * The spin SPEED reads the same two sources once more, per tick; see
 * LocalPlayerMixin.
 */
@Mixin(LevelExtractor.class)
public abstract class LevelExtractorMixin {

    @ModifyExpressionValue(
            method = "extractPlayerState",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float borderlesswindow$scalePortalDistortion(float original) {
        float factor = DistortionState.getPortalFactor();
        return original * factor * factor;
    }

    /**
     * Nausea half of the split above. Inside a portal it takes the
     * portal's percentage (see DistortionState).
     */
    @ModifyExpressionValue(
            method = "extractPlayerState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;"
                            + "getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F"))
    private float borderlesswindow$scaleNauseaDistortion(float original) {
        float factor = DistortionState.getNauseaSpinFactor();
        return original * factor * factor;
    }
}
