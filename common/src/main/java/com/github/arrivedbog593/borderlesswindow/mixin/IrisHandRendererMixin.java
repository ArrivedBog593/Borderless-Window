package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.BobbingState;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Iris compatibility: with a shaderpack on, Iris skips vanilla's
 * renderItemInHand and draws the hand itself, from its own HandRenderer,
 * so the hand hooks in GameRendererMixin never run. This repeats them on
 * Iris's path.
 * <p>
 * @Pseudo because Iris is optional: without it the target class does not
 * exist and the mixin is skipped (BorderlessMixinPlugin skips it before
 * Mixin looks for the class, which would log a warning). Each injection is require = 0 so that a
 * future Iris that renames these methods only loses the two hand settings
 * under shaders, instead of crashing the game when HandRenderer loads.
 * Checked against Iris 1.11.4 for 26.2 (same class on NeoForge and Fabric).
 */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pathways.HandRenderer")
public abstract class IrisHandRendererMixin {

    /**
     * "Hide Hand" while zooming. canRender is Iris's own check (detached
     * camera, F1, sleeping, spectator), read by both the solid and the
     * translucent hand pass, so one hook covers both.
     */
    @ModifyReturnValue(method = "canRender", at = @At("RETURN"), require = 0)
    private boolean borderlesswindow$hideHandWhileZooming(boolean original) {
        return original && !ZoomState.shouldHideHand();
    }

    /**
     * Hand bobbing, toggled independently: Iris reads vanilla's View
     * Bobbing option itself before calling bobView, the only
     * Boolean.booleanValue in setupGlState. The hurt shake before it is
     * left alone, as on vanilla's path.
     * <p>
     * The suppression is for IntelliJ's Minecraft Development plugin: Iris
     * is not on the compile classpath, so it cannot find this call in
     * setupGlState and flags it as an error. Mixin resolves it at runtime.
     */
    @SuppressWarnings("UnresolvedMixinReference")
    @ModifyExpressionValue(method = "setupGlState",
            at = @At(value = "INVOKE", target = "Ljava/lang/Boolean;booleanValue()Z"),
            require = 0)
    private boolean borderlesswindow$shouldBobHand(boolean original) {
        return original && BobbingState.isHandBobEnabled();
    }
}
