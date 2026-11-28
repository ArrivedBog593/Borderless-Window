package com.github.arrivedbog593.borderlesswindow.visual;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * How your own status effects look, read by GameRendererMixin (Night
 * Vision) and LivingEntityMixin (effect particles).
 * <p>
 * NIGHT VISION: see NightVisionMode. Only the brightness curve of the
 * last 10 seconds changes; how long the effect lasts does not.
 * <p>
 * OWN EFFECT PARTICLES: the swirls every active effect spawns around its
 * entity (LivingEntity.tickEffects). Around yourself they float through
 * your view in first person. Off stops them for your own player only:
 * other players and mobs keep theirs, and other players still see yours
 * (their game spawns them). Off also hides them on yourself in third
 * person, since it is the same particles. Effects given with hidden
 * particles (/effect ... true) have none in vanilla either.
 * <p>
 * Every setter persists immediately; init() is the only entry that does
 * not save.
 */
public final class StatusEffectState {

    /** Vanilla starts its pulse when this many ticks are left. */
    private static final int ENDING_TICKS = 200;

    private static NightVisionMode nightVisionMode = NightVisionMode.FLICKER;
    private static boolean ownParticles = true;

    private StatusEffectState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(NightVisionMode loadedNightVisionMode, boolean loadedOwnParticles) {
        nightVisionMode = loadedNightVisionMode;
        ownParticles = loadedOwnParticles;
    }

    public static NightVisionMode getNightVisionMode() {
        return nightVisionMode;
    }

    public static void setNightVisionMode(NightVisionMode mode) {
        nightVisionMode = mode;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isOwnParticlesEnabled() {
        return ownParticles;
    }

    public static void setOwnParticlesEnabled(boolean enabled) {
        ownParticles = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    /**
     * The Night Vision brightness for FADE and STEADY, or a negative value
     * for FLICKER (vanilla's own formula then runs). Same inputs as
     * vanilla's: the effect, and the partial tick it is given.
     */
    public static float nightVisionScale(MobEffectInstance effect, float partialTick) {
        return switch (nightVisionMode) {
            case FLICKER -> -1.0F;
            case STEADY -> 1.0F;
            case FADE -> {
                if (!effect.endsWithin(ENDING_TICKS)) {
                    yield 1.0F;
                }
                float left = (effect.getDuration() - partialTick) / ENDING_TICKS;
                yield Math.clamp(left, 0.0F, 1.0F);
            }
        };
    }
}
