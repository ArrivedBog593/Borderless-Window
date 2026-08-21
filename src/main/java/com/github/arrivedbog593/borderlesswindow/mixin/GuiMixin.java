package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.screeneffect.CameraOverlayFilter;
import com.github.arrivedbog593.borderlesswindow.screeneffect.ScreenEffectState;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Toggles for the full-screen overlays vanilla draws from Gui: the
 * pumpkin blur, the powder snow frost border, the Nether portal warp
 * and the spyglass scope.
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
 * All three injections sit on the call sites inside renderCameraOverlays
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
}