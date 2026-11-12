# Borderless Window 1.11.2

*NeoForge · Fabric 1.21.11 · 2026-11-11*

> **The hand settings now work with Iris shaders.** With a shaderpack on, Hide Hand While Zooming and Hand Bobbing used to have no effect; now they do.

## ⚠️ Important Notes

- Every setting stays as it was
- When updating, delete the 1.11.1 jar from your mods folder

## ✨ New Features

- **The mod's icon** next to its name in Sodium's page list, as Sodium, Iris and Reese's Sodium Options show theirs. Sodium 0.8.0, the first build for 1.21.11, has no way to show it, so there the page just has no icon
- **Links to the mod's page:** NeoForge's mod list now shows the button to its GitHub page, and the Website button in Mod Menu on Fabric works (it was greyed out)

## 🔧 Technical Changes

- **Fixed:** with an Iris shaderpack on, Hide Hand While Zooming and Hand Bobbing did nothing. Iris draws the hand itself, from its own hand renderer, so the mod's hooks on Minecraft's never ran. The mod now hooks Iris's renderer too, only when Iris is installed. Without Iris the hook is skipped before Mixin looks for Iris's class, so no warning shows up in the log

## 📦 Dependencies

- Minecraft 1.21.11 with NeoForge 21.11.x, or with Fabric Loader 0.17.3 or newer
- Optional: Sodium 0.8.x on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Los ajustes de la mano ahora funcionan con shaders de Iris.** Con un shaderpack activo, Ocultar mano al hacer zoom y Balanceo de la mano no tenían efecto; ahora sí.

## ⚠️ Notas Importantes

- Los ajustes se quedan como estaban
- Al actualizar, borra el jar de la 1.11.1 de tu carpeta de mods

## ✨ Nuevas Características

- **El ícono del mod** junto a su nombre en la lista de páginas de Sodium, como muestran el suyo Sodium, Iris y Reese's Sodium Options. Sodium 0.8.0, la primera versión para 1.21.11, no tiene forma de mostrarlo, así que ahí la página sale sin ícono
- **Ligas a la página del mod:** la lista de mods de NeoForge ahora muestra el botón a su página de GitHub, y el botón Sitio web de Mod Menu en Fabric funciona (salía en gris)

## 🔧 Cambios Técnicos

- **Corregido:** con un shaderpack de Iris activo, Ocultar mano al hacer zoom y Balanceo de la mano no hacían nada. Iris dibuja la mano por su cuenta, desde su propio renderizador de mano, así que los hooks del mod en el de Minecraft nunca corrían. Ahora el mod también se engancha al renderizador de Iris, solo cuando Iris está instalado. Sin Iris el hook se omite antes de que Mixin busque la clase de Iris, así que no sale ningún aviso en el log

## 📦 Dependencias

- Minecraft 1.21.11 con NeoForge 21.11.x, o con Fabric Loader 0.17.3 o más nuevo
- Opcional: Sodium 0.8.x en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
