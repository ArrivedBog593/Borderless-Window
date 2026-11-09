package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.distortion.DistortionState;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * Percentage slider for the two distortion options in the no-Sodium
 * config screen.
 * <p>
 * Unlike CloudHeightSlider this one is parameterised rather than tied to
 * a single setting: both distortion options are the same 0..100 control
 * over a different value, so they share the widget and pass their own
 * getter and applier. Any future percentage option can reuse it.
 * <p>
 * SAVING follows the CloudHeightSlider rule, and for the same reason:
 * vanilla calls applyValue() on every step of a drag, so applying here
 * only touches memory and the distortion updates live. The write
 * happens once in onRelease(). Keyboard changes (arrow keys) never
 * produce a release, so BorderlessConfigScreen.onClose() persists too;
 * both paths are idempotent because saveCurrent() writes the full live
 * state.
 * <p>
 * 0% is labelled "Off" rather than "0%", matching how Minecraft labels
 * the bottom of its own Distortion Effects slider.
 */
public final class DistortionSlider extends AbstractSliderButton {

    private static final int STEPS =
            DistortionState.MAX_PERCENT - DistortionState.MIN_PERCENT;

    private final String nameKey;
    private final IntConsumer applier;

    public DistortionSlider(int x, int y, int width, int height,
                            String nameKey, IntSupplier getter, IntConsumer applier) {
        super(x, y, width, height, Component.empty(),
                percentToValue(getter.getAsInt()));
        this.nameKey = nameKey;
        this.applier = applier;
        this.updateMessage();
    }

    private static double percentToValue(int percent) {
        return (double) (percent - DistortionState.MIN_PERCENT) / STEPS;
    }

    private int valueToPercent() {
        return DistortionState.MIN_PERCENT + (int) Math.round(this.value * STEPS);
    }

    @Override
    protected void updateMessage() {
        int percent = valueToPercent();
        Component valueText = percent == DistortionState.MIN_PERCENT
                ? Component.translatable("borderlesswindow.options.distortion.off")
                : Component.translatable("borderlesswindow.options.distortion.percent", percent);
        this.setMessage(Component.translatable(this.nameKey)
                .append(": ").append(valueText));
    }

    /** Fired on every step of a drag. Memory only -- see the javadoc. */
    @Override
    protected void applyValue() {
        this.applier.accept(valueToPercent());
    }

    /** End of the drag: the value has settled, write it once. */
    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        DistortionState.persist();
    }
}