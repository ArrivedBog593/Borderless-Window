package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.general.ToastState;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastComponent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Deque;

/**
 * Drops the toasts ToastState turns off. Three hooks, so a toggle also
 * applies to toasts that arrived before it was switched off:
 * - addToast, the single door every toast goes through: a toast that is
 *   off is never queued.
 * - render: removes queued toasts that are now off. A burst such as
 *   "/advancement grant @s everything" queues a hundred toasts at once,
 *   and vanilla shows five at a time, so without this they would keep
 *   coming for minutes. The ones already on screen finish their normal
 *   slide-out instead of vanishing.
 * - getToast: RecipeToast.addOrUpdate and SystemToast.addOrUpdate add to
 *   (and restart the timer of) a toast that is already on screen instead
 *   of queuing a new one. Reporting a toast that is now off as absent
 *   sends them to addToast, which drops it. The one on screen then just
 *   runs out its time. SystemToast.forceHide (the pack download) also
 *   goes through here; that toast hides on its own a few seconds later.
 */
@Mixin(ToastComponent.class)
public class ToastComponentMixin {

    @Shadow
    @Final
    private Deque<Toast> queued;

    @Inject(method = "addToast", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$filterToast(Toast toast, CallbackInfo ci) {
        if (!ToastState.shouldShow(toast)) {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void borderlesswindow$dropQueuedToasts(CallbackInfo ci) {
        if (!this.queued.isEmpty()) {
            this.queued.removeIf(toast -> !ToastState.shouldShow(toast));
        }
    }

    @ModifyReturnValue(method = "getToast", at = @At("RETURN"))
    private Toast borderlesswindow$hideDisabledToast(Toast original) {
        return original != null && !ToastState.shouldShow(original) ? null : original;
    }
}
