package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.BorderlessHandler;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Intercepta por completo el comportamiento de F11 (toggleFullScreen) y el
 * getter isFullscreen(). Al sobreescribir isFullscreen() tambien, el propio
 * Minecraft guarda el estado correcto en options.txt al cerrar el juego,
 * asi que el borderless se recuerda solo entre sesiones sin archivos
 * de configuracion aparte.
 */
@Mixin(Window.class)
public abstract class WindowMixin {

    @Unique
    private boolean borderlesswindow$enabled = false;

    @Inject(method = "toggleFullScreen", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$onToggle(CallbackInfo ci) {
        ci.cancel(); // cancelamos el fullscreen exclusivo original de Mojang
        borderlesswindow$enabled = !borderlesswindow$enabled;
        BorderlessHandler.apply((Window) (Object) this, borderlesswindow$enabled);
    }

    @Inject(method = "isFullscreen", at = @At("RETURN"), cancellable = true)
    private void borderlesswindow$isFullscreen(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(borderlesswindow$enabled);
    }
}
