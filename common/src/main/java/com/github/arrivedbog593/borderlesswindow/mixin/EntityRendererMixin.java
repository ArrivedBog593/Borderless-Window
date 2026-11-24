package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.NametagState;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nametags (see NametagState). renderNameTag is where every nametag is
 * drawn: EntityRenderer.render calls it once vanilla's shouldShowName
 * (or, on NeoForge, RenderNameTagEvent) allows it, and PlayerRenderer's
 * override draws the scoreboard line under a player's name through the
 * same super call. Cancelling here hides the whole tag, score line
 * included, whichever path asked for it.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "renderNameTag", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$filterNametag(Entity entity, Component displayName, PoseStack poseStack,
                                                MultiBufferSource bufferSource, int packedLight,
                                                float partialTick, CallbackInfo ci) {
        if (!NametagState.getMode().shows(entity)) {
            ci.cancel();
        }
    }
}
