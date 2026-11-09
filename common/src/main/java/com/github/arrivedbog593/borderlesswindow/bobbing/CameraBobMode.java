package com.github.arrivedbog593.borderlesswindow.bobbing;

/**
 * How much of the camera's walk bobbing is kept.
 * <p>
 * Vanilla's bobView applies four transformations at once: a lateral
 * sway and a vertical bounce (both in one translate), plus a roll
 * around Z that tilts the horizon and a pitch around X that nods the
 * view. Minecraft only lets you keep all four or none.
 * <p>
 * - FULL: vanilla, all four (default -- existing users see no change).
 * - VERTICAL_ONLY: keeps the vertical bounce, drops the lateral sway
 *   and both rotations. The camera still follows your steps, but the
 *   horizon never tilts -- which is the part that actually causes
 *   motion sickness for most people who turn bobbing off entirely.
 * - OFF: no camera bobbing at all.
 * <p>
 * Hand bobbing is a SEPARATE setting (see BobbingState): vanilla runs
 * bobView from two different call sites, one for the world camera and
 * one for the held item, so they can be controlled independently.
 */
public enum CameraBobMode {
    FULL("borderlesswindow.camera_bob_mode.full"),
    VERTICAL_ONLY("borderlesswindow.camera_bob_mode.vertical_only"),
    OFF("borderlesswindow.camera_bob_mode.off");

    private final String translationKey;

    CameraBobMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}