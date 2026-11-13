package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.general.ScreenMode;
import com.github.arrivedbog593.borderlesswindow.sodium.SodiumMenuRefresher;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * F11 behaves according to the "F11 Mode" option (Borderless Window
 * section of the video settings menu): toggle Windowed <-> Borderless,
 * toggle Windowed <-> Fullscreen, or cycle through all 3 modes. All 3
 * modes can also be selected directly via the "Screen Mode" option that
 * replaces Sodium's fullscreen checkbox.
 * <p>
 * isFullscreen() is overridden, so options.txt correctly stores whether
 * the current state differs from WINDOWED, regardless of which of the
 * two non-windowed modes is active.
 */
@Mixin(Window.class)
public abstract class WindowMixin {

    @Shadow
    private int framebufferHeight;

    @Inject(method = "toggleFullScreen", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$onToggle(CallbackInfo ci) {
        ci.cancel();

        // Swallow toggles that happen before our config is loaded: vanilla's
        // Minecraft constructor auto-calls toggleFullScreen() at boot when
        // options.txt says fullscreen=true, which runs before client setup.
        // initializeFromConfig() is the single authority on the startup mode.
        if (!BorderlessHandler.isInitialized()) {
            return;
        }

        Window self = (Window) (Object) this;
        BorderlessHandler.setMode(self, BorderlessHandler.getNextF11Mode());

        // If Sodium's video settings menu is open right now, refresh its
        // controls so "Screen Mode" reflects the F11 change live.
        SodiumMenuRefresher.refreshIfSodiumMenuOpen();
    }

    @Inject(method = "isFullscreen", at = @At("RETURN"), cancellable = true)
    private void borderlesswindow$isFullscreen(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(BorderlessHandler.getCurrentMode() != ScreenMode.WINDOWED);
    }

    /**
     * Our Borderless window is 1 px taller than the monitor, and the game
     * would render that extra row too, off-screen. Here the framebuffer
     * size the game renders at leaves it out, so it matches the monitor
     * (1920x1080 instead of 1920x1081: F3, screenshots, one row less to
     * draw), and RenderTargetMixin presents the frame at the top of the
     * window. Minecraft 26.2 does the same for its own non-exclusive
     * fullscreen. A newHeight of 0 (minimized) is left alone.
     * <p>
     * BorderlessHandler.setMode switches the mode before moving the window,
     * so the resize callback already sees the new one.
     */
    @ModifyVariable(method = "onFramebufferResize", at = @At("HEAD"), argsOnly = true, ordinal = 1)
    private int borderlesswindow$hideBottomRowOnResize(int newHeight) {
        return newHeight > 1 && BorderlessHandler.hasHiddenBottomRow() ? newHeight - 1 : newHeight;
    }

    /**
     * Same as above for the size read straight from GLFW (at window
     * creation, before the saved mode applies, so normally a no-op).
     */
    @Inject(method = "refreshFramebufferSize", at = @At("TAIL"))
    private void borderlesswindow$hideBottomRowOnRefresh(CallbackInfo ci) {
        if (this.framebufferHeight > 1 && BorderlessHandler.hasHiddenBottomRow()) {
            this.framebufferHeight--;
        }
    }
}