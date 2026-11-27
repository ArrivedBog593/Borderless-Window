package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Refuses particles that would spawn past the distance of their group in
 * DistanceState (ParticleCategories, or the General slider).
 * <p>
 * doAddParticle is where vanilla already applies its own 32-block limit:
 * every particle from Level.addParticle ends up here, and anything
 * farther than 32 blocks from the camera is dropped unless it overrides
 * the limiter. This adds a shorter limit at the same spot. Identical on
 * both loaders.
 * <p>
 * WHY AT SPAWN AND NOT AT RENDER. Refusing the particle means it is never
 * created, ticked or kept in the engine's queues, which is where the cost
 * of a busy farm or a big fight actually is. Particles live a second or
 * two, so one born out of range does not linger long enough to matter if
 * the player walks toward it.
 * <p>
 * FORCED PARTICLES. overrideLimiter is the particle type's own flag
 * (ParticleType.getOverrideLimiter) OR the caller's request (a command, a
 * server packet). Only the caller's request is honored: a type that
 * merely ignores vanilla's 32 blocks (explosions, poof, sweep, campfire
 * smoke...) is exactly what a player lowers these sliders for. When the
 * type's flag is set the two cannot be told apart, so such a particle is
 * capped even if a caller also forced it.
 */
@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "doAddParticle", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$cullParticleByDistance(ParticleOptions particle,
                                                         boolean overrideLimiter,
                                                         boolean alwaysShow,
                                                         double x, double y, double z,
                                                         double xSpeed, double ySpeed, double zSpeed,
                                                         CallbackInfo ci) {
        if (overrideLimiter && !particle.getType().getOverrideLimiter()) {
            return; // forced by the caller
        }

        int cap = DistanceState.particleCap(particle);
        if (cap == DistanceState.DEFAULT) {
            return;
        }
        double distanceSqr = this.minecraft.gameRenderer.getMainCamera().position()
                .distanceToSqr(x, y, z);
        if (DistanceState.isBeyond(cap, distanceSqr)) {
            ci.cancel();
        }
    }
}
