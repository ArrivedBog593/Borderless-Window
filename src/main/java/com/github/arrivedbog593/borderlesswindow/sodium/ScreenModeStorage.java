package com.github.arrivedbog593.borderlesswindow.sodium;

import com.github.arrivedbog593.borderlesswindow.bobbing.BobbingState;
import com.github.arrivedbog593.borderlesswindow.bobbing.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.cloud.CloudState;
import com.github.arrivedbog593.borderlesswindow.distortion.DistortionState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelStyle;
import com.github.arrivedbog593.borderlesswindow.fog.FogState;
import com.github.arrivedbog593.borderlesswindow.fov.FovState;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayState;
import com.github.arrivedbog593.borderlesswindow.screeneffect.ScreenEffectState;
import com.github.arrivedbog593.borderlesswindow.window.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.window.F11Mode;
import com.github.arrivedbog593.borderlesswindow.window.ScreenMode;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;

/**
 * The Sodium Config API is only a presentation layer: it does not store
 * anything by itself. This class is the minimal "storage" it asks for:
 * when the user changes an option in the menu, the actual change is
 * applied immediately through the corresponding state holder
 * (BorderlessHandler for window state, FpsOverlayState for the overlay,
 * FogState for fog, CloudState for cloud height, ScreenEffectState for
 * the water/fire overlays, BobbingState for bobbing,
 * EnchantmentLevelState for the enchantment numerals).
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

    private boolean pendingPumpkinOverlay = ScreenEffectState.isPumpkinOverlayEnabled();
    private boolean pumpkinOverlayDirty = false;

    private boolean pendingFreezeOverlay = ScreenEffectState.isFreezeOverlayEnabled();
    private boolean freezeOverlayDirty = false;

    private boolean pendingPortalOverlay = ScreenEffectState.isPortalOverlayEnabled();
    private boolean portalOverlayDirty = false;

    private boolean pendingSpyglassOverlay = ScreenEffectState.isSpyglassOverlayEnabled();
    private boolean spyglassOverlayDirty = false;

    private CameraBobMode pendingCameraBob = BobbingState.getCameraBobMode();
    private boolean cameraBobDirty = false;

    private boolean pendingHandBob = BobbingState.isHandBobEnabled();
    private boolean handBobDirty = false;

    private int pendingDistortionPortal = DistortionState.getPortalPercent();
    private boolean distortionPortalDirty = false;

    private int pendingDistortionNausea = DistortionState.getNauseaPercent();
    private boolean distortionNauseaDirty = false;

    private int pendingFovSpeed = FovState.getSpeedPercent();
    private boolean fovSpeedDirty = false;

    private int pendingFovFlying = FovState.getFlyingPercent();
    private boolean fovFlyingDirty = false;

    private int pendingFovBow = FovState.getBowPercent();
    private boolean fovBowDirty = false;

    private int pendingFovFluid = FovState.getFluidPercent();
    private boolean fovFluidDirty = false;

    // The two vanilla sliders these pages sit under. Mirrored here so a
    // Sodium user can reach them without leaving for the accessibility
    // menu -- Sodium's own video settings drop both. Their value lives
    // in options.txt, NOT in our config: duplicating it would guarantee
    // the two copies drift apart.
    private int pendingVanillaDistortion = 0;
    private boolean vanillaDistortionDirty = false;

    private int pendingVanillaFov = 0;
    private boolean vanillaFovDirty = false;

    private EnchantmentLevelStyle pendingEnchantmentLevelStyle = EnchantmentLevelState.getStyle();
    private boolean enchantmentLevelStyleDirty = false;

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

    public boolean getPumpkinOverlay() {
        return ScreenEffectState.isPumpkinOverlayEnabled();
    }

    public void setPumpkinOverlay(boolean enabled) {
        this.pendingPumpkinOverlay = enabled;
        this.pumpkinOverlayDirty = true;
    }

    public boolean getFreezeOverlay() {
        return ScreenEffectState.isFreezeOverlayEnabled();
    }

    public void setFreezeOverlay(boolean enabled) {
        this.pendingFreezeOverlay = enabled;
        this.freezeOverlayDirty = true;
    }

    public boolean getPortalOverlay() {
        return ScreenEffectState.isPortalOverlayEnabled();
    }

    public void setPortalOverlay(boolean enabled) {
        this.pendingPortalOverlay = enabled;
        this.portalOverlayDirty = true;
    }

    public boolean getSpyglassOverlay() {
        return ScreenEffectState.isSpyglassOverlayEnabled();
    }

    public void setSpyglassOverlay(boolean enabled) {
        this.pendingSpyglassOverlay = enabled;
        this.spyglassOverlayDirty = true;
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

    public int getDistortionPortal() {
        return DistortionState.getPortalPercent();
    }

    public void setDistortionPortal(int percent) {
        this.pendingDistortionPortal = percent;
        this.distortionPortalDirty = true;
    }

    public int getDistortionNausea() {
        return DistortionState.getNauseaPercent();
    }

    public void setDistortionNausea(int percent) {
        this.pendingDistortionNausea = percent;
        this.distortionNauseaDirty = true;
    }

    public int getFovSpeed() {
        return FovState.getSpeedPercent();
    }

    public void setFovSpeed(int percent) {
        this.pendingFovSpeed = percent;
        this.fovSpeedDirty = true;
    }

    public int getFovFlying() {
        return FovState.getFlyingPercent();
    }

    public void setFovFlying(int percent) {
        this.pendingFovFlying = percent;
        this.fovFlyingDirty = true;
    }

    public int getFovBow() {
        return FovState.getBowPercent();
    }

    public void setFovBow(int percent) {
        this.pendingFovBow = percent;
        this.fovBowDirty = true;
    }

    public int getFovFluid() {
        return FovState.getFluidPercent();
    }

    public void setFovFluid(int percent) {
        this.pendingFovFluid = percent;
        this.fovFluidDirty = true;
    }

    /**
     * Vanilla stores these as a 0..1 Double and labels them with
     * percentValueOrOffLabel, which prints value * 100 -- so a plain
     * x100 matches the number Minecraft itself shows.
     * <p>
     * Note fovEffectScale's ValueSet carries an
     * xmap(Mth::square, Math::sqrt). That only bends how far the
     * physical slider travels for a given value; the stored value and
     * the printed percentage are unaffected, so no square root is
     * needed here.
     */
    public int getVanillaDistortion() {
        return percentOf(Minecraft.getInstance().options.screenEffectScale().get());
    }

    public void setVanillaDistortion(int percent) {
        this.pendingVanillaDistortion = percent;
        this.vanillaDistortionDirty = true;
    }

    public int getVanillaFov() {
        return percentOf(Minecraft.getInstance().options.fovEffectScale().get());
    }

    public void setVanillaFov(int percent) {
        this.pendingVanillaFov = percent;
        this.vanillaFovDirty = true;
    }

    private static int percentOf(double value) {
        return (int) Math.round(value * 100.0D);
    }

    public EnchantmentLevelStyle getEnchantmentLevelStyle() {
        return EnchantmentLevelState.getStyle();
    }

    public void setEnchantmentLevelStyle(EnchantmentLevelStyle style) {
        this.pendingEnchantmentLevelStyle = style;
        this.enchantmentLevelStyleDirty = true;
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

        if (this.pumpkinOverlayDirty) {
            ScreenEffectState.setPumpkinOverlayEnabled(this.pendingPumpkinOverlay);
            this.pumpkinOverlayDirty = false;
        }

        if (this.freezeOverlayDirty) {
            ScreenEffectState.setFreezeOverlayEnabled(this.pendingFreezeOverlay);
            this.freezeOverlayDirty = false;
        }

        if (this.portalOverlayDirty) {
            ScreenEffectState.setPortalOverlayEnabled(this.pendingPortalOverlay);
            this.portalOverlayDirty = false;
        }

        if (this.spyglassOverlayDirty) {
            ScreenEffectState.setSpyglassOverlayEnabled(this.pendingSpyglassOverlay);
            this.spyglassOverlayDirty = false;
        }

        if (this.cameraBobDirty) {
            BobbingState.setCameraBobMode(this.pendingCameraBob);
            this.cameraBobDirty = false;
        }

        if (this.handBobDirty) {
            BobbingState.setHandBobEnabled(this.pendingHandBob);
            this.handBobDirty = false;
        }

        if (this.distortionPortalDirty) {
            DistortionState.setPortalPercent(this.pendingDistortionPortal);
            this.distortionPortalDirty = false;
        }

        if (this.distortionNauseaDirty) {
            DistortionState.setNauseaPercent(this.pendingDistortionNausea);
            this.distortionNauseaDirty = false;
        }

        if (this.fovSpeedDirty) {
            FovState.setSpeedPercent(this.pendingFovSpeed);
            this.fovSpeedDirty = false;
        }

        if (this.fovFlyingDirty) {
            FovState.setFlyingPercent(this.pendingFovFlying);
            this.fovFlyingDirty = false;
        }

        if (this.fovBowDirty) {
            FovState.setBowPercent(this.pendingFovBow);
            this.fovBowDirty = false;
        }

        if (this.fovFluidDirty) {
            FovState.setFluidPercent(this.pendingFovFluid);
            this.fovFluidDirty = false;
        }

        // Vanilla options: written straight to options.txt. save() is
        // called once per flush rather than per option, since Sodium
        // applies a whole screenful at a time.
        if (this.vanillaDistortionDirty || this.vanillaFovDirty) {
            Options options = Minecraft.getInstance().options;
            if (this.vanillaDistortionDirty) {
                options.screenEffectScale().set(this.pendingVanillaDistortion / 100.0D);
                this.vanillaDistortionDirty = false;
            }
            if (this.vanillaFovDirty) {
                options.fovEffectScale().set(this.pendingVanillaFov / 100.0D);
                this.vanillaFovDirty = false;
            }
            options.save();
        }

        if (this.enchantmentLevelStyleDirty) {
            EnchantmentLevelState.setStyle(this.pendingEnchantmentLevelStyle);
            this.enchantmentLevelStyleDirty = false;
        }

        if (this.screenModeDirty) {
            Window window = Minecraft.getInstance().getWindow();
            BorderlessHandler.setMode(window, this.pendingValue);
            this.screenModeDirty = false;
        }
    }
}