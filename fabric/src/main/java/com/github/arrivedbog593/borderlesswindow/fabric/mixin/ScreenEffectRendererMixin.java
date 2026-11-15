package com.github.arrivedbog593.borderlesswindow.fabric.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectHandler;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Fabric's stand-in for NeoForge's ExtractBlockScreenEffectEvent.
 * <p>
 * submit (renderScreenEffect up to 26.1.2) calls one method per view
 * overlay; skipping a call is what cancelling the event does on NeoForge.
 * Only the water and fire calls are wrapped: submitBlockSprite, the
 * overlay drawn with your head inside a block, is never touched (see
 * ScreenEffectHandler).
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {

    @WrapWithCondition(method = "submit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;submitWater("
                            + "Lnet/minecraft/client/renderer/state/level/PlayerRenderState$WaterOverlay;"
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;"
                            + "Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"))
    private boolean borderlesswindow$drawWater(PlayerRenderState.WaterOverlay waterOverlay, PoseStack poseStack,
                                               SubmitNodeCollector submitNodeCollector) {
        return !ScreenEffectHandler.shouldHideWater();
    }

    /**
     * The renderer keeps no Minecraft of its own since 26.3 (it draws from
     * the frame's render state), so the player comes from the instance.
     */
    @WrapWithCondition(method = "submit",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ScreenEffectRenderer;submitFire("
                            + "Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;"
                            + "Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private boolean borderlesswindow$drawFire(PoseStack poseStack, SubmitNodeCollector submitNodeCollector,
                                              TextureAtlasSprite sprite) {
        return !ScreenEffectHandler.shouldHideFire(Minecraft.getInstance().player);
    }
}
