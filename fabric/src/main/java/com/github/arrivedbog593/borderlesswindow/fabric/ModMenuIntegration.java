package com.github.arrivedbog593.borderlesswindow.fabric;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Fabric's stand-in for NeoForge's Config button: Fabric has no mod list of
 * its own, so the config screen for users without Sodium opens from Mod
 * Menu. Optional -- Mod Menu only loads this class when it is installed.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return BorderlessConfigScreen::new;
    }
}
