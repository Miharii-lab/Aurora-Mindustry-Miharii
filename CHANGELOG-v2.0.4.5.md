# Aurora AI Companion 2.0.4.6

## Estabilización profunda: aparición, investigación, propietario y prueba real

### Corrección crítica de empaquetado
- Se normalizó el directorio de assets de Java a `src/assets/`.
- El task `jar` ahora empaqueta directamente `src/assets` en la raíz del JAR.
- Se añadió una comprobación explícita en la documentación: una JVM mod debe probarse con el JAR compilado, no importando el ZIP del proyecto fuente.

### Aparición e identidad
- Aurora conserva un `UnitType` físico real con `UnitEntity::create`.
- Se mantiene el controlador `AuroraAI` y el control manual del jugador.
- Se conserva el sprite 48x48 en `src/assets/sprites/units/aurora.png`.
- Se añadió bundle español para que el contenido aparezca como `Aurora` y no con el identificador interno completo.
- Se mantienen descripción, detalles, núcleo rosa pastel y capacidades aéreas.

### Rama de investigación
- `alwaysUnlocked` deja de ocultar el verdadero flujo de progresión.
- Aurora recibe una rama propia como hija de `Air Factory`.
- Se añaden 150 cobre + 100 plomo como coste de investigación.
- La rama se añade a todos los nodos existentes de Air Factory, evitando reconstruir los árboles vanilla.
- En partidas personalizadas/sandbox, `unlockedNow()` mantiene a Aurora disponible inmediatamente para pruebas.

### Propietario
- En single-player, si existe un único jugador aliado, ese jugador se convierte siempre en propietario, independientemente de la posición exacta de aparición.
- En multijugador aliado, se mantiene la selección por cercanía cuando no existe un propietario persistido.
- Nunca se reemplaza silenciosamente un propietario persistido.
- Los comandos continúan priorizando la Aurora de su dueño.

### Minería
- Se mantiene `MINE_RESOURCE` como tarea genérica.
- Aurora continúa descubriendo automáticamente minerales mediante `Item.getAllOres()`.
- `mineTier` 5 permite las durezas cubiertas por los taladros vanilla de Mindustry 159.7.
- La construcción de infraestructura sigue seleccionando el taladro mínimo apropiado por dureza.
- No existe fallback operativo a cobre.

### Diagnóstico
- Se conserva `/aurora estado` para comprobar dueño, tarea, estado, controlador, recurso y taladro.
- Se mantiene la persistencia de propietario, memoria, exploración, aprendizaje y estado.

### Preparación de compilación
- Se añadió workflow de GitHub Actions para compilar el JAR con JDK 17 y Gradle.
- El artefacto generado es `AuroraAICompanionDesktop.jar`.

## Nota de prueba
Esta versión es la primera que distingue claramente entre el proyecto fuente y el artefacto ejecutable de Mindustry. Para una prueba dentro del juego debe instalarse el JAR compilado o un ZIP que contenga el JAR en la raíz junto con `mod.hjson`/assets según el método de distribución. El entorno de esta entrega no dispone de las dependencias de Mindustry, por lo que no se declara un JAR compilado como si hubiera sido verificado cuando no lo fue.
