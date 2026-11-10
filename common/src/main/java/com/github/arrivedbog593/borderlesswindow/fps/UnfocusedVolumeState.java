package com.github.arrivedbog593.borderlesswindow.fps;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

/**
 * Holds the volume applied while the game window is not focused (alt-tabbed,
 * minimized, or another window in front), as a percentage of the regular
 * Master Volume.
 * <p>
 * HOW IT APPLIES. Vanilla multiplies Master into each sound's volume and
 * hands the result to OpenAL through Channel.setVolume(). ChannelMixin
 * scales only that hand-over, never the volume SoundEngine computes. That
 * matters because SoundEngine refuses to start most sounds whose volume
 * is 0 -- scaling its number would drop sounds that begin while muted
 * (ambient loops), and they would stay missing after clicking back in.
 * Scaling only the OpenAL call silences them instead, and they are
 * audible the moment focus returns.
 * <p>
 * MinecraftMixin calls {@link #refresh()} whenever the focus changes, which
 * re-sends every playing sound's volume through the same path.
 * <p>
 * SCALE: percentages, 0..100, default 100 (vanilla: no change in the
 * background). 0 mutes. Split apply/persist like UnfocusedFpsState: it is
 * a slider.
 */
public final class UnfocusedVolumeState {

    public static final int MIN_PERCENT = 0;
    public static final int MAX_PERCENT = 100;
    public static final int DEFAULT_PERCENT = MAX_PERCENT;
    /** 1%, matching vanilla's own volume sliders. */
    public static final int STEP = 1;

    private static int percent = DEFAULT_PERCENT;

    private UnfocusedVolumeState() {
    }

    /** Called once at startup with the value loaded from disk. */
    public static void init(int loadedPercent) {
        percent = sanitize(loadedPercent);
    }

    public static int getPercent() {
        return percent;
    }

    /**
     * What ChannelMixin multiplies every sound's volume by: the setting
     * while unfocused, 1 otherwise. The percentage is checked first so the
     * default never queries the Minecraft instance.
     * <p>
     * If Dynamic FPS also lowers the background volume, the two reductions
     * multiply (50% of 50% is 25%).
     */
    public static float getGainFactor() {
        if (percent >= MAX_PERCENT) {
            return 1.0F;
        }
        if (Minecraft.getInstance().isWindowActive()) {
            return 1.0F;
        }
        return percent / 100.0F;
    }

    /**
     * Re-sends every playing sound's volume so it picks up the current
     * factor: refreshing MASTER is how vanilla's own Master slider reaches
     * all of them. Recomputes volumes only -- nothing is stopped or
     * restarted -- and is a no-op until the engine has loaded.
     */
    public static void refresh() {
        Minecraft minecraft = Minecraft.getInstance();
        // Not dead code, despite the IDE: Minecraft's packages are
        // non-null by default, but its constructor calls
        // setWindowActive(true) before it creates the sound manager, and
        // MinecraftMixin runs this from there.
        //noinspection ConstantValue
        if (minecraft.getSoundManager() == null) {
            return;
        }
        minecraft.getSoundManager().refreshCategoryVolume(SoundSource.MASTER);
    }

    /** Memory only -- takes effect the next time the window loses focus. */
    public static void applyPercent(int newPercent) {
        percent = sanitize(newPercent);
    }

    /** Applies and persists in one step (discrete, one-shot changes). */
    public static void setPercent(int newPercent) {
        applyPercent(newPercent);
        persist();
    }

    /**
     * Writes the whole live config to disk. Cheap to call redundantly:
     * saveCurrent() always serializes the full state.
     */
    public static void persist() {
        BorderlessConfigFile.saveCurrent();
    }

    private static int sanitize(int value) {
        return Math.clamp(value, MIN_PERCENT, MAX_PERCENT);
    }
}
