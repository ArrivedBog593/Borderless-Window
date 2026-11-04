package com.github.arrivedbog593.borderlesswindow.config;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * Generic stepped integer slider for the no-Sodium config screen: a
 * [min, max] range moved in fixed steps, with the label text supplied by
 * the caller. The Sodium side gets the same presentation from
 * IntegerOptionBuilder.setRange(min, max, step) + setValueFormatter, so
 * both UIs can share one formatter.
 * <p>
 * A "Default"/"Off" slot is just the leftmost value of the range with its
 * own label in the formatter -- the holder decides what that value means.
 * <p>
 * SAVING follows the CloudHeightSlider rule: applyValue() fires on every
 * step of a drag, so the applier only touches memory and the persister
 * runs once in onRelease(). BorderlessConfigScreen.onClose() persists too,
 * for keyboard changes.
 * <p>
 * KEYBOARD: vanilla moves a slider by one PIXEL per arrow press, which is
 * fine for a 100-step control but takes a couple dozen presses per notch
 * on a short range. keyPressed() moves one whole step instead.
 */
public final class IntOptionSlider extends AbstractSliderButton {

    private final String nameKey;
    private final int min;
    private final int max;
    private final int step;
    private final IntFunction<Component> formatter;
    private final IntConsumer applier;
    private final Runnable persister;

    public IntOptionSlider(int x, int y, int width, int height, String nameKey,
                           int min, int max, int step, IntFunction<Component> formatter,
                           IntSupplier getter, IntConsumer applier, Runnable persister) {
        super(x, y, width, height, Component.empty(), 0.0);
        this.nameKey = nameKey;
        this.min = min;
        this.max = max;
        this.step = step;
        this.formatter = formatter;
        this.applier = applier;
        this.persister = persister;
        this.value = this.toSliderValue(getter.getAsInt());
        this.updateMessage();
    }

    private double toSliderValue(int option) {
        int clamped = Math.clamp(option, this.min, this.max);
        return (double) (clamped - this.min) / (this.max - this.min);
    }

    private int toOptionValue() {
        int steps = (int) Math.round(this.value * (this.max - this.min) / this.step);
        return Math.min(this.min + steps * this.step, this.max);
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Component.translatable(this.nameKey)
                .append(": ").append(this.formatter.apply(this.toOptionValue())));
    }

    /** Fired on every step of a drag. Memory only -- see the javadoc. */
    @Override
    protected void applyValue() {
        this.applier.accept(this.toOptionValue());
    }

    /** End of the drag: the value has settled, write it once. */
    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        this.persister.run();
    }

    /**
     * Lets vanilla decide whether the arrow press applies at all (the
     * slider must be selected with Enter first), then replaces its
     * one-pixel nudge with a full step.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        int before = this.toOptionValue();
        boolean handled = super.keyPressed(keyCode, scanCode, modifiers);
        if (handled && (keyCode == GLFW.GLFW_KEY_LEFT || keyCode == GLFW.GLFW_KEY_RIGHT)) {
            int target = before + (keyCode == GLFW.GLFW_KEY_LEFT ? -this.step : this.step);
            this.value = this.toSliderValue(target);
            this.applyValue();
            this.updateMessage();
        }
        return handled;
    }
}
