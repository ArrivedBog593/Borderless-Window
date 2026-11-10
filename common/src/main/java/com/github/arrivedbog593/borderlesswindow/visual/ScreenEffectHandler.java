package com.github.arrivedbog593.borderlesswindow.visual;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * Decides whether the water and fire screen overlays are drawn, according
 * to the toggles in ScreenEffectState. Loader-neutral, like FogHandler:
 * each loader asks from wherever it hooks the overlays (NeoForge:
 * RenderBlockScreenEffectEvent, cancelled to skip the draw).
 * <p>
 * Vanilla draws three view overlays (ScreenEffectRenderer):
 * - WATER: the rippling texture drawn while the camera is underwater.
 * - FIRE: the flames drawn across the screen while the player is burning.
 * - BLOCK: the texture drawn while the player's head is inside a solid
 *   block. Deliberately NOT offered as an option -- hiding it would let
 *   you see out of blocks you are suffocating in, which is an x-ray, not
 *   a quality-of-life tweak. There is no method for it here on purpose.
 * <p>
 * SMART FIRE: when the fire overlay is left on and smart fire is enabled,
 * the flames are hidden only while Fire Resistance is active. Note this
 * also clears your view while swimming in lava with the effect, since
 * vanilla draws the fire overlay whenever the player is burning. That is
 * the same category of small visual advantage as the lava fog toggle, and
 * the same reasoning applies: it is opt-in, off by default, and stated
 * plainly in the tooltip.
 */
public final class ScreenEffectHandler {

    private ScreenEffectHandler() {
    }

    public static boolean shouldHideWater() {
        return !ScreenEffectState.isWaterOverlayEnabled();
    }

    public static boolean shouldHideFire(Player player) {
        if (!ScreenEffectState.isFireOverlayEnabled()) {
            return true;
        }
        return ScreenEffectState.isSmartFireEnabled()
                && player != null
                && player.hasEffect(MobEffects.FIRE_RESISTANCE);
    }
}
