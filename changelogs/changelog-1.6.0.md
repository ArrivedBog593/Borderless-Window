# 1.6.0

## Camera overlays

- Added four toggles for the full-screen overlays Minecraft draws over your view: **Pumpkin**, **Freeze**, **Portal** and **Spyglass**
- **Pumpkin Overlay** removes the blur drawn while wearing a carved pumpkin, so it can be worn against endermen without the visual cost. Helmet overlays added by other mods are never affected
- **Freeze Overlay** removes the frost border drawn while freezing in powder snow. That border also warns you are taking freeze damage; the frozen hearts on the HUD still show it
- **Portal Overlay** removes the purple warp drawn while standing inside a Nether portal, which mostly gets in the way while building or aligning them
- **Spyglass Overlay** removes the scope texture and the black bars. The zoom itself is unchanged — only the frame around it is removed
- All four live in the existing Screen Overlays section, alongside the water and fire toggles
- The vignette is deliberately not offered: it doubles as the world border warning, and a plain toggle would delete that warning along with the darkening

## Field of view

- Minecraft has one **FOV Effects** slider for every source at once. It is now split into four: **Speed**, **Flying**, **Bow** and **Fluid**, each 0-100% with 100% being vanilla
- **Speed FOV** is the one worth having: with Speed potions or a modified movement attribute the FOV never settles, and until now there was no way to soften that without removing the bow zoom too
- **Flying FOV** covers creative flight only. Elytra gliding is a different thing and is not affected
- **Bow FOV** keeps the same easing curve and only changes how deep the zoom goes. The spyglass is never touched: its zoom is what the item does, not a visual effect
- **Fluid FOV** is the squeeze applied underwater and in lava
- All four sit under Minecraft's own FOV Effects slider rather than replacing it — with that at Off, nothing here has any effect

## Distortion

- Minecraft has one **Distortion Effects** slider covering both the Nether portal and the Nausea effect. It is now split into **Portal Distortion** and **Nausea Distortion**, each 0-100%
- The two share one accumulator internally, so the source is attributed per tick from the rate vanilla itself picks. Walking into a portal while under a Nausea potion correctly applies the portal percentage, and applies the Nausea one again the moment you step out
- Both sit under Minecraft's own Distortion Effects slider, same as the FOV options
- The green nausea texture is left entirely to vanilla. It is Minecraft's accessibility trade — a static cue that replaces the spinning as you lower *its* slider — and driving it from ours turned "less motion" into a bright green full-screen overlay, which is the opposite of what the option is for

## Sodium integration

- Two vanilla sliders Sodium's video settings leave out are now available inside the mod's pages: **Distortion Effects (Minecraft)** and **FOV Effects (Minecraft)**, each at the top of the page it governs so the hierarchy reads top-down
- Both are the same settings found in Minecraft's Accessibility Settings and are saved with Minecraft's options, not the mod's
- The Sodium section now has nine per-feature pages: General, FPS Overlay, Fog, Clouds, Screen Overlays, Bobbing, FOV, Distortion and Enchantment Style
- Every option remains fully available without Sodium via the Mods menu Config screen, except the two vanilla sliders above — without Sodium those are one click away in Minecraft's own menus and duplicating them would serve nobody

## Internal

- New `fov/` and `distortion/` packages; `screeneffect/` gains `CameraOverlayFilter`
- New mixins: `GuiMixin`, `AbstractClientPlayerMixin`, `LocalPlayerMixin`
- The camera overlays need bytecode rather than an event: NeoForge exposes the whole group as a single `CAMERA_OVERLAYS` GUI layer, so cancelling it would remove all of them at once. The pumpkin and freeze overlays share one vanilla method and are told apart by texture
- The spyglass injection wraps the call site rather than the method, so NeoForge's `getScopeOverlayTexture` patch keeps working for mods that supply custom scopes
- Distortion source attribution reads the per-tick rate vanilla chooses inside `handleConfusionTransitionEffect` with MixinExtras `@Local`. Three earlier approaches were tried and each failed on a real case: asking for the Nausea effect at render time gets the overlap backwards, the boolean the method receives stays true long after you leave a portal, and measuring how far the accumulator moved breaks both when it clamps at 1.0 and when changing dimension resets it
- The speed FOV injection lerps the movement speed attribute toward the walking speed, which is algebraically the same as scaling the multiplier vanilla derives from it — there is no constant in that expression to scale directly