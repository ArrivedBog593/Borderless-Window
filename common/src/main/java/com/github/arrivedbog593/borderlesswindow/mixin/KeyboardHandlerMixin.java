package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records keyboard activity for the idle FPS cap (see IdleFpsState).
 * <p>
 * keyPress is where vanilla's key events land (SDL's since 26.3, queued
 * onto the render thread by SDLEventHandler) -- presses, repeats and
 * releases alike. Typed characters always come with a keyPress, so
 * charTyped is not hooked.
 */
@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {

    @Inject(method = "keyPress", at = @At("HEAD"))
    private void borderlesswindow$markInput(CallbackInfo ci) {
        IdleFpsState.markInput();
    }
}
