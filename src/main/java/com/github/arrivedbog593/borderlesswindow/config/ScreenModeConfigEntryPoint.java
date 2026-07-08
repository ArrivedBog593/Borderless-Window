package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.ScreenMode;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Entrypoint de la Sodium Config API. Sodium instancia esta clase y llama
 * a registerConfigLate() despues de que el juego arranco.
 *
 * IMPORTANTE - cosas a verificar la primera vez que corras esto:
 *
 * 1. El target de registerOptionReplacement() abajo es
 *    ResourceLocation.fromNamespaceAndPath("sodium", "fullscreen").
 *    Es mi mejor estimacion del ID interno que usa Sodium para su
 *    propia opcion de "Pantalla completa", pero no lo pude confirmar
 *    linea por linea contra el codigo fuente exacto de la version
 *    0.8.12-beta.2. Si al abrir el menu de video ves TU opcion
 *    "Modo de pantalla" apareciendo como una opcion NUEVA en vez de
 *    reemplazar la de Sodium, es que este ID esta mal -- avisame y
 *    lo verificamos juntos (se puede sacar del jar de Sodium con un
 *    simple grep del archivo assets/sodium/lang/en_us.json, buscando
 *    la traduccion de "fullscreen").
 *
 * 2. Si el compilador se queja de "ResourceLocation" o de los paquetes
 *    "net.caffeinemc.mods.sodium.api...", es casi seguro un tema de
 *    coordenadas Maven (ver build.gradle) -- puede que la version
 *    exacta del artefacto para 1.21.1 tenga un sufijo distinto al que
 *    puse ahi.
 */
@ConfigEntryPointForge("borderlesswindow")
public class ScreenModeConfigEntryPoint implements ConfigEntryPoint {

    private final ScreenModeStorage storage = new ScreenModeStorage();
    private final Runnable flushHandler = this.storage::flush;

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerModOptions("sodium")
                .registerOptionReplacement(
                        ResourceLocation.fromNamespaceAndPath("sodium", "fullscreen"),
                        builder.createEnumOption(
                                        ResourceLocation.fromNamespaceAndPath("borderlesswindow", "screen_mode"),
                                        ScreenMode.class)
                                .setName(Component.literal("Modo de pantalla"))
                                .setTooltip(Component.literal(
                                        "Elige entre ventana normal, pantalla completa sin bordes, o el fullscreen normal de Minecraft."))
                                .setElementNameProvider(mode -> Component.literal(mode.getDisplayName()))
                                .setStorageHandler(this.flushHandler)
                                .setBinding(this.storage::setScreenMode, this.storage::getScreenMode)
                                .setDefaultValue(ScreenMode.WINDOWED)
                );
    }
}
