package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Restores the saved settings once the game window is up and running, on
 * both loaders.
 * <p>
 * WHY THE SECOND FRAME, not the end of the Minecraft constructor: the
 * screen mode is vanilla's since 26.3, but a config from an older version
 * still holds one, applied once (see BorderlessHandler.initializeFromConfig),
 * and switching modes belongs where F11 always runs: in a frame, on the
 * thread that owns the window, after the first one has shown it. Fabric's
 * own client entrypoint runs inside the constructor, and Fabric API's
 * lifecycle events would make Fabric API a required dependency.
 * <p>
 * WHY NOT NEOFORGE'S FMLClientSetupEvent (used up to 26.1.2): since 26.1
 * its enqueueWork runs on a loader thread (modloading-sync-worker), not on
 * the thread that owns the window, while that one waits in the Minecraft
 * constructor for mod loading to finish. Changing the window from there
 * hung the game on a white window with NeoForge's early loading screen off.
 * runTick always runs on the window's thread.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftInitMixin {

    @Unique
    private int borderlesswindow$framesBeforeInit = 1;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void borderlesswindow$initializeFromConfig(boolean renderLevel, CallbackInfo ci) {
        if (BorderlessHandler.isInitialized()) {
            return;
        }
        if (this.borderlesswindow$framesBeforeInit-- > 0) {
            return;
        }
        BorderlessHandler.initializeFromConfig();
    }
}
