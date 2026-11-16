# Borderless Window 1.11.3

*NeoForge · Fabric 26.1.2 · 2026-11-16*

> **A round of fixes.** Fullscreen Resolution now works, a resource reload no longer waits for a minimized window, the config is always saved, and the jar no longer breaks a dedicated server that has it.

## ⚠️ Important Notes

- Every setting stays as it was
- When updating, delete the 1.11.2 jar from your mods folder
- On NeoForge it now needs 26.1.2.71, the first stable build, or newer: the betas before it are no longer accepted
- Fullscreen now uses the Fullscreen Resolution set in the video settings. If it was ever moved off Current, Fullscreen switches to that resolution after updating; set it back to Current to keep the desktop's

## 🐛 Fixes

- **Fullscreen Resolution works.** The slider in Minecraft's video settings and in Sodium's did nothing: Fullscreen always took the desktop resolution. Fullscreen now uses the one you pick, and changing it while in Fullscreen applies it right away
- **Pause Rendering When Minimized no longer holds up a resource reload.** A reload that finished while the window was minimized waited for it to come back before closing its loading screen; drawing now goes on during a reload
- **The config is always saved.** On a fresh Fabric instance without a `config` folder, no setting was ever remembered. The file is now also written safely (a crash mid-save can no longer cut it short), a damaged file is kept as `borderlesswindow.json.bak` instead of being overwritten, and both cases are reported in the log
- **Dedicated servers.** A NeoForge server pack that kept the jar could fail to start or crash later; the mod now just stays idle on a server
- **Zoom.** Presses of the zoom key in Hold mode piled up and were replayed on switching to Toggle, which could turn the zoom on by itself
- **Translations.** The Spanish texts use the names Minecraft shows for its own options and effects in each variant (for example Velocidad de destello and Cuerpo ignífugo in Spain), and the two distortion tooltips match the rest

## 🔧 Technical Changes

- Borderless only adds its extra row of height on Windows, where the trick is needed; elsewhere the window matches the monitor
- Closing the config screen or applying Sodium's page writes the config once instead of once per option
- "Vertical Only" bobbing uses a lighter hook (`ModifyArg` instead of `ModifyArgs`)
- The build compiles against the oldest MixinExtras the jar can meet, and the release workflows run with fewer permissions

## 📦 Dependencies

- Minecraft 26.1.2 with NeoForge 26.1.2.71 or newer, or with Fabric Loader 0.18.4 or newer
- Optional: Sodium 0.9.0 or newer on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Una ronda de arreglos.** La Resolución de pantalla completa ya funciona, una recarga de recursos ya no espera a que vuelva una ventana minimizada, la config siempre se guarda y el jar ya no rompe un servidor dedicado que lo tenga.

## ⚠️ Notas Importantes

- Los ajustes se quedan como estaban
- Al actualizar, borra el jar de la 1.11.2 de tu carpeta de mods
- En NeoForge ahora pide la 26.1.2.71, la primera estable, o más nueva: ya no se aceptan las betas anteriores
- La Pantalla completa ahora usa la Resolución de pantalla completa de los ajustes de video. Si alguna vez se movió de Actual, al actualizar la Pantalla completa cambia a esa resolución; regrésala a Actual para conservar la del escritorio

## 🐛 Arreglos

- **La Resolución de pantalla completa funciona.** El slider de los ajustes de video de Minecraft y el de Sodium no hacían nada: la Pantalla completa siempre tomaba la resolución del escritorio. Ahora usa la que elijas, y cambiarla estando en Pantalla completa la aplica en el momento
- **Pausar render al minimizar ya no detiene una recarga de recursos.** Una recarga que terminaba con la ventana minimizada esperaba a que volviera para cerrar su pantalla de carga; ahora el dibujo sigue durante una recarga
- **La config siempre se guarda.** En una instancia nueva de Fabric sin carpeta `config`, ningún ajuste se recordaba. Además, el archivo ahora se escribe de forma segura (un cierre a media escritura ya no lo deja cortado), un archivo dañado se conserva como `borderlesswindow.json.bak` en vez de sobrescribirse, y los dos casos quedan en el log
- **Servidores dedicados.** Un server pack de NeoForge que conservaba el jar podía no arrancar o tronar después; ahora el mod simplemente no hace nada en un servidor
- **Zoom.** Las pulsaciones de la tecla de zoom en modo Mantener se acumulaban y se repetían al cambiar a Alternar, lo que podía activar el zoom solo
- **Traducciones.** Los textos en español usan los nombres que Minecraft muestra para sus propias opciones y efectos en cada variante (por ejemplo Velocidad de destello y Cuerpo ignífugo en España), y los dos tooltips de distorsión quedan como los demás

## 🔧 Cambios Técnicos

- Sin bordes solo agrega su fila de altura extra en Windows, donde hace falta el truco; en los demás sistemas la ventana mide lo mismo que el monitor
- Cerrar la pantalla de config o aplicar la página de Sodium escribe la config una vez en lugar de una por opción
- El balanceo "Solo vertical" usa un hook más ligero (`ModifyArg` en vez de `ModifyArgs`)
- El build compila contra el MixinExtras más viejo con el que puede encontrarse el jar, y los workflows de publicación corren con menos permisos

## 📦 Dependencias

- Minecraft 26.1.2 con NeoForge 26.1.2.71 o más nuevo, o con Fabric Loader 0.18.4 o más nuevo
- Opcional: Sodium 0.9.0 o más nuevo en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
