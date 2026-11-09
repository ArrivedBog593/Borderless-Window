package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.cloud.CloudState;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Overrides the cloud height a dimension reports, so the "Cloud Height"
 * option can raise or lower the cloud layer. This is the single source
 * of truth for cloud altitude in 1.21.1: both vanilla's LevelRenderer
 * and Sodium's CloudRenderer read {@code level.effects().getCloudHeight()},
 * so one mixin here covers BOTH rendering paths -- the feature works
 * identically with and without Sodium (no Sodium mixin needed).
 * <p>
 * TWO separate pass-through conditions, both required:
 * <p>
 * 1. NO OVERRIDE SET. When CloudState holds its DEFAULT sentinel the
 *    original value is returned untouched, so dimensions from other mods
 *    keep whatever height they declare.
 * <p>
 * 2. THE DIMENSION HAS NO CLOUDS. Vanilla returns NaN to mean "no cloud
 *    layer here" -- the Nether and the End do this, and modded
 *    dimensions may too. LevelRenderer.renderClouds tests that NaN and
 *    skips rendering entirely. Replacing it with a real number therefore
 *    does not move a cloud layer, it CREATES one where the dimension
 *    intends none: clouds start rendering in the End (very visible,
 *    since it has no ceiling) and inside the Nether whenever the chosen
 *    height falls below its bedrock roof at y=128.
 * <p>
 *    This option moves clouds; it does not add them. Adding clouds to a
 *    cloudless dimension would be a different feature under a different
 *    name, so the NaN is preserved unconditionally -- including while an
 *    override is active, which is the case an earlier version of this
 *    mixin got wrong.
 */
@Mixin(DimensionSpecialEffects.class)
public abstract class CloudHeightMixin {

    @Inject(method = "getCloudHeight", at = @At("RETURN"), cancellable = true)
    private void borderlesswindow$overrideCloudHeight(CallbackInfoReturnable<Float> cir) {
        if (!CloudState.isOverridden()) {
            return;
        }

        // Condition 2 above: never turn a cloudless dimension into a
        // cloudy one.
        if (Float.isNaN(cir.getReturnValueF())) {
            return;
        }

        cir.setReturnValue((float) CloudState.getHeight());
    }
}