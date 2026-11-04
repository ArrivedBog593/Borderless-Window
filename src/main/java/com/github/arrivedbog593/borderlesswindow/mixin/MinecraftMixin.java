package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedFpsState;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Caps the frame rate while the window is not focused (see
 * UnfocusedFpsState).
 * <p>
 * getFramerateLimit() is the single place runTick asks for the cap, right
 * after updateDisplay(): anything under 260 is handed to
 * RenderSystem.limitDisplayFPS, which sleeps the render thread in
 * glfwWaitEventsTimeout -- so input and focus events are still handled
 * while capped, and the cap lifts on the very next frame after the window
 * regains focus. Vanilla returns 60 here in menus with no world loaded;
 * taking the minimum keeps that, and keeps a regular Max Framerate that is
 * already lower than the unfocused cap.
 * <p>
 * Focus comes from Minecraft.windowActive, which vanilla updates from the
 * GLFW focus callback. Sodium does not touch this path, so the cap works
 * the same with and without it.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public abstract boolean isWindowActive();

    @Inject(method = "getFramerateLimit", at = @At("RETURN"), cancellable = true)
    private void borderlesswindow$limitWhenUnfocused(CallbackInfoReturnable<Integer> cir) {
        if (!UnfocusedFpsState.isEnabled() || this.isWindowActive()) {
            return;
        }

        int limit = UnfocusedFpsState.getLimit();
        if (cir.getReturnValueI() > limit) {
            cir.setReturnValue(limit);
        }
    }
}
