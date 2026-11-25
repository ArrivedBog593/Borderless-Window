package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Stops drawing block entities (chests, signs, banners, heads, beds...)
 * past the distance of their slider in DistanceState (its category's, or
 * the General one). Vanilla draws them up to a fixed
 * BlockEntityRenderer.getViewDistance(), 64 blocks, that no option
 * changes. DistanceState.blockEntityCap decides the category and which
 * ones are always drawn (beacons, End portals and gateways, moving
 * pistons).
 * <p>
 * Since 1.21.9 block entities are drawn from a render state:
 * tryExtractRenderState asks the renderer's shouldRender and builds no
 * state when it says no, so nothing of that block entity is drawn. The
 * wrap answers no past the cap as well; it only ever turns a yes into a
 * no. On NeoForge the three-argument tryExtractRenderState delegates to a
 * four-argument one (with a frustum), which holds the call; Sodium
 * 0.8.15 for 1.21.11 calls the three-argument one from
 * SodiumWorldRenderer.extractBlockEntity, so this covers vanilla, both
 * loaders and Sodium.
 * <p>
 * THE "*" IN THE SELECTOR IS REQUIRED. A bare method name selects only the
 * FIRST method of that name in the class (Mixin's default quantifier). On
 * NeoForge that is the three-argument overload, which has no shouldRender
 * call, so the wrap found nothing and the game crashed at startup
 * ("failed injection check, (0/1) succeeded"). "*" selects every
 * overload; the call is in exactly one of them on either loader.
 * <p>
 * Distance is measured from the camera to the block's center, as in
 * vanilla's BlockEntityRenderer.shouldRender, with the camera position
 * the dispatcher passes it.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @WrapOperation(
            method = "tryExtractRenderState*",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRenderer;shouldRender(Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/phys/Vec3;)Z"))
    private boolean borderlesswindow$cullByDistance(BlockEntityRenderer<BlockEntity, ?> renderer,
                                                   BlockEntity blockEntity, Vec3 cameraPos,
                                                   Operation<Boolean> original) {
        if (!original.call(renderer, blockEntity, cameraPos)) {
            return false;
        }
        int cap = DistanceState.blockEntityCap(blockEntity);
        if (cap == DistanceState.DEFAULT) {
            return true;
        }
        BlockPos pos = blockEntity.getBlockPos();
        double dx = pos.getX() + 0.5 - cameraPos.x;
        double dy = pos.getY() + 0.5 - cameraPos.y;
        double dz = pos.getZ() + 0.5 - cameraPos.z;
        return !DistanceState.isBeyond(cap, dx * dx + dy * dy + dz * dz);
    }
}
