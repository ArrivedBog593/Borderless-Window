package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.general.LanguageState;
import com.github.arrivedbog593.borderlesswindow.general.RecipeViewerSearchRefresh;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.CompletableFuture;

/**
 * Switches the language without the loading screen (see LanguageState).
 * <p>
 * onDone is where the language screen applies a new selection: it sets
 * LanguageManager's code and options.languageCode, then calls
 * reloadResourcePacks, a full reload, only when the language actually
 * changed. The options are saved when the screen closes
 * (OptionsSubScreen.removed), as in vanilla.
 * <p>
 * Instead of the full reload, LanguageManager reloads itself from the
 * resource manager already loaded: the same step the full reload runs
 * for it, with the same callback that rebuilds the search trees. The
 * language list it reads comes from the same packs, so nothing else
 * changes. Recipe viewers, which rebuild their search only on a full
 * reload, are then refreshed one by one (RecipeViewerSearchRefresh). The
 * return value is unused by onDone, but a completed future keeps the
 * contract.
 */
@Mixin(LanguageSelectScreen.class)
public abstract class LanguageSelectScreenMixin {

    @WrapOperation(
            method = "onDone",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/Minecraft;reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Void> borderlesswindow$reloadLanguageOnly(Minecraft minecraft,
                                                                       Operation<CompletableFuture<Void>> original) {
        if (!LanguageState.isFastReloadEnabled()) {
            return original.call(minecraft);
        }
        minecraft.getLanguageManager().onResourceManagerReload(minecraft.getResourceManager());
        RecipeViewerSearchRefresh.refresh(minecraft);
        return CompletableFuture.completedFuture(null);
    }
}
