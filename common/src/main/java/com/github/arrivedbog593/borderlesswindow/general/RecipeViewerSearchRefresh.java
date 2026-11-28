package com.github.arrivedbog593.borderlesswindow.general;

import com.github.arrivedbog593.borderlesswindow.mixin.ReloadableResourceManagerAccessor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Brings recipe viewers' search up to the new language after a fast
 * language switch (see LanguageState), without the full resource reload
 * they normally rely on. Checked against their 26.2 jars:
 * <p>
 * JEI (30.48) rebuilds its ingredient filter (the search index of
 * translated names) in a small ResourceManagerReloadListener registered
 * for client resources. On NeoForge it is a lambda from
 * mezz.jei.neoforge.JustEnoughItemsClient, added through
 * AddClientReloadListenersEvent, which NeoForge sorts into the resource
 * manager's own list. On Fabric it is registered as jei:resources_reload
 * through Fabric API's resource loader v1 (its sibling jei:lifecycle,
 * for server data, restarts JEI and is left alone); Fabric API 0.161
 * keeps such listeners out of the manager's list and in its own map, see
 * fabricJeiListener. That one listener's onResourceManagerReload runs
 * by itself, on this thread, as a full reload would run it on the game
 * executor. Before JEI has started (main menu) it does nothing, and JEI
 * starts in the new language anyway.
 * <p>
 * EMI bakes its search inside its own reload, which it starts itself
 * after every full resource reload (EmiReloadManager.reload(), only in a
 * world: see emiCanReload). The same call is made here; EMI does the
 * work on its own thread. No EMI build for 26.2 was on Modrinth when
 * this was written; the code is the one checked on 1.21.1, kept for when
 * one appears.
 * <p>
 * REI rebuilds its own cache: before every search it compares the
 * language code with the last one (Argument.LAST_LANGUAGE, still there in
 * 26.2.820) and drops the cache when it changed.
 * But it only searches again when the text changes, so the results on
 * screen would stay those of the previous language. Its public API
 * (REIRuntime.getInstance().getOverlay(), then
 * ScreenOverlay.queueReloadSearch(), checked in REI 26.2.820 for both
 * loaders) runs the current search again. JEI (forceUpdate in its
 * listener) and EMI (forceRecalculate at the end of its reload) already
 * do that.
 * <p>
 * Everything goes through class names and reflection, so none of them is
 * a dependency. JEI's and EMI's are their internals, not an API (REI's
 * is its public API): if a version moves them, the failure is logged once
 * per viewer and the search simply stays in the previous language until
 * the next full reload (F3+T), as it would without this class. It never
 * throws.
 */
public final class RecipeViewerSearchRefresh {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String JEI_NEOFORGE_PACKAGE = "mezz.jei.neoforge.";
    private static final String JEI_FABRIC_SEARCH_ID = "jei:resources_reload";
    private static final String FABRIC_RESOURCE_LOADER = "net.fabricmc.fabric.impl.resource.ResourceLoaderImpl";
    private static final String EMI_RELOAD_MANAGER = "dev.emi.emi.runtime.EmiReloadManager";
    private static final String EMI_RECIPE_MANAGER = "dev.emi.emi.runtime.ProxyRecipeManager";
    private static final String REI_RUNTIME = "me.shedaniel.rei.api.client.REIRuntime";
    private static final String REI_SCREEN_OVERLAY = "me.shedaniel.rei.api.client.overlay.ScreenOverlay";

    private static boolean jeiFailureLogged = false;
    private static boolean emiFailureLogged = false;
    private static boolean reiFailureLogged = false;

    private RecipeViewerSearchRefresh() {
    }

    /** Called on the render thread right after the language was reloaded. */
    public static void refresh(Minecraft minecraft) {
        refreshJei(minecraft);
        refreshEmi(minecraft);
        refreshRei();
    }

    private static void refreshJei(Minecraft minecraft) {
        if (!(minecraft.getResourceManager() instanceof ReloadableResourceManager resourceManager)) {
            return;
        }
        List<ResourceManagerReloadListener> found = new ArrayList<>();
        for (PreparableReloadListener listener
                : ((ReloadableResourceManagerAccessor) resourceManager).borderlesswindow$getListeners()) {
            if (listener instanceof ResourceManagerReloadListener simple
                    && listener.getClass().getName().startsWith(JEI_NEOFORGE_PACKAGE)) {
                found.add(simple);
            }
        }
        try {
            if (fabricJeiListener() instanceof ResourceManagerReloadListener simple) {
                found.add(simple);
            }
        } catch (Throwable t) {
            logJeiFailure(t);
        }
        for (ResourceManagerReloadListener listener : found) {
            try {
                listener.onResourceManagerReload(resourceManager);
            } catch (Throwable t) {
                logJeiFailure(t);
            }
        }
    }

    /**
     * JEI's client reload listener, which Fabric API's resource loader v1
     * holds under JEI_FABRIC_SEARCH_ID, or null outside Fabric. Fabric API
     * 0.161 (checked in 0.161.0, resource loader v1 2.0.13) does not put
     * these listeners in the resource manager's own
     * list: every reload adds them to a copy of it (SimpleReloadInstanceMixin,
     * ResourceLoaderImpl.sort). ResourceLoaderImpl keeps them in its
     * addedReloaders map from the moment they are registered, so JEI's is
     * there even though JEI registers it at CLIENT_STARTED, after the first
     * resource reload. The map is Fabric API's internals, read by
     * reflection like the rest of this class.
     */
    private static Object fabricJeiListener() throws ReflectiveOperationException {
        Class<?> loader;
        try {
            loader = Class.forName(FABRIC_RESOURCE_LOADER);
        } catch (ClassNotFoundException e) {
            return null; // not Fabric
        }
        Object instance = loader.getMethod("get", PackType.class).invoke(null, PackType.CLIENT_RESOURCES);
        Field field = loader.getDeclaredField("addedReloaders");
        field.setAccessible(true);
        if (field.get(instance) instanceof Map<?, ?> reloaders) {
            for (Map.Entry<?, ?> entry : List.copyOf(reloaders.entrySet())) {
                if (JEI_FABRIC_SEARCH_ID.equals(String.valueOf(entry.getKey()))) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    private static void logJeiFailure(Throwable t) {
        if (!jeiFailureLogged) {
            jeiFailureLogged = true;
            LOGGER.warn("Could not refresh JEI's search after the language change; "
                    + "it updates on the next full reload (F3+T)", t);
        }
    }

    private static void refreshEmi(Minecraft minecraft) {
        Class<?> reloadManager;
        try {
            reloadManager = Class.forName(EMI_RELOAD_MANAGER);
        } catch (ClassNotFoundException e) {
            return; // EMI is not installed
        } catch (Throwable t) {
            logEmiFailure(t);
            return;
        }
        try {
            if (emiCanReload(minecraft)) {
                reloadManager.getMethod("reload").invoke(null);
            }
        } catch (Throwable t) {
            logEmiFailure(t);
        }
    }

    /**
     * The condition EMI itself checks before its post-reload reload. EMI
     * 1.1.x for 1.21.1 (checked in the 1.1.24 jar's MinecraftClientMixin)
     * asks for a level with a recipe manager; newer EMI asks
     * ProxyRecipeManager.isAvailable(), which does not exist in 1.1.x.
     * Without ProxyRecipeManager only the level is checked: a ClientLevel's
     * recipe manager is never null.
     */
    private static boolean emiCanReload(Minecraft minecraft) throws ReflectiveOperationException {
        Class<?> proxyRecipeManager;
        try {
            proxyRecipeManager = Class.forName(EMI_RECIPE_MANAGER);
        } catch (ClassNotFoundException e) {
            return minecraft.level != null;
        }
        return Boolean.TRUE.equals(proxyRecipeManager.getMethod("isAvailable").invoke(null));
    }

    private static void logEmiFailure(Throwable t) {
        if (!emiFailureLogged) {
            emiFailureLogged = true;
            LOGGER.warn("Could not refresh EMI's search after the language change; "
                    + "it updates on the next full reload (F3+T)", t);
        }
    }

    private static void refreshRei() {
        Class<?> runtime;
        try {
            runtime = Class.forName(REI_RUNTIME);
        } catch (ClassNotFoundException e) {
            return; // REI is not installed
        } catch (Throwable t) {
            logReiFailure(t);
            return;
        }
        try {
            Object instance = runtime.getMethod("getInstance").invoke(null);
            Object overlay = runtime.getMethod("getOverlay").invoke(instance);
            if (overlay instanceof Optional<?> optional && optional.isPresent()) {
                Object screenOverlay = optional.get();
                Class.forName(REI_SCREEN_OVERLAY).getMethod("queueReloadSearch").invoke(screenOverlay);
            }
        } catch (Throwable t) {
            logReiFailure(t);
        }
    }

    private static void logReiFailure(Throwable t) {
        if (!reiFailureLogged) {
            reiFailureLogged = true;
            LOGGER.warn("Could not refresh REI's search after the language change; "
                    + "it updates when the search text changes", t);
        }
    }
}
