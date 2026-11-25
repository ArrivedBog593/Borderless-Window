package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Stops drawing entities past the distance of their slider in
 * DistanceState (its category's, or the General one).
 * DistanceState.entityCap decides the category and which entities are
 * always drawn (other players, glowing entities, floating text).
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
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$cullByDistance(Entity entity, Frustum frustum,
                                                 double camX, double camY, double camZ,
                                                 CallbackInfoReturnable<Boolean> cir) {
        int cap = DistanceState.entityCap(entity);
        if (cap != DistanceState.DEFAULT
                && DistanceState.isBeyond(cap, entity.distanceToSqr(camX, camY, camZ))) {
            cir.setReturnValue(false);
        }
    }
}
