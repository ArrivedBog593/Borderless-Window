package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.ScreenMode;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * Entrypoint de la Sodium Config API. Sodium instancia esta clase y llama
 * a registerConfigLate() despues de que el juego arranco.
 * <p>
 * Verificado contra el codigo fuente real de Sodium mc1.21.1-0.8.12-beta.2:
 * - El ID de su opcion de pantalla completa es "sodium:general.fullscreen"
 *   (definido en SodiumConfigBuilder.java linea 170).
 * - El reemplazo se matchea por ID exacto en Config.java (overrides.get(option.id)),
 *   por eso con un ID incorrecto simplemente no pasa nada, sin error.
 * - registerOwnModOptions() usa el mod id del @ConfigEntryPointForge.
 */
@SuppressWarnings("unused")
@ConfigEntryPointForge("borderlesswindow")
public class ScreenModeConfigEntryPoint implements ConfigEntryPoint {

    private final ScreenModeStorage storage = new ScreenModeStorage();

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setName("Borderless Window")
                // Pagina propia del mod, aparece con su encabezado en la lista
                // izquierda del menu de video. Contiene la opcion "Modo de F11".
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.pages.general"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                ResourceLocation.parse("borderlesswindow:f11_mode"),
                                                ScreenMode.class)
                                        .setName(Component.translatable("borderlesswindow.options.f11_mode.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.f11_mode.tooltip"))
                                        .setElementNameProvider(mode -> Component.translatable(mode.getTranslationKey()))
                                        // WINDOWED no tiene sentido como destino de F11
                                        .setAllowedValues(Set.of(ScreenMode.BORDERLESS, ScreenMode.FULLSCREEN))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setF11Target, this.storage::getF11Target)
                                        .setDefaultValue(ScreenMode.BORDERLESS)
                                )
                        )
                )
                .registerOptionReplacement(
                        ResourceLocation.parse("sodium:general.fullscreen"),
                        builder.createEnumOption(
                                        ResourceLocation.parse("borderlesswindow:screen_mode"),
                                        ScreenMode.class)
                                .setName(Component.translatable("borderlesswindow.options.screen_mode.name"))
                                .setTooltip(Component.translatable("borderlesswindow.options.screen_mode.tooltip"))
                                .setElementNameProvider(mode -> Component.translatable(mode.getTranslationKey()))
                                .setStorageHandler(this.storage::flush)
                                .setBinding(this.storage::setScreenMode, this.storage::getScreenMode)
                                .setDefaultValue(ScreenMode.WINDOWED)
                )
                // La opcion "Resolucion en pantalla completa" de Sodium dependia
                // de "sodium:general.fullscreen" (que nuestro reemplazo elimino),
                // y eso crasheaba la validacion de dependencias. Este overlay
                // reemplaza SOLO su condicion de habilitado: ahora depende de
                // nuestra opcion enum y se activa unicamente en modo Pantalla
                // completa (el unico modo donde la resolucion exclusiva aplica).
                // Nombre, binding, formatter, etc. se heredan del original.
                .registerOptionOverlay(
                        ResourceLocation.parse("sodium:general.fullscreen_resolution"),
                        builder.createIntegerOption(
                                        ResourceLocation.parse("sodium:general.fullscreen_resolution"))
                                .setEnabledProvider(
                                        state -> state.readEnumOption(
                                                ResourceLocation.parse("borderlesswindow:screen_mode"),
                                                ScreenMode.class) == ScreenMode.FULLSCREEN,
                                        ResourceLocation.parse("borderlesswindow:screen_mode"))
                );
    }
}