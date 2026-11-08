package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records mouse activity for the idle FPS cap (see IdleFpsState).
 * <p>
 * onMove, onPress and onScroll are where vanilla's GLFW mouse callbacks
 * land (queued onto the render thread through Minecraft.execute), so
 * every move, click and scroll passes through one of them.
 */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {

    @Inject(method = {"onMove", "onPress", "onScroll"}, at = @At("HEAD"))
    private void borderlesswindow$markInput(CallbackInfo ci) {
        IdleFpsState.markInput();
    }
}
