package com.github.arrivedbog593.borderlesswindow.fps;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import com.github.arrivedbog593.borderlesswindow.mixin.SoundManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;

/**
 * Holds the volume applied while the game window is not focused (alt-tabbed,
 * minimized, or another window in front), as a percentage of the regular
 * Master Volume.
 * <p>
 * HOW IT APPLIES. Vanilla's Master Volume is not multiplied into each
 * sound: it is the OpenAL listener gain, set through Listener.setGain().
 * ListenerMixin scales only the value handed to OpenAL there, so the
 * listener's own gain field keeps vanilla's number. That matters because
 * SoundEngine skips starting any sound while that field is 0 -- scaling
 * the field would drop sounds that begin while muted (ambient loops,
 * music), and they would stay missing after clicking back in. Scaling
 * only the OpenAL call silences them instead, and they are audible the
 * moment focus returns.
 * <p>
 * MinecraftMixin calls {@link #refresh()} whenever the focus changes, which
 * re-sends the Master Volume through the same path.
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
     * What ListenerMixin multiplies the listener gain by: the setting while
     * unfocused, 1 otherwise. The percentage is checked first so the
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
     * Re-applies the Master Volume so the listener gain picks up the
     * current factor. Goes straight to SoundEngine.updateCategoryVolume
     * rather than SoundManager.updateSourceVolume: the latter stops every
     * playing sound when Master is 0, which a focus change has no business
     * doing. updateCategoryVolume is a no-op until the engine has loaded.
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
        ((SoundManagerAccessor) minecraft.getSoundManager()).borderlesswindow$getSoundEngine()
                .updateCategoryVolume(SoundSource.MASTER,
                        minecraft.options.getSoundSourceVolume(SoundSource.MASTER));
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
