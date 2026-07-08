# Borderless Window — guía de integración

Este no es un proyecto Gradle completo (no incluyo el wrapper de NeoForge
porque no tengo acceso de red a su Maven desde aquí). Son los **archivos
fuente** para pegar dentro de un MDK de NeoForge 1.21.1 nuevo, igual al que
ya usas para CustomGear.

## Pasos

1. Descarga el MDK oficial de NeoForge 1.21.1 (el mismo que usaste para
   CustomGear) y ábrelo en IntelliJ.
2. Copia estas carpetas dentro de `src/main/...` de ese proyecto,
   reemplazando lo que venga por defecto:
   - `java/com/github/arrivedbog593/borderlesswindow/`
   - `resources/borderlesswindow.mixins.json`
   - `resources/META-INF/neoforge.mods.toml` (reemplaza el que trae el MDK)
3. En tu `build.gradle`, asegúrate de tener el plugin de mixins activado
   (el MDK de NeoForge ya lo trae configurado por defecto para
   `${mod_id}.mixins.json` — solo cambia esa referencia por
   `borderlesswindow.mixins.json` si el nombre no coincide).
4. Cambia `mod_id` en `gradle.properties` a `borderlesswindow` (o el
   nombre que prefieras, pero debe coincidir con `MODID` en
   `BorderlessWindowMod.java` y con el `modId` del `.toml`).
5. `./gradlew runClient` para probar. F11 debería dar borderless en vez
   de fullscreen exclusivo.

## Qué hace cada archivo

- **WindowMixin.java** — intercepta `toggleFullScreen()` (lo que dispara F11)
  y `isFullscreen()` (lo que Minecraft usa para saber qué guardar en
  `options.txt`). Al sobreescribir ambos, el estado borderless se guarda
  y se recupera con el sistema nativo de opciones del juego — no hace
  falta un archivo de config aparte.
- **BorderlessHandler.java** — la lógica GLFW real: en vez de fullscreen
  exclusivo (que cambia la resolución física del monitor y es lo que te
  estaba rompiendo la ventana), estira la ventana al tamaño del monitor
  y le quita la decoración, sin tocar la resolución real. Por eso no
  debería haber más conflictos de escalado ni advertencias de resolución.

## Limitaciones conocidas / cosas para mejorar después

- Usa siempre el monitor primario (`glfwGetPrimaryMonitor`). Si tienes
  más de un monitor y quieres borderless en el secundario, hay que
  detectar en qué monitor está la ventana antes de aplicar — puedo
  ayudarte con eso si lo necesitas.
- No agrega ninguna pantalla de configuración; F11 sigue siendo el único
  control. Si luego quieres una opción en el menú de video, se puede
  agregar fácil ya que la lógica base ya existe.
- Cero dependencias obligatorias de Sodium ni de ningún otro mod — solo
  NeoForge y Minecraft 1.21.1.
