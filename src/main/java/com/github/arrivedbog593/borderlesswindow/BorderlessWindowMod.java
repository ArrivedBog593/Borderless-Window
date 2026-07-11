package com.github.arrivedbog593.borderlesswindow;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only mod that replaces Minecraft's exclusive fullscreen with a
 * 3-mode system (Windowed / Borderless / Fullscreen), integrated into
 * Sodium's video settings menu when Sodium is present.
 * <p>
 * At client startup it restores the mode saved in
 * config/borderlesswindow.json (via BorderlessHandler.initializeFromConfig).
 */
@Mod(BorderlessWindowMod.MODID)
public class BorderlessWindowMod {

    public static final String MODID = "borderlesswindow";

    public BorderlessWindowMod(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // enqueueWork runs on the game's main thread -- mandatory, since
        // GLFW calls are not thread-safe.
        event.enqueueWork(() ->
                BorderlessHandler.initializeFromConfig(Minecraft.getInstance().getWindow()));
    }
}