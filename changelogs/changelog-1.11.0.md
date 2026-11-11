# Borderless Window 1.11.0

*NeoForge · Fabric 26.1.2 · 2026-11-11*

> **Now on Minecraft 26.1.2.** Every feature of 1.11.0, on both loaders, with nothing else to install, and a Fullscreen mode that is always exclusive.

## ⚠️ Important Notes

- **The first version for Minecraft 26.1.2.** Download the file for 26.1.2 and your loader; the 1.21.x jars do not work here
- **Fullscreen is always exclusive.** Minecraft 26.1 added its own "Exclusive Fullscreen" option, off by default. Off, Minecraft's fullscreen is not really exclusive: it stays on top of everything and still minimizes when you Alt+Tab, a weaker version of Borderless. Sodium turns that option on at first launch, so the same Fullscreen behaved differently with and without Sodium. Now the mod's Fullscreen is exclusive either way, and Minecraft's option is hidden from Video Settings, since the Screen Mode selector already covers both kinds of fullscreen. Borderless is still the one to use with HDR or for instant Alt+Tab
- **If you type with a Chinese, Japanese or Korean input method**, use Borderless instead of Fullscreen. Sodium keeps Minecraft's fullscreen non-exclusive for those input methods, and Borderless works better with them
- **Sodium integration needs Sodium 0.9.0 or newer.** Sodium's 26.1 builds replaced its Fullscreen checkbox with a "Fullscreen Mode" selector (Off / Exclusive / Borderless); the mod's Screen Mode takes its place, as it did with the checkbox
- Same config file and option IDs as on 1.21.x: copying `config/borderlesswindow.json` from an older instance carries your settings over

## ✨ New Features

- **Everything from 1.11.0, on Minecraft 26.1.2**: the three screen modes and configurable F11, the FPS counter, the background options with the idle limit in two steps, the fog toggles and Fog Start, cloud height, sky and weather, the held item name, screen and camera overlays, split FOV, distortion and view bobbing, zoom, enchantment numerals, draw distances, and Minecraft's own sliders that Sodium leaves out
- With Sodium, the mod's six pages and the Screen Mode selector work as on 1.21.11, also alongside Reese's Sodium Options and Iris

## 🔧 Technical Changes

- Ported to Minecraft 26.1.2, which ships unobfuscated, runs on Java 25 and splits every frame into update, extract and render. Every hook was checked against the 26.1.2 code on both loaders:
  - **Zoom and Fluid FOV**: the camera now works out its own FOV, so both live in `Camera` (`calculateFov` and `modifyFovBasedOnDeathOrFluid`). The zoom still applies last, after NeoForge's ComputeFov event
  - **Pause Rendering When Minimized**: skips extracting, drawing and presenting the frame in `Minecraft.renderFrame`
  - **Weather**: empties the rain and snow columns of the weather render state. Skipping the draw call, as before, now crashes on an empty buffer
  - **Fog**: the same hook, now on the `FogData` that `FogRenderer.setupFog` returns
  - **Unfocused volume**: follows the window's own focus callback (`Window.onFocus`), which also counts clicking back in as activity for the idle limit
  - **HUD, overlays and screens**: on Minecraft's renamed extract methods (`GuiGraphicsExtractor`)
- **Fullscreen**: Minecraft's option only sets a GLFW hint (`GLFW_SOFT_FULLSCREEN`, from LWJGL's own GLFW) when the game starts. On Windows that hint just makes the window 1 px taller than the monitor so it is not taken over as exclusive. The mod forces it off in `GLX._initGlfw`; GLFW reads it each time it fits a window to a monitor, so it covers every switch to Fullscreen without a restart
- **On Fabric**, the bundled key binding module is now `fabric-key-mapping-api-v1`, renamed in 26.1, and the mod jar is no longer remapped. Fabric API is still not required
- **Fixed:** the title of the mod's config screen (without Sodium) was not drawn. Since Minecraft 1.21.6 text with a fully transparent color is skipped, and the title's white had no alpha

## 📦 Dependencies

- Minecraft 26.1.2 with NeoForge 26.1.2.x, or with Fabric Loader 0.18.4 or newer
- Optional: Sodium 0.9.0 or newer on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Ahora en Minecraft 26.1.2.** Todas las funciones de la 1.11.0, en los dos loaders y sin instalar nada más, y con un modo Pantalla completa que siempre es exclusivo.

## ⚠️ Notas Importantes

- **La primera versión para Minecraft 26.1.2.** Descarga el archivo para 26.1.2 y tu loader; los jars de la 1.21.x no funcionan aquí
- **Pantalla completa siempre es exclusiva.** Minecraft 26.1 agregó su propia opción "Pantalla completa exclusiva", desactivada por defecto. Desactivada, la pantalla completa de Minecraft no es exclusiva de verdad: se queda encima de todo y aun así se minimiza con Alt+Tab, una versión peor de Sin bordes. Sodium activa esa opción en su primer arranque, así que la misma Pantalla completa se comportaba distinto con y sin Sodium. Ahora la Pantalla completa del mod es exclusiva en los dos casos, y la opción de Minecraft se oculta de Opciones de gráficos, porque el selector de Modo de pantalla ya cubre los dos tipos de pantalla completa. Sin bordes sigue siendo el modo para usar con HDR o para un Alt+Tab instantáneo
- **Si escribes con un método de entrada chino, japonés o coreano**, usa Sin bordes en lugar de Pantalla completa. Sodium deja la pantalla completa de Minecraft no exclusiva para esos métodos de entrada, y Sin bordes funciona mejor con ellos
- **La integración con Sodium requiere Sodium 0.9.0 o más nuevo.** Las versiones de Sodium para 26.1 reemplazaron su checkbox de Pantalla completa con un selector "Fullscreen Mode" (Off / Exclusive / Borderless); el Modo de pantalla del mod toma su lugar, como lo hacía con el checkbox
- El mismo archivo de configuración e IDs de opciones que en 1.21.x: copiar `config/borderlesswindow.json` de una instancia anterior conserva tus ajustes

## ✨ Nuevas Características

- **Todo lo de la 1.11.0, en Minecraft 26.1.2**: los tres modos de pantalla y F11 configurable, el contador de FPS, las opciones de segundo plano con el límite por inactividad en dos escalones, los toggles de niebla y el Inicio de la niebla, la altura de las nubes, cielo y clima, el nombre del objeto en mano, los overlays de pantalla y de cámara, el FOV, la distorsión y el balanceo separados, el zoom, los números de encantamientos, las distancias de dibujado, y los sliders de Minecraft que Sodium deja fuera
- Con Sodium, las seis páginas del mod y el selector de Modo de pantalla funcionan como en 1.21.11, también junto a Reese's Sodium Options e Iris

## 🔧 Cambios Técnicos

- Portado a Minecraft 26.1.2, que viene sin ofuscar, corre en Java 25 y divide cada frame en actualizar, extraer y dibujar. Cada hook se revisó contra el código de 26.1.2 en los dos loaders:
  - **Zoom y FOV en fluidos**: la cámara ahora calcula su propio FOV, así que los dos viven en `Camera` (`calculateFov` y `modifyFovBasedOnDeathOrFluid`). El zoom se sigue aplicando al final, después del evento ComputeFov de NeoForge
  - **Pausar render al minimizar**: se salta extraer, dibujar y presentar el frame en `Minecraft.renderFrame`
  - **Clima**: vacía las columnas de lluvia y nieve del estado de render del clima. Saltarse la llamada de dibujo, como antes, ahora crashea con un buffer vacío
  - **Niebla**: el mismo hook, ahora sobre el `FogData` que regresa `FogRenderer.setupFog`
  - **Volumen sin foco**: sigue el callback de foco de la propia ventana (`Window.onFocus`), que además cuenta volver a hacer clic en el juego como actividad para el límite por inactividad
  - **HUD, overlays y pantallas**: sobre los métodos extract renombrados de Minecraft (`GuiGraphicsExtractor`)
- **Pantalla completa**: la opción de Minecraft solo define una pista de GLFW (`GLFW_SOFT_FULLSCREEN`, del GLFW propio de LWJGL) al iniciar el juego. En Windows esa pista solo hace la ventana 1 px más alta que el monitor para que no se tome como exclusiva. El mod la fuerza a desactivada en `GLX._initGlfw`; GLFW la lee cada vez que ajusta una ventana a un monitor, así que cubre cada cambio a Pantalla completa sin reiniciar
- **En Fabric**, el módulo incluido para las teclas ahora es `fabric-key-mapping-api-v1`, renombrado en 26.1, y el jar del mod ya no se remapea. Sigue sin requerir Fabric API
- **Corregido:** el título de la pantalla de configuración del mod (sin Sodium) no se dibujaba. Desde Minecraft 1.21.6 se omite el texto con un color totalmente transparente, y el blanco del título no tenía alfa

## 📦 Dependencias

- Minecraft 26.1.2 con NeoForge 26.1.2.x, o con Fabric Loader 0.18.4 o más nuevo
- Opcional: Sodium 0.9.0 o más nuevo en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
