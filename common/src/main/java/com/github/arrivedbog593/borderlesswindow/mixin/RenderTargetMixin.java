package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderTarget;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Presents the frame at the top of our Borderless window. WindowMixin has
 * the game render 1 px shorter than that window, at the monitor's size;
 * Minecraft.runTick draws the main render target to the window through a
 * viewport that starts at its bottom edge (OpenGL counts rows from the
 * bottom), so the viewport is raised by that 1 px: it covers the rows on
 * the monitor, and the one below the screen is left out. Minecraft 26.2
 * does the same for its own non-exclusive fullscreen. In any other mode
 * the frame and the window have the same size, and nothing moves.
 * <p>
 * _blitToScreen also draws other targets into the main one (the glowing
 * entity outline), so only the main target, the one that goes to the
 * screen, is raised: the viewport moves only during the call made from
 * borderlesswindow$blitPastSodium.
 */
@Mixin(RenderTarget.class)
public abstract class RenderTargetMixin {

    // Render thread only, like every blit.
    @Unique
    private static boolean borderlesswindow$presenting = false;

    @Shadow
    protected abstract void _blitToScreen(int width, int height, boolean disableBlend);

    /**
     * Sodium cancels blitToScreen(int, int, boolean) at its head and copies
     * the frame with glBlitFramebuffer to the bottom-left corner instead,
     * which skips the viewport raised below: the frame would land on the
     * hidden row, and the top row on the monitor would keep the clear color
     * (the red of the loading screen on the title screen). runTick calls
     * the two-argument overload, so in Borderless the main target goes
     * straight to vanilla's _blitToScreen, past Sodium's replacement -- the
     * same path Sodium itself falls back to with its Intel workaround on.
     */
    @WrapOperation(method = "blitToScreen(II)V",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;blitToScreen(IIZ)V"))
    private void borderlesswindow$blitPastSodium(RenderTarget instance, int width, int height,
                                                 boolean disableBlend, Operation<Void> original) {
        if (BorderlessHandler.hasHiddenBottomRow() && instance == Minecraft.getInstance().getMainRenderTarget()) {
            borderlesswindow$presenting = true;
            try {
                this._blitToScreen(width, height, disableBlend);
            } finally {
                borderlesswindow$presenting = false;
            }
        } else {
            original.call(instance, width, height, disableBlend);
        }
    }

    @ModifyArg(method = "_blitToScreen",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_viewport(IIII)V"),
            index = 1)
    private int borderlesswindow$raiseViewport(int y) {
        return borderlesswindow$presenting ? y + 1 : y;
    }
}
