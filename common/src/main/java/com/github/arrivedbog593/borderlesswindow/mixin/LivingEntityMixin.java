package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The Nausea Distortion slider under an Iris shaderpack. Iris redoes the
 * spin with its own copy of vanilla's formula, which reads Nausea's blend
 * factor straight from the player; GameRendererMixin's renderLevel hook
 * never sees that call. While GameRendererMixin.scaleIrisDistortion has
 * DistortionState's flag raised (only around the one call where Iris
 * computes the spin), the factor is scaled the same way the vanilla path
 * scales it. Any other caller, or any other effect, gets it untouched.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @ModifyReturnValue(method = "getEffectBlendFactor", at = @At("RETURN"))
    private float borderlesswindow$scaleIrisNausea(float original,
                                                   @Local(argsOnly = true) Holder<MobEffect> effect) {
        if (!DistortionState.isScalingIrisSpin() || effect != MobEffects.NAUSEA) {
            return original;
        }
        float factor = DistortionState.getNauseaSpinFactor();
        return original * factor * factor;
    }
}
