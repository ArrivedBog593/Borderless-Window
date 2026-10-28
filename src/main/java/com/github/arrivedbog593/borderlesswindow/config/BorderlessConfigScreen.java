package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.bobbing.BobbingState;
import com.github.arrivedbog593.borderlesswindow.bobbing.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.cloud.CloudState;
import com.github.arrivedbog593.borderlesswindow.distortion.DistortionState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelStyle;
import com.github.arrivedbog593.borderlesswindow.fog.FogState;
import com.github.arrivedbog593.borderlesswindow.fov.FovState;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayState;
import com.github.arrivedbog593.borderlesswindow.screeneffect.ScreenEffectState;
import com.github.arrivedbog593.borderlesswindow.window.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.window.F11Mode;
import com.github.arrivedbog593.borderlesswindow.window.ScreenMode;
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
 * feature ("Window", "FPS Overlay", "Fog", "Clouds", "Screen Overlays",
 * "Bobbing", "Interface"),
 * mirroring the pages the Sodium integration shows. The list scrolls, so
 * adding features never pushes options off-screen the way the previous
 * flat layout did; a new feature only needs addHeader() + addOption()
 * calls here.
 * <p>
 * Changes apply immediately (same behavior as vanilla's fullscreen
 * option). Persistence is automatic: the state holders save to
 * config/borderlesswindow.json on every change. The one exception is the
 * cloud height slider, which defers its write to avoid hammering the disk
 * mid-drag -- onClose() covers the keyboard path for it.
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
                                Component.translatable(mode.getTranslationKey()))
                        .withValues(ScreenMode.values())
                        .withInitialValue(BorderlessHandler.getCurrentMode())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.screen_mode.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.screen_mode.name"),
                                (button, mode) -> BorderlessHandler.setMode(
                                        Minecraft.getInstance().getWindow(), mode)));

        // F11 behavior selector. No window change involved, applies silently.
        list.addOption(
                CycleButton.builder((F11Mode mode) ->
                                Component.translatable(mode.getTranslationKey()))
                        .withValues(F11Mode.values())
                        .withInitialValue(BorderlessHandler.getF11Target())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.f11_mode.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.f11_mode.name"),
                                (button, mode) -> BorderlessHandler.setF11Target(mode)));

        // --- FPS overlay --------------------------------------------
        list.addHeader(Component.translatable("config.borderlesswindow.section.fps"));

        // Overlay mode (Off / Simple / Extended). Applies instantly: the
        // overlay reads FpsOverlayState every frame.
        list.addOption(
                CycleButton.builder((FpsOverlayMode mode) ->
                                Component.translatable(mode.getTranslationKey()))
                        .withValues(FpsOverlayMode.values())
                        .withInitialValue(FpsOverlayState.getMode())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fps_overlay_mode.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.fps_overlay_mode.name"),
                                (button, mode) -> FpsOverlayState.setMode(mode)));

        // Overlay position (screen corner).
        list.addOption(
                CycleButton.builder((FpsOverlayPosition position) ->
                                Component.translatable(position.getTranslationKey()))
                        .withValues(FpsOverlayPosition.values())
                        .withInitialValue(FpsOverlayState.getPosition())
                        .withTooltip(position -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fps_overlay_position.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.fps_overlay_position.name"),
                                (button, position) -> FpsOverlayState.setPosition(position)));

        // --- Fog -----------------------------------------------------
        list.addHeader(Component.translatable("config.borderlesswindow.section.fog"));

        // On = vanilla fog, Off = removed. Applied live: FogHandler
        // consults FogState every time fog is set up.
        list.addOption(
                CycleButton.onOffBuilder(FogState.isTerrainFogEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fog_terrain.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.fog_terrain.name"),
                                (button, value) -> FogState.setTerrainFogEnabled(value)));

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

        // --- Clouds ---------------------------------------------------
        list.addHeader(Component.translatable("config.borderlesswindow.section.clouds"));

        // "Default" (leftmost) leaves each dimension's own height untouched;
        // any other value overrides it via CloudHeightMixin.
        CloudHeightSlider cloudSlider =
                new CloudHeightSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT);
        cloudSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.cloud_height.tooltip")));
        list.addOption(cloudSlider);

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
                                Component.translatable(mode.getTranslationKey()))
                        .withValues(CameraBobMode.values())
                        .withInitialValue(BobbingState.getCameraBobMode())
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
        // once; these split it per source (see FovState). Reusing
        // DistortionSlider: it is a plain 0-100 percentage widget and
        // persist() writes the whole config either way. Worth renaming
        // to PercentSlider now that a second feature uses it.
        list.addHeader(Component.translatable("config.borderlesswindow.section.fov"));

        DistortionSlider fovSpeedSlider = new DistortionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.fov_speed.name",
                FovState::getSpeedPercent, FovState::applySpeedPercent);
        fovSpeedSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.fov_speed.tooltip")));
        list.addOption(fovSpeedSlider);

        DistortionSlider fovFlyingSlider = new DistortionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.fov_flying.name",
                FovState::getFlyingPercent, FovState::applyFlyingPercent);
        fovFlyingSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.fov_flying.tooltip")));
        list.addOption(fovFlyingSlider);

        DistortionSlider fovBowSlider = new DistortionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.fov_bow.name",
                FovState::getBowPercent, FovState::applyBowPercent);
        fovBowSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.fov_bow.tooltip")));
        list.addOption(fovBowSlider);

        DistortionSlider fovFluidSlider = new DistortionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.fov_fluid.name",
                FovState::getFluidPercent, FovState::applyFluidPercent);
        fovFluidSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.fov_fluid.tooltip")));
        list.addOption(fovFluidSlider);

        // --- Distortion -----------------------------------------------
        // Its own section rather than living under Screen Overlays: this
        // warps the camera, it is not something drawn on top. Both sit
        // UNDER Minecraft's own Distortion Effects slider, same as the
        // bobbing options sit under View Bobbing.
        list.addHeader(Component.translatable("config.borderlesswindow.section.distortion"));

        DistortionSlider portalSlider = new DistortionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.distortion_portal.name",
                DistortionState::getPortalPercent, DistortionState::applyPortalPercent);
        portalSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.distortion_portal.tooltip")));
        list.addOption(portalSlider);

        DistortionSlider nauseaSlider = new DistortionSlider(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                "borderlesswindow.options.distortion_nausea.name",
                DistortionState::getNauseaPercent, DistortionState::applyNauseaPercent);
        nauseaSlider.setTooltip(Tooltip.create(
                Component.translatable("borderlesswindow.options.distortion_nausea.tooltip")));
        list.addOption(nauseaSlider);

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
                                Component.translatable(style.getTranslationKey()))
                        .withValues(EnchantmentLevelStyle.values())
                        .withInitialValue(EnchantmentLevelState.getStyle())
                        .withTooltip(style -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.enchantment_level_style.tooltip")))
                        .create(0, 0, OPTION_WIDTH, OPTION_HEIGHT,
                                Component.translatable("borderlesswindow.options.enchantment_level_style.name"),
                                (button, style) -> EnchantmentLevelState.setStyle(style)));

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
        // The cloud slider applies in memory and persists on mouse release;
        // arrow-key changes never produce a release, so flush here too.
        // Redundant when nothing changed -- saveCurrent() writes the whole
        // live state either way.
        CloudState.persist();
        DistortionState.persist();
        FovState.persist();

        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }
}