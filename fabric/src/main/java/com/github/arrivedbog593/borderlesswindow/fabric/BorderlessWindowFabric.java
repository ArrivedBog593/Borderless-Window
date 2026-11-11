package com.github.arrivedbog593.borderlesswindow.fabric;

import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

/**
 * Fabric client entry point. Only registers the zoom key and its Controls
 * section: it has to happen here, before the Minecraft constructor builds
 * Options and reads the saved key bindings, so the key shows up in
 * Controls and keeps its binding. The saved settings are restored later, on the second frame
 * (see MinecraftInitMixin).
 */
public class BorderlessWindowFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyMapping.Category.register(ZoomState.CATEGORY.id());
        KeyMappingHelper.registerKeyMapping(ZoomState.KEY);
    }
}
