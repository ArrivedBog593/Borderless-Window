package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.ScreenMode;
import com.mojang.blaze3d.platform.Window;
import com.github.arrivedbog593.borderlesswindow.BorderlessHandler;
import net.minecraft.client.Minecraft;

/**
 * La Sodium Config API es solo una capa de presentacion: no guarda nada
 * por si sola. Esta clase es el "storage" minimo que le pedimos: cuando
 * el usuario cambia la opcion en el menu, aplicamos el cambio real a la
 * ventana inmediatamente a traves de BorderlessHandler.
 * <p>
 * No hace falta persistir el valor en un archivo aparte: como el mixin
 * WindowMixin ya sobreescribe isFullscreen(), Minecraft sigue guardando
 * el estado "es fullscreen si/no" en options.txt igual que antes. El
 * *tipo* de modo (borderless vs. fullscreen real) sí se pierde entre
 * sesiones con esta implementacion minima -- si quieres que también se
 * recuerde cuál de los dos eligió la última vez, se puede agregar un
 * pequeño archivo de config propio más adelante.
 */
public final class ScreenModeStorage {

    private ScreenMode pendingValue = BorderlessHandler.getCurrentMode();
    private boolean screenModeDirty = false;

    private ScreenMode pendingF11Target = BorderlessHandler.getF11Target();
    private boolean f11TargetDirty = false;

    public ScreenMode getScreenMode() {
        return BorderlessHandler.getCurrentMode();
    }

    public void setScreenMode(ScreenMode mode) {
        this.pendingValue = mode;
        this.screenModeDirty = true;
    }

    public ScreenMode getF11Target() {
        return BorderlessHandler.getF11Target();
    }

    public void setF11Target(ScreenMode target) {
        this.pendingF11Target = target;
        this.f11TargetDirty = true;
    }

    /**
     * Se llama cuando Sodium aplica los cambios (setStorageHandler).
     * <p>
     * IMPORTANTE: solo aplicamos lo que el usuario cambio DE VERDAD en el
     * menu (flags dirty). Antes, flush() aplicaba pendingValue siempre --
     * y como pendingValue solo se actualiza al tocar la opcion en el menu,
     * aplicar cualquier otra opcion despues de usar F11 revertia el modo
     * al valor viejo cacheado.
     */
    public void flush() {
        if (this.f11TargetDirty) {
            BorderlessHandler.setF11Target(this.pendingF11Target);
            this.f11TargetDirty = false;
        }

        if (this.screenModeDirty) {
            Window window = Minecraft.getInstance().getWindow();
            BorderlessHandler.setMode(window, this.pendingValue);
            this.screenModeDirty = false;
        }
    }
}