package com.github.arrivedbog593.borderlesswindow.neoforge;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigScreen;
import com.github.arrivedbog593.borderlesswindow.fog.FogHandler;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlay;
import com.github.arrivedbog593.borderlesswindow.screeneffect.ScreenEffectHandler;
import com.github.arrivedbog593.borderlesswindow.window.BorderlessHandler;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

/**
 * NeoForge entry point. Client-only mod that replaces Minecraft's exclusive
 * fullscreen with a 3-mode system (Windowed / Borderless / Fullscreen) and
 * adds a set of video and quality-of-life options. Every feature is
 * integrated into Sodium's video settings menu when Sodium is present and
 * configurable from the Mods menu (Config button) when it is not.
 * <p>
 * Everything else lives in common/; this class only connects it to
 * NeoForge: the client setup that restores the saved settings, the three
 * events common/ needs (HUD, fog, block screen overlays), and the Config
 * button. Each event handler just translates the event for the
 * loader-neutral class that decides.
 */
@Mod(BorderlessWindowNeoForge.MOD_ID)
@SuppressWarnings("unused")
public class BorderlessWindowNeoForge {

    public static final String MOD_ID = "borderlesswindow";

    public BorderlessWindowNeoForge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(BorderlessWindowNeoForge::onClientSetup);

        // Game event bus (not the mod bus) for the per-frame events.
        NeoForge.EVENT_BUS.addListener(BorderlessWindowNeoForge::onRenderGui);
        NeoForge.EVENT_BUS.addListener(BorderlessWindowNeoForge::onRenderFog);
        NeoForge.EVENT_BUS.addListener(BorderlessWindowNeoForge::onRenderBlockScreenEffect);

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

    /** Fires once per rendered frame, after the HUD. */
    private static void onRenderGui(RenderGuiEvent.Post event) {
        FpsOverlay.render(event.getGuiGraphics());
    }

    /**
     * Fires every time vanilla sets up fog. NeoForge requires cancelling
     * the event for the new plane distances to take effect.
     */
    private static void onRenderFog(ViewportEvent.RenderFog event) {
        if (FogHandler.shouldRemove(event.getType(), event.getCamera())) {
            event.setNearPlaneDistance(FogHandler.NO_FOG_NEAR);
            event.setFarPlaneDistance(FogHandler.NO_FOG_FAR);
            event.setCanceled(true);
        }
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
}
