# Borderless Window

A lightweight client-side mod for **NeoForge 1.21.1** that adds **borderless fullscreen** to Minecraft, replacing the exclusive fullscreen toggle with a proper 3-mode system — plus a configurable FPS overlay and per-type fog toggles.

*[Español más abajo](#español)*

## Features

- **Three screen modes**: Windowed, Borderless, and Minecraft's regular exclusive Fullscreen.
- **Configurable F11**: choose what the key does — toggle Borderless, toggle exclusive Fullscreen, or **cycle through all three modes**.
- **FPS overlay**: Simple mode shows the current FPS; Extended mode shows a single line with the current FPS plus the Min / Avg / Max over the last 15 seconds. Position configurable (any screen corner). Stats sample the game's own FPS counter, so they always match F3 and respect VSync / FPS caps. Auto-hides while F3 is open.
- **Fog toggles**: disable each fog type independently — Terrain (the distance haze, including the Nether's thick fog), Water, Lava, and Powder Snow. Fog from the Blindness and Darkness effects is never affected, so gameplay mechanics stay intact.
- **Config screen included**: change everything from the Mods menu (select the mod → Config button). No other mod needed.
- **HDR-friendly**: avoids the black-screen flash / color profile switch that exclusive fullscreen (and naive borderless implementations) cause with Windows HDR enabled.
- **Multi-monitor aware**: borderless and fullscreen apply on the monitor the window is currently on, not always the primary one.
- **Persistent**: all settings are remembered between game sessions (`config/borderlesswindow.json`).
- **Sodium integration** (optional): with [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) 0.8.x installed, the "Fullscreen" checkbox in its video settings is replaced by a "Screen Mode" selector, and the mod gets its own section with per-feature pages (General, FPS Overlay, Fog). Built entirely on Sodium's official Config API — no fragile mixins into Sodium internals. Also plays nice with Reese's Sodium Options.
- **Zero required dependencies**: works with or without Sodium.
- **Translatable**: all strings live in standard lang files (`en_us`, `es_mx`, `es_es` included — contributions welcome).

## Why borderless?

Exclusive fullscreen changes your monitor's actual display mode, which causes slow alt-tabbing, black-screen flashes with HDR, resolution desync bugs, and broken window states when the game restarts. Borderless fullscreen is just a window sized to your monitor with no decorations: it looks identical, but alt-tab is instant and nothing ever desyncs.

## Configuration

- **In game (no Sodium):** Mods menu → Borderless Window → Config. Eight options: Screen Mode, F11 Mode, FPS Overlay, FPS Overlay Position, and the four fog toggles. Changes apply instantly.
- **In game (with Sodium):** Video Settings → the "Screen Mode" option (replaces Sodium's fullscreen checkbox) and the "Borderless Window" section with its General, FPS Overlay, and Fog pages.
- **Config file:** `config/borderlesswindow.json`. If the mod ever gets stuck in a weird state, delete this file and restart — defaults are regenerated safely.

## Requirements

- Minecraft **1.21.1**
- **NeoForge** 21.1.x
- (Optional) **Sodium 0.8.0+** for the video settings integration

## License

[MIT](LICENSE)

---

## Español

Un mod ligero client-side para **NeoForge 1.21.1** que agrega **pantalla completa sin bordes** a Minecraft, reemplazando el fullscreen exclusivo con un sistema de 3 modos — más un contador de FPS configurable y opciones de niebla por tipo.

### Características

- **Tres modos de pantalla**: Ventana, Sin bordes, y la Pantalla completa exclusiva normal de Minecraft.
- **F11 configurable**: elige qué hace la tecla — alternar Sin bordes, alternar Pantalla completa exclusiva, o **ciclar entre los tres modos**.
- **Contador de FPS**: el modo Simple muestra los FPS actuales; el modo Extendido muestra una sola línea con los FPS actuales más el Mín. / Prom. / Máx. de los últimos 15 segundos. Posición configurable (cualquier esquina de la pantalla). Las estadísticas muestrean el contador de FPS del propio juego, así que siempre coinciden con F3 y respetan el VSync / límite de FPS. Se oculta solo mientras F3 está abierto.
- **Opciones de niebla**: desactiva cada tipo de niebla por separado — Terreno (la neblina de distancia, incluida la niebla densa del Nether), Agua, Lava y Nieve polvo. La niebla de los efectos de Ceguera y Oscuridad nunca se ve afectada, así que las mecánicas del juego quedan intactas.
- **Pantalla de configuración incluida**: cambia todo desde el menú de Mods (selecciona el mod → botón Config). No requiere ningún otro mod.
- **Compatible con HDR**: evita el parpadeo de pantalla negra / cambio de perfil de color que el fullscreen exclusivo (y los borderless mal implementados) causan con el HDR de Windows activado.
- **Soporte multi-monitor**: el modo sin bordes y el fullscreen se aplican en el monitor donde está la ventana, no siempre en el primario.
- **Persistente**: todos los ajustes se recuerdan entre sesiones (`config/borderlesswindow.json`).
- **Integración con Sodium** (opcional): con [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) 0.8.x instalado, el checkbox de "Pantalla completa" de su menú de video se reemplaza por un selector de "Modo de pantalla", y el mod obtiene su propia sección con páginas por feature (General, Contador de FPS, Niebla). Construido completamente sobre la Config API oficial de Sodium — sin mixins frágiles a sus clases internas. También funciona bien con Reese's Sodium Options.
- **Cero dependencias obligatorias**: funciona con o sin Sodium.
- **Traducible**: todos los textos viven en lang files estándar (`en_us`, `es_mx`, `es_es` incluidos — se aceptan contribuciones).

### ¿Por qué sin bordes?

El fullscreen exclusivo cambia el modo de video real del monitor, lo que causa alt-tabs lentos, parpadeos negros con HDR, bugs de desincronización de resolución y estados de ventana rotos al reiniciar el juego. La pantalla sin bordes es solo una ventana del tamaño del monitor sin decoración: se ve idéntica, pero el alt-tab es instantáneo y nada se desincroniza.

### Configuración

- **En juego (sin Sodium):** menú de Mods → Borderless Window → Config. Ocho opciones: Modo de pantalla, Modo de F11, Contador de FPS, Posición del contador, y las cuatro opciones de niebla. Los cambios aplican al instante.
- **En juego (con Sodium):** Ajustes de video → la opción "Modo de pantalla" (reemplaza el checkbox de fullscreen de Sodium) y la sección "Borderless Window" con sus páginas General, Contador de FPS y Niebla.
- **Archivo de config:** `config/borderlesswindow.json`. Si el mod queda en un estado raro, borra este archivo y reinicia — los valores por defecto se regeneran sin problema.

### Requisitos

- Minecraft **1.21.1**
- **NeoForge** 21.1.x
- (Opcional) **Sodium 0.8.0+** para la integración con el menú de video

### Licencia

[MIT](LICENSE)