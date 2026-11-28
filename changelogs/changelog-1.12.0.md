# Borderless Window 1.12.0

*NeoForge · Fabric 26.3 · 2026-11-27*

> **Distances for everything, and a batch of small options.** The Distances page now covers every kind of entity, block entity and particle, each with its own slider. Plus a fast language switch, notification toggles, nametags, a calmer Night Vision ending, a lower fire overlay and more.

## ⚠️ Important Notes

- Every setting stays as it was, including the four distances from before (Dropped Items, Experience Orbs, Armor Stands and Particles), which keep their values and their place
- **Fast Language Switch is on by default**: changing the language no longer reloads every resource behind the loading screen. Turn it off on the General page to get Minecraft's full reload back. Every other new option starts at Minecraft's own behavior
- **Particle distances now also apply to particles that ignore the Particles setting**, such as explosions, poofs, sword sweeps and campfire smoke. Before, a cap left them alone. Particles a command or the server forces stay untouched
- **Chloride is now marked incompatible.** Both mods replace Sodium's Fullscreen option and take over F11, and with both installed the game crashes at the title screen. With both installed, NeoForge and Fabric now stop at their error screen before the game loads, instead of crashing
- When updating, delete the 1.11.1 jar from your mods folder

## ✨ New Features

### Distances for every kind of entity, block entity and particle

- The Distances page has three groups: **Entities**, **Block Entities** and **Particles**. Each one starts with a **General** slider for the whole group, followed by one slider per kind
- **Entities**: Hostile Mobs, Land Animals, Water Mobs, Villagers and Golems, Dropped Items, Experience Orbs, Armor Stands and Mannequins, Frames, Paintings and Leash Knots, Cushions (new in 26.3), Boats and Minecarts, Falling Blocks and TNT, and Projectiles
- **Block Entities**: Chests (including ender chests and shulker boxes), Sign Text, Banners, Heads, Books (enchanting tables and lecterns), Bells, Campfire Food, Decorated Pots, and Spawners and Vaults. Since 26.2 a sign is an ordinary block drawn with the terrain, so only its text can be hidden, and beds are not block entities at all, so they have no slider
- **Particles**: Smoke and Fire, Redstone, Explosions, Effects, Combat, Water and Drips, Mobs, and Portals. Particles from other mods, and the few of Minecraft's in no group, follow General. The potent sulfur added in 26.2 is covered too: its geysers go with Water and Drips, its noxious gas with Smoke and Fire, and the sulfur cube's goo with Mobs
- A kind left on "General" follows its General slider; any other value replaces it for that kind, higher or lower. A General slider on "Default" keeps Minecraft's own distance
- Drawing only, as before: entities keep moving, items can still be picked up, and nothing changes for the server or other players. Players, glowing entities, text displays, armor stands with a visible name (holograms), beacons, End portals and gateways, and moving pistons are always drawn
- Lowering these mostly helps with lots of things in view: mob farms, item sorters, storage rooms, particle-heavy builds

### Fast Language Switch

- Changing the language applies almost at once, without the loading screen. Only the translations are reloaded, instead of every texture, model, sound and shader
- The creative inventory and recipe book search follow the new language, and so does the search of **JEI** and **REI**, even with text already typed in it (and of **EMI**, once it has a build for 26.3). Other mods that cache translated names may keep the old language until the next full reload (F3+T)
- In the new **Menus** group on the General page

### Notifications

- Six toggles in a new **Notifications** group on the General page: **Advancements**, **Recipes**, **Tutorial**, **Narrator**, **Unverified Chat Warning** and **Server Pack Download**. All on by default
- System errors, the world backup notice and other mods' notifications always show. A failed server pack download still shows its error with the download toggle off
- Turning one off in the middle of a burst stops the rest; the ones already on screen finish their animation

### Status effects

- **Night Vision Ending**: how Night Vision behaves in its last 10 seconds. **Flicker** is Minecraft's own pulsing; **Fade Out** dims the screen smoothly and reaches darkness just as the effect ends; **Steady** keeps full brightness until the end.
- **Your Effect Particles**: removes the swirls your own effects spawn around you, which float through your view in first person. Other players and mobs keep theirs, and other players still see yours
- Both are in a new **Status Effects** group on the Visual Effects page

### Screen overlays

- **Fire Overlay Height**: lowers the flames drawn while you are burning, from 100% (Minecraft's) down to 10%, so they cover less of the screen and you still notice you are on fire. Smart Fire Overlay still applies
- **Totem Animation**: turns off the Totem of Undying that pops up over the screen when one saves you. Its particles and sound stay. Items from other mods that use the same animation are hidden too

### Nametags

- **Nametags**: **All**, **Players Only**, **Mobs Only** or **None**. Mobs Only covers everything that is not a player, including named armor stands, which servers often use for floating text. Only the drawing goes: chat, the player list and death messages still show the names
- In a new **Nametags** group on the Visual Effects page

### More of Minecraft's hidden options

- **Damage Tilt**, **Darkness Pulsing** and **Hide Lightning Flashes**, from Minecraft's Accessibility menu, which Sodium's video settings leave out
- They sit with **Menu Background Blur** in a new **Minecraft** group on the General page. Saved with Minecraft's options, exactly as if changed from its Accessibility menu

## 🔧 Changes

- The mod's config screen (without Sodium) lays options out in **two columns**, like Minecraft's Video Settings, so the list is about half as long. Text too long for its column scrolls sideways
- Screen Mode is no longer on that screen: it is in Minecraft's Video Settings, on Sodium's General page and on F11
- The option groups on the Sodium pages now have names. Reese's Sodium Options makes each one collapsible
- Menu Background Blur moved from the Menus group to the new Minecraft group, on the same page

## 🔧 Technical Changes

- neoforge.mods.toml no longer declares modLoader and loaderVersion, which NeoForge no longer needs. The NeoForge and Minecraft versions the jar accepts are unchanged
- On NeoForge the jar now asks for Sodium 0.9.3 or newer, as these notes already did: Sodium 0.9.2 for NeoForge never started on stable NeoForge 26.3. With an older Sodium, NeoForge now says so before loading
- New config keys for the new options. Configs from older versions load as they are, and the missing keys start at their defaults

## 📦 Dependencies

- Minecraft 26.3 with NeoForge 26.3.0.76 or newer, or with Fabric Loader 0.19.3 or newer
- Optional: Sodium 0.9.2 or newer on Fabric, Sodium 0.9.3 or newer on NeoForge, and Mod Menu on Fabric
- Not compatible with Chloride

---

<details>
<summary><b>🇪🇸 Leer en español</b></summary>

> **Distancias para todo, y un montón de opciones chicas.** La página de Distancias ahora cubre cada tipo de entidad, entidad de bloque y partícula, cada uno con su propio slider. Además, cambio rápido de idioma, interruptores de notificaciones, nombres sobre entidades, un final de la Visión nocturna más tranquilo, un overlay de fuego más bajo y más.

## ⚠️ Notas Importantes

- Los ajustes se quedan como estaban, incluidas las cuatro distancias de antes (Objetos tirados, Orbes de experiencia, Soportes para armadura y Partículas), que conservan su valor y su lugar
- **El Cambio rápido de idioma viene activado**: cambiar el idioma ya no recarga todos los recursos detrás de la pantalla de carga. Desactívalo en la página General para volver a la recarga completa de Minecraft. Todas las demás opciones nuevas empiezan con el comportamiento del propio Minecraft
- **Las distancias de partículas ahora también aplican a las partículas que ignoran el ajuste de Partículas**, como explosiones, nubes de humo al morir, barridos de espada y el humo de las fogatas. Antes, el límite no las tocaba. Las partículas que fuerza un comando o el servidor siguen sin tocarse
- **Chloride ahora está marcado como incompatible.** Los dos mods reemplazan la opción de Pantalla completa de Sodium y se adueñan de F11, y con los dos instalados el juego crashea en la pantalla de título. Con los dos instalados, NeoForge y Fabric ahora se detienen en su pantalla de error antes de cargar el juego, en vez de crashear
- Al actualizar, borra el jar de la 1.11.1 de tu carpeta de mods

## ✨ Novedades

### Distancias para cada tipo de entidad, entidad de bloque y partícula

- La página de Distancias tiene tres grupos: **Entidades**, **Entidades de bloque** y **Partículas**. Cada uno empieza con un slider **General** para todo el grupo, seguido de un slider por tipo
- **Entidades**: Mobs hostiles, Animales terrestres, Mobs acuáticos, Aldeanos y gólems, Objetos tirados, Orbes de experiencia, Soportes y maniquíes, Marcos, cuadros y nudos de rienda, Cojines (nuevos en 26.3), Botes y vagonetas, Bloques que caen y TNT, y Proyectiles
- **Entidades de bloque**: Cofres (incluidos cofres de ender y cajas de shulker), Texto de letreros, Estandartes, Cabezas, Libros (mesas de encantamientos y atriles), Campanas, Comida en fogatas, Vasijas decoradas, y Generadores y bóvedas. Desde 26.2 el letrero es un bloque normal que se dibuja con el terreno, así que solo se puede ocultar su texto, y las camas ya no son entidades de bloque, así que no tienen slider
- **Partículas**: Humo y fuego, Redstone, Explosiones, Efectos, Combate, Agua y goteos, Mobs, y Portales. Las partículas de otros mods, y las pocas de Minecraft que no están en ningún grupo, siguen a General. El azufre potente que llegó en 26.2 también entra: sus géiseres van con Agua y goteos, su gas con Humo y fuego, y la baba del cubo de azufre con Mobs
- Un tipo en "General" sigue a su slider General; cualquier otro valor lo reemplaza para ese tipo, más alto o más bajo. Un slider General en "Por defecto" conserva la distancia del propio Minecraft
- Solo el dibujo, como antes: las entidades se siguen moviendo, los objetos se pueden recoger y nada cambia para el servidor ni para otros jugadores. Los jugadores, las entidades que brillan, los text displays, los soportes para armadura con nombre visible (hologramas), los faros, los portales y accesos del End y los pistones en movimiento siempre se dibujan
- Bajarlas ayuda sobre todo cuando hay muchas cosas a la vista: granjas de mobs, clasificadores de objetos, almacenes, construcciones llenas de partículas

### Cambio rápido de idioma

- Cambiar el idioma se aplica casi al instante, sin la pantalla de carga. Solo se recargan las traducciones, en vez de todas las texturas, modelos, sonidos y shaders
- La búsqueda del inventario creativo y del libro de recetas sigue al nuevo idioma, y también la de **JEI** y **REI**, aunque ya tengan texto escrito (y la de **EMI**, cuando tenga versión para 26.3). Otros mods que guardan nombres traducidos pueden quedarse en el idioma anterior hasta la siguiente recarga completa (F3+T)
- En el nuevo grupo **Menús** de la página General

### Notificaciones

- Seis interruptores en un nuevo grupo **Notificaciones** de la página General: **Progresos**, **Recetas**, **Tutorial**, **Narración**, **Aviso de chat no verificable** y **Descarga de paquetes del servidor**. Todos activados por defecto
- Los errores del sistema, el aviso de respaldo del mundo y las notificaciones de otros mods siempre se muestran. Una descarga fallida de paquetes del servidor sigue mostrando su error con ese interruptor apagado
- Apagar uno en medio de una ráfaga detiene las que faltan; las que ya están en pantalla terminan su animación

### Efectos de estado

- **Final de la Visión nocturna**: cómo se comporta la Visión nocturna en sus últimos 10 segundos. **Parpadeo** es el pulso del propio Minecraft; **Desvanecer** oscurece la pantalla poco a poco y llega a la oscuridad justo cuando se acaba el efecto; **Fija** mantiene el brillo completo hasta el final.
- **Tus partículas de efectos**: quita los remolinos que tus propios efectos generan a tu alrededor, que en primera persona flotan frente a tu vista. Los demás jugadores y los mobs conservan los suyos, y los demás jugadores siguen viendo los tuyos
- Los dos están en un nuevo grupo **Efectos de estado** de la página Efectos visuales

### Overlays de pantalla

- **Altura del overlay de fuego**: baja las llamas que se dibujan mientras te quemas, del 100% (el de Minecraft) hasta el 10%, así tapan menos pantalla y sigues notando que estás en llamas. El Overlay de fuego inteligente sigue funcionando
- **Animación del tótem**: quita el Tótem de inmortalidad que aparece en la pantalla cuando te salva. Sus partículas y su sonido se mantienen. Los objetos de otros mods que usan la misma animación también se ocultan

### Nombres sobre entidades

- **Nombres sobre entidades**: **Todos**, **Solo jugadores**, **Solo mobs** o **Ninguno**. Solo mobs incluye todo lo que no es jugador, también los soportes para armadura con nombre, que los servidores suelen usar para texto flotante. Solo se quita el dibujo: el chat, la lista de jugadores y los mensajes de muerte siguen mostrando los nombres
- En un nuevo grupo **Nombres** de la página Efectos visuales

### Más opciones escondidas de Minecraft

- **Tambaleo por daño**, **Pulso de oscuridad** y **Ocultar relámpagos**, del menú de Accesibilidad de Minecraft, que los ajustes de video de Sodium no incluyen
- Están junto al **Desenfoque de fondo** en un nuevo grupo **Minecraft** de la página General. Se guardan con las opciones de Minecraft, igual que si se cambiaran desde su menú de Accesibilidad

## 🔧 Cambios

- La pantalla de configuración del mod (sin Sodium) acomoda las opciones en **dos columnas**, como los Ajustes de video de Minecraft, así que la lista mide como la mitad. El texto que no cabe en su columna se desplaza de lado
- El Modo de pantalla ya no está en esa pantalla: está en los Ajustes de video de Minecraft, en la página General de Sodium y en F11
- Los grupos de opciones de las páginas de Sodium ahora tienen nombre. Reese's Sodium Options hace que cada uno se pueda contraer
- El Desenfoque de fondo pasó del grupo Menús al nuevo grupo Minecraft, en la misma página

## 🔧 Cambios Técnicos

- neoforge.mods.toml ya no declara modLoader ni loaderVersion, que NeoForge ya no necesita. Las versiones de NeoForge y de Minecraft que acepta el jar no cambian
- En NeoForge el jar ahora pide Sodium 0.9.3 o más nuevo, como ya lo decían estas notas: Sodium 0.9.2 para NeoForge nunca arrancó con NeoForge 26.3 estable. Con un Sodium más viejo, NeoForge ahora lo avisa antes de cargar
- Claves nuevas en el config para las opciones nuevas. Los configs de versiones anteriores cargan tal cual, y las claves que faltan empiezan en su valor por defecto

## 📦 Dependencias

- Minecraft 26.3 con NeoForge 26.3.0.76 o más nuevo, o con Fabric Loader 0.19.3 o más nuevo
- Opcional: Sodium 0.9.2 o más nuevo en Fabric, Sodium 0.9.3 o más nuevo en NeoForge, y Mod Menu en Fabric
- No es compatible con Chloride

</details>
