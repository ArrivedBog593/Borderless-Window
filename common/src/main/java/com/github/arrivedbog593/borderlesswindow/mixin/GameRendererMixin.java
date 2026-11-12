package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.BobbingState;
import com.github.arrivedbog593.borderlesswindow.camera.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;
import org.joml.Quaternionfc;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobView("
                            + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private boolean borderlesswindow$shouldBobHand(GameRenderer instance,
                                                   CameraRenderState cameraState,
                                                   PoseStack poseStack) {
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
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobView("
                            + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V"))
    private void borderlesswindow$wrapCameraBob(GameRenderer instance,
                                                CameraRenderState cameraState,
                                                PoseStack poseStack,
                                                Operation<Void> original) {
        CameraBobMode mode = BobbingState.getCameraBobMode();

        if (mode == CameraBobMode.OFF) {
            return;
        }

        if (mode == CameraBobMode.FULL) {
            original.call(instance, cameraState, poseStack);
            return;
        }

        this.borderlesswindow$strippingCameraBob = true;
        try {
            original.call(instance, cameraState, poseStack);
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
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionfc;)V"))
    private boolean borderlesswindow$shouldApplyBobRotation(PoseStack instance,
                                                            Quaternionfc rotation) {
        return !this.borderlesswindow$strippingCameraBob;
    }

    /**
     * Splits vanilla's single Distortion Effects slider by source, portal
     * half.
     * <p>
     * renderLevel reads the two sources apart -- the portal's intensity
     * with this Mth.lerp (the only one in the method), Nausea's with
     * getEffectBlendFactor below -- takes the larger and multiplies it by
     * the squared vanilla scale. Scaling each source before that max is
     * all the split needs.
     * <p>
     * Squaring our own factor matches vanilla's f2 * f2, so 50% here feels
     * like 50% on Minecraft's slider.
     * <p>
     * tick() reads the same two sources once more, to blend the spin SPEED
     * (20 for the portal, 7 for Nausea, weighted by intensity). Scaled
     * there too, or a muted portal would still speed up the Nausea spin
     * while you stand in one.
     */
    @ModifyExpressionValue(
            method = "renderLevel",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float borderlesswindow$scalePortalDistortion(float original) {
        float factor = DistortionState.getPortalFactor();
        return original * factor * factor;
    }

    /** The portal's intensity as tick() reads it for the spin speed. */
    @ModifyExpressionValue(
            method = "tick",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/player/LocalPlayer;portalEffectIntensity:F",
                    opcode = Opcodes.GETFIELD))
    private float borderlesswindow$scalePortalSpinSpeed(float original) {
        return this.borderlesswindow$scalePortalDistortion(original);
    }

    /**
     * Nausea half of the split above, in renderLevel and tick() alike.
     * Inside a portal it takes the portal's percentage (see
     * DistortionState).
     */
    @ModifyExpressionValue(
            method = {"renderLevel", "tick"},
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;"
                            + "getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F"))
    private float borderlesswindow$scaleNauseaDistortion(float original) {
        float factor = DistortionState.getNauseaSpinFactor();
        return original * factor * factor;
    }

    /**
     * Start of every client tick, before the entities tick: settles
     * whether the player is in a portal for this tick (see
     * DistortionState.latchPortalContact).
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void borderlesswindow$latchPortalContact(CallbackInfo ci) {
        DistortionState.latchPortalContact();
    }

    /**
     * Start of every frame: reads the zoom key and advances the zoom
     * animation before the camera works out this frame's FOV. Since 26.1
     * that happens in update (Camera.update), the first of the frame's
     * update / extract / render steps; the FOV hooks live in CameraMixin.
     * See ZoomState.
     */
    @Inject(method = "update", at = @At("HEAD"))
    private void borderlesswindow$updateZoom(DeltaTracker deltaTracker, CallbackInfo ci) {
        ZoomState.onFrame();
    }

    /** "Hide Hand" while zooming: the held item is simply not drawn. */
    @WrapWithCondition(
            method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand("
                            + "Lnet/minecraft/client/renderer/state/level/CameraRenderState;"
                            + "FLorg/joml/Matrix4fc;)V"))
    private boolean borderlesswindow$shouldRenderHand(GameRenderer instance, CameraRenderState cameraState,
                                                      float partialTick, Matrix4fc modelViewMatrix) {
        return !ZoomState.shouldHideHand();
    }
}