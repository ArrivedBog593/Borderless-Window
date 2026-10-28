package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fov.FovState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Per-source control over the FOV pull, for the three sources that live
 * in AbstractClientPlayer.getFieldOfViewModifier.
 * <p>
 * Vanilla builds one multiplier by chaining them: flying multiplies by
 * 1.1, movement speed by (speed / walkSpeed + 1) / 2, and a drawing bow
 * by 1 - t * 0.15. The "FOV Effects" slider does not appear in this
 * method, but it still reaches all three: the combined multiplier is
 * handed to NeoForge's ClientHooks.getFieldOfViewModifier, and the
 * ComputeFovModifierEvent constructor lerps it toward 1.0 by that slider
 * (it replaced vanilla's own lerp when the hook was patched in). What the
 * game does not offer is a way to soften them SEPARATELY -- one slider
 * moves all of them at once.
 * <p>
 * So these injections sit under vanilla's slider: they scale each source
 * before the combined value reaches the event, and the slider then scales
 * the result. With FOV Effects at Off, nothing here has any effect.
 * <p>
 * Each injection scales the DISTANCE from 1.0 rather than the value, so
 * 0% leaves the multiplier at exactly 1.0 and 100% leaves vanilla's
 * number untouched.
 * <p>
 * THE SPYGLASS IS NOT TOUCHED. Its branch returns 0.1F outright, and
 * that value is the zoom itself; see FovState. That early return also
 * skips the NeoForge hook, so vanilla's slider never touches it either.
 * <p>
 * NOTE ON CONSTANTS: two of these target inline float constants rather
 * than method calls, because that is what the arithmetic is -- there is
 * no invocation to wrap. @ModifyExpressionValue accepts a CONSTANT
 * injection point for exactly this, and unlike @ModifyConstant it
 * stacks with other mods.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {

    /**
     * Flying multiplies by 1.1. Scaling the constant toward 1.0 is the
     * whole feature: at 0% the creative flight pull disappears. The
     * check is abilities.flying, which is creative (and spectator)
     * flight only -- elytra gliding never reaches this branch.
     */
    @ModifyExpressionValue(
            method = "getFieldOfViewModifier",
            at = @At(value = "CONSTANT", args = "floatValue=1.1"))
    private float borderlesswindow$scaleFlyingFov(float original) {
        return Mth.lerp(FovState.getFlyingFactor(), 1.0F, original);
    }

    /**
     * Speed is the one people actually complain about -- with Speed
     * potions or a modified movement attribute the FOV never settles.
     * <p>
     * Vanilla computes (v / w + 1) / 2, where v is the movement speed
     * attribute and w the walking speed, so there is no constant to
     * scale. Lerping v toward w instead is algebraically the same
     * thing: substituting v' = lerp(t, w, v) into that expression
     * yields lerp(t, 1.0, (v / w + 1) / 2), which is precisely the
     * scaling the other sliders apply. At 0% the attribute reads as the
     * walking speed, the ratio is 1, and the multiplier is 1.0.
     * <p>
     * Vanilla's own guard against w == 0 (and the NaN it would produce)
     * runs after this and is unaffected.
     */
    @ModifyExpressionValue(
            method = "getFieldOfViewModifier",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/AbstractClientPlayer;"
                            + "getAttributeValue(Lnet/minecraft/core/Holder;)D"))
    private double borderlesswindow$scaleSpeedFov(double original) {
        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        float walking = self.getAbilities().getWalkingSpeed();
        return Mth.lerp(FovState.getSpeedFactor(), walking, (float) original);
    }

    /**
     * A fully drawn bow narrows the FOV by 15%. Scaling the 0.15 keeps
     * the same easing curve and only changes how deep the zoom goes;
     * at 0% the view stays put while drawing.
     */
    @ModifyExpressionValue(
            method = "getFieldOfViewModifier",
            at = @At(value = "CONSTANT", args = "floatValue=0.15"))
    private float borderlesswindow$scaleBowFov(float original) {
        return original * FovState.getBowFactor();
    }
}