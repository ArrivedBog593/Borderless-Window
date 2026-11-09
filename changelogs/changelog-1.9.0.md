# Borderless Window 1.9.0

*NeoForge · Fabric 1.21.1 · 2026-11-08*

> **Now on Fabric too.** The same mod, with every feature, for both loaders. Without Sodium, it now also lives in Minecraft's own Video Settings, and it has a logo of its own.

## ⚠️ Important Notes

- **The jar has a new name**: `borderlesswindow-neoforge-1.9.0+mc1.21.1.jar` (or `-fabric-`). When updating, delete the old `borderlesswindow-1.8.0.jar` from your mods folder so the mod is not loaded twice
- Settings carry over untouched: both loaders read the same `config/borderlesswindow.json`, so you can even move a setup from one to the other
- **On Fabric there is nothing else to install.** Fabric API is not required: the one piece the mod needs from it, to load its translations, is bundled inside the jar, as Sodium does. If you already have Fabric API, they get along
- Nothing changes in how the mod behaves on NeoForge

## ✨ New Features

### Fabric support

- Every feature works on **Fabric 1.21.1** (Fabric Loader 0.16.0 or newer): screen modes and F11, the FPS counter and the background options, fog, clouds, screen and camera overlays, field of view, distortion, bobbing, distances and enchantment numerals
- The **Sodium** integration is the same: the Screen Mode selector in its video settings and the mod's six pages
- With **Mod Menu** installed, the mod's entry in its list opens the config screen, like NeoForge's Config button. Mod Menu is optional

### Minecraft's Video Settings

- Without Sodium, the **Fullscreen** toggle in Minecraft's Video Settings becomes a **Screen Mode** selector: Windowed, Borderless or Fullscreen. It applies at once and follows F11 while the screen is open
- **Fullscreen Resolution** is greyed out unless the mode is Fullscreen, the only mode it applies to, as on the mod's Sodium page
- A **Borderless Window...** button at the end of the list opens the full config screen
- So the mod can be set up entirely from Minecraft's own menus on both loaders, with no other mod. With Sodium installed nothing changes here: its video settings replace this screen, and the mod's options are in its pages as before

### Logo

- The mod has a logo now, a grass block with arrows pointing out to the corners, shown in NeoForge's mod list and in Mod Menu

## 🔧 Technical Changes

- The project is now multiloader, after jaredlll08's MultiLoader-Template: `common/` holds nearly all the code and is compiled against plain Minecraft, so a loader-specific call there fails to build; `neoforge/` and `fabric/` only add each loader's entry point and hooks
- On Fabric, fog, the water and fire overlays, the FPS counter and startup are hooked with the mod's own mixins rather than Fabric API events. The saved screen mode is applied on the second frame, once the window is up, so it opens straight in Borderless
- The config file path now comes from the game directory rather than a NeoForge API; it is the same folder as before
- One branch per Minecraft version (`NeoFabric/1.21.1`), with release tags such as `v1.9.0+mc1.21.1`. Every build makes both jars, and `[curseforge]` uploads each one with its loader
- Gradle 9.8, Fabric Loom 1.16, and stable Parchment mappings for 1.21.1

## 📦 Dependencies

- Minecraft 1.21.1 with NeoForge 21.1.x, or with Fabric Loader 0.16.0 or newer
- Optional: Sodium 0.8.x on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Ahora también en Fabric.** El mismo mod, con todas sus funciones, para los dos loaders. Sin Sodium, ahora también vive en los Ajustes de video del propio Minecraft, y tiene un logo propio.

## ⚠️ Notas Importantes

- **El jar tiene un nombre nuevo**: `borderlesswindow-neoforge-1.9.0+mc1.21.1.jar` (o `-fabric-`). Al actualizar, borra el viejo `borderlesswindow-1.8.0.jar` de tu carpeta de mods para que el mod no se cargue dos veces
- Los ajustes se mantienen: los dos loaders leen el mismo `config/borderlesswindow.json`, así que incluso puedes pasar tu configuración de uno al otro
- **En Fabric no hay que instalar nada más.** No requiere Fabric API: la única pieza que el mod necesita de ella, para cargar sus traducciones, viene dentro del jar, igual que hace Sodium. Si ya tienes Fabric API, se llevan bien
- En NeoForge no cambia nada en cómo se comporta el mod

## ✨ Nuevas Características

### Soporte para Fabric

- Todas las funciones trabajan en **Fabric 1.21.1** (Fabric Loader 0.16.0 o más nuevo): modos de pantalla y F11, el contador de FPS y las opciones de segundo plano, niebla, nubes, overlays de pantalla y de cámara, campo de visión, distorsión, balanceo, distancias y números de encantamientos
- La integración con **Sodium** es la misma: el selector de Modo de pantalla en sus ajustes de video y las seis páginas del mod
- Con **Mod Menu** instalado, la entrada del mod en su lista abre la pantalla de configuración, como el botón Config de NeoForge. Mod Menu es opcional

### Ajustes de video de Minecraft

- Sin Sodium, el interruptor de **Pantalla completa** de los Ajustes de video de Minecraft se convierte en un selector de **Modo de pantalla**: En ventana, Sin bordes o Pantalla completa. Se aplica al instante y sigue a F11 mientras la pantalla está abierta
- **Resolución de pantalla completa** sale en gris salvo en Pantalla completa, el único modo donde tiene efecto, igual que en la página del mod en Sodium
- Un botón **Borderless Window...** al final de la lista abre la pantalla de configuración completa
- Así, el mod se configura por completo desde los menús del propio Minecraft en los dos loaders, sin ningún otro mod. Con Sodium instalado aquí no cambia nada: sus ajustes de video reemplazan esta pantalla, y las opciones del mod siguen en sus páginas como antes

### Logo

- El mod ahora tiene logo, un bloque de pasto con flechas hacia las esquinas, que se ve en la lista de mods de NeoForge y en Mod Menu

## 🔧 Cambios Técnicos

- El proyecto ahora es multiloader, siguiendo el MultiLoader-Template de jaredlll08: `common/` tiene casi todo el código y se compila contra Minecraft puro, así que una llamada propia de un loader ahí no compila; `neoforge/` y `fabric/` solo agregan el punto de entrada y los enganches de cada loader
- En Fabric, la niebla, los overlays de agua y fuego, el contador de FPS y el arranque se enganchan con mixins propios del mod en lugar de eventos de Fabric API. El modo de pantalla guardado se aplica en el segundo frame, con la ventana ya lista, así que el juego abre directo en Sin bordes
- La ruta del archivo de configuración ahora sale de la carpeta del juego en lugar de una API de NeoForge; es la misma carpeta que antes
- Una rama por versión de Minecraft (`NeoFabric/1.21.1`), con tags de release como `v1.9.0+mc1.21.1`. Cada build genera los dos jars, y `[curseforge]` sube cada uno con su loader
- Gradle 9.8, Fabric Loom 1.16 y mappings estables de Parchment para 1.21.1

## 📦 Dependencias

- Minecraft 1.21.1 con NeoForge 21.1.x, o con Fabric Loader 0.16.0 o más nuevo
- Opcional: Sodium 0.8.x en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
