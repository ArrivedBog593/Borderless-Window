# Borderless Window 1.11.1

*NeoForge · Fabric 26.1.2 · 2026-11-12*

> **The hand settings now work with Iris shaders.** With a shaderpack on, Hide Hand While Zooming and Hand Bobbing used to have no effect; now they do. Also fixes a hang at launch on NeoForge with its early loading screen turned off.

## ⚠️ Important Notes

- Every setting stays as it was
- When updating, delete the 1.11.0 jar from your mods folder

## ✨ New Features

- **The mod's icon** next to its name in Sodium's page list, as Sodium, Iris and Reese's Sodium Options show theirs
- **Links to the mod's page:** NeoForge's mod list now shows the button to its GitHub page, and the Website button in Mod Menu on Fabric works (it was greyed out)

## 🔧 Technical Changes

- **Fixed:** with an Iris shaderpack on, Hide Hand While Zooming and Hand Bobbing did nothing. Iris draws the hand itself, from its own hand renderer, so the mod's hooks on Minecraft's never ran. The mod now hooks Iris's renderer too, only when Iris is installed. Without Iris the hook is skipped before Mixin looks for Iris's class, so no warning shows up in the log
- **Fixed:** on NeoForge with its early loading screen off (`earlyWindowControl = false` in `config/fml.toml`), the game could hang on a white window at launch. The mod restored the saved screen mode from NeoForge's client setup, which since 26.1 runs on a loader thread; moving the window from there waits for the main thread, which was busy waiting for mod loading. The saved mode is now restored on the second frame on both loaders, as it already was on Fabric

## 📦 Dependencies

- Minecraft 26.1.2 with NeoForge 26.1.2.x, or with Fabric Loader 0.18.4 or newer
- Optional: Sodium 0.9.0 or newer on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Los ajustes de la mano ahora funcionan con shaders de Iris.** Con un shaderpack activo, Ocultar mano al hacer zoom y Balanceo de la mano no tenían efecto; ahora sí. También corrige un cuelgue al arrancar en NeoForge con su pantalla de carga temprana desactivada.

## ⚠️ Notas Importantes

- Los ajustes se quedan como estaban
- Al actualizar, borra el jar de la 1.11.0 de tu carpeta de mods

## ✨ Nuevas Características

- **El ícono del mod** junto a su nombre en la lista de páginas de Sodium, como muestran el suyo Sodium, Iris y Reese's Sodium Options
- **Ligas a la página del mod:** la lista de mods de NeoForge ahora muestra el botón a su página de GitHub, y el botón Sitio web de Mod Menu en Fabric funciona (salía en gris)

## 🔧 Cambios Técnicos

- **Corregido:** con un shaderpack de Iris activo, Ocultar mano al hacer zoom y Balanceo de la mano no hacían nada. Iris dibuja la mano por su cuenta, desde su propio renderizador de mano, así que los hooks del mod en el de Minecraft nunca corrían. Ahora el mod también se engancha al renderizador de Iris, solo cuando Iris está instalado. Sin Iris el hook se omite antes de que Mixin busque la clase de Iris, así que no sale ningún aviso en el log
- **Corregido:** en NeoForge con su pantalla de carga temprana desactivada (`earlyWindowControl = false` en `config/fml.toml`), el juego podía colgarse en una ventana blanca al arrancar. El mod restauraba el modo de pantalla guardado desde el client setup de NeoForge, que desde 26.1 corre en un hilo del loader; mover la ventana desde ahí espera al hilo principal, que estaba ocupado esperando la carga de mods. Ahora el modo guardado se restaura en el segundo frame en los dos loaders, como ya pasaba en Fabric

## 📦 Dependencias

- Minecraft 26.1.2 con NeoForge 26.1.2.x, o con Fabric Loader 0.18.4 o más nuevo
- Opcional: Sodium 0.9.0 o más nuevo en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
