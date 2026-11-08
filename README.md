# Borderless Window

[![CurseForge](https://cf.way2muchnoise.eu/versions/minecraft-borderless-window.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-borderless-window)
[![CurseForge](https://cf.way2muchnoise.eu/minecraft-borderless-window.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-borderless-window)

*[Español](README_ES.md)*

A lightweight client-side mod for **NeoForge 1.21.1** that adds **borderless fullscreen** to Minecraft, replacing the exclusive fullscreen toggle with a proper 3-mode system — plus a set of video and quality-of-life options Minecraft doesn't expose.

## Features

### Window

- **Three screen modes**: Windowed, Borderless, and Minecraft's regular exclusive Fullscreen.
- **Configurable F11**: toggle Borderless, toggle exclusive Fullscreen, or **cycle through all three modes**.
- **HDR-friendly**: avoids the black-screen flash and color profile switch that exclusive fullscreen (and naive borderless implementations) cause with Windows HDR enabled.
- **Multi-monitor aware**: applies on the monitor the window is currently on, not always the primary one.

### Video and quality of life

- **FPS overlay**: the current FPS, or a single line with the Min / Avg / Max over the last 15 seconds. Any screen corner. It samples the game's own counter, so it always matches F3 and respects VSync and FPS caps, and it hides itself while F3 is open.
- **Background options**: Minecraft 1.21.1 keeps running at full speed and full volume when nobody is looking. Four independent options fix that:
  - **Unfocused FPS limit**: cap the frame rate while the game is in the background (alt-tabbed, minimized, or behind another window), from 5 to 60 FPS.
  - **Idle FPS limit**: cap the frame rate after a few minutes with no keyboard or mouse input, even while the window is focused. Any input lifts it at once.
  - **Pause rendering when minimized**: stop drawing frames entirely while minimized. The game itself keeps running — only the picture stops.
  - **Unfocused volume**: lower or mute the game while it is in the background, as a share of your Master Volume.
  - All four work alongside Dynamic FPS: the lower frame rate cap wins.
- **Draw distances**: stop drawing dropped items, experience orbs, and armor stands past a distance you choose, and stop particles from spawning past one. Purely visual — items can still be picked up and orbs still reach you. Glowing entities and named armor stands (holograms, floating text) are always drawn.
- **Fog toggles**: Terrain (the distance haze, including the Nether's thick fog), Water, Lava, and Powder Snow, each independent. Fog from Blindness and Darkness is never affected, so those effects still work as intended.
- **Cloud height**: raise or lower the cloud layer between y=64 and y=320. On Default every dimension keeps its own height, and dimensions with no clouds at all — the Nether, the End, cloudless dimensions from other mods — stay that way at any setting.
- **Screen overlays**: remove the water texture drawn over your view while swimming, and the flames drawn while burning. Fire also has a smart mode that hides the flames only while Fire Resistance is active. The suffocation overlay shown inside solid blocks is never touched.
- **Camera overlays**: remove the pumpkin blur, the powder snow frost border, the Nether portal warp, and the spyglass scope. The spyglass zoom itself is unchanged — only the frame around it goes. The vignette is left alone on purpose: it doubles as the world border warning.
- **Split field of view**: Minecraft has one FOV Effects slider for every source at once. Here Speed, Flying, Bow, and Fluid are four separate sliders, so you can calm the constant FOV shift from Speed potions without losing the bow zoom.
- **Split distortion**: same idea for the Distortion Effects slider — the Nether portal and the Nausea effect get one slider each, correctly told apart even when both happen at once.
- **Split view bobbing**: Minecraft turns bobbing on or off for everything at once. Here the camera and the held item are separate, and the camera adds a **Vertical Only** mode that keeps the up-and-down bounce while removing the sway and the tilt — the horizon stays level, which is the part that causes motion sickness for most people.
- **Enchantment numerals**: Minecraft only ships Roman numerals up to X and shows raw text like `enchantment.level.11` above that — levels are legal all the way to 255. This fixes it and lets you choose the style: Roman up to X and plain numbers past it, always Roman, or always numbers. Resource packs that do translate high levels keep working.
- **Minecraft's hidden sliders**: Sodium's video settings leave out several of Minecraft's own sliders. The mod brings back Distortion Effects, FOV Effects, Glint Speed, Glint Strength, and Menu Background Blur, each next to the options it relates to.

### Configuration and compatibility

- **Config screen included**: change everything from the Mods menu (select the mod → Config button). No other mod needed.
- **Sodium integration** (optional): with [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) 0.8.x installed, the "Fullscreen" checkbox in its video settings is replaced by a "Screen Mode" selector, and the mod gets its own section with six pages. Built entirely on Sodium's official Config API — no fragile mixins into Sodium internals. Also plays nice with Reese's Sodium Options.
- **Zero required dependencies**: works with or without Sodium.
- **Persistent**: all settings are remembered between sessions (`config/borderlesswindow.json`).
- **Translatable**: all strings live in standard lang files (`en_us`, `es_mx`, `es_es` included — contributions welcome).

## Why borderless?

Exclusive fullscreen changes your monitor's actual display mode, which causes slow alt-tabbing, black-screen flashes with HDR, resolution desync bugs, and broken window states when the game restarts. Borderless fullscreen is just a window sized to your monitor with no decorations: it looks identical, but alt-tab is instant and nothing ever desyncs.

## Configuration

- **In game (no Sodium):** Mods menu → Borderless Window → Config. Every option lives in one scrollable list grouped by feature, in the same order as the Sodium pages. Changes apply instantly.
- **In game (with Sodium):** Video Settings → the "Screen Mode" option (replaces Sodium's fullscreen checkbox) and the "Borderless Window" section with its six pages: General, Enchantments, FPS & Background, Visual Effects, Camera, and Distances. Minecraft's own sliders that Sodium leaves out sit on the pages they relate to; they are saved with Minecraft's options, exactly as if changed from its Accessibility menu.
- **Config file:** `config/borderlesswindow.json`. If the mod ever gets stuck in a weird state, delete this file and restart — defaults are regenerated safely.

Every setting defaults to vanilla behavior, so installing or updating the mod never changes how the game looks until you ask it to.

**Note on refined settings:** several options sharpen a Minecraft setting rather than replacing it, and do nothing while that setting is off. Bobbing sits under "View Bobbing", the four FOV sliders under "FOV Effects", and the two distortion sliders under "Distortion Effects". The last two live in Minecraft's Accessibility Settings.

## Requirements

- Minecraft **1.21.1**
- **NeoForge** 21.1.x
- (Optional) **Sodium 0.8.0+** for the video settings integration

## License

[MIT](LICENSE)
