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
 * Refuses particles that would spawn past the distance in DistanceState.
 * <p>
 * doAddParticle is where vanilla already applies its own 32-block limit:
 * every particle from Level.addParticle ends up here, and anything
 * farther than 32 blocks from the camera is dropped unless it overrides
 * the limiter. This adds a shorter limit at the same spot, under the same
 * rule. Identical on both loaders.
 * <p>
 * WHY AT SPAWN AND NOT AT RENDER. Refusing the particle means it is never
 * created, ticked or kept in the engine's queues, which is where the cost
 * of a busy farm or a big fight actually is. Particles live a second or
 * two, so one born out of range does not linger long enough to matter if
 * the player walks toward it.
 * <p>
 * FORCED PARTICLES are left alone, exactly as vanilla does with its own
 * limit: overrideLimiter is true when the caller asked for them to be
 * shown regardless of settings, or when the particle type always is.
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
        if (overrideLimiter) {
            return;
        }

        double distanceSqr = this.minecraft.gameRenderer.mainCamera().position()
                .distanceToSqr(x, y, z);
        if (DistanceState.isBeyond(DistanceState.getParticles(), distanceSqr)) {
            ci.cancel();
        }
    }
}
