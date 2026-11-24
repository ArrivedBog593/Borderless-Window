package com.github.arrivedbog593.borderlesswindow.general;

import com.github.arrivedbog593.borderlesswindow.mixin.ReloadableResourceManagerAccessor;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.util.profiling.InactiveProfiler;
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Brings recipe viewers' search up to the new language after a fast
 * language switch (see LanguageState), without the full resource reload
 * they normally rely on. Checked against their 1.21.1 sources:
 * <p>
 * JEI rebuilds its ingredient filter (the search index of translated
 * names) in a small reload listener registered on the client resource
 * manager: on NeoForge a ResourceManagerReloadListener lambda from
 * mezz.jei.neoforge.JustEnoughItemsClient; on Fabric a
 * JeiIdentifiableResourceReloadListener with the id jei:resources_reload
 * (its sibling jei:lifecycle restarts JEI and is left alone), which
 * current Fabric API keeps out of the resource manager's list (see
 * fabricClientListeners). That one listener is run by itself, on this
 * thread, as a full reload would run it on the game executor. Before JEI has started (main menu) it does
 * nothing, and JEI starts in the new language anyway.
 * <p>
 * EMI bakes its search inside its own reload, which it starts itself
 * after every full resource reload (EmiReloadManager.reload(), only in a
 * world: see emiCanReload). The same call is made here; EMI does the
 * work on its own thread.
 * <p>
 * REI rebuilds its own cache: before every search it compares the
 * language code with the last one and drops the cache when it changed.
 * But it only searches again when the text changes, so the results on
 * screen would stay those of the previous language. Its public API
 * (REIRuntime.getInstance().getOverlay(), then
 * ScreenOverlay.queueReloadSearch(), checked in REI 16.0.799) runs the
 * current search again. JEI (forceUpdate in its listener) and EMI
 * (forceRecalculate at the end of its reload) already do that.
 * <p>
 * Everything goes through class names and reflection, so none of them is
 * a dependency. JEI's and EMI's are their internals, not an API (REI's
 * is its public API): if a version moves
 * them, the failure is logged once per viewer and the search simply stays
 * in the previous language until the next full reload (F3+T), as it would
 * without this class. It never throws.
 */
public final class RecipeViewerSearchRefresh {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String JEI_NEOFORGE_PACKAGE = "mezz.jei.neoforge.";
    private static final String JEI_FABRIC_WRAPPER = "mezz.jei.fabric.events.JeiIdentifiableResourceReloadListener";
    private static final String JEI_FABRIC_SEARCH_ID = "jei:resources_reload";
    private static final String FABRIC_RESOURCE_HELPER =
            "net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl";
    private static final String EMI_RELOAD_MANAGER = "dev.emi.emi.runtime.EmiReloadManager";
    private static final String EMI_RECIPE_MANAGER = "dev.emi.emi.runtime.ProxyRecipeManager";
    private static final String REI_RUNTIME = "me.shedaniel.rei.api.client.REIRuntime";
    private static final String REI_SCREEN_OVERLAY = "me.shedaniel.rei.api.client.overlay.ScreenOverlay";

    /**
     * Runs a listener's apply step at once: nothing to wait for. A method
     * reference, not a lambda: wait is generic, which a lambda cannot
     * implement.
     */
    private static final PreparableReloadListener.PreparationBarrier NO_WAIT =
            CompletableFuture::completedFuture;

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

    private static void refreshJei(Minecraft minecraft) {
        if (!(minecraft.getResourceManager() instanceof ReloadableResourceManager resourceManager)) {
            return;
        }
        List<PreparableReloadListener> managerListeners = List.copyOf(
                ((ReloadableResourceManagerAccessor) resourceManager).borderlesswindow$getListeners());
        Set<Object> listeners = Collections.newSetFromMap(new IdentityHashMap<>());
        listeners.addAll(managerListeners);
        try {
            listeners.addAll(fabricClientListeners(managerListeners));
        } catch (Throwable t) {
            logJeiFailure(t);
        }
        for (Object candidate : listeners) {
            if (!(candidate instanceof PreparableReloadListener listener)) {
                continue;
            }
            try {
                if (isJeiSearchListener(listener)) {
                    listener.reload(NO_WAIT, resourceManager,
                            InactiveProfiler.INSTANCE, InactiveProfiler.INSTANCE,
                            Runnable::run, Runnable::run).join();
                }
            } catch (Throwable t) {
                logJeiFailure(t);
            }
        }
    }

    /**
     * The full client listener list a reload would run, Fabric API's
     * listeners included, or nothing outside Fabric. Fabric API 0.116
     * (checked in 0.116.18) no longer puts its listeners in the resource
     * manager's own list: every reload passes a copy through its public
     * static ResourceManagerHelperImpl.sort(PackType, List), which adds
     * them. The same call is made here on a copy of the list. Its only
     * other effect is refilling Fabric's own set of added listeners, which
     * every reload does too; for client resources it needs no registry
     * lookup, and each registered listener comes back as the same instance.
     * <p>
     * Reading that set directly is not enough: JEI registers its listeners
     * at CLIENT_STARTED, after the first resource reload, and the set only
     * learns about them at the next reload.
     * <p>
     * Older Fabric API added the listeners to the manager's list itself,
     * which the caller already covers; its sort returned nothing.
     */
    private static Collection<?> fabricClientListeners(List<PreparableReloadListener> managerListeners)
            throws ReflectiveOperationException {
        Class<?> helper;
        try {
            helper = Class.forName(FABRIC_RESOURCE_HELPER);
        } catch (ClassNotFoundException e) {
            return List.of(); // not Fabric
        }
        Method sort = helper.getMethod("sort", PackType.class, List.class);
        Object sorted = sort.invoke(null, PackType.CLIENT_RESOURCES, new ArrayList<>(managerListeners));
        return sorted instanceof Collection<?> all ? List.copyOf(all) : List.of();
    }

    private static void logJeiFailure(Throwable t) {
        if (!jeiFailureLogged) {
            jeiFailureLogged = true;
            LOGGER.warn("Could not refresh JEI's search after the language change; "
                    + "it updates on the next full reload (F3+T)", t);
        }
    }

    private static boolean isJeiSearchListener(PreparableReloadListener listener) throws ReflectiveOperationException {
        String className = listener.getClass().getName();
        if (listener instanceof ResourceManagerReloadListener && className.startsWith(JEI_NEOFORGE_PACKAGE)) {
            return true;
        }
        if (className.equals(JEI_FABRIC_WRAPPER)) {
            Method getFabricId = listener.getClass().getMethod("getFabricId");
            return JEI_FABRIC_SEARCH_ID.equals(String.valueOf(getFabricId.invoke(listener)));
        }
        return false;
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
     * Only the level is checked here: on 1.21.1 a ClientLevel's recipe
     * manager is never null.
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
}
