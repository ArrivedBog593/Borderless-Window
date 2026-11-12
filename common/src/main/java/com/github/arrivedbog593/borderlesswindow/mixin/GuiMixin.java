package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * "Hide HUD" while zooming. Since 26.2 Gui holds the screens and the
 * overlays and leaves the HUD to Hud, which it extracts from here; the
 * HUD's own overlays and the held item's name are in HudMixin.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    /**
     * Done the way F1 does it: the HUD's hidden flag is raised for the
     * duration of this one call. Everything that already honors F1 --
     * every HUD layer on both loaders, other mods' HUD elements, the FPS
     * counter -- hides with it, and the player's own F1 state is put back
     * untouched afterwards. The flag only has a toggle, so it is toggled
     * twice, and only when F1 has not hidden the HUD already.
     * <p>
     * Extracting the HUD also copies the flag into the frame's GUI render
     * state, where the hand, the screen effects and the 3D crosshair read
     * it later on. Up to 26.1.2 those read F1 on their own and the zoom
     * never hid them, so the copy goes back to the player's state; the
     * hand has a zoom option of its own.
     * <p>
     * The only call to Hud.extractRenderState, on both loaders: NeoForge
     * draws its HUD layers from inside it.
     */
    @WrapOperation(
            method = "extractRenderState",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;extractRenderState("
                            + "Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
    private void borderlesswindow$hideHudWhileZooming(Hud hud, GuiGraphicsExtractor guiGraphics,
                                                      DeltaTracker deltaTracker,
                                                      Operation<Void> original) {
        if (!ZoomState.shouldHideHud() || hud.isHidden()) {
            original.call(hud, guiGraphics, deltaTracker);
            return;
        }
        hud.toggle();
        try {
            original.call(hud, guiGraphics, deltaTracker);
        } finally {
            hud.toggle();
        }
        Minecraft.getInstance().gameRenderer.gameRenderState().guiRenderState.isHudHidden = false;
    }
}
