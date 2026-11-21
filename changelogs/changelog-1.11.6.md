# Borderless Window 1.11.6

*NeoForge · Fabric 1.21.1 · 2026-11-20*

> **Alt+Tab always leaves exclusive Fullscreen.** Right after the monitor switched modes, Windows could put the game back on screen. Plus a clearer tooltip and a warning against a mod with the same ID.

## ⚠️ Important Notes

- Every setting stays as it was
- When updating, delete the 1.11.5 jar from your mods folder
- **On NeoForge, the CurseForge file is now marked incompatible with Borderless by nimueller.** Both mods use the mod ID `borderlesswindow`, and with both installed NeoForge silently loads only theirs, without an error. The CurseForge app now warns before you install them together

## 🐛 Fixes

- **Alt+Tab out of Fullscreen at a lower Fullscreen Resolution.** The game minimized and the desktop got its resolution back, but Windows could show the game again a moment later, without the focus: the screen flickered and the game stayed in front, back at its own resolution, until clicked. It now gets minimized again, so the app you picked comes to the front. This also happens without the mod; it showed up more often with it, since the mod keeps the Fullscreen Resolution you pick, which Windows would otherwise undo. Borderless and Windowed were not affected

## 🔧 Changes

- **Clearer Pause Rendering When Minimized tooltip**: no frames are drawn, while the game loop keeps running, slowed to 10 times per second. It used to say "10 FPS", which read as if frames were still being drawn

## 📦 Dependencies

- Minecraft 1.21.1 with NeoForge 21.1.26 or newer, or with Fabric Loader 0.16.0 or newer
- Optional: Sodium 0.8.x on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Alt+Tab siempre sale de la Pantalla completa exclusiva.** Justo después de que el monitor cambiaba de modo, Windows podía volver a poner el juego en pantalla. Además, un tooltip más claro y un aviso contra un mod con el mismo ID.

## ⚠️ Notas Importantes

- Los ajustes se quedan como estaban
- Al actualizar, borra el jar de la 1.11.5 de tu carpeta de mods
- **En NeoForge, el archivo de CurseForge ahora está marcado como incompatible con Borderless de nimueller.** Los dos mods usan el ID `borderlesswindow`, y con los dos instalados NeoForge carga solo el suyo, en silencio y sin error. La app de CurseForge ahora avisa antes de instalarlos juntos

## 🐛 Arreglos

- **Alt+Tab desde Pantalla completa a una Resolución de pantalla completa menor.** El juego se minimizaba y el escritorio recuperaba su resolución, pero Windows podía volver a mostrar el juego un momento después, sin el foco: la pantalla parpadeaba y el juego se quedaba al frente, otra vez en su resolución, hasta darle clic. Ahora se vuelve a minimizar, así que queda al frente la aplicación que elegiste. También pasa sin el mod; con él se notaba más, porque el mod mantiene la Resolución de pantalla completa que eliges, que Windows de otro modo deshace. Sin bordes y Ventana no tenían el problema

## 🔧 Cambios

- **Tooltip más claro de Pausar render al minimizar**: no se dibuja ningún frame, mientras el ciclo del juego sigue corriendo, más lento: 10 veces por segundo. Antes decía "10 FPS", que sonaba a que se seguían dibujando frames

## 📦 Dependencias

- Minecraft 1.21.1 con NeoForge 21.1.26 o más nuevo, o con Fabric Loader 0.16.0 o más nuevo
- Opcional: Sodium 0.8.x en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
