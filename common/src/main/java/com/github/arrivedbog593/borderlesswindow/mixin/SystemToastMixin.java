package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.general.SystemToastTitle;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Remembers each SystemToast's title (see SystemToastTitle). Up to 26.1
 * the toast kept it in a title field, read through an accessor; since
 * 26.2 it keeps only titleLines, the title already split to fit the
 * toast's width.
 * <p>
 * The private update(Component, Component) is where every title arrives:
 * the constructor and reset (addOrUpdate on a toast already on screen)
 * both call it. The descriptor is spelled out because Toast's own
 * update(ToastManager, long), which SystemToast overrides, shares the name.
 * The constructor runs before ToastManager.addToast, so the title is
 * always there when ToastState asks.
 */
@Mixin(SystemToast.class)
public abstract class SystemToastMixin implements SystemToastTitle {

    @Unique
    private @Nullable Component borderlesswindow$title;

    @Inject(method = "update(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"))
    private void borderlesswindow$rememberTitle(Component title, @Nullable Component message, CallbackInfo ci) {
        this.borderlesswindow$title = title;
    }

    @Override
    public @Nullable Component borderlesswindow$getTitle() {
        return this.borderlesswindow$title;
    }
}
