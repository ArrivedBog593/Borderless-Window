package com.github.arrivedbog593.borderlesswindow.fabric.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fabric's stand-in for NeoForge's RenderBlockScreenEffectEvent.
 * <p>
 * renderScreenEffect calls one method per view overlay; skipping a call is
 * what cancelling the event does on NeoForge. Only the water and fire calls
 * are wrapped: renderTex, the overlay drawn with your head inside a block,
 * is never touched (see ScreenEffectHandler).
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @WrapWithCondition(method = "renderScreenEffect",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderWater(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private static boolean borderlesswindow$drawWater(Minecraft minecraft, PoseStack poseStack) {
        return !ScreenEffectHandler.shouldHideWater();
    }

    @WrapWithCondition(method = "renderScreenEffect",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;renderFire(Lnet/minecraft/client/Minecraft;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private static boolean borderlesswindow$drawFire(Minecraft minecraft, PoseStack poseStack) {
        return !ScreenEffectHandler.shouldHideFire(minecraft.player);
    }
}
