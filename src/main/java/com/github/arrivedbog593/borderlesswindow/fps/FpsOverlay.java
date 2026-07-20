package com.github.arrivedbog593.borderlesswindow.fps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

/**
 * Draws the FPS overlay. Registered on the NeoForge event bus
 * (RenderGuiEvent.Post) from the mod constructor -- public NeoForge API
 * only, no mixins, and fully independent from Sodium.
 * <p>
 * SIMPLE mode shows "60 FPS"; EXTENDED shows a single line:
 * "60 FPS (Min 55 - Avg 58 - Max 62)".
 * <p>
 * MEASUREMENT APPROACH -- why we sample vanilla's counter instead of
 * timing frames ourselves: a first implementation measured the elapsed
 * time between consecutive render events and derived min/avg/max from
 * those deltas. That is subtly wrong under VSync: the GPU driver queues
 * 1-3 frames, so the CPU alternately runs ahead (short deltas) and
 * blocks on the full queue (long deltas). CPU-side frame times
 * genuinely oscillate around the average even while the on-screen
 * presentation is a perfectly paced 180 FPS -- producing absurd
 * readings like "Max 278" with VSync capping at 180, and a "Min" stuck
 * well below what the player actually experiences.
 * <p>
 * Vanilla's own FPS counter (Minecraft.getFps(), the same number F3
 * shows) counts frames actually presented per rolling second, so it is
 * ground truth for what the player sees. We sample it once per second
 * into a 15-sample sliding window and take the min / average / max of
 * those samples. At a steady 180 FPS all three read 180, and Max can
 * never exceed the VSync cap. Since every sample is already a
 * one-second average, the raw minimum of the window is naturally
 * hitch-resistant -- no percentile machinery needed.
 * <p>
 * The overlay hides itself while the F3 debug screen is open (F3
 * already shows FPS) and while the GUI is hidden with F1; sampling
 * continues in both cases so the stats stay warm.
 */
public final class FpsOverlay {

    /** Sliding window over which min / avg / max are computed. */
    private static final long WINDOW_NANOS = 15_000_000_000L;

    /**
     * One sample per second: vanilla recomputes its FPS counter once
     * per second, so sampling faster would only record duplicates.
     */
    private static final long SAMPLE_INTERVAL_NANOS = 1_000_000_000L;

    /**
     * A gap longer than this between render events means rendering of
     * the in-world HUD was paused (menu screen, a window minimized). The
     * window is reset, and one full sample interval must elapse before
     * sampling resumes -- vanilla's counter needs a second to reflect
     * in-world rendering again after such a pause.
     */
    private static final long PAUSE_THRESHOLD_NANOS = 2_000_000_000L;

    /** Don't show min / avg / max until the window has this many samples. */
    private static final int MIN_SAMPLES = 2;

    private static final int MARGIN = 4;
    private static final int TEXT_COLOR = 0xFFFFFFFF;

    private record FpsSample(long timestampNanos, int fps) {
    }

    private static final ArrayDeque<FpsSample> samples = new ArrayDeque<>();
    private static long lastRenderNanos = 0L;
    private static long lastSampleNanos = 0L;

    private FpsOverlay() {
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (FpsOverlayState.getMode() == FpsOverlayMode.OFF) {
            // Drop any stale data, so re-enabling starts a fresh window.
            samples.clear();
            lastRenderNanos = 0L;
            lastSampleNanos = 0L;
            return;
        }

        long now = System.nanoTime();
        Minecraft minecraft = Minecraft.getInstance();
        sample(now, minecraft);

        if (minecraft.options.hideGui || minecraft.getDebugOverlay().showDebugScreen()) {
            return;
        }

        draw(event.getGuiGraphics(), minecraft);
    }

    private static void sample(long now, Minecraft minecraft) {
        if (lastRenderNanos != 0L && now - lastRenderNanos > PAUSE_THRESHOLD_NANOS) {
            // Rendering was paused: the old window no longer reflects
            // "now", and vanilla's counter is polluted by the pause.
            // Clearing and delaying the next sample by one interval
            // (lastSampleNanos = now) skips the polluted second.
            samples.clear();
            lastSampleNanos = now;
        }
        lastRenderNanos = now;

        if (now - lastSampleNanos >= SAMPLE_INTERVAL_NANOS) {
            samples.addLast(new FpsSample(now, minecraft.getFps()));
            lastSampleNanos = now;
        }

        while (!samples.isEmpty()
                && now - samples.peekFirst().timestampNanos() > WINDOW_NANOS) {
            samples.removeFirst();
        }
    }

    private static void draw(GuiGraphics guiGraphics, Minecraft minecraft) {
        Font font = minecraft.font;

        // SIMPLE:   "60 FPS"
        // EXTENDED: "60 FPS (Min 55 - Avg 58 - Max 62)" -- single line;
        //           "--" placeholders while the window is still warming up.
        Component line = getLine(minecraft);

        FpsOverlayPosition position = FpsOverlayState.getPosition();
        int y = position.isTop()
                ? MARGIN
                : guiGraphics.guiHeight() - MARGIN - font.lineHeight;
        int x = position.isLeft()
                ? MARGIN
                : guiGraphics.guiWidth() - MARGIN - font.width(line);

        guiGraphics.drawString(font, line, x, y, TEXT_COLOR, true);
    }

    private static @NotNull Component getLine(Minecraft minecraft) {
        Component line;
        if (FpsOverlayState.getMode() == FpsOverlayMode.EXTENDED) {
            int min = Integer.MAX_VALUE;
            int max = Integer.MIN_VALUE;
            long total = 0L;
            for (FpsSample sample : samples) {
                min = Math.min(min, sample.fps());
                max = Math.max(max, sample.fps());
                total += sample.fps();
            }
            boolean warm = samples.size() >= MIN_SAMPLES;
            line = Component.translatable("borderlesswindow.fps_overlay.extended",
                    minecraft.getFps(),
                    warm ? String.valueOf(min) : "--",
                    warm ? String.valueOf(Math.round((double) total / samples.size())) : "--",
                    warm ? String.valueOf(max) : "--");
        } else {
            line = Component.translatable("borderlesswindow.fps_overlay.fps",
                    minecraft.getFps());
        }
        return line;
    }
}