package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.SkyState;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.world.level.MoonPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Hides the sun, moon and stars on request (see SkyState).
 * <p>
 * renderSunMoonAndStars draws the three with one private method each, the
 * same on both loaders (NeoForge does not patch SkyRenderer). Skipping a
 * call skips that body and nothing else: the pose stack is pushed and
 * popped around it by the caller, and each method builds and draws its
 * own quads, so there is nothing left half done. The sun still moves,
 * sets and lights the world; the sunrise glow, drawn separately, stays.
 * <p>
 * Dimensions that draw their own sky (NeoForge's custom sky renderers, a
 * custom-sky mod) never reach these calls and are not affected.
 */
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @WrapWithCondition(
            method = "renderSunMoonAndStars",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean borderlesswindow$shouldDrawSun(SkyRenderer instance, float rainBrightness,
                                                   PoseStack poseStack) {
        return SkyState.isSunMoonEnabled();
    }

    @WrapWithCondition(
            method = "renderSunMoonAndStars",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon("
                            + "Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean borderlesswindow$shouldDrawMoon(SkyRenderer instance, MoonPhase moonPhase,
                                                    float rainBrightness, PoseStack poseStack) {
        return SkyState.isSunMoonEnabled();
    }

    @WrapWithCondition(
            method = "renderSunMoonAndStars",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean borderlesswindow$shouldDrawStars(SkyRenderer instance, float starBrightness,
                                                     PoseStack poseStack) {
        return SkyState.isStarsEnabled();
    }
}
