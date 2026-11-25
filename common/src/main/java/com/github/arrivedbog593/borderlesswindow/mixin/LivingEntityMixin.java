package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.visual.StatusEffectState;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Two client-side hooks on LivingEntity.
 * <p>
 * IRIS NAUSEA. The Nausea Distortion slider under an Iris shaderpack. Iris
 * redoes the spin with its own copy of vanilla's formula, which reads
 * Nausea's blend factor straight from the player; GameRendererMixin's
 * renderLevel hook never sees that call. While
 * GameRendererMixin.scaleIrisDistortion has DistortionState's flag raised
 * (only around the one call where Iris computes the spin), the factor is
 * scaled the same way the vanilla path scales it. Any other caller, or any
 * other effect, gets it untouched.
 * <p>
 * OWN EFFECT PARTICLES (see StatusEffectState). tickEffects spawns the
 * swirls of every active effect around its entity, one addParticle call;
 * it is skipped for your own player only. LocalPlayer is the client's own
 * player and no one else: other players are RemotePlayers, and in single
 * player the server ticks a ServerPlayer, whose ServerLevel.addParticle
 * draws nothing anyway.
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

    @WrapWithCondition(
            method = "tickEffects",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"))
    private boolean borderlesswindow$spawnEffectParticle(Level level, ParticleOptions particle,
                                                         double x, double y, double z,
                                                         double xSpeed, double ySpeed, double zSpeed) {
        // Through LivingEntity: the IDE sees this mixin class, which no
        // LocalPlayer extends, and would call the check always false.
        LivingEntity self = (LivingEntity) (Object) this;
        return StatusEffectState.isOwnParticlesEnabled() || !(self instanceof LocalPlayer);
    }
}
