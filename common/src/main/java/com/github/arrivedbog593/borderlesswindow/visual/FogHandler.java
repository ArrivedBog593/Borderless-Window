package com.github.arrivedbog593.borderlesswindow.visual;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;

/**
 * Applies the four fog toggles and the fog start (FogState) to the fog
 * vanilla just computed. Called from FogRendererMixin on both loaders,
 * between FogRenderer.setupFog filling its FogData and uploading it.
 * <p>
 * Since 1.21.6 vanilla draws two fogs at once and shows whichever is
 * thicker at each point:
 * - ENVIRONMENTAL (environmentalStart/End): what the camera is in. Water,
 *   lava, powder snow, Blindness, Darkness or, in the open air, the
 *   dimension's own fog: the Nether's thick fog, rain fog, the Ender
 *   Dragon's. The FogType says which one, and maps 1:1 to our toggles
 *   (ATMOSPHERIC = in the open air = Terrain Fog).
 * - RENDER DISTANCE (renderDistanceStart/End): the band that hides the
 *   edge of the loaded terrain, the same in every situation. It belongs
 *   to the Terrain Fog toggle and the fog start.
 * Plus skyEnd and cloudEnd, how far the sky and the clouds fade into the
 * fog color; each environment sets them along with its own fog.
 * <p>
 * The toggles are therefore independent: Water Fog off clears the water
 * and keeps the terrain edge hidden, Terrain Fog off clears the edge
 * everywhere and the open-air fog.
 * <p>
 * "Removing" fog = pushing its planes out to ~1,000,000 blocks. The
 * near/far values are deliberately large and DIFFERENT: identical values
 * would make the linear fog factor divide by zero (NaN) in shaders.
 * Sodium's terrain shaders read the same fog buffer vanilla uploads
 * afterwards, so the toggles work identically with and without Sodium.
 * <p>
 * GAMEPLAY GUARD: fog is never touched while the Blindness or Darkness
 * effects are active, regardless of the toggles. Those effects are
 * implemented as fog, and stripping them would neutralize a gameplay
 * mechanic (borderline cheating on servers). Vanilla puts them ahead of
 * water and the open air, but behind lava and powder snow, so checking
 * the FogType alone would not be enough.
 */
public final class FogHandler {

    public static final float NO_FOG_NEAR = 1_000_000.0f;
    public static final float NO_FOG_FAR = 1_100_000.0f;

    private FogHandler() {
    }

    /**
     * @param type           the FogType vanilla chose (camera fluid, or
     *                       ATMOSPHERIC in the open air)
     * @param renderDistance the render distance in blocks: the far edge
     *                       of the render distance fog
     */
    public static void apply(FogData fog, FogType type, Camera camera, float renderDistance) {
        // GAMEPLAY GUARD in the class Javadoc.
        Entity entity = camera.entity();
        if (entity instanceof LivingEntity living
                && (living.hasEffect(MobEffects.BLINDNESS)
                || living.hasEffect(MobEffects.DARKNESS))) {
            return;
        }

        boolean terrainOff = !FogState.isTerrainFogEnabled();
        if (terrainOff) {
            fog.renderDistanceStart = NO_FOG_NEAR;
            fog.renderDistanceEnd = NO_FOG_FAR;
        } else {
            int start = FogState.getTerrainStart();
            if (start != FogState.DEFAULT_START) {
                fog.renderDistanceStart = renderDistance * start / 100.0F;
            }
        }

        boolean environmentOff = switch (type) {
            case ATMOSPHERIC, NONE -> terrainOff;
            case WATER -> !FogState.isWaterFogEnabled();
            case LAVA -> !FogState.isLavaFogEnabled();
            case POWDER_SNOW -> !FogState.isPowderSnowFogEnabled();
        };
        if (environmentOff) {
            fog.environmentalStart = NO_FOG_NEAR;
            fog.environmentalEnd = NO_FOG_FAR;
            // What the open air would give them: the sky fades with the
            // terrain edge, the clouds at their own range.
            fog.skyEnd = terrainOff ? NO_FOG_FAR : renderDistance;
            fog.cloudEnd = Minecraft.getInstance().options.cloudRange().get() * 16;
        }
    }
}
