package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.StatusEffectState;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Own effect particles (see StatusEffectState). tickEffects spawns the
 * swirls of every active effect around its entity, one addParticle call;
 * it is skipped for your own player only.
 * <p>
 * LocalPlayer is the client's own player and no one else: other players
 * are RemotePlayers, and in single player the server ticks a
 * ServerPlayer, whose ServerLevel.addParticle draws nothing anyway.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

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
