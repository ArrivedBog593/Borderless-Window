# Borderless Window 1.11.4

*NeoForge · Fabric 26.1.2 · 2026-11-21*

> **Alt+Tab always leaves exclusive Fullscreen.** Right after the monitor switched modes, Windows could put the game back on screen. Also, Minecraft's "Reduce FPS When" is greyed out while the Idle FPS Limit replaces it, a clearer tooltip, and a warning against a mod with the same ID.

## ⚠️ Important Notes

- Every setting stays as it was
- When updating, delete the 1.11.3 jar from your mods folder
- **On NeoForge, the CurseForge file is now marked incompatible with Borderless by nimueller.** Both mods use the mod ID `borderlesswindow`, and with both installed NeoForge silently loads only theirs, without an error. The CurseForge app now warns before you install them together

## ✨ New Features

- **Minecraft's "Reduce FPS When" is greyed out while the Idle FPS Limit is on**, in Minecraft's video settings and on Sodium's page, since the Idle FPS Limit replaces it then. Its tooltip says why, and your choice in it is kept for when the Idle FPS Limit is turned off

## 🐛 Fixes

- **Alt+Tab out of Fullscreen at a lower Fullscreen Resolution.** The game minimized and the desktop got its resolution back, but Windows could show the game again a moment later, without the focus: the screen flickered and the game stayed in front, back at its own resolution, until clicked. It now gets minimized again, so the app you picked comes to the front. This also happens without the mod; it showed up more often with it, since the mod keeps the Fullscreen Resolution you pick, which Windows would otherwise undo. Borderless and Windowed were not affected

## 🔧 Changes

- **Clearer Pause Rendering When Minimized tooltip**: no frames are drawn, while the game loop keeps running 10 times per second, as Minecraft does when minimized. It used to say "10 FPS", which read as if frames were still being drawn

## 📦 Dependencies

- Minecraft 26.1.2 with NeoForge 26.1.2.71 or newer, or with Fabric Loader 0.18.4 or newer
- Optional: Sodium 0.9.0 or newer on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Alt+Tab siempre sale de la Pantalla completa exclusiva.** Justo después de que el monitor cambiaba de modo, Windows podía volver a poner el juego en pantalla. Además, "Reducir FPS" de Minecraft se ve desactivada mientras el Límite por inactividad la reemplaza, un tooltip más claro y un aviso contra un mod con el mismo ID.

## ⚠️ Notas Importantes

- Los ajustes se quedan como estaban
- Al actualizar, borra el jar de la 1.11.3 de tu carpeta de mods
- **En NeoForge, el archivo de CurseForge ahora está marcado como incompatible con Borderless de nimueller.** Los dos mods usan el ID `borderlesswindow`, y con los dos instalados NeoForge carga solo el suyo, en silencio y sin error. La app de CurseForge ahora avisa antes de instalarlos juntos

## ✨ Nuevas Características

- **"Reducir FPS" de Minecraft se ve desactivada mientras el Límite por inactividad está activado**, en los ajustes de video de Minecraft y en la página de Sodium, porque el Límite por inactividad la reemplaza mientras tanto. Su tooltip explica por qué, y lo que elegiste ahí se conserva para cuando desactives el Límite por inactividad

## 🐛 Arreglos

- **Alt+Tab desde Pantalla completa a una Resolución de pantalla completa menor.** El juego se minimizaba y el escritorio recuperaba su resolución, pero Windows podía volver a mostrar el juego un momento después, sin el foco: la pantalla parpadeaba y el juego se quedaba al frente, otra vez en su resolución, hasta darle clic. Ahora se vuelve a minimizar, así que queda al frente la aplicación que elegiste. También pasa sin el mod; con él se notaba más, porque el mod mantiene la Resolución de pantalla completa que eliges, que Windows de otro modo deshace. Sin bordes y Ventana no tenían el problema

## 🔧 Cambios

- **Tooltip más claro de Pausar render al minimizar**: no se dibuja ningún frame, mientras el ciclo del juego sigue corriendo 10 veces por segundo, como lo hace Minecraft al minimizar. Antes decía "10 FPS", que sonaba a que se seguían dibujando frames

## 📦 Dependencias

- Minecraft 26.1.2 con NeoForge 26.1.2.71 o más nuevo, o con Fabric Loader 0.18.4 o más nuevo
- Opcional: Sodium 0.9.0 o más nuevo en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
