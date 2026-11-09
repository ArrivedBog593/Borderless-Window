package com.github.arrivedbog593.borderlesswindow.fabric.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric's stand-in for NeoForge's RenderGuiEvent.Post: draws the FPS
 * overlay once per frame, after every HUD layer -- the end of Gui.render is
 * exactly where NeoForge fires that event.
 */
@Mixin(Gui.class)
public abstract class GuiRenderMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void borderlesswindow$renderFpsOverlay(GuiGraphics guiGraphics, DeltaTracker deltaTracker,
                                                   CallbackInfo ci) {
        FpsOverlay.render(guiGraphics);
    }
}
