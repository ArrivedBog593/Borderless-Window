# Borderless Window 1.10.0

*NeoForge · Fabric 1.21.11 · 2026-11-10*

> **Now on Minecraft 1.21.11.** Every feature of 1.10.0, on both loaders, with nothing else to install.

## ⚠️ Important Notes

- **The first version for Minecraft 1.21.11.** Download the file for 1.21.11 and your loader; the 1.21.1 jars do not work here
- **Minecraft 1.21.11 has its own "Inactivity FPS Limit"** (in Video Settings, or on Sodium's Performance page): 10 FPS while minimized and, in its default "AFK" mode, 30 FPS after a minute without input and 10 after ten. It stacks with the mod's background options and the lowest limit wins. To use only the mod's Idle FPS Limit, set Minecraft's to "Minimized". Pause Rendering When Minimized still goes further than Minecraft: it stops drawing frames instead of drawing them at 10 FPS
- **The fog toggles are independent.** Minecraft 1.21.11 draws two fogs at once: the one from where you are (water, lava, the Nether, rain) and the one that hides the edge of the loaded terrain. Water Fog off clears the water and keeps that edge hidden; Terrain Fog off clears it everywhere
- Same config file and option IDs as on 1.21.1: copying `config/borderlesswindow.json` from a 1.21.1 instance carries your settings over

## ✨ New Features

- **Everything from 1.10.0, on Minecraft 1.21.11**: the three screen modes and configurable F11, the FPS counter, the background options, the fog toggles and Fog Start, cloud height, sky and weather, the held item name, screen and camera overlays, split FOV, distortion and view bobbing, zoom, enchantment numerals, draw distances, and Minecraft's own sliders that Sodium leaves out
- With Sodium, the mod's six pages and the Screen Mode selector work as on 1.21.1. Iris shaders work too: the rain is hidden, and packs that draw their own sky keep their sun, moon and stars

## 🔧 Technical Changes

- Ported to Minecraft 1.21.11, whose rendering changed throughout since 1.21.1. Every hook was checked against the 1.21.11 code on both loaders:
  - **Fog**: one mixin for both loaders, on the fog data `FogRenderer.setupFog` builds before uploading it. NeoForge's RenderFog event and the Fabric-only fog mixin are gone. Sodium reads the same data, so the toggles work the same with it
  - **Clouds**: the cloud height is now an environment attribute; the mod moves only the drawing, in `CloudRenderer`. Dimensions without clouds never reach it
  - **Sky and weather**: the sun, moon and stars in `SkyRenderer`, the rain and snow in `WeatherEffectRenderer`
  - **Particles**: `ClientLevel.doAddParticle`, where Minecraft applies its own 32-block limit
  - **Frame rate caps**: `FramerateLimitTracker`, where Minecraft's own Inactivity FPS Limit lives
  - **Unfocused volume**: scales each sound's volume as it reaches OpenAL (`Channel.setVolume`). Minecraft 1.21.11 no longer has a listener gain
- **Distortion**: Minecraft now tracks the portal and Nausea apart. Inside a Nether portal the Portal slider still rules the whole spin, Nausea included, as on 1.21.1 — also when arriving inside the destination portal
- **The zoom key's Controls category** is now registered by each loader, as Minecraft 1.21.11 requires
- **On Fabric**, the bundled resource loader is now `fabric-resource-loader-v1`. Fabric API is still not required

## 📦 Dependencies

- Minecraft 1.21.11 with NeoForge 21.11.x, or with Fabric Loader 0.17.3 or newer
- Optional: Sodium 0.8.x on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Ahora en Minecraft 1.21.11.** Todas las funciones de la 1.10.0, en los dos loaders y sin instalar nada más.

## ⚠️ Notas Importantes

- **La primera versión para Minecraft 1.21.11.** Descarga el archivo para 1.21.11 y tu loader; los jars de la 1.21.1 no funcionan aquí
- **Minecraft 1.21.11 tiene su propio "Reducir FPS"** (en Opciones de gráficos, o en la página Rendimiento de Sodium): 10 FPS con el juego minimizado y, en su modo por defecto "Al ausentarse", 30 FPS tras un minuto sin actividad y 10 tras diez. Se suma a las opciones de segundo plano del mod y gana el límite más bajo. Para usar solo el Límite por inactividad del mod, pon el de Minecraft en "Al minimizar". Pausar render al minimizar sigue yendo más allá que Minecraft: deja de dibujar frames en vez de dibujarlos a 10 FPS
- **Los toggles de niebla son independientes.** Minecraft 1.21.11 dibuja dos nieblas a la vez: la de donde estás (agua, lava, el Nether, la lluvia) y la que oculta el borde del terreno cargado. La Niebla de agua desactivada despeja el agua y deja oculto ese borde; la Niebla de terreno desactivada lo despeja en todas partes
- El mismo archivo de configuración e IDs de opciones que en la 1.21.1: copiar `config/borderlesswindow.json` desde una instancia de 1.21.1 conserva tus ajustes

## ✨ Nuevas Características

- **Todo lo de la 1.10.0, en Minecraft 1.21.11**: los tres modos de pantalla y F11 configurable, el contador de FPS, las opciones de segundo plano, los toggles de niebla y el Inicio de la niebla, la altura de las nubes, cielo y clima, el nombre del objeto en mano, los overlays de pantalla y de cámara, el FOV, la distorsión y el balanceo separados, el zoom, los números de encantamientos, las distancias de dibujado, y los sliders de Minecraft que Sodium deja fuera
- Con Sodium, las seis páginas del mod y el selector de Modo de pantalla funcionan como en la 1.21.1. Los shaders de Iris también: la lluvia se oculta, y los packs que dibujan su propio cielo conservan su sol, luna y estrellas

## 🔧 Cambios Técnicos

- Portado a Minecraft 1.21.11, cuyo render cambió por completo desde la 1.21.1. Cada hook se revisó contra el código de 1.21.11 en los dos loaders:
  - **Niebla**: un solo mixin para los dos loaders, sobre los datos de niebla que arma `FogRenderer.setupFog` antes de subirlos. Ya no se usan el evento RenderFog de NeoForge ni el mixin de niebla propio de Fabric. Sodium lee los mismos datos, así que los toggles funcionan igual con él
  - **Nubes**: la altura de las nubes ahora es un atributo de entorno; el mod solo mueve el dibujo, en `CloudRenderer`. Las dimensiones sin nubes nunca llegan ahí
  - **Cielo y clima**: el sol, la luna y las estrellas en `SkyRenderer`, la lluvia y la nieve en `WeatherEffectRenderer`
  - **Partículas**: `ClientLevel.doAddParticle`, donde Minecraft aplica su propio límite de 32 bloques
  - **Límites de FPS**: `FramerateLimitTracker`, donde vive el Reducir FPS de Minecraft
  - **Volumen sin foco**: escala el volumen de cada sonido al llegar a OpenAL (`Channel.setVolume`). Minecraft 1.21.11 ya no tiene ganancia del listener
- **Distorsión**: Minecraft ahora lleva el portal y la Náusea por separado. Dentro de un portal del Nether el slider de Portal sigue mandando sobre todo el giro, Náusea incluida, como en la 1.21.1 — también al aparecer dentro del portal de llegada
- **La categoría de la tecla de zoom en Controles** ahora la registra cada loader, como lo pide Minecraft 1.21.11
- **En Fabric**, el cargador de recursos incluido ahora es `fabric-resource-loader-v1`. Sigue sin requerir Fabric API

## 📦 Dependencias

- Minecraft 1.21.11 con NeoForge 21.11.x, o con Fabric Loader 0.17.3 o más nuevo
- Opcional: Sodium 0.8.x en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
