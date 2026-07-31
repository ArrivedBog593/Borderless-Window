package com.github.arrivedbog593.borderlesswindow.screeneffect;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;

/**
 * Removes the water and fire screen overlays according to the toggles in
 * ScreenEffectState. Registered on the NeoForge event bus
 * (RenderBlockScreenEffectEvent) from the mod constructor -- public
 * NeoForge API only, no mixins, and fully independent from Sodium, same
 * as FogHandler.
 * <p>
 * The event fires right before vanilla draws one of the three view
 * overlays and is cancellable; cancelling skips the draw entirely. Its
 * OverlayType maps to our toggles:
 * - WATER: the rippling texture drawn while the camera is underwater.
 * - FIRE: the flames drawn across the screen while the player is burning.
 * - BLOCK: the texture drawn while the player's head is inside a solid
 *   block. Deliberately NOT offered as an option -- hiding it would let
 *   you see out of blocks you are suffocating in, which is an x-ray, not
 *   a quality-of-life tweak.
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

    public static void onRenderBlockScreenEffect(RenderBlockScreenEffectEvent event) {
        boolean remove = switch (event.getOverlayType()) {
            case WATER -> !ScreenEffectState.isWaterOverlayEnabled();
            case FIRE -> shouldHideFire(event.getPlayer());
            case BLOCK -> false; // never touched, see the class javadoc
        };

        if (remove) {
            event.setCanceled(true);
        }
    }

    private static boolean shouldHideFire(Player player) {
        if (!ScreenEffectState.isFireOverlayEnabled()) {
            return true;
        }
        return ScreenEffectState.isSmartFireEnabled()
                && player != null
                && player.hasEffect(MobEffects.FIRE_RESISTANCE);
    }
}