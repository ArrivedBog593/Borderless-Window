package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.distortion.DistortionState;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records which source is driving spinningEffectIntensity, so the two
 * distortion sliders can tell a Nether portal apart from Nausea.
 * <p>
 * Vanilla keeps ONE accumulator fed by an if/else chain, and picks a
 * per-tick rate in each branch: 0.0125 inside a portal, 0.006667 under
 * Nausea, -0.05 while decaying, 0 when nothing applies. That rate IS
 * the attribution, so it is captured here with {@link Local} instead of
 * being reconstructed from the outside.
 * <p>
 * THREE EARLIER APPROACHES FAILED, and they are worth recording so no
 * one tries them again:
 * <p>
 * 1. Asking hasEffect(CONFUSION) at render time. Wrong whenever the
 *    player has the potion AND walks into a portal, which is the exact
 *    case the split exists for.
 * <p>
 * 2. Reading the boolean this method receives. It reports that a
 *    portalProcess with a CONFUSION transition exists, not that the
 *    player is in a portal now -- measured in game, it stayed true for
 *    the entire remaining duration of the potion after walking out.
 *    Vanilla's portal branch also requires
 *    portalProcess.isInsidePortalThisTick(), which it consumes every
 *    tick and which is unreachable from here.
 * <p>
 * 3. Measuring how much the accumulator moved between ticks. Correct in
 *    principle -- the rates are distinct -- but it breaks twice: the
 *    accumulator clamps at 1.0, so a player already saturated by the
 *    potion produces no delta to read on entering a portal, and
 *    changing dimension builds a fresh LocalPlayer whose first tick
 *    reads one enormous fake delta.
 * <p>
 * Injecting at RETURN means vanilla's branch has already run.
 * <p>
 * THE RATE ALONE MISSES ONE CASE: arriving through a portal. The player
 * lands inside the destination portal on cooldown, vanilla skips its
 * portal branch, and the rate reads as Nausea (or decay) while the player
 * is still standing in the portal. EntityMixin records portal contact
 * regardless of cooldown, and that contact counts as the portal here.
 * Contact is recorded while moving and consumed on the next tick, the
 * same one-tick lag vanilla's own isInsidePortalThisTick has.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    /**
     * Midpoint between the two rising rates (0.006667 and 0.0125), so
     * the float representation can't flip the decision either way.
     */
    @Unique
    private static final float PORTAL_RATE_THRESHOLD = 0.0095F;

    @Inject(method = "handleConfusionTransitionEffect", at = @At("RETURN"))
    private void borderlesswindow$recordDistortionSource(
            boolean insidePortal,
            CallbackInfo ci,
            @Local(ordinal = 0) float rate) {
        // insidePortal is part of the target signature and deliberately
        // unused -- see approach 2 in the class javadoc.
        boolean touchingPortal = DistortionState.consumeTouchingPortal();
        if (rate > PORTAL_RATE_THRESHOLD || touchingPortal) {
            DistortionState.setLastSourceWasPortal(true);
        } else if (rate > 0.0F) {
            DistortionState.setLastSourceWasPortal(false);
        }
        // rate <= 0: decaying or idle. Keeping the last attribution is
        // what makes the fade-out match whatever caused it.
    }
}