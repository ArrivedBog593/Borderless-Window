# 1.7.0

## Unfocused FPS limit

- New **Unfocused FPS Limit**: caps the frame rate while the game window is not focused — after Alt+Tab, while minimized, or with another window or monitor in front. Off by default, then 5 to 60 FPS in steps of 5
- Minecraft 1.21.1 keeps rendering at full speed in the background, burning GPU power and heat on frames nobody sees. 1.21.2 later added its own limit; this brings the same idea to 1.21.1
- It can only lower the frame rate: a regular Max Framerate that is already lower still wins, and menus with no world loaded keep Minecraft's own 60 FPS. The cap lifts on the very next frame after you click back into the game
- With **Dynamic FPS** installed the option steps aside and is greyed out, with a tooltip saying why. Both mods hook the same methods, and two settings fighting over one behavior would only be confusing — Dynamic FPS covers this and more

## Draw distances

- Four new sliders that stop drawing things past a distance you choose: **Dropped Items**, **Experience Orbs**, **Armor Stands** and **Particles**. Every slider starts at **Default** on the left, which keeps Minecraft's own distance
- Minecraft draws items up to 16 blocks away and orbs up to 32 (both scaled by its Entity Distance option), armor stands as far as the server sends them, and spawns particles up to 32 blocks away. These sliders can only lower those numbers
- It is purely visual: items and orbs are only hidden, never removed. Items can still be picked up, orbs still fly to you, and nothing changes for the server or other players
- **Glowing** entities are always drawn, since the outline is information meant to be seen from afar
- **Armor stands with a visible name** are always drawn: servers and adventure maps use them as floating text and holograms
- Particles are refused when they would spawn, which is where their cost is, rather than hidden while they already exist. Particles the game marks as always visible are left alone, exactly as Minecraft does with its own 32-block limit

## Sodium integration

- The mod's section went from nine pages to four: **General** (F11, FPS overlay and unfocused limit, enchantment numerals), **Visual Effects** (fog, clouds, screen overlays), **Camera** (bobbing, FOV, distortion) and **Distances**. Inside each page the features are split into groups, the same way Sodium's own General page does it
- Every option keeps its ID, so existing settings carry over untouched
- The no-Sodium config screen follows the same order, keeping its per-feature headers

## Internal

- New `distance/` package; `fps/` gains `UnfocusedFpsState`
- New mixins: `MinecraftMixin` (`getFramerateLimit`), `EntityRenderDispatcherMixin` (`shouldRender`) and `LevelRendererMixin` (`addParticleInternal`). Sodium's entity culling wraps the frustum test one call deeper, inside `EntityRenderer.shouldRender`, so the two stack cleanly
- New generic `IntOptionSlider` for the no-Sodium screen: a stepped integer slider with the label supplied by the caller. Arrow keys move one whole step instead of vanilla's one pixel
- New `build.yml` workflow: builds every push and pull request to `master` and keeps the jar as an artifact for 14 days. A commit message containing `[curseforge]` also uploads that build to CurseForge
- Dependabot now keeps the workflow actions up to date
