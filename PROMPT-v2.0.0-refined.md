# Aurora AI Companion 2.0.3.2 — refined development prompt

Continúa el desarrollo de Aurora AI Companion para Mindustry 159.7 en Android y PC usando únicamente la API oficial del juego. Mantén el mod modular, documentado y eficiente en dispositivos móviles.

## Objetivo
Aurora debe sentirse como una compañera real: tiene un propietario elegido por el jugador, recibe órdenes en español, ejecuta tareas autónomamente, evalúa amenazas y puede proponer construcciones antes de realizarlas.

## Arquitectura obligatoria
- `AuroraAI`: adaptador fino de Mindustry.
- `AuroraBrain`: máquina de decisión con presupuesto periódico, no cada frame.
- `AuroraProfile`: estado mínimo por unidad.
- `AuroraRegistry`: owner y perfiles runtime.
- Sistemas independientes: movimiento, minería, construcción, reparación, combate y comunicación.
- `planning/`: propuestas y colas de construcción.
- `bridge/`: interfaz aislada para una futura IA externa.

## Owner
- El jugador debe poder elegir quién es el propietario.
- Solo el propietario o un administrador puede ordenar a Aurora.
- Aurora nunca debe cambiar de dueño por proximidad.
- Preparar persistencia futura sin inventar formatos incompatibles.

## Órdenes en español
Soportar como mínimo:
- `/aurora dueña <jugador>`
- `/aurora seguir`
- `/aurora espera`
- `/aurora mina`
- `/aurora repara`
- `/aurora defiende`
- `/aurora nucleo`
- `/aurora construye ...`
- `/aurora propone muralla`
- `/aurora si` y `/aurora no`

## Construcción inteligente local
Cuando el jugador pida una defensa, Aurora puede generar una propuesta local sencilla, mostrar un resumen y esperar confirmación. Ejemplo: “Propongo una muralla de cobre con dos Duo. ¿La construyo?”

Después de aceptar:
1. validar cada posición;
2. añadir pocos `BuildPlan` por ciclo;
3. dejar que `BuilderAI` ejecute la construcción;
4. omitir posiciones inválidas;
5. continuar hasta completar el plan.

En versiones futuras, reemplazar el generador simple por un `ConstructionPlanner` que considere amenazas, recursos, potencia y geometría.

## Autonomía
Aurora debe poder seguir, esperar, minar cobre, reparar, defender y volver al núcleo. Si el inventario de minería está casi lleno, debe regresar, descargar y retomar la tarea. Si una tarea termina, debe seleccionar la siguiente acción razonable o volver a seguir al propietario.

## Recursos y rendimiento
No escanear todo el mapa cada frame. Usar el `BlockIndexer` oficial para localizar minerales y edificios. La búsqueda de recursos debe hacerse bajo demanda o con caché temporal. La visibilidad/fog no debe provocar un escaneo completo: el mapa ya contiene sus tiles; descubrir terreno no requiere reconstruir el índice.

Objetivo de pensamiento: aproximadamente 0.80 s entre evaluaciones completas, mientras el movimiento normal sigue el ciclo de Mindustry. El combate se evalúa según necesidad.

## Seguridad y estabilidad
- Nada de red bloqueante durante gameplay.
- Nada de I/O en el hilo principal.
- No crear objetos continuamente dentro de loops calientes.
- No tocar APIs de gameplay desde hilos externos.
- Validar unidades, jugadores, bloques, equipos y posiciones antes de usarlos.

## Futuras versiones
- 2.1: persistencia del owner, cola de tareas y planner de construcción.
- 2.5: detección de cuellos de botella con muestreo presupuestado.
- 3.0: personalidad y estados de ánimo.
- 4.0: proveedor de IA externa asíncrono, cacheado y con fallback local.

No reescribir la arquitectura para añadir inteligencia futura. La IA externa debe ser una capa de decisión opcional, nunca una dependencia del gameplay básico.
