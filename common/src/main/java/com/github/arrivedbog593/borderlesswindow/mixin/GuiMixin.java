package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.general.HudState;
import com.github.arrivedbog593.borderlesswindow.visual.CameraOverlayFilter;
import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectState;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Toggles for the full-screen overlays vanilla draws from Gui: the
 * pumpkin blur, the powder snow frost border, the Nether portal warp
 * and the spyglass scope. Also the held item's name above the hotbar.
 * <p>
 * WHY A MIXIN AND NOT AN EVENT. The water and fire overlays have a
 * NeoForge event of their own (RenderBlockScreenEffectEvent, see
 * ScreenEffectHandler), so those needed no bytecode at all. These do
 * not: NeoForge exposes the whole group as a single GUI layer,
 * VanillaGuiLayers.CAMERA_OVERLAYS, and cancelling RenderGuiLayerEvent
 * for it would remove ALL of them at once -- including the vignette --
 * which is useless for per-overlay control. Verified against
 * VanillaGuiLayers in NeoForge 1.21.1: there is one constant for the
 * group, not one per overlay.
 * <p>
 * The three overlay injections sit on the call sites inside renderCameraOverlays
 * rather than inside the render methods themselves, so the methods stay
 * callable and untouched for any other mod that invokes them.
 * <p>
 * THE VIGNETTE IS DELIBERATELY NOT OFFERED. It is drawn from the same
 * method, but it doubles as the world border warning: renderVignette
 * tints the screen cyan as you approach the border. A plain toggle
 * would delete that warning along with the darkening, which is the same
 * category of mistake as removing the suffocation overlay -- hiding
 * information the game is trying to give you. Offering it would mean
 * keeping the border case alive, which needs the local distance value
 * from inside renderVignette; a separate feature if it is ever wanted.
 * <p>
 * MixinExtras rather than @Redirect, for the reason spelled out in
 * GameRendererMixin: @WrapWithCondition stacks with other mods touching
 * the same call sites, @Redirect claims them exclusively. Note the
 * package is injector.v2 here, NOT the wrapoperation path used by
 * EnchantmentMixin.
 */
@Mixin(Gui.class)
public abstract class GuiMixin {

    /**
     * Covers BOTH calls to renderTextureOverlay inside
     * renderCameraOverlays -- the pumpkin blur and the powder snow
     * outline -- because @At without an ordinal matches every matching
     * invocation in the target method. CameraOverlayFilter tells them
     * apart by texture.
     * <p>
     * Only the vanilla pumpkin is affected. NeoForge added an else
     * branch calling IClientItemExtensions.renderHelmetOverlay for
     * modded helmets, which is a different call and stays untouched:
     * another mod's helmet overlay is not ours to remove.
     */
    @WrapWithCondition(
            method = "renderCameraOverlays",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderTextureOverlay("
                            + "Lnet/minecraft/client/gui/GuiGraphics;"
                            + "Lnet/minecraft/resources/ResourceLocation;F)V"))
    private boolean borderlesswindow$shouldRenderTextureOverlay(Gui instance,
                                                                GuiGraphics guiGraphics,
                                                                ResourceLocation texture,
                                                                float alpha) {
        return CameraOverlayFilter.shouldRenderTextureOverlay(texture);
    }

    /**
     * The purple warp drawn while standing in a Nether portal. Vanilla
     * fades it in with the spin intensity and skips it entirely under
     * Nausea, both of which are decided before this call, so skipping
     * here changes nothing else.
     */
    @WrapWithCondition(
            method = "renderCameraOverlays",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderPortalOverlay("
                            + "Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    private boolean borderlesswindow$shouldRenderPortalOverlay(Gui instance,
                                                               GuiGraphics guiGraphics,
                                                               float alpha) {
        return ScreenEffectState.isPortalOverlayEnabled();
    }

    /**
     * The scope texture and the four black bars drawn while looking
     * through a spyglass. Skipping the call drops all five, which is
     * what "off" should mean -- the zoom itself lives in GameRenderer
     * and is not touched.
     * <p>
     * Wrapping the CALL rather than the method matters here: NeoForge
     * patched renderSpyglassOverlay to pull its texture from
     * IClientItemExtensions.getScopeOverlayTexture, which is how mods
     * like Spyglass Improvements supply custom scopes. Leaving the
     * method intact means their feature keeps working whenever this
     * option is left on.
     */
    @WrapWithCondition(
            method = "renderCameraOverlays",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Gui;renderSpyglassOverlay("
                            + "Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    private boolean borderlesswindow$shouldRenderSpyglassOverlay(Gui instance,
                                                                 GuiGraphics guiGraphics,
                                                                 float scopeScale) {
        return ScreenEffectState.isSpyglassOverlayEnabled();
    }

    /**
     * The held item's name above the hotbar (see HudState).
     * <p>
     * FABRIC'S HOOK ONLY, though it is applied on both loaders. Vanilla
     * draws the name through this one-argument method, called from
     * renderHotbarAndDecorations. NeoForge does not: its SELECTED_ITEM_NAME
     * HUD layer calls a two-argument overload (with a y shift) directly,
     * so on NeoForge this injection is never reached and
     * BorderlessWindowNeoForge cancels that layer through
     * RenderGuiLayerEvent instead.
     * <p>
     * The descriptor is spelled out on purpose. A bare method name does
     * NOT match every overload: Mixin settles on a single one, which on
     * NeoForge was this unused method -- the first version of this option
     * did nothing there.
     */
    @Inject(method = "renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V",
            at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$hideHeldItemName(CallbackInfo ci) {
        if (!HudState.isHeldItemNameEnabled()) {
            ci.cancel();
        }
    }
}