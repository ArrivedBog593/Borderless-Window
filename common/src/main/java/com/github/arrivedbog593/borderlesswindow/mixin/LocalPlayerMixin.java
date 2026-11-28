package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * The distortion's spin SPEED, split by source like its strength (see
 * LevelExtractorMixin), and the Totem of Undying animation.
 * <p>
 * Since 26.3 the player advances the spin itself, in tickSpinningEffect
 * (GameRenderer.tick did up to 26.2): it blends the speed from the two
 * sources (20 for the portal, 7 for Nausea, weighted by intensity).
 * Scaled here too, or a muted portal would still speed up the Nausea spin
 * while you stand in one. The player ticks after GameRenderer.tick, where
 * DistortionState settles whether it is in a portal this tick.
 */
@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @ModifyExpressionValue(
            method = "tickSpinningEffect",
            at = @At(value = "FIELD",
                    target = "Lnet/minecraft/client/player/LocalPlayer;portalEffectIntensity:F",
                    opcode = Opcodes.GETFIELD))
    private float borderlesswindow$scalePortalSpinSpeed(float original) {
        float factor = DistortionState.getPortalFactor();
        return original * factor * factor;
    }

    @ModifyExpressionValue(
            method = "tickSpinningEffect",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/player/LocalPlayer;"
                            + "getEffectBlendFactor(Lnet/minecraft/core/Holder;F)F"))
    private float borderlesswindow$scaleNauseaSpinSpeed(float original) {
        float factor = DistortionState.getNauseaSpinFactor();
        return original * factor * factor;
    }

    /**
     * The Totem of Undying animation (see ScreenEffectState). Cancelled
     * where it starts rather than where it is drawn, so nothing is left
     * ticking; the totem's particles and sound come from elsewhere and
     * stay. Since 26.3 the animation belongs to the player (an
     * ItemActivation it shares with its respawned self); up to 26.2 it was
     * GameRenderer.displayItemActivation.
     */
    @Inject(method = "displayItemActivation", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$skipTotemAnimation(ItemStack stack, CallbackInfo ci) {
        if (!ScreenEffectState.isTotemAnimationEnabled()) {
            ci.cancel();
        }
    }
}
