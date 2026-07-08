package com.github.arrivedbog593.borderlesswindow;

import com.mojang.blaze3d.platform.Window;
import org.lwjgl.glfw.GLFWVidMode;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Logica pura de GLFW para activar/desactivar borderless fullscreen.
 *
 * Truco: en vez de fullscreen exclusivo (glfwSetWindowMonitor con un monitor
 * real, que cambia la resolucion fisica del monitor y es lo que rompe tu
 * config actual), usamos glfwSetWindowMonitor con monitor=NULL (modo
 * ventana) pero con el tamaño y posicion EXACTOS del monitor, y quitamos
 * la decoracion de la ventana. Resultado: se ve como fullscreen pero
 * tecnicamente sigue siendo una ventana, por lo que Windows nunca
 * cambia la resolucion real y no hay conflictos de escalado.
 */
public final class BorderlessHandler {

    // Guardamos el tamaño/posicion de la ventana normal para restaurarla
    // al salir de borderless.
    private static int savedX, savedY, savedWidth, savedHeight;

    private BorderlessHandler() {
    }

    public static void apply(Window window, boolean enable) {
        long handle = window.getWindow();

        if (enable) {
            int[] x = new int[1], y = new int[1], w = new int[1], h = new int[1];
            glfwGetWindowPos(handle, x, y);
            glfwGetWindowSize(handle, w, h);
            savedX = x[0];
            savedY = y[0];
            savedWidth = w[0];
            savedHeight = h[0];

            long monitor = glfwGetPrimaryMonitor();
            GLFWVidMode vidMode = glfwGetVideoMode(monitor);
            int[] monitorX = new int[1], monitorY = new int[1];
            glfwGetMonitorPos(monitor, monitorX, monitorY);

            glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_FALSE);
            glfwSetWindowMonitor(handle, 0L, monitorX[0], monitorY[0],
                    vidMode.width(), vidMode.height(), GLFW_DONT_CARE);
        } else {
            glfwSetWindowAttrib(handle, GLFW_DECORATED, GLFW_TRUE);
            glfwSetWindowMonitor(handle, 0L, savedX, savedY,
                    savedWidth, savedHeight, GLFW_DONT_CARE);
        }
    }
}
