package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.bobbing.BobbingState;
import com.github.arrivedbog593.borderlesswindow.bobbing.CameraBobMode;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

/**
 * Splits vanilla's single "View Bobbing" option into a camera setting
 * and a hand setting, and adds a middle ground for the camera.
 * <p>
 * WHY FOUR INJECTIONS. GameRenderer.bobView applies four transforms in
 * one method -- lateral sway and vertical bounce (a single translate),
 * a Z rotation that rolls the horizon, and an X rotation that pitches
 * the view. Vanilla calls that one method from two places, both behind
 * options.bobView():
 * <p>
 * - renderLevel, for the world camera
 * - renderItemInHand, for the held item
 * <p>
 * Turning a call site off entirely is easy: wrap the invocation. But
 * VERTICAL_ONLY needs to suppress three of the four transforms INSIDE
 * bobView, and only when it was entered from the camera path -- the
 * hand must keep its full motion. Since bobView itself carries no
 * indication of who called it, the camera wrapper raises a flag for the
 * duration of that one call and the two inner injections read it.
 * <p>
 * The flag is safe as plain state: all of this runs on the render
 * thread, the camera call completes before renderItemInHand is reached,
 * and the try/finally guarantees it is lowered even if rendering throws.
 * <p>
 * WHY MIXINEXTRAS AND NOT @Redirect. @Redirect claims a call site
 * exclusively -- a second mod redirecting the same invocation is a hard
 * conflict, and bobbing is exactly the kind of thing other QoL mods
 * touch. @WrapWithCondition and @WrapOperation stack cleanly. MixinExtras
 * ships inside NeoForge (jarJar'd in projects/neoforge/build.gradle), so
 * this adds no dependency.
 * <p>
 * NOTE: everything here lives under vanilla's own View Bobbing option.
 * With that turned off, neither call site runs and none of this applies.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    /**
     * Raised only while the CAMERA call to bobView is executing, so the
     * two injections below know to strip transforms. Never set during
     * the hand's call.
     */
    @Unique
    private boolean borderlesswindow$strippingCameraBob = false;

    /**
     * The held item's bobbing, toggled independently. Skipping the call
     * leaves the pose stack untouched, exactly as if vanilla's option
     * were off -- but only for the hand.
     */
    @WrapWithCondition(
            method = "renderItemInHand",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private boolean borderlesswindow$shouldBobHand(GameRenderer instance,
                                                   PoseStack poseStack,
                                                   float partialTicks) {
        return BobbingState.isHandBobEnabled();
    }

    /**
     * The world camera's bobbing. FULL delegates to vanilla untouched,
     * OFF skips it, and VERTICAL_ONLY delegates with the flag raised so
     * the inner injections drop the sway and both rotations.
     * <p>
     * Delegating rather than reimplementing matters: the actual bob
     * curve (walkDist, the oBob/bob interpolation, the magic factors)
     * stays vanilla's, so a future change to that formula carries over
     * instead of silently drifting from a copy kept here.
     */
    @WrapOperation(
            method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void borderlesswindow$wrapCameraBob(GameRenderer instance,
                                                PoseStack poseStack,
                                                float partialTicks,
                                                Operation<Void> original) {
        CameraBobMode mode = BobbingState.getCameraBobMode();

        if (mode == CameraBobMode.OFF) {
            return;
        }

        if (mode == CameraBobMode.FULL) {
            original.call(instance, poseStack, partialTicks);
            return;
        }

        this.borderlesswindow$strippingCameraBob = true;
        try {
            original.call(instance, poseStack, partialTicks);
        } finally {
            this.borderlesswindow$strippingCameraBob = false;
        }
    }

    /**
     * Argument 0 of the translate is the lateral sway, argument 1 the
     * vertical bounce. VERTICAL_ONLY zeroes the first and leaves the
     * second alone.
     */
    @ModifyArgs(
            method = "bobView",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void borderlesswindow$stripLateralSway(Args args) {
        if (this.borderlesswindow$strippingCameraBob) {
            args.set(0, 0.0F);
        }
    }

    /**
     * Matches BOTH mulPose calls in bobView -- the Z roll that tilts the
     * horizon and the X pitch that nods the view. Dropping the two of
     * them is what makes VERTICAL_ONLY comfortable: the horizon line
     * stays level no matter how fast you move.
     */
    @WrapWithCondition(
            method = "bobView",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V"))
    private boolean borderlesswindow$shouldApplyBobRotation(PoseStack instance,
                                                            Quaternionf rotation) {
        return !this.borderlesswindow$strippingCameraBob;
    }
}