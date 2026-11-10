# Borderless Window 1.10.0

*NeoForge · Fabric 1.21.1 · 2026-11-09*

> **Zoom, and a few details from OptiFine.** Press C to get a closer look, with the mouse wheel to go further. Plus: hide the sun, moon, stars, rain and snow, choose where the fog starts, and turn off the item name over the hotbar. On both loaders, with nothing else to install.

## ⚠️ Important Notes

- **The zoom key is C by default**, like OptiFine. Minecraft also uses C for "Save Hotbar Activator", which only works in creative and combined with a number key. If that gets in the way, change either one in Options → Controls → Key Binds, under "Borderless Window"
- Every other setting stays as it was, and every new option starts at Minecraft's own behavior. The zoom does nothing until you press its key
- When updating, delete the 1.9.0 jar from your mods folder

## ✨ New Features

### Zoom

- **Press C to zoom in**, 4× by default. The key is in Minecraft's Controls, in its own "Borderless Window" category
- **Hold or Toggle**: zoom only while the key is held down, or press once to zoom in and again to zoom out
- **Scroll to Zoom**: while zooming, the mouse wheel zooms further in or back out, from 1× to 50×, instead of changing the hotbar slot. It follows Minecraft's Discrete Scrolling and Scroll Sensitivity. Every new zoom starts again at the Zoom Level
- **Zoom Level**: from 2× to 16×
- **Smooth Zoom**: eases in and out of the zoom, at any frame rate. Turn it off to jump straight in
- **Hide Hand** while zooming (on by default) and **Hide HUD** while zooming (off by default), the latter just like pressing F1
- **The mouse turns slower in proportion to the zoom**, so the crosshair moves across the world at the same speed as without zoom
- It only narrows the field of view: nothing is drawn any farther away, and it stacks with your FOV and the mod's FOV sliders
- **The spyglass is left alone**: while you look through one, the zoom steps aside and the mouse wheel goes back to the hotbar
- The options are a new **Zoom** group on the Camera page in Sodium, and a new section in the mod's config screen

### Sky and weather

Options Minecraft does not have, from OptiFine's Details menu. All three are on by default and only change what is drawn:

- **Sun and Moon**: hide them. Day still turns into night, and sunrises and sunsets keep their colors
- **Stars**: hide them for a plain night sky
- With shaders, packs that draw their own sun, moon and stars keep them; those are set in the pack. Rain and snow are hidden with shaders too
- **Rain and Snow**: hide the falling rain and snow. It still rains for mobs, crops and fishing, and the rain sounds and splashes stay
- A new **Sky and Weather** group on the Visual Effects page, after the clouds

### Fog start

- **Fog Start**, like OptiFine's: choose where the distance fog begins, from 1% to 95% of your render distance. **Default (~90%)** keeps Minecraft's own. Lower values give a softer, deeper haze
- Only the ordinary terrain fog moves. The Nether's thick fog, water, lava, Blindness and Darkness are left alone. It is greyed out while Terrain Fog is off

### Held item name

- **Held Item Name**: turn off the item name that appears above the hotbar when you switch slots (OptiFine's "Held Item Tooltips"). On the General page, in a new **HUD** group

## 🔧 Technical Changes

- The zoom multiplies the value `GameRenderer.getFov` returns, after NeoForge's ComputeFov event. The held item's own FOV is left out, so the hand never gets magnified
- The key and the animation are read once per frame, so a press shows on the very next frame
- On Fabric, the key is registered through `fabric-key-binding-api-v1`, bundled inside the jar like the resource loader. Fabric API is still not required
- Fog start changes the depth of the fog band in the one branch of `FogRenderer.setupFog` that computes it, so no other fog can be touched. It is a single mixin for both loaders, and NeoForge's RenderFog event sees the moved start
- The sun, moon and stars are skipped at their draw calls in `LevelRenderer.renderSky`, and the sun and moon meshes are still released. Dimensions that draw their own sky are not affected
- The code in `common/` is reorganized into one package per page of the mod's settings (`general`, `enchantment`, `fps`, `visual`, `camera`, `distance`) instead of one per feature, so new options fit into an existing one. Every slider in the config screen now uses the same widget. Nothing changes for players: the config file, the option IDs and the translations are the same

## 📦 Dependencies

- Minecraft 1.21.1 with NeoForge 21.1.x, or with Fabric Loader 0.16.0 or newer
- Optional: Sodium 0.8.x on either loader, and Mod Menu on Fabric

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Zoom, y algunos detalles de OptiFine.** Presiona C para ver de cerca, con la rueda del mouse para acercar más. Además: ocultar el sol, la luna, las estrellas, la lluvia y la nieve, elegir dónde empieza la niebla, y quitar el nombre del objeto sobre la barra. En los dos loaders y sin instalar nada más.

## ⚠️ Notas Importantes

- **La tecla de zoom es la C por defecto**, como en OptiFine. Minecraft también usa la C para "Activador de guardar barra", que solo funciona en creativo y combinado con una tecla de número. Si estorba, cambia cualquiera de las dos en Opciones → Controles → Asignación de teclas, en "Borderless Window"
- Los demás ajustes se quedan como estaban, y cada opción nueva empieza en el comportamiento normal de Minecraft. El zoom no hace nada hasta que presionas su tecla
- Al actualizar, borra el jar de la 1.9.0 de tu carpeta de mods

## ✨ Nuevas Características

### Zoom

- **Presiona C para acercar la vista**, 4× por defecto. La tecla está en los Controles de Minecraft, en su propia categoría "Borderless Window"
- **Mantener o Alternar**: zoom solo mientras tienes la tecla presionada, o una pulsación para acercar y otra para regresar
- **Zoom con la rueda**: mientras haces zoom, la rueda del mouse acerca o aleja más, de 1× a 50×, en vez de cambiar de casilla en la barra. Respeta el Desplazamiento discreto y la Sensibilidad de la rueda de Minecraft. Cada zoom nuevo vuelve a empezar en el Nivel de zoom
- **Nivel de zoom**: de 2× a 16×
- **Zoom suave**: entra y sale del zoom de forma gradual, a cualquier cantidad de FPS. Desactívalo para que sea instantáneo
- **Ocultar mano** al hacer zoom (activado por defecto) y **Ocultar HUD** al hacer zoom (desactivado por defecto), este último igual que presionar F1
- **El mouse gira más lento en proporción al zoom**, así que la mira recorre el mundo a la misma velocidad que sin zoom
- Solo cierra el campo de visión: nada se dibuja más lejos, y se suma a tu FOV y a los sliders de FOV del mod
- **El catalejo no se toca**: mientras miras por uno, el zoom se hace a un lado y la rueda del mouse vuelve a la barra
- Las opciones son un grupo nuevo **Zoom** en la página Cámara de Sodium, y una sección nueva en la pantalla de configuración del mod

### Cielo y clima

Opciones que Minecraft no tiene, del menú Detalles de OptiFine. Las tres vienen activadas por defecto y solo cambian lo que se dibuja:

- **Sol y luna**: ocultarlos. El día se sigue haciendo noche, y los amaneceres y atardeceres conservan sus colores
- **Estrellas**: ocultarlas para un cielo nocturno liso
- Con shaders, los packs que dibujan su propio sol, luna y estrellas los conservan; eso se ajusta en el pack. La lluvia y la nieve se ocultan también con shaders
- **Lluvia y nieve**: ocultar la lluvia y la nieve que caen. Sigue lloviendo para los mobs, los cultivos y la pesca, y se quedan el sonido de la lluvia y las salpicaduras
- Un grupo nuevo **Cielo y clima** en la página Efectos visuales, después de las nubes

### Inicio de la niebla

- **Inicio de la niebla**, como el de OptiFine: elige dónde empieza la niebla de distancia, del 1% al 95% de tu distancia de renderizado. **Por defecto (~90%)** conserva el de Minecraft. Los valores más bajos dan una neblina más suave y profunda
- Solo se mueve la niebla normal del terreno. La niebla densa del Nether, el agua, la lava, la Ceguera y la Oscuridad no se tocan. Sale en gris mientras la Niebla de terreno está desactivada

### Nombre del objeto en mano

- **Nombre del objeto en mano**: quita el nombre del objeto que aparece sobre la barra al cambiar de casilla (los "Held Item Tooltips" de OptiFine). En la página General, en un grupo nuevo **HUD**

## 🔧 Cambios Técnicos

- El zoom multiplica el valor que devuelve `GameRenderer.getFov`, después del evento ComputeFov de NeoForge. El FOV propio del objeto en la mano queda fuera, así que la mano nunca se agranda
- La tecla y la animación se leen una vez por frame, así que una pulsación se nota desde el siguiente frame
- En Fabric, la tecla se registra con `fabric-key-binding-api-v1`, incluido dentro del jar igual que el cargador de recursos. Sigue sin requerir Fabric API
- El inicio de la niebla cambia la profundidad de la franja de niebla en la única rama de `FogRenderer.setupFog` que la calcula, así que ninguna otra niebla se puede tocar. Es un solo mixin para los dos loaders, y el evento RenderFog de NeoForge ve el inicio ya movido
- El sol, la luna y las estrellas se saltan en sus llamadas de dibujo de `LevelRenderer.renderSky`, y los meshes del sol y la luna se siguen liberando. Las dimensiones que dibujan su propio cielo no se ven afectadas
- El código de `common/` se reorganiza en un paquete por página de los ajustes del mod (`general`, `enchantment`, `fps`, `visual`, `camera`, `distance`) en lugar de uno por función, así las opciones nuevas entran en uno que ya existe. Todos los sliders de la pantalla de configuración usan ahora el mismo widget. Para los jugadores no cambia nada: el archivo de configuración, los IDs de las opciones y las traducciones son los mismos

## 📦 Dependencias

- Minecraft 1.21.1 con NeoForge 21.1.x, o con Fabric Loader 0.16.0 o más nuevo
- Opcional: Sodium 0.8.x en cualquiera de los dos loaders, y Mod Menu en Fabric

</details>
