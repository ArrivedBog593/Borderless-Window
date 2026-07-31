package com.github.arrivedbog593.borderlesswindow.sodium;

import com.github.arrivedbog593.borderlesswindow.bobbing.BobbingState;
import com.github.arrivedbog593.borderlesswindow.bobbing.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.cloud.CloudState;
import com.github.arrivedbog593.borderlesswindow.fog.FogState;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayState;
import com.github.arrivedbog593.borderlesswindow.screeneffect.ScreenEffectState;
import com.github.arrivedbog593.borderlesswindow.window.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.window.F11Mode;
import com.github.arrivedbog593.borderlesswindow.window.ScreenMode;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;

/**
 * The Sodium Config API is only a presentation layer: it does not store
 * anything by itself. This class is the minimal "storage" it asks for:
 * when the user changes an option in the menu, the actual change is
 * applied immediately through the corresponding state holder
 * (BorderlessHandler for window state, FpsOverlayState for the overlay,
 * FogState for fog, CloudState for cloud height, ScreenEffectState for
 * the water/fire overlays, BobbingState for bobbing).
 * <p>
 * The real persistence lives in BorderlessConfigFile
 * (config/borderlesswindow.json), which the state holders save
 * automatically on every change. Additionally, the WindowMixin
 * override of isFullscreen() keeps the vanilla "fullscreen yes/no" flag
 * in options.txt consistent with the current mode.
 * <p>
 * CLOUD HEIGHT ENCODING for the Sodium slider: Sodium integer options are
 * a contiguous [min, max] range, but our real value has a "Default"
 * sentinel (CloudState.DEFAULT_HEIGHT = -1) that is disjoint from the
 * 64..320 height range. We bridge this by exposing the slider as
 * [SODIUM_CLOUD_DEFAULT_SLOT .. MAX_HEIGHT], where the single slot just
 * below MIN_HEIGHT means "Default". getCloudHeightSlider/setCloudHeight
 * translate between that slider integer and the real CloudState value.
 */
public final class ScreenModeStorage {

    /** The slider slot (just below MIN_HEIGHT) that represents "Default". */
    static final int SODIUM_CLOUD_DEFAULT_SLOT = CloudState.MIN_HEIGHT - 1;

    private ScreenMode pendingValue = BorderlessHandler.getCurrentMode();
    private boolean screenModeDirty = false;

    private F11Mode pendingF11Target = BorderlessHandler.getF11Target();
    private boolean f11TargetDirty = false;

    private FpsOverlayMode pendingFpsOverlayMode = FpsOverlayState.getMode();
    private boolean fpsOverlayModeDirty = false;

    private FpsOverlayPosition pendingFpsOverlayPosition = FpsOverlayState.getPosition();
    private boolean fpsOverlayPositionDirty = false;

    private boolean pendingTerrainFog = FogState.isTerrainFogEnabled();
    private boolean terrainFogDirty = false;

    private boolean pendingWaterFog = FogState.isWaterFogEnabled();
    private boolean waterFogDirty = false;

    private boolean pendingLavaFog = FogState.isLavaFogEnabled();
    private boolean lavaFogDirty = false;

    private boolean pendingPowderSnowFog = FogState.isPowderSnowFogEnabled();
    private boolean powderSnowFogDirty = false;

    private int pendingCloudHeight = CloudState.getHeight();
    private boolean cloudHeightDirty = false;

    private boolean pendingWaterOverlay = ScreenEffectState.isWaterOverlayEnabled();
    private boolean waterOverlayDirty = false;

    private boolean pendingFireOverlay = ScreenEffectState.isFireOverlayEnabled();
    private boolean fireOverlayDirty = false;

    private boolean pendingSmartFire = ScreenEffectState.isSmartFireEnabled();
    private boolean smartFireDirty = false;

    private CameraBobMode pendingCameraBob = BobbingState.getCameraBobMode();
    private boolean cameraBobDirty = false;

    private boolean pendingHandBob = BobbingState.isHandBobEnabled();
    private boolean handBobDirty = false;

    public ScreenMode getScreenMode() {
        return BorderlessHandler.getCurrentMode();
    }

    public void setScreenMode(ScreenMode mode) {
        this.pendingValue = mode;
        this.screenModeDirty = true;
    }

    public F11Mode getF11Target() {
        return BorderlessHandler.getF11Target();
    }

    public void setF11Target(F11Mode target) {
        this.pendingF11Target = target;
        this.f11TargetDirty = true;
    }

    public FpsOverlayMode getFpsOverlayMode() {
        return FpsOverlayState.getMode();
    }

    public void setFpsOverlayMode(FpsOverlayMode mode) {
        this.pendingFpsOverlayMode = mode;
        this.fpsOverlayModeDirty = true;
    }

    public FpsOverlayPosition getFpsOverlayPosition() {
        return FpsOverlayState.getPosition();
    }

    public void setFpsOverlayPosition(FpsOverlayPosition position) {
        this.pendingFpsOverlayPosition = position;
        this.fpsOverlayPositionDirty = true;
    }

    public boolean getTerrainFog() {
        return FogState.isTerrainFogEnabled();
    }

    public void setTerrainFog(boolean enabled) {
        this.pendingTerrainFog = enabled;
        this.terrainFogDirty = true;
    }

    public boolean getWaterFog() {
        return FogState.isWaterFogEnabled();
    }

    public void setWaterFog(boolean enabled) {
        this.pendingWaterFog = enabled;
        this.waterFogDirty = true;
    }

    public boolean getLavaFog() {
        return FogState.isLavaFogEnabled();
    }

    public void setLavaFog(boolean enabled) {
        this.pendingLavaFog = enabled;
        this.lavaFogDirty = true;
    }

    public boolean getPowderSnowFog() {
        return FogState.isPowderSnowFogEnabled();
    }

    public void setPowderSnowFog(boolean enabled) {
        this.pendingPowderSnowFog = enabled;
        this.powderSnowFogDirty = true;
    }

    /** Sodium slider getter: real height -> slider integer. */
    public int getCloudHeightSlider() {
        int h = CloudState.getHeight();
        return h == CloudState.DEFAULT_HEIGHT ? SODIUM_CLOUD_DEFAULT_SLOT : h;
    }

    /** Sodium slider setter: slider integer -> real height (stored pending). */
    public void setCloudHeightSlider(int sliderValue) {
        this.pendingCloudHeight = sliderValue <= SODIUM_CLOUD_DEFAULT_SLOT
                ? CloudState.DEFAULT_HEIGHT
                : sliderValue;
        this.cloudHeightDirty = true;
    }

    public boolean getWaterOverlay() {
        return ScreenEffectState.isWaterOverlayEnabled();
    }

    public void setWaterOverlay(boolean enabled) {
        this.pendingWaterOverlay = enabled;
        this.waterOverlayDirty = true;
    }

    public boolean getFireOverlay() {
        return ScreenEffectState.isFireOverlayEnabled();
    }

    public void setFireOverlay(boolean enabled) {
        this.pendingFireOverlay = enabled;
        this.fireOverlayDirty = true;
    }

    public boolean getSmartFire() {
        return ScreenEffectState.isSmartFireEnabled();
    }

    public void setSmartFire(boolean enabled) {
        this.pendingSmartFire = enabled;
        this.smartFireDirty = true;
    }

    public CameraBobMode getCameraBob() {
        return BobbingState.getCameraBobMode();
    }

    public void setCameraBob(CameraBobMode mode) {
        this.pendingCameraBob = mode;
        this.cameraBobDirty = true;
    }

    public boolean getHandBob() {
        return BobbingState.isHandBobEnabled();
    }

    public void setHandBob(boolean enabled) {
        this.pendingHandBob = enabled;
        this.handBobDirty = true;
    }

    /**
     * Called when Sodium applies pending changes (setStorageHandler).
     * <p>
     * IMPORTANT: we only apply what the user ACTUALLY changed in the menu
     * (dirty flags). Previously, flush() always applied pendingValue --
     * and since pendingValue is only updated when the option is touched
     * in the menu, applying any other option after using F11 reverted the
     * mode back to the stale cached value. The same rule protects every
     * option added since.
     */
    public void flush() {
        if (this.f11TargetDirty) {
            BorderlessHandler.setF11Target(this.pendingF11Target);
            this.f11TargetDirty = false;
        }

        if (this.fpsOverlayModeDirty) {
            FpsOverlayState.setMode(this.pendingFpsOverlayMode);
            this.fpsOverlayModeDirty = false;
        }

        if (this.fpsOverlayPositionDirty) {
            FpsOverlayState.setPosition(this.pendingFpsOverlayPosition);
            this.fpsOverlayPositionDirty = false;
        }

        if (this.terrainFogDirty) {
            FogState.setTerrainFogEnabled(this.pendingTerrainFog);
            this.terrainFogDirty = false;
        }

        if (this.waterFogDirty) {
            FogState.setWaterFogEnabled(this.pendingWaterFog);
            this.waterFogDirty = false;
        }

        if (this.lavaFogDirty) {
            FogState.setLavaFogEnabled(this.pendingLavaFog);
            this.lavaFogDirty = false;
        }

        if (this.powderSnowFogDirty) {
            FogState.setPowderSnowFogEnabled(this.pendingPowderSnowFog);
            this.powderSnowFogDirty = false;
        }

        if (this.cloudHeightDirty) {
            CloudState.setHeight(this.pendingCloudHeight);
            this.cloudHeightDirty = false;
        }

        if (this.waterOverlayDirty) {
            ScreenEffectState.setWaterOverlayEnabled(this.pendingWaterOverlay);
            this.waterOverlayDirty = false;
        }

        if (this.fireOverlayDirty) {
            ScreenEffectState.setFireOverlayEnabled(this.pendingFireOverlay);
            this.fireOverlayDirty = false;
        }

        if (this.smartFireDirty) {
            ScreenEffectState.setSmartFireEnabled(this.pendingSmartFire);
            this.smartFireDirty = false;
        }

        if (this.cameraBobDirty) {
            BobbingState.setCameraBobMode(this.pendingCameraBob);
            this.cameraBobDirty = false;
        }

        if (this.handBobDirty) {
            BobbingState.setHandBobEnabled(this.pendingHandBob);
            this.handBobDirty = false;
        }

        if (this.screenModeDirty) {
            Window window = Minecraft.getInstance().getWindow();
            BorderlessHandler.setMode(window, this.pendingValue);
            this.screenModeDirty = false;
        }
    }
}