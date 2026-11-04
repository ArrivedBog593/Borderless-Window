package com.github.arrivedbog593.borderlesswindow.sodium;

import com.github.arrivedbog593.borderlesswindow.bobbing.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.cloud.CloudState;
import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import com.github.arrivedbog593.borderlesswindow.distortion.DistortionState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelStyle;
import com.github.arrivedbog593.borderlesswindow.fov.FovState;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedFpsState;
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
 * list of the video settings menu. Options are gathered into a few pages,
 * each split into unnamed option groups -- the same visual grouping
 * Sodium's own General page uses -- instead of one page per feature,
 * which had grown to nine entries in the sidebar:
 * - General: F11 behavior | FPS overlay + unfocused FPS cap |
 *   enchantment numerals. Small features with no better home.
 * - Visual Effects: fog | cloud height | screen overlays. What is drawn
 *   over or around the world.
 * - Camera: bobbing | FOV | distortion. How the view itself moves and
 *   is shaped.
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
 */
@SuppressWarnings("unused")
@ConfigEntryPointForge("borderlesswindow")
public class ScreenModeConfigEntryPoint implements ConfigEntryPoint {

    private final ScreenModeStorage storage = new ScreenModeStorage();

    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        builder.registerOwnModOptions()
                .setName("Borderless Window")
                // Page 1 - General.
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
                        // FPS: the overlay and the unfocused cap.
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
                                // Lives with the overlay because both are
                                // about frame rate. Vanilla 1.21.2+ has this
                                // built in; see UnfocusedFpsState.
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:unfocused_fps_limit"))
                                        .setName(Component.translatable("borderlesswindow.options.unfocused_fps_limit.name"))
                                        .setTooltip(Component.translatable(UnfocusedFpsState.getTooltipKey()))
                                        .setEnabled(!UnfocusedFpsState.isDeferredToOtherMod())
                                        .setRange(UnfocusedFpsState.OFF, UnfocusedFpsState.MAX_LIMIT, UnfocusedFpsState.STEP)
                                        .setValueFormatter(value -> value <= UnfocusedFpsState.OFF
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.fps_overlay.fps", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setUnfocusedFpsLimit, this.storage::getUnfocusedFpsLimit)
                                        .setDefaultValue(UnfocusedFpsState.OFF)
                                )
                        )
                        // Enchantment numerals: tooltip text, not rendering.
                        // It lives in the video settings menu anyway because
                        // splitting the mod's options across two different
                        // menus would be worse for the user than the
                        // category being loose.
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
                // Page 2 - Visual Effects: fog, clouds, screen overlays.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.visuals"))
                        // Fog: the four per-type toggles.
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
                        // Cloud height.
                        // Range is [DEFAULT_SLOT .. MAX_HEIGHT]; the leftmost slot
                        // (just below MIN_HEIGHT) shows "Default" and disables the
                        // override. See ScreenModeStorage for the encoding.
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
                        // Screen overlays: drawn on top of the view.
                        // The block/suffocation overlay is deliberately not exposed;
                        // see ScreenEffectHandler.
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
                                // From Gui rather than ScreenEffectRenderer
                                // (see GuiMixin), same section regardless:
                                // the player does not care which vanilla
                                // class draws what.
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:overlay_pumpkin"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_pumpkin.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_pumpkin.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setPumpkinOverlay, this.storage::getPumpkinOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:overlay_freeze"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_freeze.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_freeze.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFreezeOverlay, this.storage::getFreezeOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:overlay_portal"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_portal.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_portal.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setPortalOverlay, this.storage::getPortalOverlay)
                                        .setDefaultValue(true)
                                )
                                .addOption(builder.createBooleanOption(
                                                ResourceLocation.parse("borderlesswindow:overlay_spyglass"))
                                        .setName(Component.translatable("borderlesswindow.options.overlay_spyglass.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.overlay_spyglass.tooltip"))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setSpyglassOverlay, this.storage::getSpyglassOverlay)
                                        .setDefaultValue(true)
                                )
                        )
                )
                // Page 3 - Camera: bobbing, FOV and distortion, how the view itself
                // moves and is shaped rather than what is drawn over it.
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
                                                ResourceLocation.parse("borderlesswindow:vanilla_fov"))
                                        .setName(Component.translatable("borderlesswindow.options.vanilla_fov.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.vanilla_fov.tooltip"))
                                        .setRange(0, 100, 5)
                                        .setValueFormatter(value -> value <= 0
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setVanillaFov, this.storage::getVanillaFov)
                                        .setDefaultValue(100)
                                )
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:fov_speed"))
                                        .setName(Component.translatable("borderlesswindow.options.fov_speed.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fov_speed.tooltip"))
                                        .setRange(FovState.MIN_PERCENT, FovState.MAX_PERCENT, 5)
                                        .setValueFormatter(value -> value <= FovState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFovSpeed, this.storage::getFovSpeed)
                                        .setDefaultValue(FovState.DEFAULT_PERCENT)
                                )
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:fov_flying"))
                                        .setName(Component.translatable("borderlesswindow.options.fov_flying.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fov_flying.tooltip"))
                                        .setRange(FovState.MIN_PERCENT, FovState.MAX_PERCENT, 5)
                                        .setValueFormatter(value -> value <= FovState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFovFlying, this.storage::getFovFlying)
                                        .setDefaultValue(FovState.DEFAULT_PERCENT)
                                )
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:fov_bow"))
                                        .setName(Component.translatable("borderlesswindow.options.fov_bow.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fov_bow.tooltip"))
                                        .setRange(FovState.MIN_PERCENT, FovState.MAX_PERCENT, 5)
                                        .setValueFormatter(value -> value <= FovState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setFovBow, this.storage::getFovBow)
                                        .setDefaultValue(FovState.DEFAULT_PERCENT)
                                )
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:fov_fluid"))
                                        .setName(Component.translatable("borderlesswindow.options.fov_fluid.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.fov_fluid.tooltip"))
                                        .setRange(FovState.MIN_PERCENT, FovState.MAX_PERCENT, 5)
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
                                                ResourceLocation.parse("borderlesswindow:vanilla_distortion"))
                                        .setName(Component.translatable("borderlesswindow.options.vanilla_distortion.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.vanilla_distortion.tooltip"))
                                        .setRange(0, 100, 5)
                                        .setValueFormatter(value -> value <= 0
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setVanillaDistortion, this.storage::getVanillaDistortion)
                                        .setDefaultValue(100)
                                )
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:distortion_portal"))
                                        .setName(Component.translatable("borderlesswindow.options.distortion_portal.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distortion_portal.tooltip"))
                                        .setRange(DistortionState.MIN_PERCENT, DistortionState.MAX_PERCENT, 5)
                                        .setValueFormatter(value -> value <= DistortionState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistortionPortal, this.storage::getDistortionPortal)
                                        .setDefaultValue(DistortionState.DEFAULT_PERCENT)
                                )
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:distortion_nausea"))
                                        .setName(Component.translatable("borderlesswindow.options.distortion_nausea.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distortion_nausea.tooltip"))
                                        .setRange(DistortionState.MIN_PERCENT, DistortionState.MAX_PERCENT, 5)
                                        .setValueFormatter(value -> value <= DistortionState.MIN_PERCENT
                                                ? Component.translatable("borderlesswindow.options.distortion.off")
                                                : Component.translatable("borderlesswindow.options.distortion.percent", value))
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistortionNausea, this.storage::getDistortionNausea)
                                        .setDefaultValue(DistortionState.DEFAULT_PERCENT)
                                )
                        )
                )
                // Page 4 - Distances: how far things are drawn. Its own
                // page because it brings four options and none of them
                // fits the others' themes. Every slider starts at
                // "Default" (no cap) on the left, like the cloud height,
                // and can only lower vanilla's own distance.
                .addPage(builder.createOptionPage()
                        .setName(Component.translatable("borderlesswindow.options.sodium.pages.distances"))
                        // Entities: drawing only, see EntityRenderDispatcherMixin.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:distance_items"))
                                        .setName(Component.translatable("borderlesswindow.options.distance_items.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distance_items.tooltip"))
                                        .setRange(DistanceState.DEFAULT, DistanceState.ITEM_MAX, DistanceState.ITEM_STEP)
                                        .setValueFormatter(DistanceState::format)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistanceItems, this.storage::getDistanceItems)
                                        .setDefaultValue(DistanceState.DEFAULT)
                                )
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:distance_xp_orbs"))
                                        .setName(Component.translatable("borderlesswindow.options.distance_xp_orbs.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distance_xp_orbs.tooltip"))
                                        .setRange(DistanceState.DEFAULT, DistanceState.ORB_MAX, DistanceState.ORB_STEP)
                                        .setValueFormatter(DistanceState::format)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistanceOrbs, this.storage::getDistanceOrbs)
                                        .setDefaultValue(DistanceState.DEFAULT)
                                )
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:distance_armor_stands"))
                                        .setName(Component.translatable("borderlesswindow.options.distance_armor_stands.name"))
                                        .setTooltip(Component.translatable("borderlesswindow.options.distance_armor_stands.tooltip"))
                                        .setRange(DistanceState.DEFAULT, DistanceState.ARMOR_STAND_MAX, DistanceState.ARMOR_STAND_STEP)
                                        .setValueFormatter(DistanceState::format)
                                        .setStorageHandler(this.storage::flush)
                                        .setBinding(this.storage::setDistanceArmorStands, this.storage::getDistanceArmorStands)
                                        .setDefaultValue(DistanceState.DEFAULT)
                                )
                        )
                        // Particles: refused at spawn, see LevelRendererMixin.
                        .addOptionGroup(builder.createOptionGroup()
                                .addOption(builder.createIntegerOption(
                                                ResourceLocation.parse("borderlesswindow:distance_particles"))
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