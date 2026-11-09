package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops drawing dropped items, experience orbs and armor stands past the
 * distances in DistanceState.
 * <p>
 * shouldRender is the one gate LevelRenderer asks before drawing any
 * entity, so answering false here skips the model, the shadow, the name
 * tag and the hitbox together. It only ever turns a yes into a no: past
 * the cap the entity is hidden, below it vanilla decides as usual
 * (distance scaling, frustum). Sodium's entity culling wraps the frustum
 * test inside EntityRenderer.shouldRender, one call deeper, so the two
 * stack cleanly.
 * <p>
 * Distance is measured from the camera, the same point vanilla uses.
 * <p>
 * TWO GUARDS keep this a pure performance option:
 * <p>
 * 1. GLOWING entities are always drawn. Glowing is information (a Spectral
 *    Arrow hit, a server highlighting something) and the outline is
 *    drawn through walls precisely so it is seen from afar.
 * <p>
 * 2. ARMOR STANDS WITH A VISIBLE NAME are always drawn. Servers and
 *    adventure maps use invisible named armor stands as floating text and
 *    holograms; hiding them would hide that text, not decoration.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$cullByDistance(Entity entity, Frustum frustum,
                                                 double camX, double camY, double camZ,
                                                 CallbackInfoReturnable<Boolean> cir) {
        int cap;
        switch (entity) {
            case ItemEntity ignored -> cap = DistanceState.getItems();
            case ExperienceOrb ignored -> cap = DistanceState.getOrbs();
            case ArmorStand ignored -> {
                // Guard 2 above.
                if (entity.isCustomNameVisible()) {
                    return;
                }
                cap = DistanceState.getArmorStands();
            }
            case null, default -> {
                return;
            }
        }

        // Guard 1 above.
        if (entity.isCurrentlyGlowing()) {
            return;
        }

        if (DistanceState.isBeyond(cap, entity.distanceToSqr(camX, camY, camZ))) {
            cir.setReturnValue(false);
        }
    }
}
