package com.github.arrivedbog593.borderlesswindow.fabric.mixin;

import com.github.arrivedbog593.borderlesswindow.window.BorderlessHandler;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric's stand-in for NeoForge's FMLClientSetupEvent: restores the saved
 * settings, screen mode included, once the game window is up and running.
 * <p>
 * WHY THE SECOND FRAME, not the end of the Minecraft constructor: there
 * the window has just been created and has not handled a single event yet.
 * Switching it to Borderless at that point resized it but left it where
 * vanilla first centered it, straddling monitors. By the start of the
 * second frame the first one has gone through updateDisplay, which shows
 * the window and pumps its pending events -- the same state F11 always
 * runs in, and close to when NeoForge fires client setup (during the
 * initial resource load). Fabric's own client entrypoint runs even earlier,
 * inside the constructor, and Fabric API's lifecycle events would make
 * Fabric API a required dependency.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftInitMixin {

    @Shadow
    @Final
    private Window window;

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
        BorderlessHandler.initializeFromConfig(this.window);
    }
}
