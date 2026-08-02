package com.github.arrivedbog593.borderlesswindow.sodium;

import com.github.arrivedbog593.borderlesswindow.bobbing.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.cloud.CloudState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelStyle;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.window.F11Mode;
import com.github.arrivedbog593.borderlesswindow.window.ScreenMode;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Sodium Config API entry point. Sodium instantiates this class and calls
 * registerConfigLate() after the game has started.
 * <p>
 * The mod registers its own header ("Borderless Window") in the left-hand
 * list of the video settings menu, with one page per feature:
 * - General: F11 behavior.
 * - FPS Overlay: overlay mode + position.
 * - Fog: the four per-type fog toggles.
 * - Clouds: the cloud height slider.
 * - Screen Overlays: water / fire / smart fire.
 * - Bobbing: camera bob mode + hand bob.
 * - Interface: enchantment numerals.
 * The Screen Mode option itself lives on Sodium's General page, since it
 * REPLACES Sodium's fullscreen checkbox (registerOptionReplacement).
 * <p>
 * The cloud TYPE (Off/Fast/Fancy) is deliberately NOT offered here: that
 * option already exists as vanilla's cloudStatus, shown by Sodium on its
 * own Quality page. We only add the height, which vanilla/Sodium does not
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
 */
@SuppressWarnings("unused")
@ConfigEntryPointForge("borderlesswindow")
public class ScreenModeConfigEntryPoint implements ConfigEntryPoint {

    private final ScreenModeStorage storage = new ScreenModeStorage();

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setName("Borderless Window")
                // Page 1 - General: F11 behavior.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.general"))
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
                )
                // Page 2 - FPS Overlay: mode + position.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.fps"))
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
                                        .setEnabledProvider(
                                                state -> state.readEnumOption(
                                                        ResourceLocation.parse("borderlesswindow:fps_overlay_mode"),
                                                        FpsOverlayMode.class) != FpsOverlayMode.OFF,
                                                ResourceLocation.parse("borderlesswindow:fps_overlay_mode"))
                                )
                        )
                )
                // Page 3 - Fog: the four per-type toggles.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.fog"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:fog_terrain"))
                                        .setName(Component.translatable("borderlesswindow.options.fog_terrain.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fog_terrain.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setTerrainFog, this.storage::getTerrainFog)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:fog_water"))
                                        .setName(Component.translatable("borderlesswindow.options.fog_water.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fog_water.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setWaterFog, this.storage::getWaterFog)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:fog_lava"))
                                        .setName(Component.translatable("borderlesswindow.options.fog_lava.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fog_lava.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setLavaFog, this.storage::getLavaFog)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:fog_powder_snow"))
                                        .setName(Component.translatable("borderlesswindow.options.fog_powder_snow.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fog_powder_snow.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setPowderSnowFog, this.storage::getPowderSnowFog)
                                        .setDefaultValue(true)
                                )
                        )
                )
                // Page 4 - Clouds: the cloud height slider.
                // Range is [DEFAULT_SLOT .. MAX_HEIGHT]; the leftmost slot
                // (just below MIN_HEIGHT) shows "Default" and disables the
                // override. See ScreenModeStorage for the encoding.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.clouds"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:cloud_height"))
                                        .setName(Component.translatable("borderlesswindow.options.cloud_height.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.cloud_height.tooltip"))
                                        .setRange(ScreenModeStorage.SODIUM_CLOUD_DEFAULT_SLOT,
                                                CloudState.MAX_HEIGHT, 1)
                                        .setValueFormatter(value -> {
                                            if (value <= ScreenModeStorage.SODIUM_CLOUD_DEFAULT_SLOT) {
                                                return Component.translatable("borderlesswindow.options.cloud_height.default");
                                            }
                                            if (value == CloudState.VANILLA_HEIGHT) {
                                                return Component.translatable("borderlesswindow.options.cloud_height.regular", value);
                                            }
                                            return Component.literal(String.valueOf(value));
                                        })
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setCloudHeightSlider,
                                                this.storage::getCloudHeightSlider)
                                        .setDefaultValue(ScreenModeStorage.SODIUM_CLOUD_DEFAULT_SLOT)
                                )
                        )
                )
                // Page 5 - Screen Overlays: the water and fire view overlays.
                // The block/suffocation overlay is deliberately not exposed;
                // see ScreenEffectHandler.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.overlays"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:overlay_water"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_water.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_water.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setWaterOverlay, this.storage::getWaterOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:overlay_fire"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_fire.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_fire.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFireOverlay, this.storage::getFireOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:overlay_fire_smart"))
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
                                                        ResourceLocation.parse("borderlesswindow:overlay_fire")),
                                                ResourceLocation.parse("borderlesswindow:overlay_fire"))
                                )
                        )
                )
                // Page 6 - Bobbing. Both options sit UNDER vanilla's own
                // View Bobbing setting (Sodium shows it on its Quality
                // page): with that off, GameRenderer never calls bobView
                // and neither of these has any effect. The Sodium API
                // cannot add a dependency on a vanilla option, so this is
                // stated in the tooltips instead of greying them out.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.bobbing"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                ResourceLocation.parse("borderlesswindow:camera_bob"),
                                                CameraBobMode.class)
                                        .setName(Component.translatable("borderlesswindow.options.camera_bob.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.camera_bob.tooltip"))
                                        .setElementNameProvider(mode -> Component.translatable(mode.getTranslationKey()))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setCameraBob, this.storage::getCameraBob)
                                        .setDefaultValue(CameraBobMode.FULL)
                                )
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:hand_bob"))
                                        .setName(Component.translatable("borderlesswindow.options.hand_bob.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.hand_bob.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setHandBob, this.storage::getHandBob)
                                        .setDefaultValue(true)
                                )
                        )
                )
                // Page 7 - Interface. The first page here that is NOT about
                // rendering: enchantment numerals are tooltip text. It
                // lives in the video settings menu anyway because splitting
                // the mod's options across two different menus would be
                // worse for the user than the category being loose. Named
                // "Interface" rather than "Enchantments" so future
                // tooltip/HUD text options have somewhere to land.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.enchantments"))
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createEnumOption(
                                                ResourceLocation.parse("borderlesswindow:enchantment_level_style"),
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