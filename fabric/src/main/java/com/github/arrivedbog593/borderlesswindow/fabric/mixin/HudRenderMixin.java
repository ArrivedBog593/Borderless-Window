package com.github.arrivedbog593.borderlesswindow.fabric.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric's stand-in for NeoForge's RenderGuiEvent.Post: draws the FPS
 * overlay once per frame, after every HUD layer -- the end of
 * Hud.extractRenderState (Gui's up to 26.1.2) is exactly where NeoForge
 * fires that event.
 */
@Mixin(Hud.class)
public abstract class HudRenderMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void borderlesswindow$renderFpsOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker,
                                                   CallbackInfo ci) {
        FpsOverlay.render(guiGraphics);
    }
}
