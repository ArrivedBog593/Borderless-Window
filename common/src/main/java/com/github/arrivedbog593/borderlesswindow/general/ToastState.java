package com.github.arrivedbog593.borderlesswindow.general;

import com.github.arrivedbog593.borderlesswindow.config.BorderlessConfigFile;
import net.minecraft.client.gui.components.toasts.AdvancementToast;
import net.minecraft.client.gui.components.toasts.RecipeToast;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * Notification (toast) toggles, one per kind of pop-up in the top-right
 * corner. Every toast goes through ToastComponent.addToast, so
 * ToastComponentMixin asks {@link #shouldShow} there and drops the ones
 * that are turned off before they are ever queued. Turning a toggle off
 * also clears those toasts from the queue; the ones already on screen
 * finish normally (see ToastComponentMixin).
 * <p>
 * Vanilla's SystemToast covers both harmless notices and real errors, so
 * it is never filtered as a whole: only the notices below, picked by
 * their id (or, for the pack download, by their title, see
 * SystemToastTitle), each with its own
 * toggle. Everything else -- resource packs or worlds that fail, low disk
 * space, chunk load/save failures, the world backup the player asked for,
 * the regional playtime reminders -- always shows. Toasts from other mods
 * are left alone too.
 * <p>
 * Verified against the 1.21.1 source:
 * - Advancement toasts come from ClientAdvancements, recipe toasts from
 *   RecipeToast.addOrUpdate. Dropping an advancement toast also drops
 *   the challenge fanfare, which AdvancementToast plays from render().
 * - Tutorial toasts come from the tutorial steps, the bundle tutorial and
 *   the one-time Social Interactions reminder (Minecraft.tick). The
 *   tutorial itself advances on what the player does, not on the toast;
 *   the steps keep calling hide()/updateProgress() on a toast that was
 *   never shown, which does nothing.
 * - addOrUpdate (recipes, system) first looks for a toast already on
 *   screen; with ours dropped it finds none and calls addToast again,
 *   which drops it again.
 * - The server pack download uses a fresh SystemToastId per download, so
 *   it is recognized by its "download.pack.title" title. When a download
 *   fails the title becomes "download.pack.failed", which always shows.
 * <p>
 * true = shown (vanilla, the default). The setters persist immediately;
 * init() is the only entry that does not save.
 */
public final class ToastState {

    private static final String PACK_DOWNLOAD_TITLE = "download.pack.title";

    private static boolean advancements = true;
    private static boolean recipes = true;
    private static boolean tutorial = true;
    private static boolean unverifiedChat = true;
    private static boolean narrator = true;
    private static boolean packDownload = true;

    private ToastState() {
    }

    /** Called once at startup with the values loaded from disk. */
    public static void init(boolean loadedAdvancements, boolean loadedRecipes,
                            boolean loadedTutorial, boolean loadedUnverifiedChat,
                            boolean loadedNarrator, boolean loadedPackDownload) {
        advancements = loadedAdvancements;
        recipes = loadedRecipes;
        tutorial = loadedTutorial;
        unverifiedChat = loadedUnverifiedChat;
        narrator = loadedNarrator;
        packDownload = loadedPackDownload;
    }

    /** Whether this toast may be queued. Called from ToastComponentMixin. */
    public static boolean shouldShow(Toast toast) {
        if (toast instanceof AdvancementToast) {
            return advancements;
        }
        if (toast instanceof RecipeToast) {
            return recipes;
        }
        if (toast instanceof TutorialToast) {
            return tutorial;
        }
        if (toast instanceof SystemToast systemToast) {
            SystemToast.SystemToastId id = systemToast.getToken();
            if (id == SystemToast.SystemToastId.UNSECURE_SERVER_WARNING) {
                return unverifiedChat;
            }
            if (id == SystemToast.SystemToastId.NARRATOR_TOGGLE) {
                return narrator;
            }
            if (isPackDownloadProgress(systemToast)) {
                return packDownload;
            }
        }
        return true;
    }

    private static boolean isPackDownloadProgress(SystemToast toast) {
        Component title = ((SystemToastTitle) toast).borderlesswindow$getTitle();
        return title != null
                && title.getContents() instanceof TranslatableContents contents
                && PACK_DOWNLOAD_TITLE.equals(contents.getKey());
    }

    public static boolean isAdvancementsEnabled() {
        return advancements;
    }

    public static void setAdvancementsEnabled(boolean enabled) {
        advancements = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isRecipesEnabled() {
        return recipes;
    }

    public static void setRecipesEnabled(boolean enabled) {
        recipes = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isTutorialEnabled() {
        return tutorial;
    }

    public static void setTutorialEnabled(boolean enabled) {
        tutorial = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isUnverifiedChatEnabled() {
        return unverifiedChat;
    }

    public static void setUnverifiedChatEnabled(boolean enabled) {
        unverifiedChat = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isNarratorEnabled() {
        return narrator;
    }

    public static void setNarratorEnabled(boolean enabled) {
        narrator = enabled;
        BorderlessConfigFile.saveCurrent();
    }

    public static boolean isPackDownloadEnabled() {
        return packDownload;
    }

    public static void setPackDownloadEnabled(boolean enabled) {
        packDownload = enabled;
        BorderlessConfigFile.saveCurrent();
    }
}
