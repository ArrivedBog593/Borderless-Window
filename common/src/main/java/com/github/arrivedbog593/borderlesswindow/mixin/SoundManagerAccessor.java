package com.github.arrivedbog593.borderlesswindow.mixin;

import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes SoundManager's private SoundEngine so UnfocusedVolumeState can
 * call updateCategoryVolume directly (see UnfocusedVolumeState.refresh).
 */
@Mixin(SoundManager.class)
public interface SoundManagerAccessor {

    @Accessor("soundEngine")
    SoundEngine borderlesswindow$getSoundEngine();
}
