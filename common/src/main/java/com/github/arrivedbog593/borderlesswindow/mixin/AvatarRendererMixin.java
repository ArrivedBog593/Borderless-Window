package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.NametagState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Avatar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * The scoreboard line drawn under a player's name (a "below name"
 * objective), hidden together with the name (see EntityRendererMixin).
 * AvatarRenderer fills it in after EntityRenderer's extractRenderState and
 * draws it even when the name itself is gone, so it is dropped where it
 * is read. Avatar covers players and mannequins; a mannequin is not a
 * player, so it follows the Mobs side of the setting.
 * <p>
 * The "*" selects every extractRenderState: besides the real one the
 * class has two compiler bridges (LivingEntity and Entity signatures)
 * without the call. A bare name takes only the first in the class file,
 * which works today only because the real one happens to come first.
 */
@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {

    @ModifyExpressionValue(
            method = "extractRenderState*",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/entity/ClientAvatarEntity;belowNameDisplay()Lnet/minecraft/network/chat/Component;"))
    private Component borderlesswindow$filterScoreLine(Component original,
                                                       @Local(argsOnly = true) Avatar avatar) {
        return NametagState.getMode().shows(avatar) ? original : null;
    }
}
