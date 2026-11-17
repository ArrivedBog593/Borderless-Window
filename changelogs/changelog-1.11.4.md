# Borderless Window 1.11.4

*NeoForge · Fabric 1.21.11 · 2026-11-17*

> **A round of fixes.** Fullscreen Resolution now works, a minimized single-player world pauses again with Pause Rendering When Minimized, the config is always saved, Camera Bobbing and the distortion sliders work under Iris shaders, and the jar no longer breaks a dedicated server that has it.

## ⚠️ Important Notes

- Every setting stays as it was
- When updating, delete the 1.11.3 jar from your mods folder
- NeoForge from 21.11.42 on is accepted again: 1.11.3 asked for 21.11.48 or newer
- Fullscreen now uses the Fullscreen Resolution set in the video settings. If it was ever moved off Current, Fullscreen switches to that resolution after updating; set it back to Current to keep the desktop's

## 🐛 Fixes

- **Fullscreen Resolution works.** The slider in Minecraft's video settings and in Sodium's did nothing: Fullscreen always took the desktop resolution. Fullscreen now uses the one you pick, and changing it while in Fullscreen applies it right away
- **Pause Rendering When Minimized no longer keeps a single-player world running.** Minimizing with Win+D or the minimize button stopped the drawing before Minecraft could pause the game, so the world kept going unattended. Drawing now stops one second after minimizing, after the pause, and never during a resource reload, which used to wait for the window to come back
- **The config is always saved.** On a fresh Fabric instance without a `config` folder, no setting was ever remembered. The file is now also written safely (a crash mid-save can no longer cut it short), a damaged file is kept as `borderlesswindow.json.bak` instead of being overwritten, and both cases are reported in the log
- **Dedicated servers.** A NeoForge server pack that kept the jar could fail to start or crash later; the mod now just stays idle on a server
- **Zoom.** Presses of the zoom key in Hold mode piled up and were replayed on switching to Toggle, which could turn the zoom on by itself
- **Camera bobbing and distortion with shaders.** With an Iris shaderpack on, Camera Bobbing and the Portal and Nausea Distortion sliders did nothing, because Iris applies the bobbing and the spin itself. They now work with and without shaders
- **Screen Mode in Minecraft's video settings after F11.** With the video settings open (without Sodium), pressing F11 updated the selector, but the next click on it could do nothing until the screen was reopened. It now always switches
- **Translations.** The Spanish texts use the names Minecraft shows for its own options and effects in each variant (for example Velocidad de destello and Cuerpo ignífugo in Spain), and the two distortion tooltips match the rest

## 🔧 Technical Changes

- Borderless only adds its extra row of height on Windows, where the trick is needed; elsewhere the window matches the monitor
- Closing the config screen or applying Sodium's page writes the config once instead of once per option
- "Vertical Only" bobbing uses a lighter hook (`ModifyArg` instead of `ModifyArgs`)
- The video settings no longer copy their option list with `clone()`, a call that older Mixin versions cannot transform (on 1.21.1 it crashed Fabric Loader 0.16.0)
- The build compiles against the oldest Mixin and MixinExtras the jar can meet, keeps its shared build logic in an included build (`build-logic`) as the MultiLoader template does, and the release workflows run with fewer permissions. Releases are now named "Borderless Window 1.11.4 for NeoForge 1.21.11" on CurseForge and "Borderless Window 1.11.4 for Minecraft 1.21.11" on GitHub

## 📦 Dependencies

- Minecraft 1.21.11 with NeoForge 21.11.42 or newer, or with Fabric Loader 0.17.3 or newer
- Optional: Sodium 0.8.x on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Una ronda de arreglos.** La Resolución de pantalla completa ya funciona, un mundo de un jugador minimizado vuelve a pausarse con Pausar render al minimizar, la config siempre se guarda, el Balanceo de la cámara y los sliders de distorsión funcionan con shaders de Iris, y el jar ya no rompe un servidor dedicado que lo tenga.

## ⚠️ Notas Importantes

- Los ajustes se quedan como estaban
- Al actualizar, borra el jar de la 1.11.3 de tu carpeta de mods
- Se vuelve a aceptar NeoForge desde la 21.11.42: la 1.11.3 pedía la 21.11.48 o más nueva
- La Pantalla completa ahora usa la Resolución de pantalla completa de los ajustes de video. Si alguna vez se movió de Actual, al actualizar la Pantalla completa cambia a esa resolución; regrésala a Actual para conservar la del escritorio

## 🐛 Arreglos

- **La Resolución de pantalla completa funciona.** El slider de los ajustes de video de Minecraft y el de Sodium no hacían nada: la Pantalla completa siempre tomaba la resolución del escritorio. Ahora usa la que elijas, y cambiarla estando en Pantalla completa la aplica en el momento
- **Pausar render al minimizar ya no deja corriendo un mundo de un jugador.** Al minimizar con Win+D o con el botón de minimizar, el dibujo se detenía antes de que Minecraft pudiera pausar el juego, así que el mundo seguía avanzando sin nadie. Ahora el dibujo se detiene un segundo después de minimizar, después de la pausa, y nunca durante una recarga de recursos, que antes esperaba a que volviera la ventana
- **La config siempre se guarda.** En una instancia nueva de Fabric sin carpeta `config`, ningún ajuste se recordaba. Además, el archivo ahora se escribe de forma segura (un cierre a media escritura ya no lo deja cortado), un archivo dañado se conserva como `borderlesswindow.json.bak` en vez de sobrescribirse, y los dos casos quedan en el log
- **Servidores dedicados.** Un server pack de NeoForge que conservaba el jar podía no arrancar o tronar después; ahora el mod simplemente no hace nada en un servidor
- **Zoom.** Las pulsaciones de la tecla de zoom en modo Mantener se acumulaban y se repetían al cambiar a Alternar, lo que podía activar el zoom solo
- **Balanceo de la cámara y distorsión con shaders.** Con un shaderpack de Iris activo, el Balanceo de la cámara y los sliders de Distorsión de portal y de náuseas no hacían nada, porque Iris aplica el balanceo y el giro por su cuenta. Ahora funcionan con y sin shaders
- **Modo de pantalla en los ajustes de video de Minecraft después de F11.** Con los ajustes de video abiertos (sin Sodium), F11 actualizaba el selector, pero el siguiente clic en él podía no hacer nada hasta volver a abrir la pantalla. Ahora siempre cambia
- **Traducciones.** Los textos en español usan los nombres que Minecraft muestra para sus propias opciones y efectos en cada variante (por ejemplo Velocidad de destello y Cuerpo ignífugo en España), y los dos tooltips de distorsión quedan como los demás

## 🔧 Cambios Técnicos

- Sin bordes solo agrega su fila de altura extra en Windows, donde hace falta el truco; en los demás sistemas la ventana mide lo mismo que el monitor
- Cerrar la pantalla de config o aplicar la página de Sodium escribe la config una vez en lugar de una por opción
- El balanceo "Solo vertical" usa un hook más ligero (`ModifyArg` en vez de `ModifyArgs`)
- Los ajustes de video ya no copian su lista de opciones con `clone()`, una llamada que las versiones viejas de Mixin no pueden transformar (en 1.21.1 crasheaba Fabric Loader 0.16.0)
- El build compila contra el Mixin y el MixinExtras más viejos con los que puede encontrarse el jar, guarda su lógica compartida en un build incluido (`build-logic`) como hace la plantilla MultiLoader, y los workflows de publicación corren con menos permisos. Las versiones ahora se llaman "Borderless Window 1.11.4 for NeoForge 1.21.11" en CurseForge y "Borderless Window 1.11.4 for Minecraft 1.21.11" en GitHub

## 📦 Dependencias

- Minecraft 1.21.11 con NeoForge 21.11.42 o más nuevo, o con Fabric Loader 0.17.3 o más nuevo
- Opcional: Sodium 0.8.x en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
