package com.github.arrivedbog593.borderlesswindow.visual;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;

/**
 * Holds the sky and weather toggles: the sun and moon, the stars, and
 * the falling rain and snow. Minecraft offers none of these as options;
 * they come from OptiFine's Details menu (Sun &amp; Moon, Stars, Rain &amp;
 * Snow), reimplemented here (see LevelRendererMixin).
 * <p>
 * ALL THREE ARE DRAWING ONLY. The time of day, the moon phase, the
 * weather itself, the rain sounds and the splashes on the ground are
 * untouched -- a hidden sun still sets, and it still rains for mobs,
 * crops and fishing. The sunrise glow and the sky color stay too: they
 * are the sky, not the sun.
 * <p>
 * true = drawn (vanilla, the default). Every setter persists immediately;
 * init() is the only entry that does not save.
 */
public final class SkyState {

    private static boolean sunMoon = true;
    private static boolean stars = true;
    private static boolean weather = true;

    private SkyState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(boolean loadedSunMoon, boolean loadedStars, boolean loadedWeather) {
        sunMoon = loadedSunMoon;
        stars = loadedStars;
        weather = loadedWeather;
    }

    public static boolean isSunMoonEnabled() {
        return sunMoon;
    }

    public static void setSunMoonEnabled(boolean enabled) {
        sunMoon = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isStarsEnabled() {
        return stars;
    }

    public static void setStarsEnabled(boolean enabled) {
        stars = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isWeatherEnabled() {
        return weather;
    }

    public static void setWeatherEnabled(boolean enabled) {
        weather = enabled;
        BorderlessConfigFile.saveCurrent();
    }
}
