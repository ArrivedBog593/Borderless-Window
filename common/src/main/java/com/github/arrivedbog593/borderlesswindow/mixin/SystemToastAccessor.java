package com.github.arrivedbog593.borderlesswindow.mixin;

import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes SystemToast's private title so ToastState can tell the server
 * pack download apart: unlike the other notices it has no fixed id.
 */
@Mixin(SystemToast.class)
public interface SystemToastAccessor {

    @Accessor("title")
    Component borderlesswindow$getTitle();
}
