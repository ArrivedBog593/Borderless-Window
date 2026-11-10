package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.camera.BobbingState;
import com.github.arrivedbog593.borderlesswindow.camera.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.camera.FovState;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomMode;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelStyle;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayState;
import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.general.F11Mode;
import com.github.arrivedbog593.borderlesswindow.general.HudState;
import com.github.arrivedbog593.borderlesswindow.general.ScreenMode;
import com.github.arrivedbog593.borderlesswindow.visual.CloudState;
import com.github.arrivedbog593.borderlesswindow.visual.FogState;
import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectState;
import com.github.arrivedbog593.borderlesswindow.visual.SkyState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

/**
 * Config screen reachable from the Mods menu (NeoForge's "Config" button
 * for this mod). This gives users WITHOUT Sodium a proper UI to change
 * every setting -- with Sodium installed, the same options also appear
 * integrated in its video settings menu.
 * <p>
 * Options live inside a scrollable BorderlessOptionsList grouped by
 * feature, in the order of the Sodium integration's pages and groups:
 * General ("Window", "HUD"), Enchantments ("Interface"), FPS &amp; Background ("FPS Overlay",
 * "Background"), Visual Effects ("Fog",
 * "Clouds", "Sky and Weather", "Screen Overlays"), Camera ("Bobbing", "Field of View",
 * "Distortion", "Zoom") and Distances. Sodium separates groups with spacing alone; a
 * single scrolling list needs the headers instead. The list scrolls, so
 * adding features never pushes options off-screen the way the previous
 * flat layout did; a new feature only needs addHeader() + addOption()
 * calls here.
 * <p>
 * Changes apply immediately (same behavior as vanilla's fullscreen
 * option). Persistence is automatic: the state holders save to
 * config/borderlesswindow.json on every change. The exception is the
 * sliders, which defer their write to avoid hammering the disk mid-drag
 * -- onClose() covers the keyboard path for them.
 */
public class BorderlessConfigScreen extends Screen {

    /** Space reserved for the title above the list. */
    private static final int HEADER_HEIGHT = 32;

    /** Space reserved for the Done button below the list. */
    private static final int FOOTER_HEIGHT = 32;

    private static final int ROW_HEIGHT = 24;

    /**
     * Option widgets are wider than vanilla's usual 200 px: translated
     * labels ("Posición del contador", "Modo de F11") plus their value
     * overflow a 200 px button and get truncated. BorderlessOptionsList
     * uses 310 px rows, so 280 fits with margin to spare.
     */
    private static final int OPTION_WIDTH = 280;
    private static final int OPTION_HEIGHT = 20;

    /**
     * The Done button keeps vanilla's 200 px. Deliberately NOT tied to
     * OPTION_WIDTH: it is centered manually below, so sharing the
     * constant would silently offset it by half the difference the next
     * time the option width changes.
     */
    private static final int DONE_WIDTH = 200;
    private static final int DONE_HEIGHT = 20;

    private final Screen parent;

    public BorderlessConfigScreen(Screen parent) {
        super(Component.translatable("config.borderlesswindow.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        BorderlessOptionsList list = new BorderlessOptionsList(this.minecraft,
                this.width, this.height - HEADER_HEIGHT - FOOTER_HEIGHT,
                HEADER_HEIGHT, ROW_HEIGHT);

        // --- Window -------------------------------------------------
        list.addHeader(Component.translatable("config.borderlesswindow.section.window"));

        // Screen mode selector. Applying it immediately resizes the game
        // window, which re-inits this screen -- the list is then rebuilt
        // with the current value, so it always stays consistent.
        list.addOption(
                CycleButton.builder((ScreenMode mode) ->
                                Component.translatable(mode.getTranslationKey()),
                                BorderlessHandler.getCurrentMode())
                        .withValues(ScreenMode.values())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.screen_mode.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.screen_mode.name"),
                                (button, mode) -> BorderlessHandler.setMode(
                                        Minecraft.getInstance().getWindow(), mode)));

        // F11 behavior selector. No window change involved, applies silently.
        list.addOption(
                CycleButton.builder((F11Mode mode) ->
                                Component.translatable(mode.getTranslationKey()),
                                BorderlessHandler.getF11Target())
                        .withValues(F11Mode.values())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.f11_mode.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.f11_mode.name"),
                                (button, mode) -> BorderlessHandler.setF11Target(mode)));

        // --- HUD ------------------------------------------------------
        // Same page as the window options on the Sodium side (General).
        list.addHeader(Component.translatable("config.borderlesswindow.section.hud"));

        list.addOption(
                CycleButton.onOffBuilder(HudState.isHeldItemNameEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.held_item_name.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.held_item_name.name"),
                                (button, value) -> HudState.setHeldItemNameEnabled(value)));

        // --- Interface ------------------------------------------------
        // First non-rendering section: this is text shown in tooltips, not
        // anything drawn in the world. Kept as its own group so future
        // tooltip/HUD tweaks have an obvious home instead of being wedged
        // into a rendering section.
        list.addHeader(Component.translatable("config.borderlesswindow.section.enchantments"));

        // Applies live: the mixin reads EnchantmentLevelState every time
        // an enchantment name is built, so reopening an inventory is
        // enough to see the change.
        list.addOption(
                CycleButton.builder((EnchantmentLevelStyle style) ->
                                Component.translatable(style.getTranslationKey()),
                                EnchantmentLevelState.getStyle())
                        .withValues(EnchantmentLevelStyle.values())
                        .withTooltip(style -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.enchantment_level_style.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.enchantment_level_style.name"),
                                (button, style) -> EnchantmentLevelState.setStyle(style)));

        // --- FPS overlay --------------------------------------------
        list.addHeader(Component.translatable("config.borderlesswindow.section.fps"));

        // Overlay mode (Off / Simple / Extended). Applies instantly: the
        // overlay reads FpsOverlayState every frame.
        list.addOption(
                CycleButton.builder((FpsOverlayMode mode) ->
                                Component.translatable(mode.getTranslationKey()),
                                FpsOverlayState.getMode())
                        .withValues(FpsOverlayMode.values())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fps_overlay_mode.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.fps_overlay_mode.name"),
                                (button, mode) -> FpsOverlayState.setMode(mode)));

        // Overlay position (screen corner).
        list.addOption(
                CycleButton.builder((FpsOverlayPosition position) ->
                                Component.translatable(position.getTranslationKey()),
                                FpsOverlayState.getPosition())
                        .withValues(FpsOverlayPosition.values())
                        .withTooltip(position -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fps_overlay_position.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.fps_overlay_position.name"),
                                (button, position) -> FpsOverlayState.setPosition(position)));

        // --- Background -----------------------------------------------
        // What changes while the game is not in front.
        list.addHeader(Component.translatable("config.borderlesswindow.section.background"));

        // Cap while the window is unfocused. "Off" (leftmost slot) is
        // vanilla: full speed in the background. See UnfocusedFpsState.
        IntOptionSlider unfocusedFpsSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.unfocused_fps_limit.name",
                UnfocusedFpsState.OFF_SLOT, UnfocusedFpsState.MAX_LIMIT, UnfocusedFpsState.STEP,
                value -> value <= UnfocusedFpsState.OFF_SLOT
                        ? Component.translatable("borderlesswindow.options.distortion.off")
                        : Component.translatable("borderlesswindow.fps_overlay.fps", value),
                () -> UnfocusedFpsState.toSlot(UnfocusedFpsState.getLimit()),
                slot -> UnfocusedFpsState.applyLimit(UnfocusedFpsState.fromSlot(slot)),
                UnfocusedFpsState::persist);
        unfocusedFpsSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.unfocused_fps_limit.tooltip")));
        list.addOption(unfocusedFpsSlider);

        // Idle cap: minutes without input before it kicks in ("Off" is
        // vanilla), then the cap itself. See IdleFpsState.
        IntOptionSlider idleDelaySlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.idle_fps_delay.name",
                IdleFpsState.OFF, IdleFpsState.MAX_DELAY_MINUTES, 1,
                value -> value <= IdleFpsState.OFF
                        ? Component.translatable("borderlesswindow.options.distortion.off")
                        : Component.translatable("borderlesswindow.options.idle_fps_delay.minutes", value),
                IdleFpsState::getDelayMinutes, IdleFpsState::applyDelayMinutes,
                IdleFpsState::persist);
        idleDelaySlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.idle_fps_delay.tooltip")));
        list.addOption(idleDelaySlider);

        IntOptionSlider idleLimitSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.idle_fps_limit.name",
                UnfocusedFpsState.MIN_LIMIT, UnfocusedFpsState.MAX_LIMIT, 1,
                value -> Component.translatable("borderlesswindow.fps_overlay.fps", value),
                IdleFpsState::getLimit, IdleFpsState::applyLimit,
                IdleFpsState::persist);
        idleLimitSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.idle_fps_limit.tooltip")));
        list.addOption(idleLimitSlider);

        // Skip drawing while minimized. Off is vanilla. See
        // MinimizedRenderState.
        CycleButton<Boolean> minimizedButton = CycleButton.onOffBuilder(MinimizedRenderState.isEnabled())
                .withTooltip(value -> Tooltip.create(
                        Component.translatable("borderlesswindow.options.minimized_skip_render.tooltip")))
                .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                        Component.translatable("borderlesswindow.options.minimized_skip_render.name"),
                        (button, value) -> MinimizedRenderState.setEnabled(value));
        list.addOption(minimizedButton);

        // Volume while unfocused, as a share of Master Volume. 100%
        // (rightmost) is vanilla. See UnfocusedVolumeState.
        IntOptionSlider unfocusedVolumeSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.unfocused_volume.name",
                UnfocusedVolumeState.MIN_PERCENT, UnfocusedVolumeState.MAX_PERCENT, UnfocusedVolumeState.STEP,
                value -> value <= UnfocusedVolumeState.MIN_PERCENT
                        ? Component.translatable("borderlesswindow.options.unfocused_volume.muted")
                        : Component.translatable("borderlesswindow.options.distortion.percent", value),
                UnfocusedVolumeState::getPercent, UnfocusedVolumeState::applyPercent,
                UnfocusedVolumeState::persist);
        unfocusedVolumeSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.unfocused_volume.tooltip")));
        list.addOption(unfocusedVolumeSlider);

        // --- Fog -----------------------------------------------------
        list.addHeader(Component.translatable("config.borderlesswindow.section.fog"));

        // Built first so the terrain toggle below can grey it out live,
        // like the smart fire toggle. Added after the four toggles, the
        // same place it has on the Sodium page. "Default" (leftmost) is
        // vanilla's start. See FogState.
        IntOptionSlider fogStartSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.fog_terrain_start.name",
                FogState.DEFAULT_START, FogState.MAX_START, FogState.START_STEP,
                FogState::formatStart,
                FogState::getTerrainStart, FogState::applyTerrainStart, FogState::persist);
        fogStartSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.fog_terrain_start.tooltip")));
        // Nothing to move while the terrain fog is removed entirely.
        fogStartSlider.active = FogState.isTerrainFogEnabled();

        // On = vanilla fog, Off = removed. Applied live: FogHandler
        // consults FogState every time fog is set up.
        list.addOption(
                CycleButton.onOffBuilder(FogState.isTerrainFogEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fog_terrain.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.fog_terrain.name"),
                                (button, value) -> {
                                    FogState.setTerrainFogEnabled(value);
                                    fogStartSlider.active = value;
                                }));

        list.addOption(
                CycleButton.onOffBuilder(FogState.isWaterFogEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fog_water.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.fog_water.name"),
                                (button, value) -> FogState.setWaterFogEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(FogState.isLavaFogEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fog_lava.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.fog_lava.name"),
                                (button, value) -> FogState.setLavaFogEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(FogState.isPowderSnowFogEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fog_powder_snow.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.fog_powder_snow.name"),
                                (button, value) -> FogState.setPowderSnowFogEnabled(value)));

        list.addOption(fogStartSlider);

        // --- Clouds ---------------------------------------------------
        list.addHeader(Component.translatable("config.borderlesswindow.section.clouds"));

        // "Default" (leftmost) leaves each dimension's own height untouched;
        // any other value overrides it via CloudRendererMixin.
        IntOptionSlider cloudSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.cloud_height.name",
                CloudState.DEFAULT_SLOT, CloudState.MAX_HEIGHT, 1,
                CloudState::formatSlot,
                () -> CloudState.toSlot(CloudState.getHeight()),
                slot -> CloudState.applyHeight(CloudState.fromSlot(slot)),
                CloudState::persist);
        cloudSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.cloud_height.tooltip")));
        list.addOption(cloudSlider);

        // --- Sky and weather ------------------------------------------
        // Drawing only: the day cycle and the weather go on. See SkyState.
        list.addHeader(Component.translatable("config.borderlesswindow.section.sky"));

        list.addOption(
                CycleButton.onOffBuilder(SkyState.isSunMoonEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.sky_sun_moon.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.sky_sun_moon.name"),
                                (button, value) -> SkyState.setSunMoonEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(SkyState.isStarsEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.sky_stars.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.sky_stars.name"),
                                (button, value) -> SkyState.setStarsEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(SkyState.isWeatherEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.sky_weather.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.sky_weather.name"),
                                (button, value) -> SkyState.setWeatherEnabled(value)));

        // --- Screen overlays ------------------------------------------
        list.addHeader(Component.translatable("config.borderlesswindow.section.overlays"));

        // Built first so the fire toggle below can capture it and grey it
        // out live, matching what setEnabledProvider does on the Sodium
        // side. Added to the list AFTER the fire toggle, so display order
        // stays Water / Fire / Smart Fire.
        CycleButton<Boolean> smartFireButton =
                CycleButton.onOffBuilder(ScreenEffectState.isSmartFireEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.overlay_fire_smart.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.overlay_fire_smart.name"),
                                (button, value) -> ScreenEffectState.setSmartFireEnabled(value));
        // Meaningless while the fire overlay is off entirely -- there is
        // nothing left for it to hide conditionally.
        smartFireButton.active = ScreenEffectState.isFireOverlayEnabled();

        list.addOption(
                CycleButton.onOffBuilder(ScreenEffectState.isWaterOverlayEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.overlay_water.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.overlay_water.name"),
                                (button, value) -> ScreenEffectState.setWaterOverlayEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(ScreenEffectState.isFireOverlayEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.overlay_fire.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.overlay_fire.name"),
                                (button, value) -> {
                                    ScreenEffectState.setFireOverlayEnabled(value);
                                    smartFireButton.active = value;
                                }));

        list.addOption(smartFireButton);

        // The four below come from Gui rather than ScreenEffectRenderer
        // (see GuiMixin), but they belong in the same section: the player
        // does not care which vanilla class draws what.
        list.addOption(
                CycleButton.onOffBuilder(ScreenEffectState.isPumpkinOverlayEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.overlay_pumpkin.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.overlay_pumpkin.name"),
                                (button, value) -> ScreenEffectState.setPumpkinOverlayEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(ScreenEffectState.isFreezeOverlayEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.overlay_freeze.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.overlay_freeze.name"),
                                (button, value) -> ScreenEffectState.setFreezeOverlayEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(ScreenEffectState.isPortalOverlayEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.overlay_portal.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.overlay_portal.name"),
                                (button, value) -> ScreenEffectState.setPortalOverlayEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(ScreenEffectState.isSpyglassOverlayEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.overlay_spyglass.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.overlay_spyglass.name"),
                                (button, value) -> ScreenEffectState.setSpyglassOverlayEnabled(value)));

        // --- Bobbing --------------------------------------------------
        list.addHeader(Component.translatable("config.borderlesswindow.section.bobbing"));

        // Both of these live UNDER vanilla's own View Bobbing option: with
        // that turned off, GameRenderer never calls bobView and neither of
        // these does anything. The tooltips say so.
        list.addOption(
                CycleButton.builder((CameraBobMode mode) ->
                                Component.translatable(mode.getTranslationKey()),
                                BobbingState.getCameraBobMode())
                        .withValues(CameraBobMode.values())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.camera_bob.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.camera_bob.name"),
                                (button, mode) -> BobbingState.setCameraBobMode(mode)));

        list.addOption(
                CycleButton.onOffBuilder(BobbingState.isHandBobEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.hand_bob.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.hand_bob.name"),
                                (button, value) -> BobbingState.setHandBobEnabled(value)));

        // --- FOV ------------------------------------------------------
        // Its own section: these change how WIDE the view is, while the
        // distortion sliders change how much it warps. All four sit under
        // Minecraft's own FOV Effects slider, which moves every source at
        // once; these split it per source (see FovState).
        list.addHeader(Component.translatable("config.borderlesswindow.section.fov"));

        IntOptionSlider fovSpeedSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.fov_speed.name",
                FovState.MIN_PERCENT, FovState.MAX_PERCENT, 1,
                BorderlessConfigScreen::formatPercent,
                FovState::getSpeedPercent, FovState::applySpeedPercent, FovState::persist);
        fovSpeedSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.fov_speed.tooltip")));
        list.addOption(fovSpeedSlider);

        IntOptionSlider fovFlyingSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.fov_flying.name",
                FovState.MIN_PERCENT, FovState.MAX_PERCENT, 1,
                BorderlessConfigScreen::formatPercent,
                FovState::getFlyingPercent, FovState::applyFlyingPercent, FovState::persist);
        fovFlyingSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.fov_flying.tooltip")));
        list.addOption(fovFlyingSlider);

        IntOptionSlider fovBowSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.fov_bow.name",
                FovState.MIN_PERCENT, FovState.MAX_PERCENT, 1,
                BorderlessConfigScreen::formatPercent,
                FovState::getBowPercent, FovState::applyBowPercent, FovState::persist);
        fovBowSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.fov_bow.tooltip")));
        list.addOption(fovBowSlider);

        IntOptionSlider fovFluidSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.fov_fluid.name",
                FovState.MIN_PERCENT, FovState.MAX_PERCENT, 1,
                BorderlessConfigScreen::formatPercent,
                FovState::getFluidPercent, FovState::applyFluidPercent, FovState::persist);
        fovFluidSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.fov_fluid.tooltip")));
        list.addOption(fovFluidSlider);

        // --- Distortion -----------------------------------------------
        // Its own section rather than living under Screen Overlays: this
        // warps the camera, it is not something drawn on top. Both sit
        // UNDER Minecraft's own Distortion Effects slider, same as the
        // bobbing options sit under View Bobbing.
        list.addHeader(Component.translatable("config.borderlesswindow.section.distortion"));

        IntOptionSlider portalSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.distortion_portal.name",
                DistortionState.MIN_PERCENT, DistortionState.MAX_PERCENT, 1,
                BorderlessConfigScreen::formatPercent,
                DistortionState::getPortalPercent, DistortionState::applyPortalPercent,
                DistortionState::persist);
        portalSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.distortion_portal.tooltip")));
        list.addOption(portalSlider);

        IntOptionSlider nauseaSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.distortion_nausea.name",
                DistortionState.MIN_PERCENT, DistortionState.MAX_PERCENT, 1,
                BorderlessConfigScreen::formatPercent,
                DistortionState::getNauseaPercent, DistortionState::applyNauseaPercent,
                DistortionState::persist);
        nauseaSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.distortion_nausea.tooltip")));
        list.addOption(nauseaSlider);

        // --- Zoom -----------------------------------------------------
        // The key itself is in Minecraft's Controls, like every other key.
        // Nothing here does anything until it is pressed. See ZoomState.
        list.addHeader(Component.translatable("config.borderlesswindow.section.zoom"));

        list.addOption(
                CycleButton.builder((ZoomMode mode) ->
                                Component.translatable(mode.getTranslationKey()),
                                ZoomState.getMode())
                        .withValues(ZoomMode.values())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.zoom_mode.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.zoom_mode.name"),
                                (button, mode) -> ZoomState.setMode(mode)));

        IntOptionSlider zoomLevelSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.zoom_level.name",
                ZoomState.MIN_LEVEL, ZoomState.MAX_LEVEL, 1,
                value -> Component.translatable("borderlesswindow.options.zoom_level.value", value),
                ZoomState::getInitialLevel, ZoomState::applyInitialLevel,
                ZoomState::persist);
        zoomLevelSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.zoom_level.tooltip")));
        list.addOption(zoomLevelSlider);

        list.addOption(
                CycleButton.onOffBuilder(ZoomState.isScrollEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.zoom_scroll.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.zoom_scroll.name"),
                                (button, value) -> ZoomState.setScrollEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(ZoomState.isSmoothEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.zoom_smooth.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.zoom_smooth.name"),
                                (button, value) -> ZoomState.setSmoothEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(ZoomState.isHideHandEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.zoom_hide_hand.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.zoom_hide_hand.name"),
                                (button, value) -> ZoomState.setHideHandEnabled(value)));

        list.addOption(
                CycleButton.onOffBuilder(ZoomState.isHideHudEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.zoom_hide_hud.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.zoom_hide_hud.name"),
                                (button, value) -> ZoomState.setHideHudEnabled(value)));

        // --- Distances ------------------------------------------------
        // "Default" (leftmost) keeps vanilla's own distance; any other
        // value can only lower it. Entities are only hidden, never
        // removed, and particles are refused at spawn (see DistanceState).
        list.addHeader(Component.translatable("config.borderlesswindow.section.distances"));

        IntOptionSlider itemsSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.distance_items.name",
                DistanceState.DEFAULT, DistanceState.ITEM_MAX, DistanceState.ITEM_STEP,
                DistanceState::format,
                DistanceState::getItems, DistanceState::applyItems, DistanceState::persist);
        itemsSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.distance_items.tooltip")));
        list.addOption(itemsSlider);

        IntOptionSlider orbsSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.distance_xp_orbs.name",
                DistanceState.DEFAULT, DistanceState.ORB_MAX, DistanceState.ORB_STEP,
                DistanceState::format,
                DistanceState::getOrbs, DistanceState::applyOrbs, DistanceState::persist);
        orbsSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.distance_xp_orbs.tooltip")));
        list.addOption(orbsSlider);

        IntOptionSlider armorStandsSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.distance_armor_stands.name",
                DistanceState.DEFAULT, DistanceState.ARMOR_STAND_MAX, DistanceState.ARMOR_STAND_STEP,
                DistanceState::format,
                DistanceState::getArmorStands, DistanceState::applyArmorStands, DistanceState::persist);
        armorStandsSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.distance_armor_stands.tooltip")));
        list.addOption(armorStandsSlider);

        IntOptionSlider particlesSlider = new IntOptionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.distance_particles.name",
                DistanceState.DEFAULT, DistanceState.PARTICLE_MAX, DistanceState.PARTICLE_STEP,
                DistanceState::format,
                DistanceState::getParticles, DistanceState::applyParticles, DistanceState::persist);
        particlesSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.distance_particles.tooltip")));
        list.addOption(particlesSlider);

        this.addRenderableWidget(list);

        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                        .bounds(this.width / 2 - DONE_WIDTH / 2, this.height - 26,
                                DONE_WIDTH, DONE_HEIGHT)
                        .build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        // The sliders apply in memory and persist on mouse release;
        // arrow-key changes never produce a release, so flush here too.
        // Redundant when nothing changed -- saveCurrent() writes the whole
        // live state either way.
        CloudState.persist();
        FogState.persist();
        DistortionState.persist();
        FovState.persist();
        UnfocusedFpsState.persist();
        UnfocusedVolumeState.persist();
        IdleFpsState.persist();
        DistanceState.persist();
        ZoomState.persist();

        this.minecraft.setScreen(this.parent);
    }

    /**
     * The 0-100 FOV and distortion sliders. 0% reads "Off", matching how
     * Minecraft labels the bottom of its own Distortion and FOV Effects
     * sliders, and the Sodium side of the same options.
     */
    private static Component formatPercent(int percent) {
        return percent <= 0
                ? Component.translatable("borderlesswindow.options.distortion.off")
                : Component.translatable("borderlesswindow.options.distortion.percent", percent);
    }
}
