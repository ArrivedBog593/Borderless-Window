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
- Cero dependencias OBLIGATORIAS: si alguien no tiene Sodium instalado,
  el mod sigue funcionando igual con F11 (Ventana ↔ Sin bordes). La
  integración con el menú de Sodium es enteramente opcional y no rompe
  nada si Sodium no está presente.
- El modo elegido en el menú de Sodium (Borderless vs. Fullscreen real)
  no se recuerda entre sesiones en esta versión — solo se recuerda
  "es fullscreen sí/no" (vía options.txt, como antes). Si quieres que
  también recuerde cuál de los dos modos era, se puede agregar un
  archivo de config chiquito para eso.

## Integración con el menú de video de Sodium (nuevo)

Esto agrega la opción "Modo de pantalla" (con 3 estados: Sin pantalla
completa / Sin bordes / Pantalla completa) directamente en el menú de
video de Sodium, **reemplazando** su checkbox de "Pantalla completa".

Usa la **Config API oficial de Sodium** (disponible desde la 0.8.x, que
es la que tienes con la 0.8.12-beta.2) — no toca ninguna clase interna
de Sodium, así que en teoría no debería romperse con futuros updates de
Sodium (a diferencia de mixinear directo a su GUI).

### Pasos adicionales para esta parte

1. Agrega el repositorio de CaffeineMC y la dependencia a tu
   `build.gradle` — mira el archivo `build.gradle.snippet` incluido
   aquí, tiene exactamente qué agregar y dónde.
2. **Verifica la versión del artefacto** en <https://maven.caffeinemc.net>
   buscando `sodium-neoforge-api`. Puse `0.8.12-beta.2+mc1.21.1` como
   mejor estimación siguiendo el patrón que usan en otras versiones,
   pero confírmalo ahí antes de compilar.
3. Copia la carpeta `config/` (con `ScreenModeStorage.java` y
   `ScreenModeConfigEntryPoint.java`) igual que el resto de los archivos.
4. `./gradlew runClient` y abre el menú de video. Si todo salió bien,
   donde antes decía "Pantalla completa" ahora debería decir
   "Modo de pantalla" con tus 3 opciones.

### Si no reemplaza la opción de Sodium (sale como opción nueva aparte)

Es el punto más incierto de todo esto: el ID `sodium:fullscreen` que
uso en `ScreenModeConfigEntryPoint.java` para apuntar al reemplazo es mi
mejor estimación, no algo que pude confirmar línea por línea contra el
código exacto de tu versión. Si pasa esto, dime y lo resolvemos rápido:
la forma más directa es abrir el `.jar` de Sodium (es un zip) y revisar
`assets/sodium/lang/en_us.json` para ver cómo está nombrada la traducción
de esa opción, lo cual nos da una pista fuerte del ID real.
