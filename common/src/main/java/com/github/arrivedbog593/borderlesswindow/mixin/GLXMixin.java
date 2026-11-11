package com.github.arrivedbog593.borderlesswindow.mixin;

import com.mojang.blaze3d.platform.GLX;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Makes the Fullscreen screen mode always exclusive.
 * <p>
 * Since 26.1 Minecraft has an Exclusive Fullscreen option, off by
 * default. It only sets one GLFW hint, GLFW_SOFT_FULLSCREEN, here in
 * _initGlfw. That hint exists only in LWJGL's own GLFW fork, and on
 * Windows all it does is make a fullscreen window 1 px taller than the
 * monitor -- the same trick BorderlessHandler uses for Borderless -- so
 * the driver does not take it over as exclusive. The window is still on
 * top of everything and still minimizes when it loses focus: a worse
 * Borderless. Sodium turns the option on at first launch (except with a
 * Chinese, Japanese or Korean input method), so without this the same
 * Fullscreen mode would behave differently with and without Sodium.
 * <p>
 * The fork keeps the hint library-wide and reads it each time it fits a
 * window to a monitor, not only when one is created, so setting it once
 * here covers every switch to Fullscreen. The vanilla option is hidden
 * from the video settings (VideoSettingsScreenMixin); the Screen Mode
 * selector already offers both kinds of fullscreen.
 * <p>
 * The only glfwWindowHint call in the method.
 */
@Mixin(GLX.class)
public abstract class GLXMixin {

    @ModifyArg(
            method = "_initGlfw",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwWindowHint(II)V"),
            index = 1)
    private static int borderlesswindow$exclusiveFullscreen(int softFullscreen) {
        return GLFW.GLFW_FALSE;
    }
}
