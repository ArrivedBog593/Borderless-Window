package com.github.arrivedbog593.borderlesswindow.sodium;

import com.github.arrivedbog593.borderlesswindow.camera.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.camera.FovState;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomMode;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelStyle;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.github.arrivedbog593.borderlesswindow.general.F11Mode;
import com.github.arrivedbog593.borderlesswindow.general.ScreenMode;
import com.github.arrivedbog593.borderlesswindow.visual.CloudState;
import com.github.arrivedbog593.borderlesswindow.visual.FogState;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.caffeinemc.mods.sodium.api.config.structure.ModOptionsBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Sodium Config API entry point. Sodium instantiates this class and calls
 * registerConfigLate() after the game has started.
 * <p>
 * The mod registers its own header ("Borderless Window") in the left-hand
 * list of the video settings menu. Options are gathered into a few pages,
 * each split into unnamed option groups -- the same visual grouping
 * Sodium's own General page uses -- instead of one page per feature,
 * which had grown to nine entries in the sidebar:
 * - General: F11 behavior | held item name | menu background blur. Small
 *   features with no better home.
 * - Enchantments: numerals | glint speed and strength.
 * - FPS & Background: FPS overlay | background (unfocused FPS cap, two-stage idle
 *   FPS cap, rendering while minimized, unfocused volume).
 * - Visual Effects: fog | cloud height | sky and weather | screen
 *   overlays. What is drawn over or around the world.
 * - Camera: bobbing | FOV | distortion | zoom. How the view itself moves
 *   and is shaped.
 * - Distances: dropped items, orbs, armor stands | particles.
 * A new feature should become a group on one of these pages; it only
 * earns a page of its own if it brings several options with it.
 * <p>
 * The Screen Mode option itself lives on Sodium's General page, since it
 * REPLACES Sodium's fullscreen checkbox (registerOptionReplacement).
 * <p>
 * The cloud TYPE (Off/Fast/Fancy) is deliberately NOT offered here: that
 * option already exists as vanilla's cloudStatus, shown by Sodium on its
 * own Quality page. We only add the height, which vanilla/Sodium do not
 * expose. The Sodium API cannot insert an option into Sodium's own pages,
 * so the height lives on our page instead.
 * <p>
 * Verified against the actual Sodium mc1.21.1-0.8.12 source code:
 * - The ID of its fullscreen option is "sodium:general.fullscreen".
 * - Replacements are matched by exact ID in Config.java, so a wrong ID
 *   silently does nothing.
 * - IntegerOptionBuilder exposes setRange(min,max,step) and
 *   setValueFormatter(ControlValueFormatter).
 * - ConfigState exposes readBooleanOption / readIntOption /
 *   readEnumOption, and setEnabledProvider takes
 *   Function&lt;ConfigState, Boolean&gt; plus the dependency IDs. Only
 *   DECLARED dependencies may be queried inside the provider -- reading
 *   an undeclared option is documented to crash.
 * <p>
 * HOW SODIUM FINDS IT depends on the loader, so this class carries no
 * registration of its own: on NeoForge a subclass in the neoforge module
 * carries the @ConfigEntryPointForge annotation (it only exists in
 * Sodium's NeoForge API), and on Fabric fabric.mod.json names this class
 * directly.
 */
public class ScreenModeConfigEntryPoint implements ConfigEntryPoint {

    private final ScreenModeStorage storage = new ScreenModeStorage();

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        ModOptionsBuilder options = builder.registerOwnModOptions()
                .setName("Borderless Window");
        // The mod icon beside the name in the page list, in its own colors
        // (setIcon would be a single-color one, tinted by Sodium). Same file
        // as the mod lists' icon. Sodium's first 1.21.11 builds (0.8.0) do
        // not have setNonTintedIcon yet: there the page just has no icon.
        try {
            options.setNonTintedIcon(Identifier.parse("borderlesswindow:icon.png"));
        } catch (LinkageError e) {
            // Older Sodium API without the method.
        }
        options
                // Page 1 - General.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.general"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                Identifier.parse("borderlesswindow:f11_mode"),
                                                F11Mode.class)
                                        .setName(Component.translatable("borderlesswindow.options.f11_mode.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.f11_mode.tooltip"))
                                        .setElementNameProvider(mode -> Component.translatable(mode.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setF11Target, this.storage::getF11Target)
                                        .setDefaultValue(F11Mode.BORDERLESS)
                                )
                        )
                        // HUD: the held item's name over the hotbar.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:held_item_name"))
                                        .setName(Component.translatable("borderlesswindow.options.held_item_name.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.held_item_name.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setHeldItemName, this.storage::getHeldItemName)
                                        .setDefaultValue(true)
                                )
                        )
                        // Vanilla's menu background blur. Like the FOV and
                        // distortion scales, it otherwise lives only in
                        // Minecraft's accessibility menu.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:vanilla_menu_blur"))
                                        .setName(Component.translatable("borderlesswindow.options.vanilla_menu_blur.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.vanilla_menu_blur.tooltip"))
                                        .setRange(0, 10, 1)
                                        .setValueFormatter(value -> value <= 0
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.literal(String.valueOf(value)))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setVanillaMenuBlur, this.storage::getVanillaMenuBlur)
                                        .setDefaultValue(5)
                                )
                        )
                )
                // Page 2 - Enchantments: the numerals in tooltips, and the
                // glint drawn on enchanted items.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.enchantments"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                Identifier.parse("borderlesswindow:enchantment_level_style"),
                                                EnchantmentLevelStyle.class)
                                        .setName(Component.translatable("borderlesswindow.options.enchantment_level_style.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.enchantment_level_style.tooltip"))
                                        .setElementNameProvider(style -> Component.translatable(style.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setEnchantmentLevelStyle,
                                                this.storage::getEnchantmentLevelStyle)
                                        .setDefaultValue(EnchantmentLevelStyle.MIXED)
                                )
                        )
                        // Vanilla's glint sliders, missing from Sodium's
                        // video settings. Strength's own callback
                        // (RenderSystem.setShaderGlintAlpha) runs on set(),
                        // so the change shows without a restart.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:vanilla_glint_speed"))
                                        .setName(Component.translatable("borderlesswindow.options.vanilla_glint_speed.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.vanilla_glint_speed.tooltip"))
                                        .setRange(0, 100, 1)
                                        .setValueFormatter(value -> value <= 0
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setVanillaGlintSpeed, this.storage::getVanillaGlintSpeed)
                                        .setDefaultValue(50)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:vanilla_glint_strength"))
                                        .setName(Component.translatable("borderlesswindow.options.vanilla_glint_strength.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.vanilla_glint_strength.tooltip"))
                                        .setRange(0, 100, 1)
                                        .setValueFormatter(value -> value <= 0
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setVanillaGlintStrength, this.storage::getVanillaGlintStrength)
                                        .setDefaultValue(75)
                                )
                        )
                )
                // Page 3 - FPS & Background: the counter, and everything
                // that changes while the game is not in front.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.fps"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                Identifier.parse("borderlesswindow:fps_overlay_mode"),
                                                FpsOverlayMode.class)
                                        .setName(Component.translatable("borderlesswindow.options.fps_overlay_mode.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fps_overlay_mode.tooltip"))
                                        .setElementNameProvider(mode -> Component.translatable(mode.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFpsOverlayMode, this.storage::getFpsOverlayMode)
                                        .setDefaultValue(FpsOverlayMode.OFF)
                                )
                                .addOption(builder.createEnumOption(
                                                Identifier.parse("borderlesswindow:fps_overlay_position"),
                                                FpsOverlayPosition.class)
                                        .setName(Component.translatable("borderlesswindow.options.fps_overlay_position.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fps_overlay_position.tooltip"))
                                        .setElementNameProvider(position -> Component.translatable(position.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFpsOverlayPosition, this.storage::getFpsOverlayPosition)
                                        .setDefaultValue(FpsOverlayPosition.TOP_LEFT)
                                        .setEnabledProvider(
                                                state -> state.readEnumOption(
                                                        Identifier.parse("borderlesswindow:fps_overlay_mode"),
                                                        FpsOverlayMode.class) != FpsOverlayMode.OFF,
                                                Identifier.parse("borderlesswindow:fps_overlay_mode"))
                                )
                        )
                        // Background. Vanilla 1.21.2+ covers the caps and
                        // the minimized behavior; see UnfocusedFpsState.
                        // Stays available with Dynamic FPS installed: the
                        // two stack, and the lower cap wins.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:unfocused_fps_limit"))
                                        .setName(Component.translatable("borderlesswindow.options.unfocused_fps_limit.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.unfocused_fps_limit.tooltip"))
                                        .setRange(UnfocusedFpsState.OFF_SLOT, UnfocusedFpsState.MAX_LIMIT, UnfocusedFpsState.STEP)
                                        .setValueFormatter(value -> value <= UnfocusedFpsState.OFF_SLOT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.fps_overlay.fps", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setUnfocusedFpsLimit, this.storage::getUnfocusedFpsLimit)
                                        .setDefaultValue(UnfocusedFpsState.OFF_SLOT)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:idle_fps_delay"))
                                        .setName(Component.translatable("borderlesswindow.options.idle_fps_delay.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.idle_fps_delay.tooltip"))
                                        .setRange(IdleFpsState.OFF, IdleFpsState.MAX_DELAY_MINUTES, 1)
                                        .setValueFormatter(IdleFpsState::formatDelay)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setIdleFpsDelay, this.storage::getIdleFpsDelay)
                                        .setDefaultValue(IdleFpsState.OFF)
                                )
                                // Only meaningful once a delay is set.
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:idle_fps_limit"))
                                        .setName(Component.translatable("borderlesswindow.options.idle_fps_limit.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.idle_fps_limit.tooltip"))
                                        .setEnabledProvider(
                                                state -> state.readIntOption(
                                                        Identifier.parse("borderlesswindow:idle_fps_delay")) != IdleFpsState.OFF,
                                                Identifier.parse("borderlesswindow:idle_fps_delay"))
                                        .setRange(UnfocusedFpsState.MIN_LIMIT, UnfocusedFpsState.MAX_LIMIT, 1)
                                        .setValueFormatter(value -> Component.translatable("borderlesswindow.fps_overlay.fps", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setIdleFpsLimit, this.storage::getIdleFpsLimit)
                                        .setDefaultValue(IdleFpsState.DEFAULT_LIMIT)
                                )
                                // Second stage: only on top of the first,
                                // and always later. See SecondIdleDelayRange.
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:idle_fps_second_delay"))
                                        .setName(Component.translatable("borderlesswindow.options.idle_fps_second_delay.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.idle_fps_second_delay.tooltip"))
                                        .setEnabledProvider(
                                                state -> state.readIntOption(
                                                        Identifier.parse("borderlesswindow:idle_fps_delay")) != IdleFpsState.OFF,
                                                Identifier.parse("borderlesswindow:idle_fps_delay"))
                                        .setRangeProvider(
                                                state -> new SecondIdleDelayRange(state.readIntOption(
                                                        Identifier.parse("borderlesswindow:idle_fps_delay"))),
                                                Identifier.parse("borderlesswindow:idle_fps_delay"))
                                        .setValueFormatter(IdleFpsState::formatDelay)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setIdleFpsSecondDelay, this.storage::getIdleFpsSecondDelay)
                                        .setDefaultValue(SecondIdleDelayRange.OFF_VALUE)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:idle_fps_second_limit"))
                                        .setName(Component.translatable("borderlesswindow.options.idle_fps_second_limit.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.idle_fps_second_limit.tooltip"))
                                        .setEnabledProvider(
                                                // "> first" also covers Off (-1, see
                                                // SecondIdleDelayRange) and a second
                                                // delay the first has just caught up with.
                                                state -> {
                                                    int first = state.readIntOption(
                                                            Identifier.parse("borderlesswindow:idle_fps_delay"));
                                                    return first != IdleFpsState.OFF && state.readIntOption(
                                                            Identifier.parse("borderlesswindow:idle_fps_second_delay")) > first;
                                                },
                                                Identifier.parse("borderlesswindow:idle_fps_delay"),
                                                Identifier.parse("borderlesswindow:idle_fps_second_delay"))
                                        .setRange(UnfocusedFpsState.MIN_LIMIT, UnfocusedFpsState.MAX_LIMIT, 1)
                                        .setValueFormatter(value -> Component.translatable("borderlesswindow.fps_overlay.fps", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setIdleFpsSecondLimit, this.storage::getIdleFpsSecondLimit)
                                        .setDefaultValue(IdleFpsState.DEFAULT_SECOND_LIMIT)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:minimized_skip_render"))
                                        .setName(Component.translatable("borderlesswindow.options.minimized_skip_render.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.minimized_skip_render.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setMinimizedSkipRender, this.storage::getMinimizedSkipRender)
                                        .setDefaultValue(false)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:unfocused_volume"))
                                        .setName(Component.translatable("borderlesswindow.options.unfocused_volume.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.unfocused_volume.tooltip"))
                                        .setRange(UnfocusedVolumeState.MIN_PERCENT, UnfocusedVolumeState.MAX_PERCENT, UnfocusedVolumeState.STEP)
                                        .setValueFormatter(value -> value <= UnfocusedVolumeState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.unfocused_volume.muted")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setUnfocusedVolume, this.storage::getUnfocusedVolume)
                                        .setDefaultValue(UnfocusedVolumeState.DEFAULT_PERCENT)
                                )
                        )
                )
                // Page 4 - Visual Effects: fog, clouds, sky and weather,
                // screen overlays.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.visuals"))
                        // Fog: the four per-type toggles.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:fog_terrain"))
                                        .setName(Component.translatable("borderlesswindow.options.fog_terrain.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fog_terrain.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setTerrainFog, this.storage::getTerrainFog)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:fog_water"))
                                        .setName(Component.translatable("borderlesswindow.options.fog_water.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fog_water.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setWaterFog, this.storage::getWaterFog)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:fog_lava"))
                                        .setName(Component.translatable("borderlesswindow.options.fog_lava.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fog_lava.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setLavaFog, this.storage::getLavaFog)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:fog_powder_snow"))
                                        .setName(Component.translatable("borderlesswindow.options.fog_powder_snow.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fog_powder_snow.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setPowderSnowFog, this.storage::getPowderSnowFog)
                                        .setDefaultValue(true)
                                )
                                // Where the terrain fog begins. Greyed out
                                // while the terrain fog is removed: there is
                                // nothing left to move. See FogState.
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:fog_terrain_start"))
                                        .setName(Component.translatable("borderlesswindow.options.fog_terrain_start.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fog_terrain_start.tooltip"))
                                        .setRange(FogState.DEFAULT_START, FogState.MAX_START, FogState.START_STEP)
                                        .setValueFormatter(FogState::formatStart)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFogTerrainStart, this.storage::getFogTerrainStart)
                                        .setDefaultValue(FogState.DEFAULT_START)
                                        .setEnabledProvider(
                                                state -> state.readBooleanOption(
                                                        Identifier.parse("borderlesswindow:fog_terrain")),
                                                Identifier.parse("borderlesswindow:fog_terrain"))
                                )
                        )
                        // Cloud height.
                        // Range is [DEFAULT_SLOT .. MAX_HEIGHT]; the leftmost slot
                        // (just below MIN_HEIGHT) shows "Default" and disables the
                        // override. See CloudState for the encoding.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:cloud_height"))
                                        .setName(Component.translatable("borderlesswindow.options.cloud_height.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.cloud_height.tooltip"))
                                        .setRange(CloudState.DEFAULT_SLOT, CloudState.MAX_HEIGHT, 1)
                                        .setValueFormatter(CloudState::formatSlot)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setCloudHeightSlider,
                                                this.storage::getCloudHeightSlider)
                                        .setDefaultValue(CloudState.DEFAULT_SLOT)
                                )
                        )
                        // Sky and weather: drawing only, the day cycle and
                        // the weather go on. See SkyState.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:sky_sun_moon"))
                                        .setName(Component.translatable("borderlesswindow.options.sky_sun_moon.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.sky_sun_moon.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setSunMoon, this.storage::getSunMoon)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:sky_stars"))
                                        .setName(Component.translatable("borderlesswindow.options.sky_stars.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.sky_stars.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setStars, this.storage::getStars)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:sky_weather"))
                                        .setName(Component.translatable("borderlesswindow.options.sky_weather.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.sky_weather.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setWeather, this.storage::getWeather)
                                        .setDefaultValue(true)
                                )
                        )
                        // Screen overlays: drawn on top of the view.
                        // The block/suffocation overlay is deliberately not exposed;
                        // see ScreenEffectHandler.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:overlay_water"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_water.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_water.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setWaterOverlay, this.storage::getWaterOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:overlay_fire"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_fire.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_fire.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFireOverlay, this.storage::getFireOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:overlay_fire_smart"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_fire_smart.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_fire_smart.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setSmartFire, this.storage::getSmartFire)
                                        .setDefaultValue(false)
                                        // Greyed out when the fire overlay is
                                        // off entirely: there is nothing left
                                        // for it to hide conditionally.
                                        .setEnabledProvider(
                                                state -> state.readBooleanOption(
                                                        Identifier.parse("borderlesswindow:overlay_fire")),
                                                Identifier.parse("borderlesswindow:overlay_fire"))
                                )
                                // From Gui rather than ScreenEffectRenderer
                                // (see GuiMixin), same section regardless:
                                // the player does not care which vanilla
                                // class draws what.
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:overlay_pumpkin"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_pumpkin.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_pumpkin.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setPumpkinOverlay, this.storage::getPumpkinOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:overlay_freeze"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_freeze.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_freeze.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFreezeOverlay, this.storage::getFreezeOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:overlay_portal"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_portal.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_portal.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setPortalOverlay, this.storage::getPortalOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:overlay_spyglass"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_spyglass.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_spyglass.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setSpyglassOverlay, this.storage::getSpyglassOverlay)
                                        .setDefaultValue(true)
                                )
                        )
                )
                // Page 5 - Camera: bobbing, FOV, distortion and zoom, how the
                // view itself moves and is shaped rather than what is drawn
                // over it.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.camera"))
                        // Bobbing. Both options sit UNDER vanilla's own
                        // View Bobbing setting (Sodium shows it on its Quality
                        // page): with that off, GameRenderer never calls bobView
                        // and neither of these has any effect. The Sodium API
                        // cannot add a dependency on a vanilla option, so this is
                        // stated in the tooltips instead of greying them out.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                Identifier.parse("borderlesswindow:camera_bob"),
                                                CameraBobMode.class)
                                        .setName(Component.translatable("borderlesswindow.options.camera_bob.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.camera_bob.tooltip"))
                                        .setElementNameProvider(mode -> Component.translatable(mode.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setCameraBob, this.storage::getCameraBob)
                                        .setDefaultValue(CameraBobMode.FULL)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:hand_bob"))
                                        .setName(Component.translatable("borderlesswindow.options.hand_bob.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.hand_bob.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setHandBob, this.storage::getHandBob)
                                        .setDefaultValue(true)
                                )
                        )
                        // FOV. A separate group from distortion: these change
                        // how wide the view is, not how much it warps. Only the
                        // fluid one sits under Minecraft's FOV Effects slider --
                        // verified against 1.21.1, that option is read in exactly
                        // one place, so sprint/fly/bow are effects vanilla offers
                        // no control over at all. Reuses the distortion
                        // percent/off formatter keys: same 0-100 presentation.
                        .addOptionGroup(builder.createOptionGroup()
                                // Vanilla's own slider, first so the
                                // hierarchy reads top-down: this one
                                // scales everything below it. Sodium's
                                // video settings drop it entirely, and
                                // it only lives in Minecraft's
                                // accessibility menu otherwise.
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:vanilla_fov"))
                                        .setName(Component.translatable("borderlesswindow.options.vanilla_fov.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.vanilla_fov.tooltip"))
                                        .setRange(0, 100, 1)
                                        .setValueFormatter(value -> value <= 0
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setVanillaFov, this.storage::getVanillaFov)
                                        .setDefaultValue(100)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:fov_speed"))
                                        .setName(Component.translatable("borderlesswindow.options.fov_speed.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fov_speed.tooltip"))
                                        .setRange(FovState.MIN_PERCENT, FovState.MAX_PERCENT, 1)
                                        .setValueFormatter(value -> value <= FovState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFovSpeed, this.storage::getFovSpeed)
                                        .setDefaultValue(FovState.DEFAULT_PERCENT)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:fov_flying"))
                                        .setName(Component.translatable("borderlesswindow.options.fov_flying.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fov_flying.tooltip"))
                                        .setRange(FovState.MIN_PERCENT, FovState.MAX_PERCENT, 1)
                                        .setValueFormatter(value -> value <= FovState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFovFlying, this.storage::getFovFlying)
                                        .setDefaultValue(FovState.DEFAULT_PERCENT)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:fov_bow"))
                                        .setName(Component.translatable("borderlesswindow.options.fov_bow.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fov_bow.tooltip"))
                                        .setRange(FovState.MIN_PERCENT, FovState.MAX_PERCENT, 1)
                                        .setValueFormatter(value -> value <= FovState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFovBow, this.storage::getFovBow)
                                        .setDefaultValue(FovState.DEFAULT_PERCENT)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:fov_fluid"))
                                        .setName(Component.translatable("borderlesswindow.options.fov_fluid.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fov_fluid.tooltip"))
                                        .setRange(FovState.MIN_PERCENT, FovState.MAX_PERCENT, 1)
                                        .setValueFormatter(value -> value <= FovState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFovFluid, this.storage::getFovFluid)
                                        .setDefaultValue(FovState.DEFAULT_PERCENT)
                                )
                        )
                        // Distortion: warps the camera instead of drawing
                        // on top of it. Both options multiply
                        // Minecraft's own Distortion Effects slider rather than
                        // replacing it; the Sodium API cannot depend on a vanilla
                        // option, so the tooltips say so.
                        .addOptionGroup(builder.createOptionGroup()
                                // Vanilla's own slider, first so the
                                // hierarchy reads top-down: this one
                                // scales everything below it. Sodium's
                                // video settings drop it entirely, and
                                // it only lives in Minecraft's
                                // accessibility menu otherwise.
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:vanilla_distortion"))
                                        .setName(Component.translatable("borderlesswindow.options.vanilla_distortion.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.vanilla_distortion.tooltip"))
                                        .setRange(0, 100, 1)
                                        .setValueFormatter(value -> value <= 0
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setVanillaDistortion, this.storage::getVanillaDistortion)
                                        .setDefaultValue(100)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:distortion_portal"))
                                        .setName(Component.translatable("borderlesswindow.options.distortion_portal.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distortion_portal.tooltip"))
                                        .setRange(DistortionState.MIN_PERCENT, DistortionState.MAX_PERCENT, 1)
                                        .setValueFormatter(value -> value <= DistortionState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistortionPortal, this.storage::getDistortionPortal)
                                        .setDefaultValue(DistortionState.DEFAULT_PERCENT)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:distortion_nausea"))
                                        .setName(Component.translatable("borderlesswindow.options.distortion_nausea.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distortion_nausea.tooltip"))
                                        .setRange(DistortionState.MIN_PERCENT, DistortionState.MAX_PERCENT, 1)
                                        .setValueFormatter(value -> value <= DistortionState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistortionNausea, this.storage::getDistortionNausea)
                                        .setDefaultValue(DistortionState.DEFAULT_PERCENT)
                                )
                        )
                        // Zoom. The key is in Minecraft's Controls, like
                        // every other key; nothing here does anything until
                        // it is pressed. See ZoomState.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                Identifier.parse("borderlesswindow:zoom_mode"),
                                                ZoomMode.class)
                                        .setName(Component.translatable("borderlesswindow.options.zoom_mode.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.zoom_mode.tooltip"))
                                        .setElementNameProvider(mode -> Component.translatable(mode.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setZoomMode, this.storage::getZoomMode)
                                        .setDefaultValue(ZoomMode.HOLD)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:zoom_level"))
                                        .setName(Component.translatable("borderlesswindow.options.zoom_level.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.zoom_level.tooltip"))
                                        .setRange(ZoomState.MIN_LEVEL, ZoomState.MAX_LEVEL, 1)
                                        .setValueFormatter(value -> Component.translatable(
                                                "borderlesswindow.options.zoom_level.value", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setZoomLevel, this.storage::getZoomLevel)
                                        .setDefaultValue(ZoomState.DEFAULT_LEVEL)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:zoom_scroll"))
                                        .setName(Component.translatable("borderlesswindow.options.zoom_scroll.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.zoom_scroll.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setZoomScroll, this.storage::getZoomScroll)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:zoom_smooth"))
                                        .setName(Component.translatable("borderlesswindow.options.zoom_smooth.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.zoom_smooth.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setZoomSmooth, this.storage::getZoomSmooth)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:zoom_hide_hand"))
                                        .setName(Component.translatable("borderlesswindow.options.zoom_hide_hand.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.zoom_hide_hand.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setZoomHideHand, this.storage::getZoomHideHand)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                Identifier.parse("borderlesswindow:zoom_hide_hud"))
                                        .setName(Component.translatable("borderlesswindow.options.zoom_hide_hud.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.zoom_hide_hud.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setZoomHideHud, this.storage::getZoomHideHud)
                                        .setDefaultValue(false)
                                )
                        )
                )
                // Page 6 - Distances: how far things are drawn. Its own
                // page because it brings four options and none of them
                // fits the others' themes. Every slider starts at
                // "Default" (no cap) on the left, like the cloud height,
                // and can only lower vanilla's own distance.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.distances"))
                        // Entities: drawing only, see EntityRenderDispatcherMixin.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:distance_items"))
                                        .setName(Component.translatable("borderlesswindow.options.distance_items.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distance_items.tooltip"))
                                        .setRange(DistanceState.DEFAULT, DistanceState.ITEM_MAX, DistanceState.ITEM_STEP)
                                        .setValueFormatter(DistanceState::format)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistanceItems, this.storage::getDistanceItems)
                                        .setDefaultValue(DistanceState.DEFAULT)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:distance_xp_orbs"))
                                        .setName(Component.translatable("borderlesswindow.options.distance_xp_orbs.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distance_xp_orbs.tooltip"))
                                        .setRange(DistanceState.DEFAULT, DistanceState.ORB_MAX, DistanceState.ORB_STEP)
                                        .setValueFormatter(DistanceState::format)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistanceOrbs, this.storage::getDistanceOrbs)
                                        .setDefaultValue(DistanceState.DEFAULT)
                                )
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:distance_armor_stands"))
                                        .setName(Component.translatable("borderlesswindow.options.distance_armor_stands.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distance_armor_stands.tooltip"))
                                        .setRange(DistanceState.DEFAULT, DistanceState.ARMOR_STAND_MAX, DistanceState.ARMOR_STAND_STEP)
                                        .setValueFormatter(DistanceState::format)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistanceArmorStands, this.storage::getDistanceArmorStands)
                                        .setDefaultValue(DistanceState.DEFAULT)
                                )
                        )
                        // Particles: refused at spawn, see ClientLevelMixin.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(
                                                Identifier.parse("borderlesswindow:distance_particles"))
                                        .setName(Component.translatable("borderlesswindow.options.distance_particles.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distance_particles.tooltip"))
                                        .setRange(DistanceState.DEFAULT, DistanceState.PARTICLE_MAX, DistanceState.PARTICLE_STEP)
                                        .setValueFormatter(DistanceState::format)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistanceParticles, this.storage::getDistanceParticles)
                                        .setDefaultValue(DistanceState.DEFAULT)
                                )
                        )
                )
                .registerOptionReplacement(
                        Identifier.parse("sodium:general.fullscreen"),
                        builder.createEnumOption(
                                        Identifier.parse("borderlesswindow:screen_mode"),
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
                        Identifier.parse("sodium:general.fullscreen_resolution"),
                        builder.createIntegerOption(
                                        Identifier.parse("sodium:general.fullscreen_resolution"))
                                .setEnabledProvider(
                                        state -> state.readEnumOption(
                                                Identifier.parse("borderlesswindow:screen_mode"),
                                                ScreenMode.class) == ScreenMode.FULLSCREEN,
                                        Identifier.parse("borderlesswindow:screen_mode"))
                );
    }
}