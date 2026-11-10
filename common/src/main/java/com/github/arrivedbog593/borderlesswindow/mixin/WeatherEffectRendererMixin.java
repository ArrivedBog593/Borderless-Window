package com.github.arrivedbog593.borderlesswindow.mixin;

import com.github.arrivedbog593.borderlesswindow.visual.SkyState;
import net.minecraft.client.renderer.WeatherEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the falling rain and snow on request (see SkyState).
 * <p>
 * renderInstances writes the rain and the snow columns' quads, once for
 * each. It sits below render, which NeoForge splits into two overloads
 * (one for its custom weather renderers), so stopping it here covers both
 * loaders with one injection. The buffer render already asked for simply
 * stays empty and is drawn as nothing.
 * <p>
 * The rain sounds and the splashes on the ground come from
 * tickRainParticles, a separate method, and stay.
 */
@Mixin(WeatherEffectRenderer.class)
public abstract class WeatherEffectRendererMixin {

    @Inject(method = "renderInstances", at = @At("HEAD"), cancellable = true)
    private void borderlesswindow$hideWeather(CallbackInfo ci) {
        if (!SkyState.isWeatherEnabled()) {
            ci.cancel();
        }
    }
}
