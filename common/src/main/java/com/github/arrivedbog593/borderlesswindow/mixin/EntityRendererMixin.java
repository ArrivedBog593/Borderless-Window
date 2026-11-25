package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.NametagState;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Nametags (see NametagState). Since 1.21.2 an entity is drawn from a
 * render state, and the nametag is drawn only when extractRenderState
 * filled in its text. That is decided at the end of this method: vanilla
 * asks shouldShowName, and on NeoForge RenderNameTagEvent may also force
 * it on. Clearing the text at the very end, after both, hides it whichever
 * way it was turned on. Every renderer calls this one first from its own
 * extractRenderState, and none of them sets the text again afterwards.
 * <p>
 * A player's score line under the name is filled in separately, by
 * AvatarRenderer: see AvatarRendererMixin.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void borderlesswindow$filterNametag(Entity entity, EntityRenderState renderState,
                                                float partialTick, CallbackInfo ci) {
        if (renderState.nameTag != null && !NametagState.getMode().shows(entity)) {
            renderState.nameTag = null;
        }
    }
}
