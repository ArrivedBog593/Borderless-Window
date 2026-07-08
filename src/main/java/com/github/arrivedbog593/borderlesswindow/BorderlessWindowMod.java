package com.github.arrivedbog593.borderlesswindow;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Mod client-only que reemplaza el fullscreen exclusivo de Minecraft
 * por un sistema de 3 modos (Ventana / Sin bordes / Pantalla completa)
 * integrado al menu de video de Sodium.
 * <p>
 * Al arrancar el cliente, restaura el modo guardado en
 * config/borderlesswindow.json (via BorderlessHandler.initializeFromConfig).
 */
@Mod(BorderlessWindowMod.MODID)
public class BorderlessWindowMod {

    public static final String MODID = "borderlesswindow";

    public BorderlessWindowMod(IEventBus modEventBus) {
        modEventBus.addListener(this::onClientSetup);
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // enqueueWork corre en el hilo principal del juego -- obligatorio
        // porque las llamadas GLFW no son thread-safe.
        event.enqueueWork(() ->
                BorderlessHandler.initializeFromConfig(Minecraft.getInstance().getWindow()));
    }
}