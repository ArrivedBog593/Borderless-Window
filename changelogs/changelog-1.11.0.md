# Borderless Window 1.11.0

*NeoForge · Fabric 26.3 · 2026-11-20*

> **Now on Minecraft 26.3.** Every feature of 1.11.0, on both loaders, with nothing else to install, and every fix that came after it on the other Minecraft versions already included.

## ⚠️ Important Notes

- **The first version for Minecraft 26.3.** Download the file for 26.3 and your loader; the 26.2, 26.1.2 and 1.21.x jars do not work here
- **The screen modes now run on Minecraft's own.** Minecraft 26.3 has a borderless fullscreen of its own, split across two toggles, "Fullscreen" and "Exclusive Fullscreen". The Screen Mode selector sets both for you: Borderless is Fullscreen with Exclusive off, Fullscreen is both on. "Exclusive Fullscreen" stays hidden from Video Settings, as on 26.1.2 and 26.2, and the F11 Mode option works as before
- **The screen mode is now saved with Minecraft's options** (`options.txt`), not in the mod's config. The first launch with a config copied from an older version applies the mode saved there once
- **If you type with a Chinese, Japanese or Korean input method**, use Borderless instead of Fullscreen: Minecraft itself warns that exclusive fullscreen might stop some input methods from working
- **NeoForge 26.3.0.76 or newer** (the first stable build), or **Fabric Loader 0.19.3 or newer**: the two small pieces of Fabric API inside the jar need it on 26.3
- **Sodium integration needs Sodium 0.9.2 or newer**, the first one for 26.3
- **Sodium on NeoForge needs Sodium 0.9.3 or newer** (a beta for now): Sodium 0.9.2 for NeoForge stops the game at launch on every stable NeoForge 26.3, a Sodium issue fixed in its 0.9.3 betas. Those betas need Iris 1.12.1, not yet out for 26.3, so Sodium and Iris can't be used together on NeoForge 26.3 for now. Fabric is not affected
- Same config file and option IDs as on the other versions: copying `config/borderlesswindow.json` from an older instance carries your settings over

## ✨ New Features

- **Everything from 1.11.0, on Minecraft 26.3**: the three screen modes and configurable F11, the FPS counter, the background options with the idle limit in two steps, the fog toggles and Fog Start, cloud height, sky and weather, the held item name, screen and camera overlays, split FOV, distortion and view bobbing, zoom, enchantment numerals, draw distances, Minecraft's own sliders that Sodium leaves out, and the hand settings with Iris shaders
- **The fixes from 1.11.1 on 26.2, from the start**: Fullscreen uses the Fullscreen Resolution you pick; a resource reload no longer waits for a minimized window with Pause Rendering When Minimized on; the config is always saved, written safely, and a damaged file is kept as `borderlesswindow.json.bak`; the jar stays idle on a NeoForge dedicated server; presses of the zoom key in Hold mode no longer pile up; the Screen Mode selector in Minecraft's video settings always switches after F11; and the Spanish texts use the names Minecraft shows for its own options
- **Minecraft's "Reduce FPS When" is greyed out while the Idle FPS Limit is on**, in Minecraft's video settings and on Sodium's page, since the Idle FPS Limit replaces it then. Its tooltip says why, and your choice in it is kept for when the Idle FPS Limit is turned off
- **Clearer Pause Rendering When Minimized tooltip**: no frames are drawn, while the game loop keeps running 10 times per second, as Minecraft does when minimized
- **Fullscreen minimizes on Alt+Tab again**, as on 26.2, and the desktop gets its own resolution back while you use other apps. Minecraft 26.3 kept the game on screen, and with Vulkan the desktop stayed at the lower Fullscreen Resolution. Borderless and Windowed never minimize
- **Switching from Fullscreen at a lower resolution straight to Borderless covers the whole screen.** Minecraft 26.3 sized the Borderless window from the lower resolution, leaving a small window in a corner of the desktop
- **A maximized window comes back maximized** from Borderless or Fullscreen, and Restore still takes it back to its normal size. Minecraft 26.3 brought it back as a plain window the size of the maximized one, slightly off the screen

## 🔧 Technical Changes

- Ported to Minecraft 26.3. Every hook was checked against the 26.3 code on both loaders:
  - **Window**: Minecraft 26.3 runs on SDL3 instead of GLFW and handles Windowed, borderless and exclusive fullscreen itself, without a restart. The mod no longer moves the window: the selector and F11 set Minecraft's two options, and Minecraft applies them on the next frame (Borderless on Windows is still a window 1 px larger than the monitor, now Minecraft's own, with the picture at the monitor's exact resolution). Fullscreen's resolution is set again if Windows puts the desktop's back, as on 26.2: a second after a new one is picked while in Fullscreen, or on coming back from Alt+Tab
  - **Distortion**: the portal and nausea spin moved to where the frame is extracted, so the Portal and Nausea sliders follow it there
  - **Cloud height**, **draw distances**, **view bobbing**, **Hide Hand While Zooming**, **sun, moon and stars**, and on Fabric the **water and fire overlays** follow Minecraft's renamed methods and new arguments
  - **Pause Rendering When Minimized** reads the window's own minimized state, which Minecraft now tracks from SDL's events
- The build compiles against the oldest Mixin and MixinExtras the jar can meet, keeps its shared build logic in an included build (`build-logic`) as the MultiLoader template does, and the release workflows run with fewer permissions. Releases are named "Borderless Window 1.11.0 for NeoForge 26.3" on CurseForge and "Borderless Window 1.11.0 for Minecraft 26.3" on GitHub
- The mixin configs declare Java 25, the version Minecraft 26.3 runs on

## 📦 Dependencies

- Minecraft 26.3 with NeoForge 26.3.0.76 or newer, or with Fabric Loader 0.19.3 or newer
- Optional: Sodium 0.9.2 or newer on Fabric, Sodium 0.9.3 or newer on NeoForge, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Ahora en Minecraft 26.3.** Todas las funciones de la 1.11.0, en los dos loaders y sin instalar nada más, con todos los arreglos que salieron después en las otras versiones de Minecraft ya incluidos.

## ⚠️ Notas Importantes

- **La primera versión para Minecraft 26.3.** Descarga el archivo para 26.3 y tu loader; los jars de 26.2, 26.1.2 y 1.21.x no funcionan aquí
- **Los modos de pantalla ahora usan los del propio Minecraft.** Minecraft 26.3 tiene su propia pantalla completa sin bordes, repartida en dos interruptores, "Pantalla completa" y "Pantalla completa exclusiva". El selector de Modo de pantalla ajusta los dos por ti: Sin bordes es Pantalla completa con la exclusiva apagada, Pantalla completa es las dos prendidas. "Pantalla completa exclusiva" sigue oculta en los Ajustes de video, como en 26.1.2 y 26.2, y la opción Modo de F11 funciona igual que antes
- **El modo de pantalla ahora se guarda con las opciones de Minecraft** (`options.txt`), no en la config del mod. El primer arranque con una config copiada de una versión anterior aplica una vez el modo guardado ahí
- **Si escribes con un método de entrada chino, japonés o coreano**, usa Sin bordes en lugar de Pantalla completa: el propio Minecraft avisa que la pantalla completa exclusiva puede impedir que funcionen algunos métodos de entrada
- **NeoForge 26.3.0.76 o más nuevo** (la primera versión estable), o **Fabric Loader 0.19.3 o más nuevo**: las dos piezas pequeñas de Fabric API que vienen dentro del jar lo piden en 26.3
- **La integración con Sodium requiere Sodium 0.9.2 o más nuevo**, el primero para 26.3
- **Sodium en NeoForge requiere Sodium 0.9.3 o más nuevo** (por ahora una beta): Sodium 0.9.2 para NeoForge detiene el juego al arrancar en cualquier NeoForge 26.3 estable, un problema de Sodium corregido en sus betas 0.9.3. Esas betas requieren Iris 1.12.1, que todavía no sale para 26.3, así que por ahora Sodium e Iris no se pueden usar juntos en NeoForge 26.3. Fabric no se ve afectado
- El mismo archivo de configuración e IDs de opciones que en las otras versiones: copiar `config/borderlesswindow.json` de una instancia anterior conserva tus ajustes

## ✨ Nuevas Características

- **Todo lo de la 1.11.0, en Minecraft 26.3**: los tres modos de pantalla y F11 configurable, el contador de FPS, las opciones de segundo plano con el límite por inactividad en dos escalones, los toggles de niebla y el Inicio de la niebla, la altura de las nubes, cielo y clima, el nombre del objeto en mano, los overlays de pantalla y de cámara, el FOV, la distorsión y el balanceo separados, el zoom, los números de encantamientos, las distancias de dibujado, los sliders de Minecraft que Sodium deja fuera, y los ajustes de la mano con shaders de Iris
- **Los arreglos de la 1.11.1 de 26.2, desde el principio**: Pantalla completa usa la Resolución de pantalla completa que elijas; una recarga de recursos ya no espera a que vuelva una ventana minimizada con Pausar render al minimizar activado; la config siempre se guarda, se escribe de forma segura y un archivo dañado se conserva como `borderlesswindow.json.bak`; el jar se queda inactivo en un servidor dedicado de NeoForge; las pulsaciones de la tecla de zoom en modo Mantener ya no se acumulan; el selector de Modo de pantalla en los ajustes de video de Minecraft siempre cambia después de F11; y los textos en español usan los nombres que Minecraft muestra para sus propias opciones
- **"Reducir FPS" de Minecraft se ve desactivada mientras el Límite por inactividad está activado**, en los ajustes de video de Minecraft y en la página de Sodium, porque el Límite por inactividad la reemplaza mientras tanto. Su tooltip explica por qué, y lo que elegiste ahí se conserva para cuando desactives el Límite por inactividad
- **Tooltip más claro de Pausar render al minimizar**: no se dibuja ningún frame, mientras el ciclo del juego sigue corriendo 10 veces por segundo, como lo hace Minecraft al minimizar
- **Pantalla completa vuelve a minimizarse con Alt+Tab**, como en 26.2, y el escritorio recupera su propia resolución mientras usas otras aplicaciones. Minecraft 26.3 dejaba el juego en pantalla, y con Vulkan el escritorio se quedaba en la Resolución de pantalla completa más baja. Sin bordes y Ventana nunca se minimizan
- **Pasar de Pantalla completa a una resolución más baja directo a Sin bordes cubre toda la pantalla.** Minecraft 26.3 calculaba la ventana de Sin bordes con la resolución más baja, y dejaba una ventana pequeña en una esquina del escritorio
- **Una ventana maximizada vuelve maximizada** de Sin bordes o Pantalla completa, y Restaurar la sigue regresando a su tamaño normal. Minecraft 26.3 la regresaba como una ventana normal del tamaño de la maximizada, un poco fuera de la pantalla

## 🔧 Cambios Técnicos

- Portado a Minecraft 26.3. Cada hook se revisó contra el código de 26.3 en los dos loaders:
  - **Ventana**: Minecraft 26.3 usa SDL3 en lugar de GLFW y maneja él mismo la ventana, la pantalla completa sin bordes y la exclusiva, sin reiniciar. El mod ya no mueve la ventana: el selector y F11 ajustan las dos opciones de Minecraft, y Minecraft las aplica en el siguiente frame (Sin bordes en Windows sigue siendo una ventana 1 px más grande que el monitor, ahora la del propio Minecraft, con la imagen a la resolución exacta del monitor). La resolución de Pantalla completa se vuelve a aplicar si Windows regresa la del escritorio, como en 26.2: un segundo después de elegir una nueva estando en Pantalla completa, o al volver de Alt+Tab
  - **Distorsión**: el giro del portal y de las náuseas se pasó a donde se extrae el frame, así que los sliders de Portal y Náuseas lo siguen ahí
  - **Altura de las nubes**, **distancias de dibujado**, **balanceo**, **Ocultar mano al hacer zoom**, **sol, luna y estrellas**, y en Fabric los **overlays de agua y fuego** siguen a los métodos renombrados y los argumentos nuevos de Minecraft
  - **Pausar render al minimizar** lee el estado minimizado de la propia ventana, que Minecraft ahora sigue con los eventos de SDL
- El build compila contra el Mixin y el MixinExtras más viejos que puede encontrarse el jar, guarda su lógica de build compartida en un build incluido (`build-logic`) como la plantilla MultiLoader, y los workflows de publicación corren con menos permisos. Las versiones se llaman "Borderless Window 1.11.0 for NeoForge 26.3" en CurseForge y "Borderless Window 1.11.0 for Minecraft 26.3" en GitHub
- Las configuraciones de mixins declaran Java 25, la versión con la que corre Minecraft 26.3

## 📦 Dependencias

- Minecraft 26.3 con NeoForge 26.3.0.76 o más nuevo, o con Fabric Loader 0.19.3 o más nuevo
- Opcional: Sodium 0.9.2 o más nuevo en Fabric, Sodium 0.9.3 o más nuevo en NeoForge, y Mod Menu en Fabric

</details>
