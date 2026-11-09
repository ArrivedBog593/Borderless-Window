package com.github.arrivedbog593.borderlesswindow.neoforge;

import com.github.arrivedbog593.borderlesswindow.sodium.ScreenModeConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.ConfigEntryPointForge;

/**
 * How Sodium finds the config entry point on NeoForge: it scans for this
 * annotation, which only exists in Sodium's NeoForge API. All the pages
 * and options live in the common ScreenModeConfigEntryPoint.
 */
@SuppressWarnings("unused")
@ConfigEntryPointForge(BorderlessWindowNeoForge.MOD_ID)
public class NeoForgeSodiumConfigEntryPoint extends ScreenModeConfigEntryPoint {
}
