package com.github.arrivedbog593.borderlesswindow;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Client-only mod that replaces Minecraft's exclusive fullscreen with a
 * 3-mode system (Windowed / Borderless / Fullscreen) and adds a
 * configurable FPS overlay. Every feature is integrated into Sodium's
 * video settings menu when Sodium is present, and configurable from the
 * Mods menu (Config button) when it is not.
 * <p>
 * At client startup it restores the settings saved in
 * config/borderlesswindow.json (via BorderlessHandler.initializeFromConfig).
 */
@Mod(BorderlessWindowMod.MODID)
public class BorderlessWindowMod {

    public static final String MODID = "borderlesswindow";

    public BorderlessWindowMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onClientSetup);

        // FPS overlay rendering. Game event bus (not the mod bus):
        // RenderGuiEvent fires there once per rendered frame.
        NeoForge.EVENT_BUS.addListener(FpsOverlay::onRenderGui);

        // Registers the config screen shown by NeoForge's "Config" button
        // in the Mods menu. This is the UI for users without Sodium.
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new BorderlessConfigScreen(parent));
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // enqueueWork runs on the game's main thread -- mandatory, since
        // GLFW calls are not thread-safe.
        event.enqueueWork(() ->
                BorderlessHandler.initializeFromConfig(Minecraft.getInstance().getWindow()));
    }
}
