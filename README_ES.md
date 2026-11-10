# Borderless Window

[![CurseForge](https://cf.way2muchnoise.eu/versions/minecraft-borderless-window.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-borderless-window)
[![CurseForge](https://cf.way2muchnoise.eu/minecraft-borderless-window.svg)](https://www.curseforge.com/minecraft/mc-mods/minecraft-borderless-window)

*[English](README.md)*

Un mod ligero client-side para **NeoForge y Fabric 1.21.1** que agrega **pantalla completa sin bordes** a Minecraft, reemplazando el fullscreen exclusivo con un sistema de 3 modos — más un conjunto de opciones de video y de calidad de vida que Minecraft no ofrece.

## Características

### Ventana

- **Tres modos de pantalla**: Ventana, Sin bordes, y la Pantalla completa exclusiva normal de Minecraft.
- **F11 configurable**: alternar Sin bordes, alternar Pantalla completa exclusiva, o **ciclar entre los tres modos**.
- **Compatible con HDR**: evita el parpadeo de pantalla negra y el cambio de perfil de color que el fullscreen exclusivo (y los borderless mal implementados) causan con el HDR de Windows activado.
- **Soporte multi-monitor**: se aplica en el monitor donde está la ventana, no siempre en el primario.

### Video y calidad de vida

- **Contador de FPS**: los FPS actuales, o una sola línea con el Mín. / Prom. / Máx. de los últimos 15 segundos. En cualquier esquina de la pantalla. Muestrea el contador del propio juego, así que siempre coincide con F3 y respeta el VSync y el límite de FPS, y se oculta solo mientras F3 está abierto.
- **Opciones de segundo plano**: Minecraft 1.21.1 sigue corriendo a toda velocidad y a todo volumen cuando nadie lo está viendo. Cuatro opciones independientes lo arreglan:
  - **Límite de FPS sin foco**: limita los FPS mientras el juego está en segundo plano (con Alt+Tab, minimizado o detrás de otra ventana), de 5 a 60 FPS.
  - **Límite por inactividad**: limita los FPS tras unos minutos sin usar el teclado ni el mouse, aunque la ventana esté enfocada. Cualquier input lo quita al instante.
  - **Pausar render al minimizar**: deja de dibujar por completo mientras está minimizado. El juego sigue corriendo — solo se detiene la imagen.
  - **Volumen sin foco**: baja o silencia el juego mientras está en segundo plano, como proporción de tu Volumen general.
  - Las cuatro funcionan junto con Dynamic FPS: gana el límite de FPS más bajo.
- **Distancias de dibujado**: deja de dibujar objetos tirados, orbes de experiencia y soportes para armadura más allá de la distancia que elijas, y evita que aparezcan partículas más lejos de otra. Es puramente visual — los objetos se pueden seguir recogiendo y los orbes te siguen llegando. Las entidades brillantes y los soportes con nombre (hologramas, texto flotante) siempre se dibujan.
- **Opciones de niebla**: Terreno (la neblina de distancia, incluida la niebla densa del Nether), Agua, Lava y Nieve polvo, cada una por separado. La niebla de Ceguera y Oscuridad nunca se ve afectada, así que esos efectos siguen funcionando como deben. **Inicio de la niebla** elige dónde empieza la niebla de terreno, como el de OptiFine, para una neblina más suave o una mandada hasta el borde.
- **Altura de las nubes**: sube o baja la capa de nubes entre y=64 y y=320. En Por defecto cada dimensión conserva su propia altura, y las dimensiones sin nubes — el Nether, el End, las dimensiones sin nubes de otros mods — siguen sin ellas con cualquier valor.
- **Cielo y clima**: oculta el sol y la luna, las estrellas, o la lluvia y la nieve que caen — opciones que Minecraft no tiene, del menú Detalles de OptiFine. Solo se quita el dibujo: el día se sigue haciendo noche, y sigue lloviendo para los mobs, los cultivos y la pesca.
- **Nombre del objeto en mano**: quita el nombre del objeto que aparece sobre la barra al cambiar de casilla.
- **Overlays de pantalla**: quita la textura de agua que se dibuja sobre tu vista al nadar y las llamas que aparecen mientras te quemas. El fuego además tiene un modo inteligente que oculta las llamas solo mientras tengas Resistencia al Fuego. El overlay de asfixia dentro de bloques sólidos nunca se toca.
- **Overlays de cámara**: quita el difuminado de la calabaza, el borde de escarcha de la nieve polvo, la distorsión morada del portal del Nether y la mira del catalejo. El zoom del catalejo no cambia — solo se va el marco. La viñeta se deja en paz a propósito: también sirve de aviso del borde del mundo.
- **Campo de visión separado**: Minecraft tiene un solo slider de Efectos de FOV para todas las fuentes a la vez. Aquí Velocidad, Vuelo, Arco y Líquidos son cuatro sliders, así que puedes calmar el vaivén constante del FOV con pociones de Rapidez sin perder el acercamiento del arco.
- **Distorsión separada**: la misma idea con el slider de Efectos de distorsión — el portal del Nether y el efecto de Náuseas tienen un slider cada uno, bien diferenciados incluso cuando pasan los dos a la vez.
- **Balanceo separado**: Minecraft enciende o apaga el balanceo entero de una vez. Aquí la cámara y el objeto en la mano van por separado, y la cámara suma un modo **Solo vertical** que conserva el rebote de arriba a abajo, pero quita el vaivén y la inclinación — el horizonte queda nivelado, que es justo la parte que marea a la mayoría.
- **Zoom**: presiona **C** (se cambia en Controles) para acercar la vista, 4× por defecto. Mantener o alternar, la rueda del mouse para ir de 1× a 50×, suave o instantáneo, y opcionalmente ocultar la mano y el HUD mientras haces zoom. El mouse gira más lento en la misma proporción, así que apuntar se siente igual. Solo cierra el campo de visión — nada se dibuja más lejos — y se hace a un lado mientras usas un catalejo.
- **Números de encantamientos**: Minecraft solo tiene números romanos hasta el X y arriba de eso muestra texto crudo como `enchantment.level.11` — los niveles llegan legalmente hasta 255. Esto lo arregla y además te deja elegir el estilo: romanos hasta el X y números normales después, siempre romanos, o siempre números. Los resource packs que sí traducen niveles altos siguen funcionando.
- **Los sliders escondidos de Minecraft**: los ajustes de video de Sodium dejan fuera varios sliders del propio Minecraft. El mod recupera Efectos de distorsión, Efectos de FOV, Velocidad del brillo, Intensidad del brillo y Desenfoque de menús, cada uno junto a las opciones con las que se relaciona.

### Configuración y compatibilidad

- **NeoForge y Fabric**: el mismo mod, con todas sus funciones, en los dos loaders. Incluso comparten el mismo archivo de configuración.
- **Integrado en los menús del propio Minecraft**: sin Sodium, el interruptor de "Pantalla completa" de los Ajustes de video se convierte en un selector de "Modo de pantalla", y un botón "Borderless Window..." ahí mismo abre la pantalla de configuración completa. No requiere ningún otro mod en ninguno de los dos loaders.
- **Integración con Sodium** (opcional): con [Sodium](https://www.curseforge.com/minecraft/mc-mods/sodium) 0.8.x instalado, el checkbox de "Pantalla completa" de su menú de video se reemplaza por un selector de "Modo de pantalla", y el mod obtiene su propia sección con seis páginas. Construido completamente sobre la Config API oficial de Sodium — sin mixins frágiles a sus clases internas. También funciona bien con Reese's Sodium Options.
- **Nada más que instalar**: funciona con o sin Sodium. En Fabric no requiere Fabric API — las dos piezas pequeñas que usa, para cargar sus traducciones y para agregar la tecla de zoom a Controles, vienen dentro del jar, igual que hace Sodium. Mod Menu es opcional.
- **Persistente**: todos los ajustes se recuerdan entre sesiones (`config/borderlesswindow.json`).
- **Traducible**: todos los textos viven en lang files estándar (`en_us`, `es_mx`, `es_es` incluidos — se aceptan contribuciones).

## ¿Por qué sin bordes?

El fullscreen exclusivo cambia el modo de video real del monitor, lo que causa alt-tabs lentos, parpadeos negros con HDR, bugs de desincronización de resolución y estados de ventana rotos al reiniciar el juego. La pantalla sin bordes es solo una ventana del tamaño del monitor sin decoración: se ve idéntica, pero el alt-tab es instantáneo y nada se desincroniza.

## Configuración

- **En juego (sin Sodium):** Opciones → Ajustes de video → el selector de "Modo de pantalla" (en lugar de "Pantalla completa") y el botón "Borderless Window..." al final. La misma pantalla de configuración también se abre desde el menú de Mods de NeoForge (botón Config) y, en Fabric, desde Mod Menu si lo tienes instalado. Todas las opciones viven en una sola lista con scroll agrupada por feature, en el mismo orden que las páginas de Sodium. Los cambios aplican al instante.
- **En juego (con Sodium):** Ajustes de video → la opción "Modo de pantalla" (reemplaza el checkbox de fullscreen de Sodium) y la sección "Borderless Window" con sus seis páginas: General, Encantamientos, FPS y segundo plano, Efectos visuales, Cámara y Distancias. Los sliders del propio Minecraft que Sodium deja fuera están en las páginas con las que se relacionan; se guardan con las opciones de Minecraft, igual que si los cambiaras desde su menú de Accesibilidad.
- **Archivo de config:** `config/borderlesswindow.json`. Si el mod queda en un estado raro, borra este archivo y reinicia — los valores por defecto se regeneran sin problema.

Todos los ajustes vienen por defecto en el comportamiento normal del juego, así que instalar o actualizar el mod nunca cambia cómo se ve hasta que tú lo pidas. Lo único que agrega por su cuenta es la tecla de zoom, que no hace nada hasta que la presionas.

**Nota sobre los ajustes refinados:** varias opciones afinan un ajuste de Minecraft en vez de reemplazarlo, y no hacen nada mientras ese ajuste esté apagado. El balanceo va bajo "Balanceo de cámara", los cuatro sliders de FOV bajo "Efectos de FOV", y los dos de distorsión bajo "Efectos de distorsión". Los dos últimos están en los ajustes de Accesibilidad de Minecraft.

## Requisitos

- Minecraft **1.21.1**
- **NeoForge** 21.1.x, o **Fabric Loader** 0.16.0+ (no requiere Fabric API)
- (Opcional) **Sodium 0.8.0+** para la integración con el menú de video
- (Opcional, Fabric) **Mod Menu**, para abrir también la configuración desde su lista de mods

Descarga el jar de tu loader: `borderlesswindow-neoforge-…` o `borderlesswindow-fabric-…`.

## Licencia

[MIT](LICENSE)
