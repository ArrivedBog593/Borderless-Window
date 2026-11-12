package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.general.HudState;
import com.github.arrivedbog593.borderlesswindow.visual.CameraOverlayFilter;
import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectState;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Toggles for the full-screen overlays vanilla draws from Hud (part of
 * Gui up to 26.1.2): the pumpkin blur, the powder snow frost border, the
 * Nether portal warp and the spyglass scope. Also the held item's name
 * above the hotbar.
 * <p>
 * WHY A MIXIN AND NOT AN EVENT. The water and fire overlays have a
 * NeoForge event of their own (RenderBlockScreenEffectEvent, see
 * ScreenEffectHandler), so those needed no bytecode at all. These do
 * not: NeoForge exposes the whole group as a single GUI layer,
 * VanillaGuiLayers.CAMERA_OVERLAYS, and cancelling RenderGuiLayerEvent
 * for it would remove ALL of them at once -- including the vignette --
 * which is useless for per-overlay control. Verified against
 * VanillaGuiLayers in NeoForge 1.21.11: there is one constant for the
 * group, not one per overlay.
 * <p>
 * The same method draws the green Nausea texture; this class also
 * scales it to the Nausea slider (see DistortionState).
 * <p>
 * The three overlay injections sit on the call sites inside extractCameraOverlays
 * rather than inside the render methods themselves, so the methods stay
 * callable and untouched for any other mod that invokes them.
 * <p>
 * THE VIGNETTE IS DELIBERATELY NOT OFFERED. It is drawn from the same
 * method, but it doubles as the world border warning: extractVignette
 * tints the screen cyan as you approach the border. A plain toggle
 * would delete that warning along with the darkening, which is the same
 * category of mistake as removing the suffocation overlay -- hiding
 * information the game is trying to give you. Offering it would mean
 * keeping the border case alive, which needs the local distance value
 * from inside extractVignette; a separate feature if it is ever wanted.
 * <p>
 * MixinExtras rather than @Redirect, for the reason spelled out in
 * GameRendererMixin: @WrapWithCondition stacks with other mods touching
 * the same call sites, @Redirect claims them exclusively. Note the
 * package is injector.v2 here, NOT the wrapoperation path used by
 * EnchantmentMixin.
 */
@Mixin(Hud.class)
public abstract class HudMixin {

    /**
     * Covers BOTH calls to extractTextureOverlay inside
     * extractCameraOverlays -- the pumpkin blur and the powder snow
     * outline -- because @At without an ordinal matches every matching
     * invocation in the target method. CameraOverlayFilter tells them
     * apart by texture.
     * <p>
     * Only the vanilla pumpkin is affected: it reaches extractTextureOverlay
     * through the carved pumpkin's camera overlay texture. NeoForge adds a
     * call to IClientItemExtensions.renderFirstPersonOverlay for modded
     * worn items, which is a different call and stays untouched: another
     * mod's overlay is not ours to remove.
     */
    @WrapWithCondition(
            method = "extractCameraOverlays",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;extractTextureOverlay("
                            + "Lnet/minecraft/client/gui/GuiGraphicsExtractor;"
                            + "Lnet/minecraft/resources/Identifier;F)V"))
    private boolean borderlesswindow$shouldRenderTextureOverlay(Hud instance,
                                                                GuiGraphicsExtractor guiGraphics,
                                                                Identifier texture,
                                                                float alpha) {
        return CameraOverlayFilter.shouldRenderTextureOverlay(texture);
    }

    /**
     * The purple warp drawn while standing in a Nether portal. Vanilla
     * fades it in with the portal's own intensity and, while it shows,
     * draws no Nausea texture -- both decided before this call, so
     * skipping here changes nothing else.
     */
    @WrapWithCondition(
            method = "extractCameraOverlays",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;extractPortalOverlay("
                            + "Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"))
    private boolean borderlesswindow$shouldRenderPortalOverlay(Hud instance,
                                                               GuiGraphicsExtractor guiGraphics,
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
     * patched extractSpyglassOverlay to pull its texture from
     * IClientItemExtensions.getScopeOverlayTexture, which is how mods
     * like Spyglass Improvements supply custom scopes. Leaving the
     * method intact means their feature keeps working whenever this
     * option is left on.
     */
    @WrapWithCondition(
            method = "extractCameraOverlays",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/Hud;extractSpyglassOverlay("
                            + "Lnet/minecraft/client/gui/GuiGraphicsExtractor;F)V"))
    private boolean borderlesswindow$shouldRenderSpyglassOverlay(Hud instance,
                                                                 GuiGraphicsExtractor guiGraphics,
                                                                 float scopeScale) {
        return ScreenEffectState.isSpyglassOverlayEnabled();
    }

    /**
     * Mirrors our Nausea slider onto vanilla's nausea texture.
     * <p>
     * The green overlay is Mojang's accessibility trade: it appears when
     * Distortion Effects is below 100%, at intensity (1 - scale), so
     * removing the spin substitutes a static cue. Folding our factor into
     * the scale it reads makes our slider behave exactly like vanilla's --
     * 50% here draws the same overlay 50% there would.
     * <p>
     * Linear, not squared: vanilla uses this value linearly here while
     * squaring it in GameRenderer.renderLevel, so matching each site keeps
     * our slider in the same relationship to the texture that Minecraft's
     * own has.
     * <p>
     * The only floatValue() in the method, and only reached on the Nausea
     * branch: vanilla draws this texture only while no portal warp shows.
     * The portal guard covers the rest: standing in the destination
     * portal on cooldown, the warp has faded but the player is still in
     * the portal, where the Nausea slider does not speak (see
     * DistortionState), so vanilla's scale passes through untouched.
     */
    @ModifyExpressionValue(
            method = "extractCameraOverlays",
            at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F"))
    private float borderlesswindow$scaleNauseaOverlayCue(float original) {
        if (DistortionState.isInPortal()) {
            return original;
        }
        return original * DistortionState.getNauseaFactor();
    }

    /**
     * The held item's name above the hotbar (see HudState).
     * <p>
     * FABRIC'S HOOK ONLY, though it is applied on both loaders. Vanilla
     * draws the name through this one-argument method, called from
     * extractHotbarAndDecorations. NeoForge does not: its SELECTED_ITEM_NAME
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
    @Inject(method = "extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V",
            at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$hideHeldItemName(CallbackInfo ci) {
        if (!HudState.isHeldItemNameEnabled()) {
            ci.cancel();
        }
    }
}