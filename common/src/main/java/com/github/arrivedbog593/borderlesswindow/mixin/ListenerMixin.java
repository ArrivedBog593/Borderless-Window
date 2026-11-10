package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.mojang.blaze3d.audio.Listener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Lowers the volume while the window is not focused (see
 * UnfocusedVolumeState).
 * <p>
 * Listener.setGain() is where vanilla applies the Master Volume: it sends
 * the value to OpenAL (AL_GAIN on the listener) and stores it in a field.
 * Only the OpenAL argument is scaled, never the stored field -- SoundEngine
 * reads that field to decide whether a sound may start at all, and must
 * keep seeing vanilla's value.
 */
@Mixin(Listener.class)
public abstract class ListenerMixin {

    @ModifyArg(method = "setGain",
            at = @At(value = "INVOKE", target = "Lorg/lwjgl/openal/AL10;alListenerf(IF)V"),
            index = 1)
    private float borderlesswindow$scaleWhenUnfocused(float gain) {
        return gain * UnfocusedVolumeState.getGainFactor();
    }
}
