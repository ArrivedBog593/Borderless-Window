package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records mouse activity for the idle FPS cap (see IdleFpsState), and
 * adapts the mouse to the zoom (see ZoomState).
 * <p>
 * onMove, onButton and onScroll are where vanilla's GLFW mouse callbacks
 * land (queued onto the render thread through Minecraft.execute), so
 * every move, click and scroll passes through one of them.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = {"onMove", "onButton", "onScroll"}, at = @At("HEAD"))
    private void borderlesswindow$markInput(CallbackInfo ci) {
        IdleFpsState.markInput();
    }

    /**
     * Scroll wheel while zoomed: changes the zoom level instead of the
     * hotbar slot. Declared after markInput so the scroll still counts as
     * input before it is cancelled.
     * <p>
     * At HEAD, ahead of NeoForge's MouseScrollingEvent, because while
     * zoomed the wheel belongs to the zoom. It honors the same two
     * options vanilla applies to the hotbar: Discrete Scrolling and
     * Scroll Sensitivity.
     */
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$scrollZoom(long windowPointer, double xOffset, double yOffset,
                                             CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (windowPointer != minecraft.getWindow().handle() || minecraft.getOverlay() != null) {
            return;
        }
        double amount = minecraft.options.discreteMouseScroll().get() ? Math.signum(yOffset) : yOffset;
        if (ZoomState.onScroll(amount * minecraft.options.mouseWheelSensitivity().get())) {
            ci.cancel();
        }
    }

    /**
     * Slower turning while zoomed, in proportion to the zoom: the final
     * rotation is scaled by the same multiplier as the FOV, so the world
     * keeps sliding across the screen at the speed it did unzoomed.
     * <p>
     * Wraps the turn call itself rather than the sensitivity, so it sits
     * after every branch vanilla and NeoForge pick -- cinematic camera,
     * the spyglass's slower aim, the CalculatePlayerTurn event. At 1x
     * (not zoomed, or the spyglass in use) the call goes through as is.
     */
    @WrapOperation(
            method = "turnPlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V"))
    private void borderlesswindow$scaleZoomTurn(LocalPlayer instance, double yRot, double xRot,
                                                Operation<Void> original) {
        double multiplier = ZoomState.getFovMultiplier();
        original.call(instance, yRot * multiplier, xRot * multiplier);
    }
}
