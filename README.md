# Borderless Window

A lightweight client-side mod for **NeoForge 1.21.1** that adds **borderless fullscreen** to Minecraft, replacing the exclusive fullscreen toggle with a proper 3-mode system.

*[Español más abajo](#español)*

## Features

- **Three screen modes**: Windowed, Borderless, and Minecraft's regular exclusive Fullscreen.
- **Configurable F11**: choose whether F11 toggles to Borderless or to exclusive Fullscreen.
- **Persistent**: the selected mode and F11 behavior are remembered between game sessions (`config/borderlesswindow.json`).
- **Sodium integration** (optional): when [Sodium](https://modrinth.com/mod/sodium) 0.8.x is installed, the "Fullscreen" checkbox in its video settings is replaced by a "Screen Mode" selector, and an "F11 Mode" option is added under its own section. Built entirely on Sodium's official Config API — no fragile mixins into Sodium internals.
- **Zero required dependencies**: works with or without Sodium. Without it, F11 still toggles borderless mode.
- **Translatable**: all strings live in standard lang files (`en_us`, `es_mx`, `es_es` included — contributions welcome).

## Why borderless?

Exclusive fullscreen changes your monitor's actual display mode, which causes slow alt-tabbing, resolution desync bugs, and broken window states when the game restarts. Borderless fullscreen is just a window sized exactly to your monitor with no decorations: it looks identical, but alt-tab is instant and nothing ever desyncs.

## Requirements

- Minecraft **1.21.1**
- **NeoForge** 21.1.x
- (Optional) **Sodium 0.8.0+** for the in-menu options

## License

[MIT](LICENSE)

---

## Español

Un mod ligero client-side para **NeoForge 1.21.1** que agrega **pantalla completa sin bordes** a Minecraft, reemplazando el fullscreen exclusivo con un sistema de 3 modos.

### Características

- **Tres modos de pantalla**: Ventana, Sin bordes, y la Pantalla completa exclusiva normal de Minecraft.
- **F11 configurable**: elige si F11 cambia a Sin bordes o a Pantalla completa exclusiva.
- **Persistente**: el modo elegido y el comportamiento de F11 se recuerdan entre sesiones (`config/borderlesswindow.json`).
- **Integración con Sodium** (opcional): con [Sodium](https://modrinth.com/mod/sodium) 0.8.x instalado, el checkbox de "Pantalla completa" de su menú de video se reemplaza por un selector de "Modo de pantalla", y se agrega la opción "Modo de F11" en su propia sección. Construido completamente sobre la Config API oficial de Sodium — sin mixins frágiles a sus clases internas.
- **Cero dependencias obligatorias**: funciona con o sin Sodium. Sin él, F11 sigue alternando el modo sin bordes.
- **Traducible**: todos los textos viven en lang files estándar (`en_us`, `es_mx`, `es_es` incluidos — se aceptan contribuciones).

### ¿Por qué sin bordes?

El fullscreen exclusivo cambia el modo de video real del monitor, lo que causa alt-tabs lentos, bugs de desincronización de resolución y estados de ventana rotos al reiniciar el juego. La pantalla sin bordes es solo una ventana del tamaño exacto del monitor sin decoración: se ve idéntica, pero el alt-tab es instantáneo y nada se desincroniza.

### Requisitos

- Minecraft **1.21.1**
- **NeoForge** 21.1.x
- (Opcional) **Sodium 0.8.0+** para las opciones en el menú

### Licencia

[MIT](LICENSE)
