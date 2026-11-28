package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fire Height: lowers the flames drawn over the view while burning (see
 * ScreenEffectState). Whether they are drawn at all is decided before
 * submitFire is called (NeoForge's event, Fabric's ScreenEffectRendererMixin);
 * this only moves them once they are.
 * <p>
 * Since 26.2, submitFire places each of the two quads with a Matrix4f
 * (translate to the side and 0.3 down, then a small turn around the
 * vertical axis) and hands it to buildFireQuad. Moving that matrix down
 * at the start of buildFireQuad is the same as a lower translate: a turn
 * around the vertical axis leaves heights alone. Both flames move down
 * together and keep their shape and tilt. The matrix is submitFire's own
 * copy, set again from the pose before each quad, so nothing leaks.
 */
@Mixin(ScreenEffectRenderer.class)
public abstract class FireOverlayMixin {

    @Inject(method = "buildFireQuad", at = @At("HEAD"))
    private static void borderlesswindow$lowerFire(TextureAtlasSprite sprite, VertexConsumer builder,
                                                   Matrix4f pose, CallbackInfo ci) {
        float drop = ScreenEffectState.fireDrop();
        if (drop != 0.0F) {
            pose.translate(0.0F, -drop, 0.0F);
        }
    }
}
