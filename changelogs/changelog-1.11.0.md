# Borderless Window 1.11.0

*NeoForge · Fabric 1.21.1 · 2026-11-10*

> **A second step for the idle limit.** Step away for a few minutes and the game drops to one frame rate; stay away longer and it drops further. Any key or mouse movement brings it all back at once.

## ⚠️ Important Notes

- Every setting stays as it was. The new options start off, so the idle limit keeps working exactly like before until you set them
- When updating, delete the 1.10.0 jar from your mods folder

## ✨ New Features

### Idle FPS limit in two steps

- **Second Idle Limit After** and **Second Idle FPS Limit**: a later cap on top of the existing idle limit. For example, 30 FPS after 5 minutes without input, then 10 FPS after 20 minutes
- The second time can go up to 60 minutes, and it is always longer than the first: its slider starts right after the first idle time, and its leftmost position is Off. Both times count from your last keyboard or mouse input
- It builds on the first step, so it is greyed out while the first idle limit is off. If you raise the first time to or past the second, the second step turns off
- When both steps apply, the lower cap wins. As before, a lower Max Framerate or Unfocused FPS Limit still wins over both
- The options are on the FPS & Background page in Sodium, right after the first idle limit, and in the same place in the mod's config screen

## 🔧 Technical Changes

- In the mod's config screen (without Sodium), the Idle FPS Limit is now greyed out while its idle time is off, as it already was on the Sodium page
- On the Sodium page, the second idle time keeps its Off position while the first one moves, so lowering the first time never turns the second step on by itself

## 📦 Dependencies

- Minecraft 1.21.1 with NeoForge 21.1.x, or with Fabric Loader 0.16.0 or newer
- Optional: Sodium 0.8.x on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Un segundo escalón para el límite por inactividad.** Aléjate unos minutos y el juego baja a unos FPS; quédate lejos más tiempo y baja todavía más. Cualquier tecla o movimiento del mouse lo regresa todo al instante.

## ⚠️ Notas Importantes

- Los ajustes se quedan como estaban. Las opciones nuevas empiezan desactivadas, así que el límite por inactividad funciona igual que antes hasta que las configures
- Al actualizar, borra el jar de la 1.10.0 de tu carpeta de mods

## ✨ Nuevas Características

### Límite por inactividad en dos escalones

- **Segundo límite por inactividad tras** y **Segundo límite de FPS por inactividad**: un límite posterior encima del límite por inactividad que ya existía. Por ejemplo, 30 FPS tras 5 minutos sin input, y luego 10 FPS tras 20 minutos
- El segundo tiempo llega hasta 60 minutos y siempre es mayor que el primero: su slider empieza justo después del primer tiempo de inactividad, y su primera posición es Desactivado. Los dos tiempos cuentan desde la última vez que usaste el teclado o el mouse
- Se apoya en el primer escalón, así que sale en gris mientras el primer límite por inactividad está desactivado. Si subes el primer tiempo hasta el segundo o más, el segundo escalón se desactiva
- Cuando aplican los dos escalones, gana el límite más bajo. Como antes, unos FPS máximos o un Límite de FPS sin foco más bajos siguen ganando sobre los dos
- Las opciones están en la página FPS y segundo plano de Sodium, justo después del primer límite por inactividad, y en el mismo lugar en la pantalla de configuración del mod

## 🔧 Cambios Técnicos

- En la pantalla de configuración del mod (sin Sodium), el Límite de FPS por inactividad ahora sale en gris mientras su tiempo de inactividad está desactivado, como ya pasaba en la página de Sodium
- En la página de Sodium, el segundo tiempo de inactividad conserva su posición Desactivado aunque se mueva el primero, así que bajar el primer tiempo nunca activa el segundo escalón por sí solo

## 📦 Dependencias

- Minecraft 1.21.1 con NeoForge 21.1.x, o con Fabric Loader 0.16.0 o más nuevo
- Opcional: Sodium 0.8.x en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
