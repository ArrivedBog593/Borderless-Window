# Borderless Window 1.11.3

*NeoForge · Fabric 1.21.11 · 2026-11-12*

> **Borderless now renders at exactly the monitor's resolution.** 1920×1080 on a 1080p monitor, as F3 and screenshots show, instead of one row more.

## ⚠️ Important Notes

- Every setting stays as it was
- When updating, delete the 1.11.2 jar from your mods folder

## ✨ New Features

- **Borderless renders at the monitor's exact resolution.** The Borderless window is still 1 px taller than the monitor, which keeps Windows and the GPU driver from treating it as exclusive fullscreen (and HDR untouched), but the game no longer draws that hidden row: the picture matches the monitor, F3 shows its resolution, and F2 screenshots come out at that size. Minecraft 26.2 does the same for its own non-exclusive fullscreen

## 🔧 Technical Changes

- The window reports a framebuffer 1 px shorter than itself while in Borderless (on Windows only), and the frame is presented at the top of the window, so the extra row stays below the screen, undrawn. Windowed and Fullscreen are unchanged

## 📦 Dependencies

- Minecraft 1.21.11 with NeoForge 21.11.x, or with Fabric Loader 0.17.3 or newer
- Optional: Sodium 0.8.x on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Sin bordes ahora dibuja exactamente a la resolución del monitor.** 1920×1080 en un monitor 1080p, como muestran F3 y las capturas, en lugar de una fila más.

## ⚠️ Notas Importantes

- Los ajustes se quedan como estaban
- Al actualizar, borra el jar de la 1.11.2 de tu carpeta de mods

## ✨ Nuevas Características

- **Sin bordes dibuja a la resolución exacta del monitor.** La ventana de Sin bordes sigue midiendo 1 px más que el monitor, lo que evita que Windows y el driver de la tarjeta gráfica la traten como pantalla completa exclusiva (y no toca el HDR), pero el juego ya no dibuja esa fila oculta: la imagen coincide con el monitor, F3 muestra su resolución y las capturas con F2 salen de ese tamaño. Minecraft 26.2 hace lo mismo con su propia pantalla completa no exclusiva

## 🔧 Cambios Técnicos

- En Sin bordes (solo en Windows), la ventana reporta un framebuffer 1 px más bajo que ella misma, y el frame se presenta en la parte de arriba de la ventana, así que la fila extra queda abajo de la pantalla, sin dibujar. Ventana y Pantalla completa no cambian

## 📦 Dependencias

- Minecraft 1.21.11 con NeoForge 21.11.x, o con Fabric Loader 0.17.3 o más nuevo
- Opcional: Sodium 0.8.x en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
