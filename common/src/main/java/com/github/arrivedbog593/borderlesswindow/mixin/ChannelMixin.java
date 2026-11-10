package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.mojang.blaze3d.audio.Channel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Lowers the volume while the window is not focused (see
 * UnfocusedVolumeState).
 * <p>
 * Channel.setVolume() is the one place a playing sound's volume reaches
 * OpenAL (AL_GAIN on its source). SoundEngine works out that volume --
 * the sound's own times its category's times Master -- and hands it over
 * here when the sound starts, every tick for moving sounds, and whenever
 * a volume slider changes. Scaling it here scales Master for every sound
 * at once, and only the value OpenAL receives: SoundEngine's own number,
 * which decides whether a sound may start at all, stays vanilla's.
 */
@Mixin(Channel.class)
public abstract class ChannelMixin {

    @ModifyVariable(method = "setVolume", at = @At("HEAD"), argsOnly = true)
    private float borderlesswindow$scaleWhenUnfocused(float volume) {
        return volume * UnfocusedVolumeState.getGainFactor();
    }
}
