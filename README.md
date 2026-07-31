# Borderless Window

A lightweight client-side mod for **NeoForge 1.21.1** that adds **borderless fullscreen** to Minecraft, replacing the exclusive fullscreen toggle with a proper 3-mode system — plus a set of video and quality-of-life options Minecraft doesn't expose.

*[Español más abajo](#español)*

## Features

### Window

- **Three screen modes**: Windowed, Borderless, and Minecraft's regular exclusive Fullscreen.
- **Configurable F11**: toggle Borderless, toggle exclusive Fullscreen, or **cycle through all three modes**.
- **HDR-friendly**: avoids the black-screen flash and colour profile switch that exclusive fullscreen (and naive borderless implementations) cause with Windows HDR enabled.
- **Multi-monitor aware**: applies on the monitor the window is currently on, not always the primary one.

### Video and quality of life

- **FPS overlay**: the current FPS, or a single line with the Min / Avg / Max over the last 15 seconds. Any screen corner. It samples the game's own counter, so it always matches F3 and respects VSync and FPS caps, and it hides itself while F3 is open.
- **Fog toggles**: Terrain (the distance haze, including the Nether's thick fog), Water, Lava and Powder Snow, each independent. Fog from Blindness and Darkness is never affected, so those effects still work as intended.
- **Cloud height**: raise or lower the cloud layer between y=64 and y=320. On Default every dimension keeps its own height, and dimensions with no clouds at all — the Nether, the End, cloudless dimensions from other mods — stay that way at any setting.
- **Screen overlays**: remove the water texture drawn over your view while swimming, and the flames drawn while burning. Fire also has a smart mode that hides the flames only while Fire Resistance is active. The suffocation overlay shown inside solid blocks is never touched.
- **Split view bobbing**: Minecraft turns bobbing on or off for everything at once. Here the camera and the held item are separate, and the camera adds a **Vertical Only** mode that keeps the up-and-down bounce while removing the sway and the tilt — the horizon stays level, which is the part that causes motion sickness for most people.

### Configuration and compatibility

- **Config screen included**: change everything from the Mods menu (select the mod → Config button). No other mod needed.
- **Sodium integration** (optional): with [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) 0.8.x installed, the "Fullscreen" checkbox in its video settings is replaced by a "Screen Mode" selector, and the mod gets its own section with per-feature pages. Built entirely on Sodium's official Config API — no fragile mixins into Sodium internals. Also plays nice with Reese's Sodium Options.
- **Zero required dependencies**: works with or without Sodium.
- **Persistent**: all settings are remembered between sessions (`config/borderlesswindow.json`).
- **Translatable**: all strings live in standard lang files (`en_us`, `es_mx`, `es_es` included — contributions welcome).

## Why borderless?

Exclusive fullscreen changes your monitor's actual display mode, which causes slow alt-tabbing, black-screen flashes with HDR, resolution desync bugs, and broken window states when the game restarts. Borderless fullscreen is just a window sized to your monitor with no decorations: it looks identical, but alt-tab is instant and nothing ever desyncs.

## Configuration

- **In game (no Sodium):** Mods menu → Borderless Window → Config. Every option lives in one scrollable list grouped by feature: Window, FPS Overlay, Fog, Clouds, Screen Overlays and Bobbing. Changes apply instantly.
- **In game (with Sodium):** Video Settings → the "Screen Mode" option (replaces Sodium's fullscreen checkbox) and the "Borderless Window" section with its six pages.
- **Config file:** `config/borderlesswindow.json`. If the mod ever gets stuck in a weird state, delete this file and restart — defaults are regenerated safely.

Every setting defaults to vanilla behavior, so installing or updating the mod never changes how the game looks until you ask it to.

**Note on bobbing:** the camera and hand options refine Minecraft's own "View Bobbing" setting rather than replacing it. With View Bobbing turned off in Minecraft's video settings, nothing bobs and these two options have no effect.

## Requirements

- Minecraft **1.21.1**
- **NeoForge** 21.1.x
- (Optional) **Sodium 0.8.0+** for the video settings integration

## License

[MIT](LICENSE)

---

## Español

Un mod ligero client-side para **NeoForge 1.21.1** que agrega **pantalla completa sin bordes** a Minecraft, reemplazando el fullscreen exclusivo con un sistema de 3 modos — más un conjunto de opciones de video y de calidad de vida que Minecraft no ofrece.

### Características

#### Ventana

- **Tres modos de pantalla**: Ventana, Sin bordes, y la Pantalla completa exclusiva normal de Minecraft.
- **F11 configurable**: alternar Sin bordes, alternar Pantalla completa exclusiva, o **ciclar entre los tres modos**.
- **Compatible con HDR**: evita el parpadeo de pantalla negra y el cambio de perfil de color que el fullscreen exclusivo (y los borderless mal implementados) causan con el HDR de Windows activado.
- **Soporte multi-monitor**: se aplica en el monitor donde está la ventana, no siempre en el primario.

#### Video y calidad de vida

- **Contador de FPS**: los FPS actuales, o una sola línea con el Mín. / Prom. / Máx. de los últimos 15 segundos. En cualquier esquina de la pantalla. Muestrea el contador del propio juego, así que siempre coincide con F3 y respeta el VSync y el límite de FPS, y se oculta solo mientras F3 está abierto.
- **Opciones de niebla**: Terreno (la neblina de distancia, incluida la niebla densa del Nether), Agua, Lava y Nieve polvo, cada una por separado. La niebla de Ceguera y Oscuridad nunca se ve afectada, así que esos efectos siguen funcionando como deben.
- **Altura de las nubes**: sube o baja la capa de nubes entre y=64 y y=320. En Por defecto cada dimensión conserva su propia altura, y las dimensiones sin nubes — el Nether, el End, las dimensiones sin nubes de otros mods — siguen sin ellas con cualquier valor.
- **Overlays de pantalla**: quita la textura de agua que se dibuja sobre tu vista al nadar y las llamas que aparecen mientras te quemas. El fuego además tiene un modo inteligente que oculta las llamas solo mientras tengas Resistencia al Fuego. El overlay de asfixia dentro de bloques sólidos nunca se toca.
- **Balanceo separado**: Minecraft enciende o apaga el balanceo entero de una vez. Aquí la cámara y el objeto en la mano van por separado, y la cámara suma un modo **Solo vertical** que conserva el rebote de arriba a abajo pero quita el vaivén y la inclinación — el horizonte queda nivelado, que es justo la parte que marea a la mayoría.

#### Configuración y compatibilidad

- **Pantalla de configuración incluida**: cambia todo desde el menú de Mods (selecciona el mod → botón Config). No requiere ningún otro mod.
- **Integración con Sodium** (opcional): con [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) 0.8.x instalado, el checkbox de "Pantalla completa" de su menú de video se reemplaza por un selector de "Modo de pantalla", y el mod obtiene su propia sección con páginas por feature. Construido completamente sobre la Config API oficial de Sodium — sin mixins frágiles a sus clases internas. También funciona bien con Reese's Sodium Options.
- **Cero dependencias obligatorias**: funciona con o sin Sodium.
- **Persistente**: todos los ajustes se recuerdan entre sesiones (`config/borderlesswindow.json`).
- **Traducible**: todos los textos viven en lang files estándar (`en_us`, `es_mx`, `es_es` incluidos — se aceptan contribuciones).

### ¿Por qué sin bordes?

El fullscreen exclusivo cambia el modo de video real del monitor, lo que causa alt-tabs lentos, parpadeos negros con HDR, bugs de desincronización de resolución y estados de ventana rotos al reiniciar el juego. La pantalla sin bordes es solo una ventana del tamaño del monitor sin decoración: se ve idéntica, pero el alt-tab es instantáneo y nada se desincroniza.

### Configuración

- **En juego (sin Sodium):** menú de Mods → Borderless Window → Config. Todas las opciones viven en una sola lista con scroll agrupada por feature: Ventana, Contador de FPS, Niebla, Nubes, Overlays de pantalla y Balanceo. Los cambios aplican al instante.
- **En juego (con Sodium):** Ajustes de video → la opción "Modo de pantalla" (reemplaza el checkbox de fullscreen de Sodium) y la sección "Borderless Window" con sus seis páginas.
- **Archivo de config:** `config/borderlesswindow.json`. Si el mod queda en un estado raro, borra este archivo y reinicia — los valores por defecto se regeneran sin problema.

Todos los ajustes vienen por defecto en el comportamiento normal del juego, así que instalar o actualizar el mod nunca cambia cómo se ve hasta que tú lo pidas.

**Nota sobre el balanceo:** las opciones de cámara y mano refinan el ajuste "Balanceo de cámara" de Minecraft, no lo reemplazan. Si lo desactivas en los ajustes de video de Minecraft, no se mece nada y estas dos opciones no tienen efecto.

### Requisitos

- Minecraft **1.21.1**
- **NeoForge** 21.1.x
- (Opcional) **Sodium 0.8.0+** para la integración con el menú de video

### Licencia

[MIT](LICENSE)