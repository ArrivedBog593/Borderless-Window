package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.fog.FogState;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayState;
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
 * Changes apply immediately (same behavior as vanilla's fullscreen
 * option). Persistence is automatic: the state holders save to
 * config/borderlesswindow.json on every change.
 */
public class BorderlessConfigScreen extends Screen {

    private final Screen parent;

    public BorderlessConfigScreen(Screen parent) {
        super(Component.translatable("config.borderlesswindow.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int y = this.height / 6 + 24;

        // Screen mode selector. Applying it immediately resizes the game
        // window, which re-inits this screen -- the button is then rebuilt
        // with the current value, so it always stays consistent.
        this.addRenderableWidget(
                CycleButton.builder((ScreenMode mode) ->
                                Component.translatable(mode.getTranslationKey()))
                        .withValues(ScreenMode.values())
                        .withInitialValue(BorderlessHandler.getCurrentMode())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.screen_mode.tooltip")))
                        .create(centerX - 100, y, 200, 20,
                                Component.translatable("borderlesswindow.options.screen_mode.name"),
                                (button, mode) -> BorderlessHandler.setMode(
                                        Minecraft.getInstance().getWindow(), mode)));

        // F11 behavior selector. No window change involved, applies silently.
        this.addRenderableWidget(
                CycleButton.builder((F11Mode mode) ->
                                Component.translatable(mode.getTranslationKey()))
                        .withValues(F11Mode.values())
                        .withInitialValue(BorderlessHandler.getF11Target())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.f11_mode.tooltip")))
                        .create(centerX - 100, y + 24, 200, 20,
                                Component.translatable("borderlesswindow.options.f11_mode.name"),
                                (button, mode) -> BorderlessHandler.setF11Target(mode)));

        // FPS overlay mode selector (Off / Simple / Extended). Applies
        // instantly: the overlay reads FpsOverlayState every frame.
        this.addRenderableWidget(
                CycleButton.builder((FpsOverlayMode mode) ->
                                Component.translatable(mode.getTranslationKey()))
                        .withValues(FpsOverlayMode.values())
                        .withInitialValue(FpsOverlayState.getMode())
                        .withTooltip(mode -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fps_overlay_mode.tooltip")))
                        .create(centerX - 100, y + 56, 200, 20,
                                Component.translatable("borderlesswindow.options.fps_overlay_mode.name"),
                                (button, mode) -> FpsOverlayState.setMode(mode)));

        // FPS overlay position selector (screen corner).
        this.addRenderableWidget(
                CycleButton.builder((FpsOverlayPosition position) ->
                                Component.translatable(position.getTranslationKey()))
                        .withValues(FpsOverlayPosition.values())
                        .withInitialValue(FpsOverlayState.getPosition())
                        .withTooltip(position -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fps_overlay_position.tooltip")))
                        .create(centerX - 100, y + 80, 200, 20,
                                Component.translatable("borderlesswindow.options.fps_overlay_position.name"),
                                (button, position) -> FpsOverlayState.setPosition(position)));

        // Fog toggles (On = vanilla fog, Off = removed). Applied live:
        // FogHandler consults FogState every time fog is set up.
        this.addRenderableWidget(
                CycleButton.onOffBuilder(FogState.isTerrainFogEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fog_terrain.tooltip")))
                        .create(centerX - 100, y + 112, 200, 20,
                                Component.translatable("borderlesswindow.options.fog_terrain.name"),
                                (button, value) -> FogState.setTerrainFogEnabled(value)));

        this.addRenderableWidget(
                CycleButton.onOffBuilder(FogState.isWaterFogEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fog_water.tooltip")))
                        .create(centerX - 100, y + 136, 200, 20,
                                Component.translatable("borderlesswindow.options.fog_water.name"),
                                (button, value) -> FogState.setWaterFogEnabled(value)));

        this.addRenderableWidget(
                CycleButton.onOffBuilder(FogState.isLavaFogEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fog_lava.tooltip")))
                        .create(centerX - 100, y + 160, 200, 20,
                                Component.translatable("borderlesswindow.options.fog_lava.name"),
                                (button, value) -> FogState.setLavaFogEnabled(value)));

        this.addRenderableWidget(
                CycleButton.onOffBuilder(FogState.isPowderSnowFogEnabled())
                        .withTooltip(value -> Tooltip.create(
                                Component.translatable("borderlesswindow.options.fog_powder_snow.tooltip")))
                        .create(centerX - 100, y + 184, 200, 20,
                                Component.translatable("borderlesswindow.options.fog_powder_snow.name"),
                                (button, value) -> FogState.setPowderSnowFogEnabled(value)));

        this.addRenderableWidget(
                Button.builder(CommonComponents.GUI_DONE, button -> this.onClose())
                        .bounds(centerX - 100, this.height - 28, 200, 20)
                        .build());
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        assert this.minecraft != null;
        this.minecraft.setScreen(this.parent);
    }
}