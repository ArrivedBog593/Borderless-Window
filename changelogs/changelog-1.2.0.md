# 1.2.0

- Added a configurable FPS overlay with three modes: Off, Simple (current FPS) and Extended (current FPS + Min / Avg / Max over the last 15 seconds)
- FPS overlay position is configurable (any of the four screen corners)
- Overlay stats sample the game's own FPS counter, so values always match F3 and respect VSync / FPS caps (no fake "Max" readings)
- Overlay auto-hides while the F3 debug screen is open or the GUI is hidden (F1)
- New options available both in the Mods menu Config screen (no Sodium needed) and integrated into Sodium's video settings menu when Sodium is installed
- Config file now also stores fps_overlay_mode and fps_overlay_position (older configs load fine, new fields fall back to defaults)