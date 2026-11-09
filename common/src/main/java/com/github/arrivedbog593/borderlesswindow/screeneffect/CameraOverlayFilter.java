package com.github.arrivedbog593.borderlesswindow.screeneffect;

import net.minecraft.resources.ResourceLocation;

/**
 * Decides which of Gui's camera overlays are drawn, mirroring the role
 * EnchantmentLevelFormatter plays for the enchantment numerals: the
 * mixin stays a thin delegation and the actual decisions live here in
 * plain Java.
 * <p>
 * WHY THE TEXTURE IS THE DISCRIMINATOR. Vanilla draws BOTH the pumpkin
 * blur and the powder snow outline through the same private method,
 * Gui.renderTextureOverlay, changing only the texture and the alpha. A
 * single injection therefore covers both, and the ResourceLocation is
 * the only thing telling them apart.
 * <p>
 * The constants are rebuilt here rather than read from Gui: they are
 * private static finals over there, so reaching them would need an
 * accessor mixin for two strings that have not changed since the
 * features were added. Equality on ResourceLocation is by value, so a
 * plain equals() is enough.
 * <p>
 * UNKNOWN TEXTURES PASS THROUGH. Anything that is not one of our two
 * returns true, so if another mod ever calls renderTextureOverlay with
 * its own texture we leave it alone. Defaulting to "draw it" is the
 * safe direction: the worst case is an option that does not cover
 * something, instead of us silently erasing another mod's overlay.
 */
public final class CameraOverlayFilter {

    private static final ResourceLocation PUMPKIN_BLUR =
            ResourceLocation.withDefaultNamespace("textures/misc/pumpkinblur.png");

    private static final ResourceLocation POWDER_SNOW_OUTLINE =
            ResourceLocation.withDefaultNamespace("textures/misc/powder_snow_outline.png");

    private CameraOverlayFilter() {
    }

    /**
     * @param texture the texture vanilla is about to draw full-screen
     * @return true to let the draw happen (vanilla behavior)
     */
    public static boolean shouldRenderTextureOverlay(ResourceLocation texture) {
        if (PUMPKIN_BLUR.equals(texture)) {
            return ScreenEffectState.isPumpkinOverlayEnabled();
        }
        if (POWDER_SNOW_OUTLINE.equals(texture)) {
            return ScreenEffectState.isFreezeOverlayEnabled();
        }
        return true;
    }
}