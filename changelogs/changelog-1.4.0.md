# 1.4.0

- Added cloud height: the cloud layer can be raised or lowered between y=64 and y=320, or left on Default so each dimension keeps its own height
- Dimensions that declare no clouds (the Nether, the End, cloudless dimensions from other mods) stay cloudless at any setting
- Added water and fire screen overlay toggles, plus a smart fire mode that hides the flames only while Fire Resistance is active
- The suffocation overlay shown inside solid blocks is never affected, regardless of the toggles (gameplay-safe by design)
- Added split view bobbing: the camera and the held item are now controlled separately, and the camera gains a Vertical Only mode that keeps the vertical bounce while removing the lateral sway and the horizon tilt
- Bobbing options refine Minecraft's own View Bobbing setting rather than replacing it, and have no effect while it is off
- Config screen rebuilt as a scrollable list grouped by feature, so options no longer overflow the screen as features are added
- Sodium video settings integration expanded to six per-feature pages: General, FPS Overlay, Fog, Clouds, Screen Overlays and Bobbing
- Fixed the FPS overlay tooltip describing the Min value as a 1% low; it is the lowest one-second sample in the window
- Fixed es_es using "Predeterminado" where both Spanish locales use "Por defecto"
- All options remain fully available without Sodium via the Mods menu Config screen
- Internal: new per-feature packages (cloud / screen-effect / bobbing); bobbing uses MixinExtras wrappers instead of @Redirect so it stacks with other mods touching the same call sites