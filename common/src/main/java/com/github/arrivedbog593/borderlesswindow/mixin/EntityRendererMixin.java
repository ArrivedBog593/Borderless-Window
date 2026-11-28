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
 * The score line under the name (a "below name" objective) goes with it.
 * Since 26.1 this same method fills it in, from Entity.belowNameDisplay,
 * into the state's scoreText (up to 1.21.11 AvatarRenderer did, for
 * players only, so that branch has an AvatarRendererMixin). It is drawn
 * even without the name, so it is cleared on its own.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void borderlesswindow$filterNametag(Entity entity, EntityRenderState renderState,
                                                float partialTick, CallbackInfo ci) {
        if (!NametagState.getMode().shows(entity)) {
            renderState.nameTag = null;
            renderState.scoreText = null;
        }
    }
}
