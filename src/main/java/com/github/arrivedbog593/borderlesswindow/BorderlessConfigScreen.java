package com.github.arrivedbog593.borderlesswindow;

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
 * the screen mode and the F11 behavior -- with Sodium installed, the
 * same options also appear integrated in its video settings menu.
 * <p>
 * Changes apply immediately (same behavior as vanilla's fullscreen
 * option). Persistence is automatic: BorderlessHandler saves to
 * config/borderlesswindow.json on every change.
 */
public class BorderlessConfigScreen extends Screen {

    private final Screen parent;

    public BorderlessConfigScreen(Screen parent) {
        super(Component.translatable("borderlesswindow.config.title"));
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