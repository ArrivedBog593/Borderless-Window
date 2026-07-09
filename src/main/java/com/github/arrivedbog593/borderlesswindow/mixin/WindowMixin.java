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
 * F11 alterna entre Ventana y el modo configurado en "Modo de F11"
 * (Sin bordes o Pantalla completa, seccion Borderless Window del menu
 * de video). Los 3 modos tambien se pueden elegir directo desde la
 * opcion "Modo de pantalla" que reemplaza a la de Sodium.
 * <p>
 * isFullscreen() se sobreescribe para que options.txt guarde correctamente
 * si el estado actual es distinto de WINDOWED, sin importar cual de los
 * dos modos "no ventana" este activo.
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

        // Si el menu de video de Sodium esta abierto en este momento,
        // refrescamos sus controles para que "Modo de pantalla" refleje
        // el cambio hecho con F11 en vivo.
        SodiumMenuRefresher.refreshIfSodiumMenuOpen();
    }

    @Inject(method = "isFullscreen", at = @At("RETURN"), cancellable = true)
    private void borderlesswindow$isFullscreen(CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(BorderlessHandler.getCurrentMode() != ScreenMode.WINDOWED);
    }
}