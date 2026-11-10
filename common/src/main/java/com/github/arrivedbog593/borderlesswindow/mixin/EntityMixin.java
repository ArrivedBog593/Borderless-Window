package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Portal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tells DistortionState when the player is standing in a Nether portal,
 * including one vanilla is ignoring because of the portal cooldown.
 * <p>
 * WHY THIS IS NEEDED. Inside a portal the portal's percentage rules the
 * whole distortion, Nausea included (see DistortionState). Vanilla's own
 * portal intensity cannot say "inside a portal": after crossing, the
 * player arrives inside the destination portal with a cooldown set by the
 * respawn packet. While it lasts, setAsInsidePortal only refreshes the
 * cooldown and never marks the portal process as "inside this tick", so
 * the portal intensity fades out even though the player never left the
 * portal. Stepping out and back in "fixes" it only because the cooldown
 * has run out by then.
 * <p>
 * HEAD, before the cooldown check, is the point: this records that the
 * player touched a portal, whatever vanilla decides to do about it.
 * NetherPortalBlock.entityInside has already checked canUsePortal, so
 * spectators and passengers never get here.
 * <p>
 * Only CONFUSION transitions count -- the same test vanilla uses to decide
 * whether a portal distorts at all -- so End portals and gateways are
 * ignored. Entity is the target because LocalPlayer does not override
 * setAsInsidePortal; the instanceof keeps every other entity, including
 * the integrated server's player, out of it.
 */
@Mixin(Entity.class)
public abstract class EntityMixin {

    @Inject(method = "setAsInsidePortal", at = @At("HEAD"))
    private void borderlesswindow$recordPortalContact(Portal portal, BlockPos pos, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (self instanceof LocalPlayer
                && portal.getLocalTransition() == Portal.Transition.CONFUSION) {
            DistortionState.markTouchingPortal();
        }
    }
}
