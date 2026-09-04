# Aurora AI Companion 2.0.4.2

## El cuerpo de Aurora

Esta versión completa la identidad física de Aurora. Hasta 2.0.4.1 el proyecto tenía cerebro, memoria y comportamiento, pero no una representación visual propia ni una ruta de desbloqueo segura.

### Añadido
- Sprite propio de Aurora en `assets/sprites/units/aurora.png`.
- Identidad visual de 32x32 píxeles: cuerpo compacto, núcleo rosa pastel, sensores y propulsores cian.
- `constructor = UnitEntity::create` explícito para dejar claro que Aurora es una unidad voladora real.
- Aurora queda desbloqueada automáticamente para facilitar las pruebas iniciales.
- Ruta de producción en `Air Factory` conservada.
- Aurora ahora puede ser controlada por el jugador y por lógica además de su IA propia.
- Movimiento omnidireccional y comportamiento hover para reforzar su identidad de compañera aérea.
- Efectos visuales: motores rosa pastel, núcleo luminoso y estela corta.
- Nombre/descripcion interna de la unidad para reforzar su personalidad.
- Minería de cobre y plomo para que la recuperación de recursos sea coherente con su capacidad de minería tier 1.

### Corrección conceptual
Aurora sigue siendo una `UnitType`, no un bloque. Por eso no aparecerá en el menú de construcción de bloques. Su lugar natural es la lista de unidades de una fábrica, y su cuerpo visual se carga desde `sprites/units/aurora.png`.

### Compatibilidad
- Mindustry 159.7
- No se reemplaza el cerebro ni los sistemas de IA de 2.0.4.1.
- Se conserva la persistencia y el aprendizaje existentes.
