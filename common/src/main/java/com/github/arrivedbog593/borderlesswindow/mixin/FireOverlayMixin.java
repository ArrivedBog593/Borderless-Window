package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectState;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Fire Height: lowers the flames drawn over the view while burning (see
 * ScreenEffectState). Whether they are drawn at all is decided before
 * renderFire is called (NeoForge's event, Fabric's ScreenEffectRendererMixin);
 * this only moves them once they are.
 * <p>
 * renderFire draws two quads, one per side, each placed by one translate
 * whose second argument (-0.3) is the height. Lowering that argument
 * moves both flames down together and keeps their shape and tilt.
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class FireOverlayMixin {

    @ModifyArg(
            method = "renderFire",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"),
            index = 1)
    private static float borderlesswindow$lowerFire(float height) {
        return height - ScreenEffectState.fireDrop();
    }
}
