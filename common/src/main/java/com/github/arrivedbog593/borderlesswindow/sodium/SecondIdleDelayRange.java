package com.github.arrivedbog593.borderlesswindow.sodium;

import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import net.caffeinemc.mods.sodium.api.config.option.SteppedValidator;

import java.util.function.Supplier;

/**
 * Range of the "Second Idle Limit After" slider on the Sodium page. It
 * starts at the first delay, so the slider can never go below it, and
 * that leftmost slot means Off -- like the no-Sodium slider, see
 * IdleFpsState.toSecondDelaySlot.
 * <p>
 * WHY NOT A PLAIN Range. Off has to stay Off while the first delay moves.
 * With Range(first, 60, 1) the Off slot would be a real number of minutes:
 * lowering the first delay from 10 to 5 would leave the value at 10, now
 * a valid "10 min", and silently turn the second stage on. So Off gets a
 * value of its own, {@link #OFF_VALUE}, that no first delay can turn into
 * minutes, and this validator does the translation -- all Sodium lets a
 * mod hook here: its slider asks the range for min/max (clamping the
 * thumb, so OFF_VALUE sits at the left end) and runs every value through
 * getValidatedValue before drawing it. ScreenModeStorage maps OFF_VALUE
 * to and from IdleFpsState.OFF.
 * <p>
 * The values that reach it (verified against Sodium 0.8.13's
 * SliderControl and StatefulOption):
 * - Dragged to the left end: exactly {@code first} -> Off.
 * - Right arrow from Off: OFF_VALUE + 1 = 0 -> the first slot past the
 *   first delay. Left arrow from there gives {@code first} -> Off again.
 * - Left arrow from Off: -2 -> Off.
 * - Right arrow at the right end: max + 1 -> max. (A plain Range would
 *   fall back to the default, Off.)
 * - A value the first delay has caught up with (it was raised to or past
 *   it): -> Off, the same rule IdleFpsState applies.
 * The first two never collide: the slider is disabled while the first
 * delay is 0, so a drag always lands on 1 or more.
 */
record SecondIdleDelayRange(int first) implements SteppedValidator {

    /** What Sodium holds for Off. Below every slot, so the thumb clamps left. */
    static final int OFF_VALUE = -1;

    @Override
    public int min() {
        return this.first;
    }

    @Override
    public int max() {
        return IdleFpsState.MAX_SECOND_DELAY_MINUTES;
    }

    @Override
    public int step() {
        return 1;
    }

    @Override
    public boolean isValueValid(int value) {
        return value == OFF_VALUE || (value > this.first && value <= this.max());
    }

    @Override
    public Integer getValidatedValue(Integer value, Supplier<Integer> defaultValueSupplier) {
        if (value == OFF_VALUE + this.step()) {
            return Math.min(this.first + this.step(), this.max());
        }
        if (value <= this.first) {
            return OFF_VALUE;
        }
        return Math.min(value, this.max());
    }
}
