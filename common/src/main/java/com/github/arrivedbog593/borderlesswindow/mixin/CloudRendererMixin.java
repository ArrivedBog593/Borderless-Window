package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.CloudState;
import net.minecraft.client.renderer.CloudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Overrides the height the cloud layer is drawn at, so the "Cloud Height"
 * option can raise or lower it. Since 1.21.11 the height is an
 * environment attribute (it can vary by biome) that LevelRenderer reads
 * every frame and hands to CloudRenderer.render; replacing that argument
 * moves the clouds and nothing else. Sodium only replaces how the cloud
 * mesh is built (buildMesh), so this covers both rendering paths -- the
 * feature works identically with and without Sodium.
 * <p>
 * Only the drawing moves. The same attribute also decides when an entity
 * counts as inside the clouds (Entity.isInClouds), which is gameplay and
 * stays where the dimension puts it.
 * <p>
 * TWO pass-through conditions:
 * <p>
 * 1. NO OVERRIDE SET. When CloudState holds its DEFAULT sentinel the
 *    original value is returned untouched, so biomes and dimensions from
 *    other mods keep whatever height they declare.
 * <p>
 * 2. THE DIMENSION HAS NO CLOUDS. This option moves clouds; it does not
 *    add them. Vanilla marks a cloudless dimension (the Nether, the End)
 *    with a fully transparent cloud color and skips the cloud pass before
 *    it ever gets here, so that holds by construction.
 * <p>
 * argsOnly + ordinal 0 = the first float parameter, the height
 * (partialTick is the second).
 */
@Mixin(CloudRenderer.class)
public abstract class CloudRendererMixin {

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float borderlesswindow$overrideCloudHeight(float height) {
        if (!CloudState.isOverridden()) {
            return height;
        }
        return (float) CloudState.getHeight();
    }
}
