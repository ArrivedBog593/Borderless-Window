package com.github.arrivedbog593.borderlesswindow.fabric.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.FogHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric's stand-in for NeoForge's ViewportEvent.RenderFog.
 * <p>
 * setupFog ends by publishing the fog planes it computed through
 * RenderSystem.setShaderFogStart / End -- the values every shader (and
 * Sodium) reads afterwards. Overwriting them at TAIL is what NeoForge's
 * event does when cancelled with new plane distances. The FogType is the
 * one setupFog itself uses: the camera's fluid.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "setupFog", at = @At("TAIL"))
    private static void borderlesswindow$removeFog(Camera camera, FogRenderer.FogMode fogMode,
                                                   float renderDistance, boolean thickFog,
                                                   float partialTick, CallbackInfo ci) {
        if (FogHandler.shouldRemove(camera.getFluidInCamera(), camera)) {
            RenderSystem.setShaderFogStart(FogHandler.NO_FOG_NEAR);
            RenderSystem.setShaderFogEnd(FogHandler.NO_FOG_FAR);
        }
    }
}
