package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.window.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.window.ScreenMode;
import com.github.arrivedbog593.borderlesswindow.sodium.SodiumMenuRefresher;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * F11 behaves according to the "F11 Mode" option (Borderless Window
 * section of the video settings menu): toggle Windowed <-> Borderless,
 * toggle Windowed <-> Fullscreen, or cycle through all 3 modes. All 3
 * modes can also be selected directly via the "Screen Mode" option that
 * replaces Sodium's fullscreen checkbox.
 * <p>
 * isFullscreen() is overridden so options.txt correctly stores whether
 * the current state differs from WINDOWED, regardless of which of the
 * two non-windowed modes is active.
 */
@Mixin(Window.class)
@SuppressWarnings("unused") // Mixin class, not instantiated directly
public abstract class WindowMixin {

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
}