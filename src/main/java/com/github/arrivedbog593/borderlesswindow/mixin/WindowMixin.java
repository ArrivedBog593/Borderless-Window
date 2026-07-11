package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.ScreenMode;
import com.github.arrivedbog593.borderlesswindow.SodiumMenuRefresher;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * F11 toggles between Windowed and the mode configured in "F11 Mode"
 * (Borderless or Fullscreen, in the Borderless Window section of the
 * video settings menu). All 3 modes can also be selected directly via
 * the "Screen Mode" option that replaces Sodium's fullscreen checkbox.
 * <p>
 * isFullscreen() is overridden, so options.txt correctly stores whether
 * the current state differs from WINDOWED, regardless of which of the
 * two non-windowed modes is active.
 */
@Mixin(Window.class)
public abstract class WindowMixin {

    @Inject(method = "toggleFullScreen", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$onToggle(CallbackInfo ci) {
        ci.cancel();
        Window self = (Window) (Object) this;
        ScreenMode next = BorderlessHandler.getCurrentMode() == ScreenMode.WINDOWED
                ? BorderlessHandler.getF11Target()
                : ScreenMode.WINDOWED;
        BorderlessHandler.setMode(self, next);

        // If Sodium's video settings menu is open right now, refresh its
        // controls so "Screen Mode" reflects the F11 change live.
        SodiumMenuRefresher.refreshIfSodiumMenuOpen();
    }

    @Inject(method = "isFullscreen", at = @At("RETURN"), cancellable = true)
    private void borderlesswindow$isFullscreen(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(BorderlessHandler.getCurrentMode() != ScreenMode.WINDOWED);
    }
}