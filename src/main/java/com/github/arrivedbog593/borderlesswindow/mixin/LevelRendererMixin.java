package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Refuses particles that would spawn past the distance in DistanceState.
 * <p>
 * addParticleInternal is where vanilla already applies its own 32-block
 * limit: every particle from Level.addParticle ends up here, and anything
 * farther than 32 blocks from the camera is dropped unless it is forced.
 * This adds a shorter limit at the same spot, under the same rule.
 * <p>
 * WHY AT SPAWN AND NOT AT RENDER. Refusing the particle means it is never
 * created, ticked or kept in the engine's queues, which is where the cost
 * of a busy farm or a big fight actually is. Particles live a second or
 * two, so one born out of range does not linger long enough to matter if
 * the player walks toward it.
 * <p>
 * FORCED PARTICLES are left alone, exactly as vanilla does with its own
 * limit: the caller asked for them to be shown regardless of settings.
 * <p>
 * Targets the nine-argument overload (the eight-argument one delegates to
 * it). Sodium does not touch this method.
 */
@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(
            method = "addParticleInternal(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"),
            cancellable = true)
    private void borderlesswindow$cullParticleByDistance(ParticleOptions options, boolean force,
                                                         boolean decreased,
                                                         double x, double y, double z,
                                                         double xSpeed, double ySpeed, double zSpeed,
                                                         CallbackInfoReturnable<Particle> cir) {
        if (force) {
            return;
        }

        double distanceSqr = this.minecraft.gameRenderer.getMainCamera().getPosition()
                .distanceToSqr(x, y, z);
        if (DistanceState.isBeyond(DistanceState.getParticles(), distanceSqr)) {
            cir.setReturnValue(null);
        }
    }
}
