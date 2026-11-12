# Borderless Window 1.11.0

*NeoForge · Fabric 26.2 · 2026-11-11*

> **Now on Minecraft 26.2.** Every feature of 1.11.0, on both loaders, with nothing else to install. With Iris shaders on, the zoom's Hide Hand and the separate Hand Bobbing now work too.

## ⚠️ Important Notes

- **The first version for Minecraft 26.2.** Download the file for 26.2 and your loader; the 26.1.2 and 1.21.x jars do not work here
- **Fullscreen is always exclusive with OpenGL**, as on 26.1.2: Minecraft's "Exclusive Fullscreen" option stays hidden from Video Settings, since the Screen Mode selector already covers both kinds of fullscreen. **With Vulkan** (Minecraft 26.2's new Graphics API option), Minecraft's fullscreen is never exclusive, whatever the mod or that option does, so Fullscreen there does not conflict with HDR. Borderless is still the one to use with HDR or for instant Alt+Tab on OpenGL
- **If you type with a Chinese, Japanese or Korean input method**, use Borderless instead of Fullscreen. Sodium keeps Minecraft's fullscreen non-exclusive for those input methods, and Borderless works better with them
- **Vulkan on NeoForge:** NeoForge 26.2.0.93's early loading screen always opens an OpenGL window, so with Graphics API on Vulkan the game stops at launch with "GLFW error 65540". This is a NeoForge issue; until it is fixed, set `earlyWindowControl = false` in `config/fml.toml`. Fabric is not affected
- **Sodium integration needs Sodium 0.9.0 or newer**, as on 26.1.2
- Same config file and option IDs as on 1.21.x and 26.1.2: copying `config/borderlesswindow.json` from an older instance carries your settings over

## ✨ New Features

- **Everything from 1.11.0, on Minecraft 26.2**: the three screen modes and configurable F11, the FPS counter, the background options with the idle limit in two steps, the fog toggles and Fog Start, cloud height, sky and weather, the held item name, screen and camera overlays, split FOV, distortion and view bobbing, zoom, enchantment numerals, draw distances, and Minecraft's own sliders that Sodium leaves out
- **Iris support for the hand settings:** with a shaderpack on, Iris draws the hand itself, so Hide Hand While Zooming and Hand Bobbing used to have no effect. Both now work with shaders as well
- **The mod's icon** next to its name in Sodium's page list, as Sodium, Iris and Reese's Sodium Options show theirs
- **Links to the mod's page:** NeoForge's mod list now shows the button to its GitHub page, and the Website button in Mod Menu on Fabric works (it was greyed out)
- **Borderless now renders at exactly the monitor's resolution** (1920×1080 on a 1080p monitor, as F3 shows). Before, the picture was one row taller than the monitor and that row was hidden below the screen

## 🔧 Technical Changes

- Ported to Minecraft 26.2. Every hook was checked against the 26.2 code on both loaders:
  - **HUD**: Minecraft 26.2 moved the HUD out of `Gui` into a new `Hud` class, so the overlay, held item name and FPS counter hooks follow it. Hide HUD While Zooming now flips the HUD's own hidden state (F1 is no longer `options.hideGui`) around one frame and puts it back, so F1 stays as you left it and the hand and screen effects are not hidden with it
  - **Pause Rendering When Minimized**: the game now presents through its window surface and already skips copying and presenting while minimized; the mod skips extracting and drawing the frame
  - **Fullscreen**: Minecraft sets its fullscreen hint in its constructor now, once for each graphics API it tries; the mod forces it off there
  - **Borderless**: Minecraft 26.2 takes 1 px off the picture of a non-exclusive fullscreen window, which is 1 px taller than the monitor, as the mod's Borderless is. The mod ties that to its Borderless mode instead of Minecraft's fullscreen flag, which it no longer follows. Switching between Borderless and Fullscreen on the same monitor also reports the size change, which Minecraft would otherwise miss
  - **On Fabric**, the FPS counter and the water and fire overlays follow Minecraft's renamed `Hud.extractRenderState` and `ScreenEffectRenderer.submit`
- **Startup**: the saved screen mode is now restored on the second frame on both loaders, as it already was on Fabric. NeoForge's client setup, which did it before, runs on a worker thread since 26.1, and moving the window from there hung the game when NeoForge's early loading screen was off
- **Iris**: the hand settings hook into Iris's own hand renderer, only when Iris is installed. Without Iris the hook is skipped before Mixin looks for Iris's class, so no warning shows up in the log

## 📦 Dependencies

- Minecraft 26.2 with NeoForge 26.2.x, or with Fabric Loader 0.18.4 or newer
- Optional: Sodium 0.9.0 or newer on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Ahora en Minecraft 26.2.** Todas las funciones de la 1.11.0, en los dos loaders y sin instalar nada más. Con shaders de Iris, ahora también funcionan Ocultar mano al hacer zoom y el Balanceo de la mano por separado.

## ⚠️ Notas Importantes

- **La primera versión para Minecraft 26.2.** Descarga el archivo para 26.2 y tu loader; los jars de 26.1.2 y 1.21.x no funcionan aquí
- **Pantalla completa siempre es exclusiva con OpenGL**, como en 26.1.2: la opción "Pantalla completa exclusiva" de Minecraft sigue oculta en Opciones de gráficos, porque el selector de Modo de pantalla ya cubre los dos tipos de pantalla completa. **Con Vulkan** (la nueva opción API gráfica de Minecraft 26.2), la pantalla completa de Minecraft nunca es exclusiva, haga lo que haga el mod o esa opción, así que ahí Pantalla completa no choca con el HDR. Sin bordes sigue siendo el modo para usar con HDR o para un Alt+Tab instantáneo en OpenGL
- **Si escribes con un método de entrada chino, japonés o coreano**, usa Sin bordes en lugar de Pantalla completa. Sodium deja la pantalla completa de Minecraft no exclusiva para esos métodos de entrada, y Sin bordes funciona mejor con ellos
- **Vulkan en NeoForge:** la pantalla de carga temprana de NeoForge 26.2.0.93 siempre abre una ventana OpenGL, así que con la API gráfica en Vulkan el juego se detiene al arrancar con "GLFW error 65540". Es un problema de NeoForge; mientras lo arreglan, pon `earlyWindowControl = false` en `config/fml.toml`. A Fabric no le pasa
- **La integración con Sodium requiere Sodium 0.9.0 o más nuevo**, como en 26.1.2
- El mismo archivo de configuración e IDs de opciones que en 1.21.x y 26.1.2: copiar `config/borderlesswindow.json` de una instancia anterior conserva tus ajustes

## ✨ Nuevas Características

- **Todo lo de la 1.11.0, en Minecraft 26.2**: los tres modos de pantalla y F11 configurable, el contador de FPS, las opciones de segundo plano con el límite por inactividad en dos escalones, los toggles de niebla y el Inicio de la niebla, la altura de las nubes, cielo y clima, el nombre del objeto en mano, los overlays de pantalla y de cámara, el FOV, la distorsión y el balanceo separados, el zoom, los números de encantamientos, las distancias de dibujado, y los sliders de Minecraft que Sodium deja fuera
- **Soporte de Iris para los ajustes de la mano:** con un shaderpack activo, Iris dibuja la mano por su cuenta, así que Ocultar mano al hacer zoom y Balanceo de la mano no tenían efecto. Ahora los dos funcionan también con shaders
- **El ícono del mod** junto a su nombre en la lista de páginas de Sodium, como muestran el suyo Sodium, Iris y Reese's Sodium Options
- **Ligas a la página del mod:** la lista de mods de NeoForge ahora muestra el botón a su página de GitHub, y el botón Sitio web de Mod Menu en Fabric funciona (salía en gris)
- **Sin bordes ahora dibuja exactamente a la resolución del monitor** (1920×1080 en un monitor 1080p, como muestra F3). Antes la imagen medía una fila más que el monitor y esa fila quedaba oculta abajo de la pantalla

## 🔧 Cambios Técnicos

- Portado a Minecraft 26.2. Cada hook se revisó contra el código de 26.2 en los dos loaders:
  - **HUD**: Minecraft 26.2 sacó el HUD de `Gui` a una clase nueva, `Hud`, así que los hooks de los overlays, el nombre del objeto en mano y el contador de FPS la siguen. Ocultar HUD al hacer zoom ahora voltea el estado oculto del propio HUD (F1 ya no es `options.hideGui`) durante un frame y lo regresa, así que F1 queda como lo dejaste y la mano y los efectos de pantalla no se ocultan con él
  - **Pausar render al minimizar**: el juego ahora presenta a través de la superficie de su ventana y ya se salta copiar y presentar mientras está minimizado; el mod se salta extraer y dibujar el frame
  - **Pantalla completa**: Minecraft ahora define su pista de pantalla completa en su constructor, una vez por cada API gráfica que prueba; el mod la fuerza a desactivada ahí
  - **Sin bordes**: Minecraft 26.2 le quita 1 px a la imagen de una ventana de pantalla completa no exclusiva, que mide 1 px más que el monitor, como el Sin bordes del mod. El mod lo liga a su modo Sin bordes en lugar del flag de pantalla completa de Minecraft, que ya no sigue. Cambiar entre Sin bordes y Pantalla completa en el mismo monitor también avisa del cambio de tamaño, que Minecraft de otro modo no notaría
  - **En Fabric**, el contador de FPS y los overlays de agua y fuego siguen a `Hud.extractRenderState` y `ScreenEffectRenderer.submit`, renombrados por Minecraft
- **Arranque**: el modo de pantalla guardado ahora se restaura en el segundo frame en los dos loaders, como ya pasaba en Fabric. El client setup de NeoForge, que lo hacía antes, corre en un hilo aparte desde 26.1, y mover la ventana desde ahí colgaba el juego con la pantalla de carga temprana de NeoForge desactivada
- **Iris**: los ajustes de la mano se enganchan al renderizador de mano propio de Iris, solo cuando Iris está instalado. Sin Iris el hook se omite antes de que Mixin busque la clase de Iris, así que no sale ningún aviso en el log

## 📦 Dependencias

- Minecraft 26.2 con NeoForge 26.2.x, o con Fabric Loader 0.18.4 o más nuevo
- Opcional: Sodium 0.9.0 o más nuevo en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
