package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.general.ScreenMode;
import com.github.arrivedbog593.borderlesswindow.sodium.SodiumMenuRefresher;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.util.Util;
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
 * isFullscreen() is overridden, so options.txt correctly stores whether
 * the current state differs from WINDOWED, regardless of which of the
 * two non-windowed modes is active.
 */
@Mixin(Window.class)
public abstract class WindowMixin {

    @Inject(method = "toggleFullScreen", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$onToggle(CallbackInfo ci) {
        ci.cancel();

        // Swallow toggles that happen before our config is loaded: vanilla's
        // Minecraft constructor auto-calls toggleFullScreen() at boot when
        // options.txt says fullscreen=true, long before MinecraftInitMixin.
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
     * Since 26.2 a "soft" fullscreen window (vanilla's non-exclusive one,
     * 1 px taller than the monitor on Windows) reports a framebuffer 1 px
     * shorter than itself, and both graphics backends present the frame at
     * the top of the window, so the extra row hangs off-screen unused.
     * Vanilla decides that from the fullscreen field and its Exclusive
     * Fullscreen option, neither of which follows the mod's modes: the
     * field stays as the launch left it (toggleFullScreen is cancelled
     * above) and Fullscreen is always exclusive (MinecraftMixin). Left
     * alone, a game launched with --fullscreen and the option off would
     * lose 1 px in every mode. Our Borderless window is the one with the
     * extra pixel (BorderlessHandler.applyBorderless), so it gets the
     * correction instead: its framebuffer matches the monitor. Windows
     * only, as vanilla: elsewhere a window manager may clamp the window to
     * the monitor, and the correction would cut a visible row.
     */
    @ModifyReturnValue(method = "isSoftScreen", at = @At("RETURN"))
    private boolean borderlesswindow$isSoftScreen(boolean original) {
        return Util.getPlatform() == Util.OS.WINDOWS
                && BorderlessHandler.getCurrentMode() == ScreenMode.BORDERLESS;
    }

    /**
     * onFocus is the GLFW focus callback's landing point, so this fires on
     * every focus change (Minecraft.setWindowActive did this up to 1.21.11;
     * since 26.1 the window keeps the focus itself and
     * Minecraft.isWindowActive just asks it). Runs at TAIL: the window must
     * already hold the new value when the volume is recomputed. Clicking
     * back into the game also counts as activity for the idle cap.
     */
    @Inject(method = "onFocus", at = @At("TAIL"))
    private void borderlesswindow$onFocusChanged(long handle, boolean focused, CallbackInfo ci) {
        UnfocusedVolumeState.refresh();
        if (focused) {
            IdleFpsState.markInput();
        }
    }
}