package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Presents the frame at the top of our Borderless window. WindowMixin has
 * the game render 1 px shorter than that window, at the monitor's size;
 * presentTexture copies the frame to the window starting at its bottom
 * edge (OpenGL counts rows from the bottom), so the copy is raised by
 * that 1 px: it fills the rows on the monitor, and the one below the
 * screen is left out. Minecraft 26.2 does the same for its own
 * non-exclusive fullscreen. In any other mode the frame and the window
 * have the same size, and nothing moves.
 * <p>
 * GlCommandEncoder is package-private, hence the string target. The blit
 * call is the only one in presentTexture; its eighth and tenth arguments
 * are the destination's bottom and top rows.
 */
@Mixin(targets = "com.mojang.blaze3d.opengl.GlCommandEncoder")
public abstract class GlCommandEncoderMixin {

    @ModifyArg(method = "presentTexture",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"),
            index = 7)
    private int borderlesswindow$raiseBottomRow(int dstY0) {
        return BorderlessHandler.hasHiddenBottomRow() ? dstY0 + 1 : dstY0;
    }

    @ModifyArg(method = "presentTexture",
            at = @At(value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/opengl/DirectStateAccess;blitFrameBuffers(IIIIIIIIIIII)V"),
            index = 9)
    private int borderlesswindow$raiseTopRow(int dstY1) {
        return BorderlessHandler.hasHiddenBottomRow() ? dstY1 + 1 : dstY1;
    }
}
