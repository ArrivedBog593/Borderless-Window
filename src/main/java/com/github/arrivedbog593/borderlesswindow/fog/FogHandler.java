package com.github.arrivedbog593.borderlesswindow.fog;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Removes fog according to the four toggles in FogState. Registered on
 * the NeoForge event bus (ViewportEvent.RenderFog) from the mod
 * constructor -- public NeoForge API only, no mixins, and fully
 * independent from Sodium.
 * <p>
 * The event fires every time vanilla sets up fog (FogRenderer.setupFog)
 * and carries the FogType, which maps 1:1 to our toggles:
 * - NONE (camera in air): terrain/atmospheric fog, including the
 *   Nether's thick fog. Both FOG_TERRAIN and FOG_SKY modes are removed
 *   so the horizon doesn't keep a mismatched haze band.
 * - WATER / LAVA / POWDER_SNOW: the corresponding submerged fogs.
 * <p>
 * "Removing" fog = pushing the fog planes out to ~1,000,000 blocks and
 * cancelling the event (NeoForge requires cancelling for the new plane
 * distances to take effect). The near/far values are deliberately large
 * and DIFFERENT: identical values would make the linear fog factor
 * divide by zero (NaN) in shaders. Sodium's terrain shaders read the
 * same fog parameters vanilla publishes after this event, so the
 * toggles work identically with and without Sodium.
 * <p>
 * GAMEPLAY GUARD: fog caused by the Blindness or Darkness effects is
 * NEVER removed, regardless of the toggles. Those effects are
 * implemented as fog, and stripping them would neutralize a gameplay
 * mechanic (borderline cheating on servers). Vanilla applies effect fog
 * while the camera is in any FogType, so the guard runs before every
 * removal.
 */
public final class FogHandler {

    private static final float NO_FOG_NEAR = 1_000_000.0f;
    private static final float NO_FOG_FAR = 1_100_000.0f;

    private FogHandler() {
    }

    public static void onRenderFog(ViewportEvent.RenderFog event) {
        boolean remove = switch (event.getType()) {
            case NONE -> !FogState.isTerrainFogEnabled();
            case WATER -> !FogState.isWaterFogEnabled();
            case LAVA -> !FogState.isLavaFogEnabled();
            case POWDER_SNOW -> !FogState.isPowderSnowFogEnabled();
        };
        if (!remove) {
            return;
        }

        // Never touch fog while a fog-based effect is active (see
        // GAMEPLAY GUARD in the class javadoc).
        Entity entity = event.getCamera().getEntity();
        if (entity instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS)
                || living.hasEffect(MobEffects.DARKNESS))) {
            return;
        }

        event.setNearPlaneDistance(NO_FOG_NEAR);
        event.setFarPlaneDistance(NO_FOG_FAR);
        event.setCanceled(true);
    }
}