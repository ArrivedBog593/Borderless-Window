# Borderless Window 1.8.0

*NeoForge 1.21.1 · 2026-11-08*

> **The game rests when you are not looking.** Three new background options join the Unfocused FPS Limit on their own page, Minecraft's glint and menu blur sliders come back to Sodium's video settings, and every slider now moves in steps of 1.

## ⚠️ Important Notes

- Every option keeps its ID, so existing settings carry over untouched. Every new option defaults to Minecraft's behavior
- **Dynamic FPS** no longer greys out the background options. Both mods can now be used together: frame rate caps stack and the lower one wins. If both lower the background volume, the reductions multiply, so it is best to set the volume in only one of them

## ✨ New Features

### Background

- **Unfocused Volume**: lowers the game volume while its window is not focused — after Alt+Tab, while minimized, or with another window or monitor in front. It is a share of your Master Volume, from 0 (Muted) to 100%, the default. Full volume returns as soon as you click back in, and sounds that start while muted (music, ambient loops) are there when you return
- **Pause Rendering When Minimized**: stops drawing frames while the window is minimized and slows the game loop to 10 FPS. The game itself keeps running — the world, sounds and multiplayer connections are unaffected; only the picture stops until you restore the window. Off by default
- **Idle FPS Limit**: after a number of minutes with no keyboard or mouse input (1 to 30, Off by default), the frame rate is capped at a value you choose (5 to 60 FPS, 30 by default) — even while the window is focused. Any key, click, scroll or mouse movement lifts it at once. Gamepad input from controller mods does not count as activity
- When several limits apply at once, the lowest wins

### Enchantments and menus

- New **Enchantments** page holding the Enchantment Numerals option, plus Minecraft's own **Glint Speed** and **Glint Strength** sliders, which Sodium's video settings leave out. Glint Strength applies instantly, no restart needed
- Minecraft's **Menu Background Blur** slider is back too, on the General page
- Like the FOV and Distortion Effects sliders, these are the same settings found under Accessibility and are saved with Minecraft's options, not the mod's

### Sliders and pages

- Every slider now moves in steps of 1: percentages by 1%, distances by 1 block, FPS limits by 1 FPS. The Unfocused FPS Limit still starts at 5 FPS: below 2 FPS Minecraft falls behind on client ticks, and 5 keeps a comfortable margin
- The mod's Sodium section now has six pages: **General** (F11, menu blur), **Enchantments**, **FPS & Background**, **Visual Effects**, **Camera** and **Distances**. The no-Sodium config screen follows the same order, with a new **Background** header

## 🔧 Technical Changes

- New `sound/` package with `UnfocusedVolumeState`; `fps/` gains `MinimizedRenderState` and `IdleFpsState`
- New mixins: `ListenerMixin` (scales only the OpenAL gain, never the listener's stored value, so sounds still start while muted), `SoundManagerAccessor`, `MouseHandlerMixin` and `KeyboardHandlerMixin` (input activity). `MinecraftMixin` now also wraps `GameRenderer.render` in `runTick` and handles focus changes
- The README and the changelogs are now split by language: `README_ES.md`, and an index in `CHANGELOG.md` / `CHANGELOG_ES.md`

## 📦 Dependencies

- No changes: NeoForge 21.1.x, and optionally Sodium 0.8.x

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **El juego descansa cuando no lo estás viendo.** Tres opciones nuevas de segundo plano se suman al Límite de FPS sin foco en su propia página, los sliders de brillo y de desenfoque de menús de Minecraft vuelven a los ajustes de video de Sodium, y todos los sliders ahora van de 1 en 1.

## ⚠️ Notas Importantes

- Todas las opciones conservan su ID, así que los ajustes que ya tenías se mantienen. Todas las opciones nuevas vienen por defecto en el comportamiento de Minecraft
- **Dynamic FPS** ya no desactiva las opciones de segundo plano. Ahora los dos mods se pueden usar juntos: los límites de FPS se apilan y gana el más bajo. Si los dos bajan el volumen en segundo plano, las reducciones se multiplican, así que conviene ajustar el volumen en solo uno de ellos

## ✨ Nuevas Características

### Segundo plano

- **Volumen sin foco**: baja el volumen del juego mientras la ventana no está enfocada — al hacer Alt+Tab, al minimizarla o con otra ventana o monitor al frente. Es una proporción de tu Volumen general, de 0 (Silenciado) a 100%, que es el valor por defecto. El volumen completo regresa en cuanto vuelves al juego, y los sonidos que empiezan mientras está silenciado (música, loops de ambiente) siguen ahí cuando regresas
- **Pausar render al minimizar**: deja de dibujar frames mientras la ventana está minimizada y baja el ciclo del juego a 10 FPS. El juego sigue corriendo — el mundo, los sonidos y las conexiones multijugador no se ven afectados; solo se detiene la imagen hasta que restauras la ventana. Desactivado por defecto
- **Límite de FPS por inactividad**: tras unos minutos sin usar el teclado ni el mouse (de 1 a 30, desactivado por defecto), los FPS se limitan al valor que elijas (de 5 a 60 FPS, 30 por defecto) — aunque la ventana esté enfocada. Cualquier tecla, clic, scroll o movimiento del mouse lo quita al instante. Los controles de mods de gamepad no cuentan como actividad
- Cuando se juntan varios límites, gana el más bajo

### Encantamientos y menús

- Nueva página **Encantamientos** con la opción de Números de encantamientos, más los sliders de **Velocidad del brillo** e **Intensidad del brillo** del propio Minecraft, que los ajustes de video de Sodium no incluyen. La Intensidad del brillo se aplica al instante, sin reiniciar
- También regresa el slider de **Desenfoque de menús** de Minecraft, en la página General
- Igual que los sliders de Efectos de FOV y de distorsión, son los mismos ajustes que están en Accesibilidad y se guardan con las opciones de Minecraft, no con las del mod

### Sliders y páginas

- Todos los sliders ahora van de 1 en 1: los porcentajes de 1%, las distancias de 1 bloque y los límites de FPS de 1 FPS. El Límite de FPS sin foco sigue empezando en 5 FPS: por debajo de 2 FPS Minecraft se atrasa en los ticks del cliente, y 5 deja un margen cómodo
- La sección del mod en Sodium ahora tiene seis páginas: **General** (F11, desenfoque de menús), **Encantamientos**, **FPS y segundo plano**, **Efectos visuales**, **Cámara** y **Distancias**. La pantalla de configuración sin Sodium sigue el mismo orden, con un encabezado nuevo de **Segundo plano**

## 🔧 Cambios Técnicos

- Nuevo paquete `sound/` con `UnfocusedVolumeState`; `fps/` suma `MinimizedRenderState` e `IdleFpsState`
- Nuevos mixins: `ListenerMixin` (escala solo la ganancia de OpenAL, nunca el valor guardado del listener, así que los sonidos siguen arrancando aunque esté silenciado), `SoundManagerAccessor`, `MouseHandlerMixin` y `KeyboardHandlerMixin` (actividad de input). `MinecraftMixin` ahora también envuelve `GameRenderer.render` en `runTick` y maneja los cambios de foco
- El README y los changelogs ahora están separados por idioma: `README_ES.md`, y un índice en `CHANGELOG.md` / `CHANGELOG_ES.md`

## 📦 Dependencias

- Sin cambios: NeoForge 21.1.x, y opcionalmente Sodium 0.8.x

</details>
