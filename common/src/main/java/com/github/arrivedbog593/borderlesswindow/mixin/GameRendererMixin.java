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
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.joml.Quaternionfc;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Splits vanilla's single "View Bobbing" option into a camera setting
 * and a hand setting, and adds a middle ground for the camera.
 * <p>
 * GameRenderer.bobView applies four transforms in one method -- lateral
 * sway and vertical bounce (a single translate), a Z rotation that rolls
 * the horizon, and an X rotation that pitches the view. It is called for
 * the world camera and for the held item, always behind
 * options.bobView():
 * <p>
 * - camera: from renderLevel, or, with an Iris shaderpack on, from the
 *   model-view matrix Iris builds itself (Iris turns renderLevel's own
 *   call into a no-op then, so a hook on that call site never sees it)
 * - hand: from renderItemInHand, or, under Iris shaders, from Iris's
 *   HandRenderer (see IrisHandRendererMixin)
 * <p>
 * So the camera setting is applied INSIDE bobView, the one place every
 * camera path goes through, and the hand calls are told apart instead:
 * both hand paths raise BobbingState's "bobbing hand" flag around their
 * call, and the injections below leave those calls alone. Any other
 * caller counts as the camera. The hand setting stays on the hand call
 * sites, which skip the call entirely when it is off.
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
     * The held item's bobbing, toggled independently. Skipping the call
     * leaves the pose stack untouched, exactly as if vanilla's option
     * were off -- but only for the hand. When it runs, the flag tells the
     * camera injections below to keep out.
     */
    @WrapOperation(
            method = "renderItemInHand",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/GameRenderer;bobView(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"))
    private void borderlesswindow$bobHand(GameRenderer instance,
                                          PoseStack poseStack,
                                          float partialTicks,
                                          Operation<Void> original) {
        if (!BobbingState.isHandBobEnabled()) {
            return;
        }
        BobbingState.setBobbingHand(true);
        try {
            original.call(instance, poseStack, partialTicks);
        } finally {
            BobbingState.setBobbingHand(false);
        }
    }

    /**
     * The camera's bobbing, OFF: the call does nothing. FULL and
     * VERTICAL_ONLY run vanilla's own bobView, so the bob curve (walkDist,
     * the oBob/bob interpolation, the magic factors) stays vanilla's and a
     * future change to it carries over; VERTICAL_ONLY only drops parts of
     * it, below.
     */
    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$skipCameraBob(PoseStack poseStack, float partialTicks,
                                                CallbackInfo ci) {
        if (!BobbingState.isBobbingHand()
                && BobbingState.getCameraBobMode() == CameraBobMode.OFF) {
            ci.cancel();
        }
    }

    /** VERTICAL_ONLY, on a camera call (not the hand's). */
    @Unique
    private static boolean borderlesswindow$keepVerticalOnly() {
        return !BobbingState.isBobbingHand()
                && BobbingState.getCameraBobMode() == CameraBobMode.VERTICAL_ONLY;
    }

    /**
     * Argument 0 of the translate is the lateral sway, argument 1 the
     * vertical bounce. VERTICAL_ONLY zeroes the first and leaves the
     * second alone. ModifyArg, not ModifyArgs: only one argument changes,
     * and ModifyArgs would build an Args object every frame.
     */
    @ModifyArg(
            method = "bobView",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"),
            index = 0)
    private float borderlesswindow$stripLateralSway(float lateralSway) {
        return borderlesswindow$keepVerticalOnly() ? 0.0F : lateralSway;
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
        return !borderlesswindow$keepVerticalOnly();
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
     * The same split under an Iris shaderpack. Iris zeroes the spin above
     * (its redirect of the screenEffectScale read) and redoes it, with its
     * own copy of vanilla's formula, inside the redirect it puts on this
     * rotation call (Iris's MixinModelViewBobbing, which moves bobbing and
     * the spin from the projection to the model-view matrix). That copy
     * cannot be hooked, so for the duration of the call the portal's two
     * intensities are scaled and put back right after (the lerp is
     * linear, so this scales its result exactly like the hook above), and
     * DistortionState's flag has LivingEntityMixin scale Nausea's blend
     * factor. The spin speed is already scaled in tick().
     * <p>
     * Without Iris, or with Iris but no shaderpack, nothing reads either
     * during this call: vanilla's rotation is plain matrix math.
     */
    @WrapOperation(
            method = "renderLevel",
            at = @At(value = "INVOKE",
                    target = "Lorg/joml/Matrix4f;rotation(Lorg/joml/Quaternionfc;)Lorg/joml/Matrix4f;",
                    remap = false))
    private Matrix4f borderlesswindow$scaleIrisDistortion(Matrix4f matrix, Quaternionfc rotation,
                                                          Operation<Matrix4f> original) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return original.call(matrix, rotation);
        }
        float portal = DistortionState.getPortalFactor();
        float intensity = player.portalEffectIntensity;
        float oldIntensity = player.oPortalEffectIntensity;
        player.portalEffectIntensity = intensity * portal * portal;
        player.oPortalEffectIntensity = oldIntensity * portal * portal;
        DistortionState.setScalingIrisSpin(true);
        try {
            return original.call(matrix, rotation);
        } finally {
            DistortionState.setScalingIrisSpin(false);
            player.portalEffectIntensity = intensity;
            player.oPortalEffectIntensity = oldIntensity;
        }
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