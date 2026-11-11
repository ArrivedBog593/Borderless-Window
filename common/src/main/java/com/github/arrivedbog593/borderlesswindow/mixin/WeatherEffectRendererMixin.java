package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.SkyState;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.client.renderer.state.level.WeatherRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the falling rain and snow on request (see SkyState).
 * <p>
 * extractRenderState gathers the rain and snow columns around the camera
 * into the frame's WeatherRenderState; render then draws whatever columns
 * it finds there, and draws nothing when there are none. Emptying both
 * lists once they are gathered therefore covers both loaders with one
 * injection: NeoForge splits render into two overloads (one for its
 * custom weather renderers), but both read these lists.
 * <p>
 * NOT renderInstances, as up to 1.21.11. Since 26.1 render writes every
 * column into one BufferBuilder and then calls buildOrThrow, which throws
 * on an empty buffer: skipping renderInstances in the rain would crash.
 * <p>
 * The rain sounds and the splashes on the ground come from
 * tickRainParticles, a separate method, and stay.
 */
@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void borderlesswindow$hideWeather(CallbackInfo ci,
                                              @Local(argsOnly = true) WeatherRenderState renderState) {
        if (!SkyState.isWeatherEnabled()) {
            renderState.rainColumns.clear();
            renderState.snowColumns.clear();
        }
    }
}
