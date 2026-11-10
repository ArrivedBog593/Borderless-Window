package com.github.arrivedbog593.borderlesswindow.neoforge;

import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigScreen;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlay;
import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.general.HudState;
import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge entry point. Client-only mod that replaces Minecraft's exclusive
 * fullscreen with a 3-mode system (Windowed / Borderless / Fullscreen) and
 * adds a set of video and quality-of-life options. Every feature is
 * integrated into Sodium's video settings menu when Sodium is present and
 * configurable from the Mods menu (Config button) when it is not.
 * <p>
 * Everything else lives in common/; this class only connects it to
 * NeoForge: the client setup that restores the saved settings, the zoom
 * key, the three events common/ needs (HUD, block screen overlays, the
 * held item name's HUD layer), and the Config button. Each event handler
 * just translates the event for the loader-neutral class that decides.
 */
@Mod(BorderlessWindowNeoForge.MOD_ID)
@SuppressWarnings("unused")
public class BorderlessWindowNeoForge {

    public static final String MOD_ID = "borderlesswindow";

    public BorderlessWindowNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(BorderlessWindowNeoForge::onClientSetup);
        modEventBus.addListener(BorderlessWindowNeoForge::onRegisterKeyMappings);

        // Game event bus (not the mod bus) for the per-frame events.
        NeoForge.EVENT_BUS.addListener(BorderlessWindowNeoForge::onRenderGui);
        NeoForge.EVENT_BUS.addListener(BorderlessWindowNeoForge::onRenderBlockScreenEffect);
        NeoForge.EVENT_BUS.addListener(BorderlessWindowNeoForge::onRenderGuiLayer);

        // Registers the config screen shown by NeoForge's "Config" button
        // in the Mods menu. This is the UI for users without Sodium.
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> new BorderlessConfigScreen(parent));
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // enqueueWork runs on the game's main thread -- mandatory, since
        // GLFW calls are not thread-safe.
        event.enqueueWork(() ->
                BorderlessHandler.initializeFromConfig(Minecraft.getInstance().getWindow()));
    }

    /** Adds the zoom key to Controls; NeoForge also saves it in options.txt. */
    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(ZoomState.CATEGORY);
        event.register(ZoomState.KEY);
    }

    /** Fires once per rendered frame, after the HUD. */
    private static void onRenderGui(RenderGuiEvent.Post event) {
        FpsOverlay.render(event.getGuiGraphics());
    }

    /** Fires right before each view overlay is drawn; cancelling skips the draw. */
    private static void onRenderBlockScreenEffect(RenderBlockScreenEffectEvent event) {
        boolean hide = switch (event.getOverlayType()) {
            case WATER -> ScreenEffectHandler.shouldHideWater();
            case FIRE -> ScreenEffectHandler.shouldHideFire(event.getPlayer());
            case BLOCK -> false; // never touched, see ScreenEffectHandler
        };
        if (hide) {
            event.setCanceled(true);
        }
    }

    /**
     * Fires before each HUD layer; cancelling skips it. The held item's
     * name is a layer of its own on NeoForge, drawn through an overload
     * that common/'s GuiMixin does not reach (see there).
     */
    private static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (!HudState.isHeldItemNameEnabled()
                && event.getName().equals(VanillaGuiLayers.SELECTED_ITEM_NAME)) {
            event.setCanceled(true);
        }
    }
}
