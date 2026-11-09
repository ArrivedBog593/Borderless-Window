package com.github.arrivedbog593.borderlesswindow.config;

import com.github.arrivedbog593.borderlesswindow.cloud.CloudState;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

/**
 * Slider for the cloud height option in the no-Sodium config screen.
 * <p>
 * The slider's normalized value [0, 1] maps to a discrete set of choices:
 * position 0 is the DEFAULT sentinel (shown as "Default", the mixin does
 * not override), and positions 1..N map linearly to heights
 * MIN_HEIGHT..MAX_HEIGHT. This mirrors how the height is presented in the
 * Sodium integration, where the leftmost slot also means "Default".
 * <p>
 * SAVING: vanilla calls applyValue() on every value change while the
 * mouse is being dragged, so applying here only touches memory -- the
 * cloud layer moves live as you drag, with no disk I/O. The config is
 * written once in onRelease(). Keyboard changes (left/right arrows) never
 * produce a release event, so BorderlessConfigScreen.onClose() persists
 * as well; both paths are idempotent because saveCurrent() always writes
 * the full live state.
 */
public final class CloudHeightSlider extends AbstractSliderButton {

    // Total selectable steps: one per integer height, plus the sentinel.
    private static final int HEIGHT_STEPS =
            CloudState.MAX_HEIGHT - CloudState.MIN_HEIGHT + 1;
    private static final int TOTAL_STEPS = HEIGHT_STEPS + 1; // + Default

    public CloudHeightSlider(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty(), heightToValue(CloudState.getHeight()));
        this.updateMessage();
    }

    /** Maps a stored height (or sentinel) to the slider's [0,1] value. */
    private static double heightToValue(int storedHeight) {
        if (storedHeight == CloudState.DEFAULT_HEIGHT) {
            return 0.0;
        }
        int index = storedHeight - CloudState.MIN_HEIGHT + 1; // 1..HEIGHT_STEPS
        return (double) index / (TOTAL_STEPS - 1);
    }

    /** Maps the slider's [0,1] value to a stored height (or sentinel). */
    private int valueToHeight() {
        int index = (int) Math.round(this.value * (TOTAL_STEPS - 1)); // 0..TOTAL_STEPS-1
        if (index <= 0) {
            return CloudState.DEFAULT_HEIGHT;
        }
        return CloudState.MIN_HEIGHT + (index - 1);
    }

    @Override
    protected void updateMessage() {
        int h = valueToHeight();
        Component valueText;
        if (h == CloudState.DEFAULT_HEIGHT) {
            valueText = Component.translatable("borderlesswindow.options.cloud_height.default");
        } else if (h == CloudState.VANILLA_HEIGHT) {
            // e.g. "192 (Regular)" -- marks the unmodified vanilla height.
            valueText = Component.translatable(
                    "borderlesswindow.options.cloud_height.regular", h);
        } else {
            valueText = Component.literal(String.valueOf(h));
        }
        this.setMessage(Component.translatable(
                        "borderlesswindow.options.cloud_height.name")
                .append(": ").append(valueText));
    }

    /**
     * Fired on every step of a drag. Memory only -- see the SAVING note
     * in the class javadoc.
     */
    @Override
    protected void applyValue() {
        CloudState.applyHeight(valueToHeight());
    }

    /** End of the drag: the value has settled, write it once. */
    @Override
    public void onRelease(double mouseX, double mouseY) {
        super.onRelease(mouseX, mouseY);
        CloudState.persist();
    }
}