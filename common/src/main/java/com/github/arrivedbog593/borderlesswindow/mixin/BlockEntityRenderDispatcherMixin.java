package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Stops drawing block entities (chests, signs, banners, heads, beds...)
 * past the distance of their slider in DistanceState (its category's, or
 * the General one). Vanilla draws them up to a fixed
 * BlockEntityRenderer.getViewDistance(), 64 blocks, that no option
 * changes. DistanceState.blockEntityCap decides the category and which
 * ones are always drawn (beacons, End portals and gateways, moving
 * pistons).
 * <p>
 * render is where vanilla asks the renderer's shouldRender before drawing
 * one block entity, and Sodium's SodiumWorldRenderer.renderBlockEntity
 * calls this same method (checked in the 0.8.13 bytecode), so one hook
 * covers both. Cancelling skips that block entity whole: a chest, a sign
 * or a bed is drawn entirely by its renderer, so it disappears, not just
 * its lid or text. It only ever turns a yes into a no.
 * <p>
 * Distance is measured from the camera to the block's center, as in
 * vanilla's BlockEntityRenderer.shouldRender.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Shadow
    public Camera camera;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$cullByDistance(BlockEntity blockEntity, float partialTick,
                                                 PoseStack poseStack, MultiBufferSource bufferSource,
                                                 CallbackInfo ci) {
        int cap = DistanceState.blockEntityCap(blockEntity);
        if (cap == DistanceState.DEFAULT) {
            return;
        }

        Vec3 camera = this.camera.getPosition();
        BlockPos pos = blockEntity.getBlockPos();
        double dx = pos.getX() + 0.5 - camera.x;
        double dy = pos.getY() + 0.5 - camera.y;
        double dz = pos.getZ() + 0.5 - camera.z;
        if (DistanceState.isBeyond(cap, dx * dx + dy * dy + dz * dz)) {
            ci.cancel();
        }
    }
}
