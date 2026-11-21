package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigScreen;
import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.general.ScreenMode;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Brings the mod into Minecraft's own Video Settings screen, for players
 * without Sodium -- the same things the Sodium integration does there:
 * <p>
 * 1. The Fullscreen toggle becomes a Screen Mode selector (Windowed /
 *    Borderless / Fullscreen), the setting the mod replaces it with.
 * 2. A "Borderless Window..." button at the end opens the full config
 *    screen.
 * 3. Options the mod's settings take over are greyed out while they do:
 *    Fullscreen Resolution outside Fullscreen, Reduce FPS When while the
 *    Idle FPS Limit is on.
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
@SuppressWarnings("unused") // lambda parameters required by CycleButton/Button callbacks
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
     * <p>
     * Both the button and the option behind it are updated. The button
     * only shows its own value; on a click it hands the next one to
     * OptionInstance.set, which does nothing if that equals the option's
     * stored value. Updating only the button, as this used to, left the
     * option on the old mode, so after F11 one click in the cycle was
     * swallowed: F11 to Windowed from Borderless, then a click to
     * Borderless, matched the stale value and never reached setMode.
     * Setting the option here runs its callback, setMode, with the mode
     * already active, which returns at once.
     * <p>
     * An injection and not an override: in 1.21.11 vanilla's screen has a
     * tick() of its own (it greys out the anisotropy slider), and a plain
     * method here would replace it.
     */
    @Inject(method = "tick", at = @At("TAIL"))
    private void borderlesswindow$syncScreenMode(CallbackInfo ci) {
        if (this.list != null && this.borderlesswindow$screenMode != null
                && this.list.findOption(this.borderlesswindow$screenMode) instanceof CycleButton<?> button
                && button.getValue() != BorderlessHandler.getCurrentMode()) {
            @SuppressWarnings("unchecked")
            CycleButton<ScreenMode> modeButton = (CycleButton<ScreenMode>) button;
            this.borderlesswindow$screenMode.set(BorderlessHandler.getCurrentMode());
            modeButton.setValue(BorderlessHandler.getCurrentMode());
        }
        borderlesswindow$updateResolution();
        borderlesswindow$updateInactivityLimit();
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
     * Vanilla's Reduce FPS When option does nothing while the mod's Idle FPS
     * Limit is on: the mod's stages replace its AFK setting, and its 10 FPS
     * while minimized applies with either value (see IdleFpsState). So the
     * button is greyed out then, with a tooltip saying why, instead of
     * offering a choice with no effect. Its value and options.txt are left
     * alone: turning the Idle FPS Limit off brings the button and its own
     * tooltip back, with the player's choice in effect again. The same rule
     * as on Sodium's page.
     * <p>
     * Only on a change, so the tooltip is set once rather than every tick.
     * A greyed-out button still shows its tooltip on hover.
     */
    @Unique
    private void borderlesswindow$updateInactivityLimit() {
        if (this.list == null
                || !(this.list.findOption(this.options.inactivityFpsLimit()) instanceof CycleButton<?> button)) {
            return;
        }
        boolean replaced = IdleFpsState.replacesVanillaAfkMode();
        if (button.active != replaced) {
            return;
        }
        button.active = !replaced;
        if (replaced) {
            button.setTooltip(Tooltip.create(
                    Component.translatable("borderlesswindow.options.vanilla.inactivity_replaced")));
        } else {
            borderlesswindow$restoreTooltip(button);
        }
    }

    /** Setting the button's own value again rebuilds its own tooltip. */
    @Unique
    private static <T> void borderlesswindow$restoreTooltip(CycleButton<T> button) {
        button.setValue(button.getValue());
    }

    /**
     * Swaps vanilla's Fullscreen toggle for the Screen Mode selector, in
     * place, so it keeps its spot in the grid. Vanilla builds the small
     * options as one array and hands it to addSmall.
     * <p>
     * Also drops vanilla's Exclusive Fullscreen toggle: the selector sets it
     * too (off for Borderless, on for Fullscreen; see BorderlessHandler).
     * Brightness moves up next to the selector.
     */
    @ModifyArg(method = "addOptions",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/OptionsList;addSmall([Lnet/minecraft/client/OptionInstance;)V"))
    private OptionInstance<?>[] borderlesswindow$replaceFullscreen(OptionInstance<?>[] smallOptions) {
        List<OptionInstance<?>> replaced = new ArrayList<>(smallOptions.length);
        for (OptionInstance<?> option : smallOptions) {
            if (option == this.options.fullscreen()) {
                this.borderlesswindow$screenMode = borderlesswindow$screenModeOption();
                replaced.add(this.borderlesswindow$screenMode);
            } else if (option != this.options.exclusiveFullscreen()) {
                replaced.add(option);
            }
        }
        return replaced.toArray(OptionInstance<?>[]::new);
    }

    @Inject(method = "addOptions", at = @At("TAIL"))
    private void borderlesswindow$addConfigButton(CallbackInfo ci) {
        if (this.list == null) {
            return;
        }
        // Right away, not only from tick(): no frame with a live slider.
        borderlesswindow$updateResolution();
        borderlesswindow$updateInactivityLimit();
        // 310 px: a single widget in a small row takes the full row width,
        // matching vanilla's "big" options above it.
        Button button = Button.builder(
                        Component.translatable("borderlesswindow.options.vanilla.open_config"),
                        pressed -> Minecraft.getInstance().gui.setScreen(new BorderlessConfigScreen(this)))
                .tooltip(Tooltip.create(Component.translatable("borderlesswindow.options.vanilla.open_config.tooltip")))
                .width(310)
                .build();
        this.list.addSmall(button, null);
    }

    /**
     * Built fresh on every screen init, so it always starts from the live
     * mode. Never registered in Options, so it has no line of its own in
     * options.txt: its callback sets vanilla's Fullscreen and Exclusive
     * Fullscreen, which are saved there (see BorderlessHandler.setMode).
     * Choosing a mode resizes the window, which re-inits this screen
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
                BorderlessHandler::setMode);
    }
}
