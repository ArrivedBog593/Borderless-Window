package com.github.arrivedbog593.borderlesswindow;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Logica GLFW para los 3 modos de pantalla:
 * <p>
 * - WINDOWED: ventana normal, con bordes, tamaño anterior.
 * - BORDERLESS: ventana sin decoracion, estirada al tamaño exacto del
 *   monitor, pero SIGUE SIENDO una ventana (glfwSetWindowMonitor con
 *   monitor=NULL). Windows nunca cambia la resolucion fisica real,
 *   por eso no hay conflictos de escalado.
 * - FULLSCREEN: fullscreen EXCLUSIVO real de Mojang/GLFW
 *   (glfwSetWindowMonitor con un monitor real). Es el comportamiento
 *   vanilla de toda la vida.
 */
public final class BorderlessHandler {

    private static ScreenMode currentMode = ScreenMode.WINDOWED;

    // A que modo cambia F11 cuando estas en ventana. Configurable desde
    // la opcion "Modo de F11" en el menu de video (seccion Borderless Window).
    private static ScreenMode f11Target = ScreenMode.BORDERLESS;

    // Tamaño/posicion de la ventana en modo WINDOWED, para poder volver a el.
    private static int savedX, savedY, savedWidth, savedHeight;
    private static boolean hasSavedWindowedState = false;

    private BorderlessHandler() {
    }

    public static ScreenMode getCurrentMode() {
        return currentMode;
    }

    public static ScreenMode getF11Target() {
        return f11Target;
    }

    public static void setF11Target(ScreenMode target) {
        // Solo BORDERLESS o FULLSCREEN tienen sentido como destino de F11.
        if (target != ScreenMode.WINDOWED) {
            f11Target = target;
            BorderlessConfigFile.save(currentMode, f11Target);
        }
    }

    /**
     * Se llama una sola vez al arrancar el juego (desde FMLClientSetupEvent).
     * <p>
     * 1. Sincroniza el estado interno con la realidad: si options.txt tenia
     *    fullscreen=true, vanilla ya abrio la ventana en fullscreen exclusivo
     *    ANTES de que corriera nuestro codigo -- lo detectamos preguntandole
     *    a GLFW si la ventana tiene un monitor asignado.
     * 2. Aplica el modo guardado en config/borderlesswindow.json.
     */
    public static void initializeFromConfig(Window window) {
        var config = BorderlessConfigFile.load();
        f11Target = config.f11Mode();

        if (glfwGetWindowMonitor(window.getWindow()) != 0L) {
            // El juego arranco en fullscreen exclusivo por options.txt.
            currentMode = ScreenMode.FULLSCREEN;
        }

        setMode(window, config.screenMode());
    }

    public static void setMode(Window window, ScreenMode mode) {
        if (mode == currentMode) {
            return;
        }

        long handle = window.getWindow();

        // Si veniamos de WINDOWED, guardamos su tamaño/posicion para poder
        // restaurarlo despues, sin importar a que modo nos movamos ahora.
        if (currentMode == ScreenMode.WINDOWED) {
            int[] x = new int[1], y = new int[1], w = new int[1], h = new int[1];
            glfwGetWindowPos(handle, x, y);
            glfwGetWindowSize(handle, w, h);
            savedX = x[0];
            savedY = y[0];
            savedWidth = w[0];
            savedHeight = h[0];
            hasSavedWindowedState = true;
        }

        switch (mode) {
            case WINDOWED -> applyWindowed(handle);
            case BORDERLESS -> applyBorderless(handle);
            case FULLSCREEN -> applyExclusiveFullscreen(handle);
        }

        currentMode = mode;
        BorderlessConfigFile.save(currentMode, f11Target);
    }

    private static void applyWindowed(long handle) {
        glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_TRUE);
        if (hasSavedWindowedState) {
            glfwSetWindowMonitor(handle, 0L, savedX, savedY, savedWidth, savedHeight, GLFW_DONT_CARE);
        } else {
            // No teniamos un estado previo guardado (p. ej. el juego arranco
            // directo en borderless/fullscreen): usamos un tamaño razonable.
            glfwSetWindowMonitor(handle, 0L, 100, 100, 1280, 720, GLFW_DONT_CARE);
        }
    }

    private static void applyBorderless(long handle) {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = glfwGetVideoMode(monitor);
        int[] monitorX = new int[1], monitorY = new int[1];
        glfwGetMonitorPos(monitor, monitorX, monitorY);

        glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_FALSE);

        // TRUCO ANTI-PROMOCION (+1 pixel de alto):
        // Si la ventana sin bordes cubre EXACTAMENTE el monitor, Windows y
        // el driver de la GPU (sobre todo NVIDIA en OpenGL) la "promueven"
        // a un modo de presentacion tipo fullscreen exclusivo (independent
        // flip). Con HDR activado, esa promocion causa el mismo parpadeo
        // negro / cambio de perfil de color que el fullscreen exclusivo --
        // justo lo que este modo intenta evitar. Hacer la ventana 1 pixel
        // mas alta que el monitor evita la deteccion; el pixel extra queda
        // fuera de la pantalla y es invisible. Es el mismo workaround que
        // usan las herramientas de borderless como Special K o Borderless
        // Gaming.
        assert vidMode != null;
        glfwSetWindowMonitor(handle, 0L, monitorX[0], monitorY[0],
                vidMode.width(), vidMode.height() + 1, GLFW_DONT_CARE);
    }

    private static void applyExclusiveFullscreen(long handle) {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidMode = glfwGetVideoMode(monitor);

        // Fullscreen exclusivo real: se le pasa el monitor de verdad.
        // La decoracion no importa aqui, GLFW la ignora en modo exclusivo.
        assert vidMode != null;
        glfwSetWindowMonitor(handle, monitor, 0, 0,
                vidMode.width(), vidMode.height(), vidMode.refreshRate());
    }
}