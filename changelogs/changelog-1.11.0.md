# Borderless Window 1.11.0

*NeoForge · Fabric 1.21.11 · 2026-11-10*

> **A second step for the idle limit, which now takes over from Minecraft's.** Step away for a few minutes and the game drops to one frame rate; stay away longer and it drops further. Any key or mouse movement brings it all back at once.

## ⚠️ Important Notes

- **While the mod's Idle FPS Limit is on, it replaces Minecraft's "AFK" mode** of Reduce FPS When (in Video Settings, or on Sodium's Performance page). Until now the two stacked and the lowest won, so a 20 FPS idle limit still ended up at 10 after ten minutes. Now the mod's idle limits are the only ones that apply, and Minecraft's 10 FPS while minimized stays. With the mod's Idle FPS Limit off (the default), Minecraft's option works exactly as before. The option itself keeps your choice; nothing is changed in `options.txt`
- Every setting stays as it was. The new options start off
- When updating, delete the 1.10.0 jar from your mods folder

## ✨ New Features

### Idle FPS limit in two steps

- **Second Idle Limit After** and **Second Idle FPS Limit**: a later cap on top of the existing idle limit. For example, 30 FPS after 5 minutes without input, then 10 FPS after 20 minutes
- The second time can go up to 60 minutes, and it is always longer than the first: its slider starts right after the first idle time, and its leftmost position is Off. Both times count from your last keyboard or mouse input
- It builds on the first step, so it is greyed out while the first idle limit is off. If you raise the first time to or past the second, the second step turns off
- When both steps apply, the lower cap wins. As before, a lower Max Framerate or Unfocused FPS Limit still wins over both
- The options are on the FPS & Background page in Sodium, right after the first idle limit, and in the same place in the mod's config screen

## 🔧 Technical Changes

- Replacing the AFK mode only changes what `FramerateLimitTracker.getThrottleReason` reads from the option: it sees "Minimized" while the mod's first idle step is on. The minimized and out-of-world menu limits are untouched
- In the mod's config screen (without Sodium), the Idle FPS Limit is now greyed out while its idle time is off, as it already was on the Sodium page
- On the Sodium page, the second idle time keeps its Off position while the first one moves, so lowering the first time never turns the second step on by itself
- **Fixed:** with Reese's Sodium Options installed, pressing F11 while Sodium's video settings were open changed the window but left Screen Mode showing the previous mode until the menu was reopened. It now updates right away, as it already did without that mod

## 📦 Dependencies

- Minecraft 1.21.11 with NeoForge 21.11.x, or with Fabric Loader 0.17.3 or newer
- Optional: Sodium 0.8.x on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Un segundo escalón para el límite por inactividad, que ahora toma el lugar del de Minecraft.** Aléjate unos minutos y el juego baja a unos FPS; quédate lejos más tiempo y baja todavía más. Cualquier tecla o movimiento del mouse lo regresa todo al instante.

## ⚠️ Notas Importantes

- **Mientras el Límite por inactividad del mod está activado, reemplaza el modo "Al ausentarse"** del Reducir FPS de Minecraft (en Opciones de gráficos, o en la página Rendimiento de Sodium). Hasta ahora los dos se sumaban y ganaba el más bajo, así que un límite por inactividad de 20 FPS terminaba en 10 a los diez minutos. Ahora solo aplican los límites por inactividad del mod, y los 10 FPS de Minecraft con el juego minimizado se quedan. Con el Límite por inactividad del mod desactivado (por defecto), la opción de Minecraft funciona igual que antes. La opción conserva lo que elegiste; no se cambia nada en `options.txt`
- Los ajustes se quedan como estaban. Las opciones nuevas empiezan desactivadas
- Al actualizar, borra el jar de la 1.10.0 de tu carpeta de mods

## ✨ Nuevas Características

### Límite por inactividad en dos escalones

- **Segundo límite por inactividad tras** y **Segundo límite de FPS por inactividad**: un límite posterior encima del límite por inactividad que ya existía. Por ejemplo, 30 FPS tras 5 minutos sin input, y luego 10 FPS tras 20 minutos
- El segundo tiempo llega hasta 60 minutos y siempre es mayor que el primero: su slider empieza justo después del primer tiempo de inactividad, y su primera posición es Desactivado. Los dos tiempos cuentan desde la última vez que usaste el teclado o el mouse
- Se apoya en el primer escalón, así que sale en gris mientras el primer límite por inactividad está desactivado. Si subes el primer tiempo hasta el segundo o más, el segundo escalón se desactiva
- Cuando aplican los dos escalones, gana el límite más bajo. Como antes, unos FPS máximos o un Límite de FPS sin foco más bajos siguen ganando sobre los dos
- Las opciones están en la página FPS y segundo plano de Sodium, justo después del primer límite por inactividad, y en el mismo lugar en la pantalla de configuración del mod

## 🔧 Cambios Técnicos

- Reemplazar el modo "Al ausentarse" solo cambia lo que `FramerateLimitTracker.getThrottleReason` lee de la opción: ve "Al minimizar" mientras el primer escalón del mod está activado. Los límites al minimizar y en los menús fuera del mundo no se tocan
- En la pantalla de configuración del mod (sin Sodium), el Límite de FPS por inactividad ahora sale en gris mientras su tiempo de inactividad está desactivado, como ya pasaba en la página de Sodium
- En la página de Sodium, el segundo tiempo de inactividad conserva su posición Desactivado aunque se mueva el primero, así que bajar el primer tiempo nunca activa el segundo escalón por sí solo
- **Corregido:** con Reese's Sodium Options instalado, presionar F11 con las opciones de video de Sodium abiertas cambiaba la ventana, pero Modo de pantalla seguía mostrando el modo anterior hasta volver a abrir el menú. Ahora se actualiza al instante, como ya pasaba sin ese mod

## 📦 Dependencias

- Minecraft 1.21.11 con NeoForge 21.11.x, o con Fabric Loader 0.17.3 o más nuevo
- Opcional: Sodium 0.8.x en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
