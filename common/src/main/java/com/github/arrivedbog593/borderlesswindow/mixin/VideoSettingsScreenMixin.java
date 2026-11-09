package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigScreen;
import com.github.arrivedbog593.borderlesswindow.window.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.window.ScreenMode;
import com.mojang.serialization.Codec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.VideoSettingsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Brings the mod into Minecraft's own Video Settings screen, for players
 * without Sodium -- the same two things the Sodium integration does there:
 * <p>
 * 1. The Fullscreen toggle becomes a Screen Mode selector (Windowed /
 *    Borderless / Fullscreen), the setting the mod replaces it with.
 * 2. A "Borderless Window..." button at the end opens the full config
 *    screen.
 * <p>
 * WHY HERE. Without this, a Fabric player without Sodium could only reach
 * the config through Mod Menu, which needs Fabric API -- two mods the mod
 * does not require. NeoForge's Config button and Mod Menu keep working as
 * extra entry points. With Sodium installed this screen is never shown:
 * Sodium opens its own video settings instead, where the mod has its pages.
 * <p>
 * Plain vanilla code, so it lives in common/ and works on every loader.
 * Extends OptionsSubScreen only to reach its protected options list.
 */
@Mixin(VideoSettingsScreen.class)
public abstract class VideoSettingsScreenMixin extends OptionsSubScreen {

    /** This screen's selector, to find its button again in tick(). */
    @Unique
    private OptionInstance<ScreenMode> borderlesswindow$screenMode;

    /** Vanilla's Fullscreen Resolution slider, greyed out outside Fullscreen. */
    @Unique
    private OptionInstance<?> borderlesswindow$resolution;

    private VideoSettingsScreenMixin(Screen lastScreen, Options options, Component title) {
        super(lastScreen, options, title);
    }

    /**
     * Keeps the selector in step with the real mode. F11 changes the mode
     * without going through the selector, and the resize that follows does
     * not rebuild this screen's widgets: OptionsSubScreen overrides
     * repositionElements to only re-lay them out, unlike a plain Screen.
     * Checked every tick, so any outside change shows within 1/20 s.
     */
    @Override
    public void tick() {
        super.tick();
        if (this.list != null && this.borderlesswindow$screenMode != null
                && this.list.findOption(this.borderlesswindow$screenMode) instanceof CycleButton<?> button
                && button.getValue() != BorderlessHandler.getCurrentMode()) {
            @SuppressWarnings("unchecked")
            CycleButton<ScreenMode> modeButton = (CycleButton<ScreenMode>) button;
            modeButton.setValue(BorderlessHandler.getCurrentMode());
        }
        borderlesswindow$updateResolution();
    }

    /**
     * Grabs the Fullscreen Resolution slider: it is the first option vanilla
     * adds with addBig (Biome Blend is the second).
     */
    @ModifyArg(method = "addOptions",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/OptionsList;addBig(Lnet/minecraft/client/OptionInstance;)V",
                    ordinal = 0))
    private OptionInstance<?> borderlesswindow$captureResolution(OptionInstance<?> option) {
        this.borderlesswindow$resolution = option;
        return option;
    }

    /**
     * The resolution only applies to exclusive Fullscreen, so the slider is
     * only usable there -- the same rule as on Sodium's page.
     */
    @Unique
    private void borderlesswindow$updateResolution() {
        if (this.list != null && this.borderlesswindow$resolution != null
                && this.list.findOption(this.borderlesswindow$resolution) instanceof AbstractWidget slider) {
            slider.active = BorderlessHandler.getCurrentMode() == ScreenMode.FULLSCREEN;
        }
    }

    /**
     * Swaps vanilla's Fullscreen toggle for the Screen Mode selector, in
     * place, so it keeps its spot in the grid. Vanilla builds the small
     * options as one array and hands it to addSmall.
     */
    @ModifyArg(method = "addOptions",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V"))
    private OptionInstance<?>[] borderlesswindow$replaceFullscreen(OptionInstance<?>[] smallOptions) {
        OptionInstance<?>[] replaced = smallOptions.clone();
        for (int i = 0; i < replaced.length; i++) {
            if (replaced[i] == this.options.fullscreen()) {
                this.borderlesswindow$screenMode = borderlesswindow$screenModeOption();
                replaced[i] = this.borderlesswindow$screenMode;
            }
        }
        return replaced;
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void borderlesswindow$addConfigButton(CallbackInfo ci) {
        if (this.list == null) {
            return;
        }
        // Right away, not only from tick(): no frame with a live slider.
        borderlesswindow$updateResolution();
        // 310 px: a single widget in a small row takes the full row width,
        // matching vanilla's "big" options above it.
        Button button = Button.builder(
                        Component.translatable("borderlesswindow.options.vanilla.open_config"),
                        pressed -> Minecraft.getInstance().setScreen(new BorderlessConfigScreen(this)))
                .tooltip(Tooltip.create(Component.translatable("borderlesswindow.options.vanilla.open_config.tooltip")))
                .width(310)
                .build();
        this.list.addSmall(button, null);
    }

    /**
     * Built fresh on every screen init, so it always starts from the live
     * mode. Never registered in Options, so nothing about it is written to
     * options.txt: the mode is saved in the mod's own config, as everywhere
     * else. Choosing a mode resizes the window, which re-inits this screen
     * -- the rebuilt selector then reads the new mode.
     */
    @Unique
    private static OptionInstance<ScreenMode> borderlesswindow$screenModeOption() {
        return new OptionInstance<>(
                "borderlesswindow.options.screen_mode.name",
                OptionInstance.cachedConstantTooltip(
                        Component.translatable("borderlesswindow.options.screen_mode.tooltip")),
                // The value alone: the cycle button already prefixes the
                // caption, as with vanilla's own enum options.
                (caption, mode) -> Component.translatable(mode.getTranslationKey()),
                new OptionInstance.Enum<>(List.of(ScreenMode.values()),
                        Codec.STRING.xmap(ScreenMode::valueOf, ScreenMode::name)),
                BorderlessHandler.getCurrentMode(),
                mode -> BorderlessHandler.setMode(Minecraft.getInstance().getWindow(), mode));
    }
}
