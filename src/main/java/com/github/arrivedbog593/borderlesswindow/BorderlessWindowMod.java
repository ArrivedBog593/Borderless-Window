package com.github.arrivedbog593.borderlesswindow;

import net.neoforged.fml.common.Mod;

/**
 * Mod client-only que reemplaza el fullscreen exclusivo de Minecraft
 * por un fullscreen borderless (sin bordes). No agrega nada mas.
 *
 * Toda la logica real vive en WindowMixin + BorderlessHandler.
 * Esta clase solo existe porque NeoForge exige un punto de entrada @Mod.
 */
@Mod(BorderlessWindowMod.MODID)
public class BorderlessWindowMod {

    public static final String MODID = "borderlesswindow";

    public BorderlessWindowMod() {
        // No hace falta inicializar nada: el mixin se engancha solo
        // a com.mojang.blaze3d.platform.Window en cuanto el juego arranca.
    }
}
