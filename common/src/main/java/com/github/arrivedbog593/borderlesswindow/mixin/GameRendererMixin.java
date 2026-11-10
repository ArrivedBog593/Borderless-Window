package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.BobbingState;
import com.github.arrivedbog593.borderlesswindow.camera.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.camera.FovState;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
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
     * The water/lava squeeze, the only FOV effect vanilla's own slider
     * reaches. The original value is already scaled by that slider (a
     * lerp between 1.0 and 0.857), so ours multiplies on top -- with
     * FOV Effects at 0% in Minecraft there is nothing left to soften.
     * <p>
     * ordinal = 1: getFov lerps twice, the fov modifier first and this
     * squeeze second, both on floats.
     */
    @ModifyExpressionValue(
            method = "getFov",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F", ordinal = 1))
    private float borderlesswindow$scaleFluidFov(float original) {
        return Mth.lerp(FovState.getFluidFactor(), 1.0F, original);
    }

    /**
     * Start of every frame: reads the zoom key and advances the zoom
     * animation before anything below reads the FOV. See ZoomState.
     */
    @Inject(method = "render", at = @At("HEAD"))
    private void borderlesswindow$updateZoom(DeltaTracker deltaTracker, boolean renderLevel,
                                             CallbackInfo ci) {
        ZoomState.onFrame();
    }

    /**
     * The zoom itself: the FOV of the view, divided by the zoom level.
     * <p>
     * At RETURN, so it is the last word on every path. On NeoForge that
     * places it after ClientHooks.getFieldOfView (the ComputeFov event),
     * and on both loaders after vanilla's FOV setting, the FOV effects and
     * the per-source sliders above -- the zoom narrows whatever they left.
     * <p>
     * Only the useFOVSetting call is the view. The other one, from
     * renderItemInHand, sizes the held item at a fixed 70 degrees, and
     * zooming it would blow the hand up to fill the screen.
     */
    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float borderlesswindow$applyZoom(float original,
                                             @Local(argsOnly = true) boolean useFovSetting) {
        if (!useFovSetting) {
            return original;
        }
        return (float) (original * ZoomState.getFovMultiplier());
    }

    /** "Hide Hand" while zooming: the held item is simply not drawn. */
    @WrapWithCondition(
            method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemInHand("
                            + "FZLorg/joml/Matrix4f;)V"))
    private boolean borderlesswindow$shouldRenderHand(GameRenderer instance, float partialTick,
                                                      boolean sleeping, Matrix4f projectionMatrix) {
        return !ZoomState.shouldHideHand();
    }

    /**
     * "Hide HUD" while zooming, done the way F1 does it: hideGui is raised
     * for the duration of this one call. Everything that already honors
     * F1 -- every HUD layer on both loaders, other mods' HUD elements, the
     * FPS counter -- hides with it, and the player's own F1 state is put
     * back untouched afterwards.
     */
    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;render("
                            + "Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
    private void borderlesswindow$hideHudWhileZooming(Gui instance, GuiGraphics guiGraphics,
                                                      DeltaTracker deltaTracker,
                                                      Operation<Void> original) {
        if (!ZoomState.shouldHideHud()) {
            original.call(instance, guiGraphics, deltaTracker);
            return;
        }
        Options options = Minecraft.getInstance().options;
        boolean hideGui = options.hideGui;
        options.hideGui = true;
        try {
            original.call(instance, guiGraphics, deltaTracker);
        } finally {
            options.hideGui = hideGui;
        }
    }
}