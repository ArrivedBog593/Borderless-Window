package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import com.github.arrivedbog593.borderlesswindow.visual.SkyState;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.particles.ParticleOptions;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Refuses particles that would spawn past the distance in DistanceState,
 * and hides the sun, moon, stars and falling rain and snow on request
 * (see SkyState).
 * <p>
 * PARTICLES. addParticleInternal is where vanilla already applies its own
 * 32-block limit: every particle from Level.addParticle ends up here, and
 * anything farther than 32 blocks from the camera is dropped unless it is
 * forced. This adds a shorter limit at the same spot, under the same rule.
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
 * <p>
 * SKY. renderSky draws the Overworld-style sky as a fixed sequence, the
 * same on both loaders (NeoForge only adds its DimensionSpecialEffects
 * hook before it): three immediate-mode draws -- sunrise glow, sun, moon
 * -- and three prebuilt buffers -- sky dome, stars, the dark lower half.
 * The ordinals below pick the sun and moon (1 and 2) and the stars (1)
 * out of those, in bytecode order, which is the order in the source
 * whether or not a given frame reaches them. Only the draw is skipped:
 * the sun still moves, sets and lights the world.
 * <p>
 * Dimensions that draw their own sky (a mod's DimensionSpecialEffects, a
 * custom-sky mod) never reach these calls and are not affected.
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

    /**
     * The sun and moon quads. A wrap and not a plain condition because the
     * mesh still has to be released: the upload inside drawWithShader is
     * what normally closes it, and an unclosed one would keep the shared
     * Tesselator buffer claimed.
     */
    @WrapOperation(
            method = "renderSky",
            at = {
                    @At(value = "INVOKE",
                            target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
                            ordinal = 1),
                    @At(value = "INVOKE",
                            target = "Lcom/mojang/blaze3d/vertex/BufferUploader;drawWithShader(Lcom/mojang/blaze3d/vertex/MeshData;)V",
                            ordinal = 2)
            })
    private void borderlesswindow$drawSunAndMoon(MeshData meshData, Operation<Void> original) {
        if (SkyState.isSunMoonEnabled()) {
            original.call(meshData);
        } else {
            meshData.close();
        }
    }

    /** The stars, a prebuilt buffer: nothing to release when skipped. */
    @WrapWithCondition(
            method = "renderSky",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/VertexBuffer;drawWithShader("
                            + "Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/client/renderer/ShaderInstance;)V",
                    ordinal = 1))
    private boolean borderlesswindow$shouldDrawStars(VertexBuffer instance, Matrix4f modelViewMatrix,
                                                     Matrix4f projectionMatrix, ShaderInstance shader) {
        return SkyState.isStarsEnabled();
    }

    /**
     * Falling rain and snow. renderLevel calls renderSnowAndRain from two
     * places (with and without Fabulous graphics); no ordinal matches
     * both. The rain sounds and the splashes on the ground come from
     * tickRain, a separate method, and stay.
     */
    @WrapWithCondition(
            method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;renderSnowAndRain("
                            + "Lnet/minecraft/client/renderer/LightTexture;FDDD)V"))
    private boolean borderlesswindow$shouldDrawWeather(LevelRenderer instance, LightTexture lightTexture,
                                                       float partialTick, double camX, double camY,
                                                       double camZ) {
        return SkyState.isWeatherEnabled();
    }
}
