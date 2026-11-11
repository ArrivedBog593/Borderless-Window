package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.FovState;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The view's FOV: the water/lava squeeze slider (see FovState) and the
 * zoom (see ZoomState).
 * <p>
 * Since 26.1 the camera works out its own FOV, once per frame in update:
 * calculateFov for the view (the FOV setting times the modifier from
 * AbstractClientPlayer.getFieldOfViewModifier, see
 * AbstractClientPlayerMixin) and calculateHudFov for the held item, a
 * fixed 70 degrees. Both then go through modifyFovBasedOnDeathOrFluid.
 * Up to 1.21.11 all of this was GameRenderer.getFov.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    private boolean isPanoramicMode;

    /**
     * The water/lava squeeze, the only FOV effect vanilla's own slider
     * reaches. The original value is already scaled by that slider (a
     * lerp between 1.0 and 0.857), so ours multiplies on top -- with
     * FOV Effects at 0% in Minecraft there is nothing left to soften.
     * <p>
     * The only Mth.lerp in the method. The trailing * matches every
     * overload: NeoForge keeps vanilla's (FF)F as a deprecated shim and
     * moves the body, this lerp included, into a (FFZ)F one (which then
     * fires its ComputeFov event); a bare name would settle on one of the
     * two.
     */
    @ModifyExpressionValue(
            method = "modifyFovBasedOnDeathOrFluid*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float borderlesswindow$scaleFluidFov(float original) {
        return Mth.lerp(FovState.getFluidFactor(), 1.0F, original);
    }

    /**
     * The zoom itself: the FOV of the view, divided by the zoom level.
     * <p>
     * At RETURN, so it is the last word: on NeoForge after the ComputeFov
     * event, and on both loaders after vanilla's FOV setting, the FOV
     * effects and the per-source sliders -- the zoom narrows whatever they
     * left. Only the view: calculateHudFov sizes the held item, and
     * zooming it would blow the hand up to fill the screen. A panoramic
     * capture keeps its fixed 90 degrees.
     */
    @ModifyReturnValue(method = "calculateFov", at = @At("RETURN"))
    private float borderlesswindow$applyZoom(float original) {
        if (this.isPanoramicMode) {
            return original;
        }
        return (float) (original * ZoomState.getFovMultiplier());
    }
}
