package com.github.arrivedbog593.borderlesswindow.sodium;

import com.github.arrivedbog593.borderlesswindow.camera.BobbingState;
import com.github.arrivedbog593.borderlesswindow.camera.CameraBobMode;
import com.github.arrivedbog593.borderlesswindow.camera.DistortionState;
import com.github.arrivedbog593.borderlesswindow.camera.FovState;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomMode;
import com.github.arrivedbog593.borderlesswindow.camera.ZoomState;
import com.github.arrivedbog593.borderlesswindow.distance.DistanceState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelState;
import com.github.arrivedbog593.borderlesswindow.enchantment.EnchantmentLevelStyle;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayMode;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayPosition;
import com.github.arrivedbog593.borderlesswindow.fps.FpsOverlayState;
import com.github.arrivedbog593.borderlesswindow.fps.IdleFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.MinimizedRenderState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedFpsState;
import com.github.arrivedbog593.borderlesswindow.fps.UnfocusedVolumeState;
import com.github.arrivedbog593.borderlesswindow.general.BorderlessHandler;
import com.github.arrivedbog593.borderlesswindow.general.F11Mode;
import com.github.arrivedbog593.borderlesswindow.general.HudState;
import com.github.arrivedbog593.borderlesswindow.general.ScreenMode;
import com.github.arrivedbog593.borderlesswindow.visual.CloudState;
import com.github.arrivedbog593.borderlesswindow.visual.FogState;
import com.github.arrivedbog593.borderlesswindow.visual.ScreenEffectState;
import com.github.arrivedbog593.borderlesswindow.visual.SkyState;
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
 * [CloudState.DEFAULT_SLOT .. MAX_HEIGHT], where the single slot just
 * below MIN_HEIGHT means "Default". getCloudHeightSlider/setCloudHeightSlider
 * translate with CloudState.toSlot/fromSlot, the same conversion the
 * no-Sodium screen uses.
 */
public final class ScreenModeStorage {

    private ScreenMode pendingValue = BorderlessHandler.getCurrentMode();
    private boolean screenModeDirty = false;

    private F11Mode pendingF11Target = BorderlessHandler.getF11Target();
    private boolean f11TargetDirty = false;

    private FpsOverlayMode pendingFpsOverlayMode = FpsOverlayState.getMode();
    private boolean fpsOverlayModeDirty = false;

    private FpsOverlayPosition pendingFpsOverlayPosition = FpsOverlayState.getPosition();
    private boolean fpsOverlayPositionDirty = false;

    private int pendingUnfocusedFpsLimit = UnfocusedFpsState.getLimit();
    private boolean unfocusedFpsLimitDirty = false;

    private int pendingUnfocusedVolume = UnfocusedVolumeState.getPercent();
    private boolean unfocusedVolumeDirty = false;

    private boolean pendingMinimizedSkipRender = MinimizedRenderState.isEnabled();
    private boolean minimizedSkipRenderDirty = false;

    private int pendingIdleFpsDelay = IdleFpsState.getDelayMinutes();
    private boolean idleFpsDelayDirty = false;

    private int pendingIdleFpsLimit = IdleFpsState.getLimit();
    private boolean idleFpsLimitDirty = false;

    private int pendingIdleFpsSecondDelay = IdleFpsState.getSecondDelayMinutes();
    private boolean idleFpsSecondDelayDirty = false;

    private int pendingIdleFpsSecondLimit = IdleFpsState.getSecondLimit();
    private boolean idleFpsSecondLimitDirty = false;

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

    private int pendingDistanceItems = DistanceState.getItems();
    private boolean distanceItemsDirty = false;

    private int pendingDistanceOrbs = DistanceState.getOrbs();
    private boolean distanceOrbsDirty = false;

    private int pendingDistanceArmorStands = DistanceState.getArmorStands();
    private boolean distanceArmorStandsDirty = false;

    private int pendingDistanceParticles = DistanceState.getParticles();
    private boolean distanceParticlesDirty = false;

    private ZoomMode pendingZoomMode = ZoomState.getMode();
    private boolean zoomModeDirty = false;

    private int pendingZoomLevel = ZoomState.getInitialLevel();
    private boolean zoomLevelDirty = false;

    private boolean pendingZoomScroll = ZoomState.isScrollEnabled();
    private boolean zoomScrollDirty = false;

    private boolean pendingZoomSmooth = ZoomState.isSmoothEnabled();
    private boolean zoomSmoothDirty = false;

    private boolean pendingZoomHideHand = ZoomState.isHideHandEnabled();
    private boolean zoomHideHandDirty = false;

    private boolean pendingZoomHideHud = ZoomState.isHideHudEnabled();
    private boolean zoomHideHudDirty = false;

    private int pendingFogTerrainStart = FogState.getTerrainStart();
    private boolean fogTerrainStartDirty = false;

    private boolean pendingSunMoon = SkyState.isSunMoonEnabled();
    private boolean sunMoonDirty = false;

    private boolean pendingStars = SkyState.isStarsEnabled();
    private boolean starsDirty = false;

    private boolean pendingWeather = SkyState.isWeatherEnabled();
    private boolean weatherDirty = false;

    private boolean pendingHeldItemName = HudState.isHeldItemNameEnabled();
    private boolean heldItemNameDirty = false;

    // The two vanilla sliders these pages sit under. Mirrored here so a
    // Sodium user can reach them without leaving for the accessibility
    // menu -- Sodium's own video settings drop both. Their value lives
    // in options.txt, NOT in our config: duplicating it would guarantee
    // the two copies drift apart.
    private int pendingVanillaDistortion = 0;
    private boolean vanillaDistortionDirty = false;

    private int pendingVanillaFov = 0;
    private boolean vanillaFovDirty = false;

    private int pendingVanillaGlintSpeed = 0;
    private boolean vanillaGlintSpeedDirty = false;

    private int pendingVanillaGlintStrength = 0;
    private boolean vanillaGlintStrengthDirty = false;

    private int pendingVanillaMenuBlur = 0;
    private boolean vanillaMenuBlurDirty = false;

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

    /** In slider positions: see UnfocusedFpsState.OFF_SLOT. */
    public int getUnfocusedFpsLimit() {
        return UnfocusedFpsState.toSlot(UnfocusedFpsState.getLimit());
    }

    public void setUnfocusedFpsLimit(int limit) {
        this.pendingUnfocusedFpsLimit = limit;
        this.unfocusedFpsLimitDirty = true;
    }

    public int getUnfocusedVolume() {
        return UnfocusedVolumeState.getPercent();
    }

    public void setUnfocusedVolume(int percent) {
        this.pendingUnfocusedVolume = percent;
        this.unfocusedVolumeDirty = true;
    }

    public boolean getMinimizedSkipRender() {
        return MinimizedRenderState.isEnabled();
    }

    public void setMinimizedSkipRender(boolean enabled) {
        this.pendingMinimizedSkipRender = enabled;
        this.minimizedSkipRenderDirty = true;
    }

    public int getIdleFpsDelay() {
        return IdleFpsState.getDelayMinutes();
    }

    public void setIdleFpsDelay(int minutes) {
        this.pendingIdleFpsDelay = minutes;
        this.idleFpsDelayDirty = true;
    }

    public int getIdleFpsLimit() {
        return IdleFpsState.getLimit();
    }

    public void setIdleFpsLimit(int limit) {
        this.pendingIdleFpsLimit = limit;
        this.idleFpsLimitDirty = true;
    }

    /** Off is SecondIdleDelayRange.OFF_VALUE on the Sodium side; see there. */
    public int getIdleFpsSecondDelay() {
        int minutes = IdleFpsState.getSecondDelayMinutes();
        return minutes == IdleFpsState.OFF ? SecondIdleDelayRange.OFF_VALUE : minutes;
    }

    public void setIdleFpsSecondDelay(int minutes) {
        this.pendingIdleFpsSecondDelay = minutes == SecondIdleDelayRange.OFF_VALUE ? IdleFpsState.OFF : minutes;
        this.idleFpsSecondDelayDirty = true;
    }

    public int getIdleFpsSecondLimit() {
        return IdleFpsState.getSecondLimit();
    }

    public void setIdleFpsSecondLimit(int limit) {
        this.pendingIdleFpsSecondLimit = limit;
        this.idleFpsSecondLimitDirty = true;
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
        return CloudState.toSlot(CloudState.getHeight());
    }

    /** Sodium slider setter: slider integer -> real height (stored pending). */
    public void setCloudHeightSlider(int sliderValue) {
        this.pendingCloudHeight = CloudState.fromSlot(sliderValue);
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

    public ZoomMode getZoomMode() {
        return ZoomState.getMode();
    }

    public void setZoomMode(ZoomMode mode) {
        this.pendingZoomMode = mode;
        this.zoomModeDirty = true;
    }

    public int getZoomLevel() {
        return ZoomState.getInitialLevel();
    }

    public void setZoomLevel(int level) {
        this.pendingZoomLevel = level;
        this.zoomLevelDirty = true;
    }

    public boolean getZoomScroll() {
        return ZoomState.isScrollEnabled();
    }

    public void setZoomScroll(boolean enabled) {
        this.pendingZoomScroll = enabled;
        this.zoomScrollDirty = true;
    }

    public boolean getZoomSmooth() {
        return ZoomState.isSmoothEnabled();
    }

    public void setZoomSmooth(boolean enabled) {
        this.pendingZoomSmooth = enabled;
        this.zoomSmoothDirty = true;
    }

    public boolean getZoomHideHand() {
        return ZoomState.isHideHandEnabled();
    }

    public void setZoomHideHand(boolean enabled) {
        this.pendingZoomHideHand = enabled;
        this.zoomHideHandDirty = true;
    }

    public boolean getZoomHideHud() {
        return ZoomState.isHideHudEnabled();
    }

    public void setZoomHideHud(boolean enabled) {
        this.pendingZoomHideHud = enabled;
        this.zoomHideHudDirty = true;
    }

    public int getFogTerrainStart() {
        return FogState.getTerrainStart();
    }

    public void setFogTerrainStart(int percent) {
        this.pendingFogTerrainStart = percent;
        this.fogTerrainStartDirty = true;
    }

    public boolean getSunMoon() {
        return SkyState.isSunMoonEnabled();
    }

    public void setSunMoon(boolean enabled) {
        this.pendingSunMoon = enabled;
        this.sunMoonDirty = true;
    }

    public boolean getStars() {
        return SkyState.isStarsEnabled();
    }

    public void setStars(boolean enabled) {
        this.pendingStars = enabled;
        this.starsDirty = true;
    }

    public boolean getWeather() {
        return SkyState.isWeatherEnabled();
    }

    public void setWeather(boolean enabled) {
        this.pendingWeather = enabled;
        this.weatherDirty = true;
    }

    public boolean getHeldItemName() {
        return HudState.isHeldItemNameEnabled();
    }

    public void setHeldItemName(boolean enabled) {
        this.pendingHeldItemName = enabled;
        this.heldItemNameDirty = true;
    }

    public int getDistanceItems() {
        return DistanceState.getItems();
    }

    public void setDistanceItems(int blocks) {
        this.pendingDistanceItems = blocks;
        this.distanceItemsDirty = true;
    }

    public int getDistanceOrbs() {
        return DistanceState.getOrbs();
    }

    public void setDistanceOrbs(int blocks) {
        this.pendingDistanceOrbs = blocks;
        this.distanceOrbsDirty = true;
    }

    public int getDistanceArmorStands() {
        return DistanceState.getArmorStands();
    }

    public void setDistanceArmorStands(int blocks) {
        this.pendingDistanceArmorStands = blocks;
        this.distanceArmorStandsDirty = true;
    }

    public int getDistanceParticles() {
        return DistanceState.getParticles();
    }

    public void setDistanceParticles(int blocks) {
        this.pendingDistanceParticles = blocks;
        this.distanceParticlesDirty = true;
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

    public int getVanillaGlintSpeed() {
        return percentOf(Minecraft.getInstance().options.glintSpeed().get());
    }

    public void setVanillaGlintSpeed(int percent) {
        this.pendingVanillaGlintSpeed = percent;
        this.vanillaGlintSpeedDirty = true;
    }

    public int getVanillaGlintStrength() {
        return percentOf(Minecraft.getInstance().options.glintStrength().get());
    }

    public void setVanillaGlintStrength(int percent) {
        this.pendingVanillaGlintStrength = percent;
        this.vanillaGlintStrengthDirty = true;
    }

    /** Already an integer 0..10 in vanilla; no conversion needed. */
    public int getVanillaMenuBlur() {
        return Minecraft.getInstance().options.menuBackgroundBlurriness().get();
    }

    public void setVanillaMenuBlur(int level) {
        this.pendingVanillaMenuBlur = level;
        this.vanillaMenuBlurDirty = true;
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

        if (this.unfocusedFpsLimitDirty) {
            UnfocusedFpsState.setLimit(UnfocusedFpsState.fromSlot(this.pendingUnfocusedFpsLimit));
            this.unfocusedFpsLimitDirty = false;
        }

        if (this.unfocusedVolumeDirty) {
            UnfocusedVolumeState.setPercent(this.pendingUnfocusedVolume);
            this.unfocusedVolumeDirty = false;
        }

        if (this.minimizedSkipRenderDirty) {
            MinimizedRenderState.setEnabled(this.pendingMinimizedSkipRender);
            this.minimizedSkipRenderDirty = false;
        }

        if (this.idleFpsDelayDirty) {
            IdleFpsState.setDelayMinutes(this.pendingIdleFpsDelay);
            this.idleFpsDelayDirty = false;
        }

        if (this.idleFpsLimitDirty) {
            IdleFpsState.setLimit(this.pendingIdleFpsLimit);
            this.idleFpsLimitDirty = false;
        }

        // After the first delay: the second one is checked against it.
        if (this.idleFpsSecondDelayDirty) {
            IdleFpsState.setSecondDelayMinutes(this.pendingIdleFpsSecondDelay);
            this.idleFpsSecondDelayDirty = false;
        }

        if (this.idleFpsSecondLimitDirty) {
            IdleFpsState.setSecondLimit(this.pendingIdleFpsSecondLimit);
            this.idleFpsSecondLimitDirty = false;
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

        if (this.distanceItemsDirty) {
            DistanceState.setItems(this.pendingDistanceItems);
            this.distanceItemsDirty = false;
        }

        if (this.distanceOrbsDirty) {
            DistanceState.setOrbs(this.pendingDistanceOrbs);
            this.distanceOrbsDirty = false;
        }

        if (this.distanceArmorStandsDirty) {
            DistanceState.setArmorStands(this.pendingDistanceArmorStands);
            this.distanceArmorStandsDirty = false;
        }

        if (this.distanceParticlesDirty) {
            DistanceState.setParticles(this.pendingDistanceParticles);
            this.distanceParticlesDirty = false;
        }

        if (this.zoomModeDirty) {
            ZoomState.setMode(this.pendingZoomMode);
            this.zoomModeDirty = false;
        }

        if (this.zoomLevelDirty) {
            ZoomState.setInitialLevel(this.pendingZoomLevel);
            this.zoomLevelDirty = false;
        }

        if (this.zoomScrollDirty) {
            ZoomState.setScrollEnabled(this.pendingZoomScroll);
            this.zoomScrollDirty = false;
        }

        if (this.zoomSmoothDirty) {
            ZoomState.setSmoothEnabled(this.pendingZoomSmooth);
            this.zoomSmoothDirty = false;
        }

        if (this.zoomHideHandDirty) {
            ZoomState.setHideHandEnabled(this.pendingZoomHideHand);
            this.zoomHideHandDirty = false;
        }

        if (this.zoomHideHudDirty) {
            ZoomState.setHideHudEnabled(this.pendingZoomHideHud);
            this.zoomHideHudDirty = false;
        }

        if (this.fogTerrainStartDirty) {
            FogState.setTerrainStart(this.pendingFogTerrainStart);
            this.fogTerrainStartDirty = false;
        }

        if (this.sunMoonDirty) {
            SkyState.setSunMoonEnabled(this.pendingSunMoon);
            this.sunMoonDirty = false;
        }

        if (this.starsDirty) {
            SkyState.setStarsEnabled(this.pendingStars);
            this.starsDirty = false;
        }

        if (this.weatherDirty) {
            SkyState.setWeatherEnabled(this.pendingWeather);
            this.weatherDirty = false;
        }

        if (this.heldItemNameDirty) {
            HudState.setHeldItemNameEnabled(this.pendingHeldItemName);
            this.heldItemNameDirty = false;
        }

        // Vanilla options: written straight to options.txt. save() is
        // called once per flush rather than per option, since Sodium
        // applies a whole screenful at a time.
        if (this.vanillaDistortionDirty || this.vanillaFovDirty
                || this.vanillaGlintSpeedDirty || this.vanillaGlintStrengthDirty
                || this.vanillaMenuBlurDirty) {
            Options options = Minecraft.getInstance().options;
            if (this.vanillaDistortionDirty) {
                options.screenEffectScale().set(this.pendingVanillaDistortion / 100.0D);
                this.vanillaDistortionDirty = false;
            }
            if (this.vanillaFovDirty) {
                options.fovEffectScale().set(this.pendingVanillaFov / 100.0D);
                this.vanillaFovDirty = false;
            }
            if (this.vanillaGlintSpeedDirty) {
                options.glintSpeed().set(this.pendingVanillaGlintSpeed / 100.0D);
                this.vanillaGlintSpeedDirty = false;
            }
            if (this.vanillaGlintStrengthDirty) {
                options.glintStrength().set(this.pendingVanillaGlintStrength / 100.0D);
                this.vanillaGlintStrengthDirty = false;
            }
            if (this.vanillaMenuBlurDirty) {
                options.menuBackgroundBlurriness().set(this.pendingVanillaMenuBlur);
                this.vanillaMenuBlurDirty = false;
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
