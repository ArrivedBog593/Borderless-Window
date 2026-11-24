package com.github.arrivedbog593.borderlesswindow.mixin;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exposes the client resource manager's reload listeners so
 * RecipeViewerSearchRefresh can run JEI's search listener on its own after
 * a fast language switch. Read only: the list is never modified.
 */
@Mixin(ReloadableResourceManager.class)
public interface ReloadableResourceManagerAccessor {

    @Accessor("listeners")
    List<PreparableReloadListener> borderlesswindow$getListeners();
}
