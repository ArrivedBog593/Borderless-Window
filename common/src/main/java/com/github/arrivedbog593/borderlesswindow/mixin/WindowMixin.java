package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reacts to focus changes. The screen modes need no hook here since 26.3:
 * vanilla's window handles all 3 itself (see BorderlessHandler), and F11
 * is handled in MinecraftMixin.
 */
@Mixin(Window.class)
public abstract class WindowMixin {

    /**
     * onFocus is where the window's focus events land (SDL's focus gained
     * and lost since 26.3, the GLFW focus callback before), so this fires
     * on every focus change (Minecraft.setWindowActive did this up to
     * 1.21.11; since 26.1 the window keeps the focus itself and
     * Minecraft.isWindowActive just asks it). Runs at TAIL: the window must
     * already hold the new value when the volume is recomputed. Clicking
     * back into the game also counts as activity for the idle cap.
     */
    @Inject(method = "onFocus", at = @At("TAIL"))
    private void borderlesswindow$onFocusChanged(boolean focused, CallbackInfo ci) {
        UnfocusedVolumeState.refresh();
        if (focused) {
            IdleFpsState.markInput();
        }
    }
}
