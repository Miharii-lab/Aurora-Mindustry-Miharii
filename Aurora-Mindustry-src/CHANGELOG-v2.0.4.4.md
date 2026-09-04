# Aurora AI Companion 2.0.4.4

## Estabilización: minería universal y preparación para pruebas

### Minería
- Eliminado el último fallback explícito a `Items.copper` durante la ejecución de `MINE_RESOURCE`.
- Aurora descubre sus objetivos de minería desde `Item.getAllOres()` en lugar de mantener una lista fija de cobre/plomo.
- `mineTier` pasa a 5, alineado con el taladro vanilla de mayor dureza de Mindustry 159.7.
- `mineHardnessScaling` queda habilitado para que los minerales más duros conserven una penalización de tiempo natural.
- Aurora puede buscar mineral tanto en suelo como en paredes.
- La selección prioriza el mineral disponible con menor stock del equipo.

### Herramientas de extracción
- Se añadió selección automática del taladro mínimo adecuado según dureza.
- Mechanical Drill: hasta tier 2.
- Pneumatic Drill: hasta tier 3.
- Laser Drill: hasta tier 4.
- Blast Drill: hasta tier 5.
- Aurora no intenta sustituir un taladro requerido por uno más débil.
- Los taladros bloqueados por investigación no se ponen en cola.

### Economía
- `EconomyAnalyzer` dejó de estar centrado exclusivamente en cobre.
- El cuello de botella de recursos se obtiene a partir del mineral presente en el mapa con menor stock.
- El flujo se mide para ese recurso real.
- La detección de infraestructura cuenta cualquier `Drill`, no solo mechanical/pneumatic.

### Prueba en juego
- Nuevo `/aurora estado` para inspeccionar dueño, tarea, estado, controlador, recurso elegido y taladro correspondiente.
- Se mantiene `aurora.state.v2` y los ordinales existentes de `AuroraTask`/`AuroraState`.
- No se cambia el propietario persistido al cargar una partida.

### Compatibilidad
- Objetivo: Mindustry v159.7.
- JDK objetivo: 17.
- Se conserva el sprite de Aurora en 48x48.
- No se elimina ningún sistema de memoria, aprendizaje, construcción, reparación, combate o exploración.
