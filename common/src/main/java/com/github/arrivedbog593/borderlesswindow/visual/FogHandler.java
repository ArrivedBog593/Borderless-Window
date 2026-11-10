package com.github.arrivedbog593.borderlesswindow.visual;

import net.minecraft.client.Camera;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;

/**
 * Decides which fog to remove according to the four toggles in FogState.
 * Loader-neutral: each loader calls {@link #shouldRemove} from wherever it
 * hooks fog setup (NeoForge: ViewportEvent.RenderFog), then applies
 * {@link #NO_FOG_NEAR} / {@link #NO_FOG_FAR} its own way.
 * <p>
 * Fog setup (FogRenderer.setupFog) runs every time vanilla sets up fog,
 * with a FogType that maps 1:1 to our toggles:
 * - NONE (camera in air): terrain/atmospheric fog, including the
 *   Nether's thick fog. Both FOG_TERRAIN and FOG_SKY modes are removed
 *   so the horizon doesn't keep a mismatched haze band.
 * - WATER / LAVA / POWDER_SNOW: the corresponding submerged fogs.
 * <p>
 * "Removing" fog = pushing the fog planes out to ~1,000,000 blocks. The
 * near/far values are deliberately large and DIFFERENT: identical values
 * would make the linear fog factor divide by zero (NaN) in shaders.
 * Sodium's terrain shaders read the same fog parameters vanilla publishes
 * afterwards, so the toggles work identically with and without Sodium.
 * <p>
 * GAMEPLAY GUARD: fog caused by the Blindness or Darkness effects is
 * NEVER removed, regardless of the toggles. Those effects are
 * implemented as fog, and stripping them would neutralize a gameplay
 * mechanic (borderline cheating on servers). Vanilla applies effect fog
 * while the camera is in any FogType, so the guard runs before every
 * removal.
 */
public final class FogHandler {

    public static final float NO_FOG_NEAR = 1_000_000.0f;
    public static final float NO_FOG_FAR = 1_100_000.0f;

    private FogHandler() {
    }

    /** Whether this fog should be pushed out to {@link #NO_FOG_NEAR} / {@link #NO_FOG_FAR}. */
    public static boolean shouldRemove(FogType type, Camera camera) {
        boolean remove = switch (type) {
            case NONE -> !FogState.isTerrainFogEnabled();
            case WATER -> !FogState.isWaterFogEnabled();
            case LAVA -> !FogState.isLavaFogEnabled();
            case POWDER_SNOW -> !FogState.isPowderSnowFogEnabled();
        };
        if (!remove) {
            return false;
        }

        // Never touch fog while a fog-based effect is active (see
        // GAMEPLAY GUARD in the class Javadoc).
        Entity entity = camera.getEntity();
        return !(entity instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS)
                || living.hasEffect(MobEffects.DARKNESS)));
    }
}
