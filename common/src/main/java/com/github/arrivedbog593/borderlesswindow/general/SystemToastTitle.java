package com.github.arrivedbog593.borderlesswindow.general;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Added to every SystemToast by SystemToastMixin: the title it was last
 * given, as a Component, so ToastState can tell the server pack download
 * apart (unlike the other notices it has no fixed id). Since 26.2 the
 * toast itself only keeps the title already split into lines, which loses
 * its translation key.
 */
public interface SystemToastTitle {

    @Nullable
    Component borderlesswindow$getTitle();
}
