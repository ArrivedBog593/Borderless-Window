package com.github.arrivedbog593.borderlesswindow.general;

import com.github.arrivedbog593.borderlesswindow.camera.BobbingState;
import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.camera.FovState;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelState;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayState;
import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.github.arrivedbog593.borderlesswindow.visual.CloudState;
import com.github.arrivedbog593.borderlesswindow.visual.FogState;
import com.github.arrivedbog593.borderlesswindow.visual.NametagState;
import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectState;
import com.github.arrivedbog593.borderlesswindow.visual.SkyState;
import com.github.arrivedbog593.borderlesswindow.visual.StatusEffectState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.lwjgl.sdl.SDLHints;

/**
 * The 3 screen modes, on top of vanilla's own window handling.
 * <p>
 * Since 26.3 (SDL instead of GLFW) vanilla has all 3 itself, picked by two
 * options it saves in options.txt:
 * <p>
 * - WINDOWED: Fullscreen off. Vanilla restores the window's last size and
 *   position.
 * - BORDERLESS: Fullscreen on, Exclusive Fullscreen off. On Windows, an
 *   undecorated window covering the monitor, 1 px wider than it so the
 *   driver does not take it over as exclusive (the same trick the mod used
 *   to do itself, then with the extra pixel in height), and a framebuffer
 *   without that pixel. Elsewhere, SDL's desktop fullscreen.
 * - FULLSCREEN: Fullscreen on, Exclusive Fullscreen on. Real exclusive
 *   fullscreen at the Fullscreen Resolution option's video mode.
 * <p>
 * Vanilla applies them without a restart and restores them at startup on
 * its own. What the mod adds is one selector for the 3 (instead of the two
 * toggles, see VideoSettingsScreenMixin and the Sodium page) and what F11
 * does (the "F11 Mode" option, see MinecraftMixin.toggleFullscreen).
 */
public final class BorderlessHandler {

    // False until initializeFromConfig() has loaded the saved config.
    private static boolean initialized = false;

    // What F11 does. Configurable through the "F11 Mode" option in the
    // video settings menu (Borderless Window section).
    private static F11Mode f11Target = F11Mode.BORDERLESS;

    private BorderlessHandler() {
    }

    /**
     * The mode the options ask for. Vanilla applies a change to Fullscreen
     * on the next frame, and puts the option back if the window could not
     * follow it (Minecraft.fullscreenStateChanged).
     */
    public static ScreenMode getCurrentMode() {
        Options options = Minecraft.getInstance().options;
        if (!options.fullscreen().get()) {
            return ScreenMode.WINDOWED;
        }
        return options.exclusiveFullscreen().get() ? ScreenMode.FULLSCREEN : ScreenMode.BORDERLESS;
    }

    public static F11Mode getF11Target() {
        return f11Target;
    }

    public static void setF11Target(F11Mode target) {
        f11Target = target;
        BorderlessConfigFile.saveCurrent();
    }

    /**
     * Computes which mode F11 should switch to from the current one,
     * according to the configured F11 mode:
     * - BORDERLESS / FULLSCREEN: toggle Windowed <-> that mode.
     * - CYCLE: Windowed -> Borderless -> Fullscreen -> Windowed.
     */
    public static ScreenMode getNextF11Mode() {
        ScreenMode currentMode = getCurrentMode();
        return switch (f11Target) {
            case BORDERLESS -> currentMode == ScreenMode.WINDOWED
                    ? ScreenMode.BORDERLESS : ScreenMode.WINDOWED;
            case FULLSCREEN -> currentMode == ScreenMode.WINDOWED
                    ? ScreenMode.FULLSCREEN : ScreenMode.WINDOWED;
            case CYCLE -> switch (currentMode) {
                case WINDOWED -> ScreenMode.BORDERLESS;
                case BORDERLESS -> ScreenMode.FULLSCREEN;
                case FULLSCREEN -> ScreenMode.WINDOWED;
            };
        };
    }

    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Called exactly once at game startup, on the second frame (see
     * MinecraftInitMixin). Hands every setting in
     * config/borderlesswindow.json to its state holder (single load point
     * for the whole config).
     * <p>
     * The screen mode is vanilla's since 26.3 and already restored by the
     * time this runs. Configs from older versions still hold the mode the
     * mod restored itself: it is applied once here, so updating does not
     * switch between Borderless and Fullscreen (those versions left
     * Exclusive Fullscreen as it was), and the save that follows drops it
     * from the file.
     */
    public static void initializeFromConfig() {
        var config = BorderlessConfigFile.load();
        f11Target = config.f11Mode();
        FpsOverlayState.init(config.fpsOverlayMode(), config.fpsOverlayPosition());
        UnfocusedFpsState.init(config.unfocusedFpsLimit());
        UnfocusedVolumeState.init(config.unfocusedVolume());
        MinimizedRenderState.init(config.minimizedSkipRender());
        IdleFpsState.init(config.idleFpsDelay(), config.idleFpsLimit(),
                config.idleFpsSecondDelay(), config.idleFpsSecondLimit());
        FogState.init(config.terrainFog(), config.waterFog(),
                config.lavaFog(), config.powderSnowFog(),
                config.fogTerrainStart());
        SkyState.init(config.sunMoon(), config.stars(), config.weather());
        HudState.init(config.heldItemName());
        LanguageState.init(config.fastLanguageReload());
        ToastState.init(config.advancementToasts(), config.recipeToasts(),
                config.tutorialToasts(), config.unverifiedChatToast(),
                config.narratorToast(), config.packDownloadToast());
        CloudState.init(config.cloudHeight());
        ScreenEffectState.init(config);
        StatusEffectState.init(config.nightVisionMode(), config.ownEffectParticles());
        NametagState.init(config.nametagMode());
        BobbingState.init(config.cameraBobMode(), config.handBob());
        EnchantmentLevelState.init(config.enchantmentLevelStyle());
        DistortionState.init(config.distortionPortal(), config.distortionNausea());
        FovState.init(config.fovSpeed(), config.fovFlying(),
                config.fovBow(), config.fovFluid());
        DistanceState.init(config.distances());
        ZoomState.init(config.zoomMode(), config.zoomLevel(),
                config.zoomScroll(), config.zoomSmooth(),
                config.zoomHideHand(), config.zoomHideHud());

        // Exclusive Fullscreen minimizes again when it loses focus.
        // Minecraft 26.3 sets this SDL hint to "0" at startup
        // (RenderSystem.initBackendSystem), so the window stayed on top in
        // the lower Fullscreen Resolution, and with Vulkan the desktop kept
        // it while other apps were in use. Reset to SDL's default ("auto"),
        // it minimizes only a window in exclusive fullscreen, and SDL gives
        // the desktop its own mode back: the classic exclusive behavior,
        // and what GLFW did up to 26.2 (GLFW_AUTO_ICONIFY). Borderless and
        // Windowed never minimize. SDL reads the hint at each focus loss, so
        // resetting it here, long after startup, is enough. WindowMixin sets
        // the chosen mode again when the window comes back.
        SDLHints.SDL_ResetHint(SDLHints.SDL_HINT_VIDEO_MINIMIZE_ON_FOCUS_LOSS);

        initialized = true;
        if (config.legacyScreenMode() != null) {
            setMode(config.legacyScreenMode());
            BorderlessConfigFile.saveCurrent();
        }
    }

    /**
     * Sets vanilla's two options to the mode and saves options.txt.
     * Exclusive Fullscreen first: while the window is fullscreen, changing
     * it applies at once (Borderless <-> Fullscreen), and turning
     * Fullscreen on then picks the right kind on the next frame. Windowed
     * keeps Exclusive Fullscreen as it was, as vanilla does.
     */
    public static void setMode(ScreenMode mode) {
        if (mode == getCurrentMode()) {
            return;
        }
        Options options = Minecraft.getInstance().options;
        if (mode != ScreenMode.WINDOWED) {
            options.exclusiveFullscreen().set(mode == ScreenMode.FULLSCREEN);
        }
        options.fullscreen().set(mode != ScreenMode.WINDOWED);
        options.save();
    }
}
