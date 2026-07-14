package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.F11Mode;
import com.github.arrivedbog593.borderlesswindow.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.ScreenMode;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Sodium Config API entry point. Sodium instantiates this class and calls
 * registerConfigLate() after the game has started.
 * <p>
 * Verified against the actual Sodium mc1.21.1-0.8.12 source code:
 * - The ID of its fullscreen option is "sodium:general.fullscreen"
 *   (defined in SodiumConfigBuilder.java).
 * - Replacements are matched by exact ID in Config.java
 *   (overrides.get(option.id)), so a wrong ID silently does nothing.
 * - registerOwnModOptions() uses the mod id from @ConfigEntryPointForge.
 */
@SuppressWarnings("unused")
@ConfigEntryPointForge("borderlesswindow")
public class ScreenModeConfigEntryPoint implements ConfigEntryPoint {

    private final ScreenModeStorage storage = new ScreenModeStorage();

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setName("Borderless Window")
                // The mod's own page, shown with its header in the left-hand
                // list of the video settings menu. First group: F11 behavior.
                // Second group: the FPS overlay options.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.pages.general"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                ResourceLocation.parse("borderlesswindow:f11_mode"),
                                                F11Mode.class)
                                        .setName(Component.translatable("borderlesswindow.options.f11_mode.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.f11_mode.tooltip"))
                                        .setElementNameProvider(mode -> Component.translatable(mode.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setF11Target, this.storage::getF11Target)
                                        .setDefaultValue(F11Mode.BORDERLESS)
                                )
                        )
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                ResourceLocation.parse("borderlesswindow:fps_overlay_mode"),
                                                FpsOverlayMode.class)
                                        .setName(Component.translatable("borderlesswindow.options.fps_overlay_mode.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fps_overlay_mode.tooltip"))
                                        .setElementNameProvider(mode -> Component.translatable(mode.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFpsOverlayMode, this.storage::getFpsOverlayMode)
                                        .setDefaultValue(FpsOverlayMode.OFF)
                                )
                                .addOption(builder.createEnumOption(
                                                ResourceLocation.parse("borderlesswindow:fps_overlay_position"),
                                                FpsOverlayPosition.class)
                                        .setName(Component.translatable("borderlesswindow.options.fps_overlay_position.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fps_overlay_position.tooltip"))
                                        .setElementNameProvider(position -> Component.translatable(position.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFpsOverlayPosition, this.storage::getFpsOverlayPosition)
                                        .setDefaultValue(FpsOverlayPosition.TOP_LEFT)
                                        // Greyed out while the overlay is off: the
                                        // position only matters when something is drawn.
                                        .setEnabledProvider(
                                                state -> state.readEnumOption(
                                                        ResourceLocation.parse("borderlesswindow:fps_overlay_mode"),
                                                        FpsOverlayMode.class) != FpsOverlayMode.OFF,
                                                ResourceLocation.parse("borderlesswindow:fps_overlay_mode"))
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
                // Sodium's "Fullscreen Resolution" option used to depend on
                // "sodium:general.fullscreen" (which our replacement removed),
                // and that crashed the dependency validation. This overlay
                // replaces ONLY its enabled condition: it now depends on our
                // enum option and is enabled solely in exclusive Fullscreen
                // mode (the only mode where switching the display resolution
                // applies). Name, binding, formatter, etc. are inherited from
                // the original option.
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
