# Aurora AI Companion 2.0.4.6

## Disponibilidad y prueba offline
- Añadido `/aurora invoca` con alias `invocar`, `llama` y `llamar`.
- La invocación no depende de que exista una Aurora previa ni de haber investigado su nodo. Está pensada como herramienta de prueba para partidas personalizadas/offline.
- Si existe una Aurora sin dueño en el equipo, el comando la adopta en lugar de crear otra. Si el jugador ya tiene una Aurora, no se crean duplicados.

## Investigación
- Serpulo: Aurora se añade como hija de Air Factory.
- Erekir: como Erekir no tiene Air Factory, Aurora se añade como nodo de acceso bajo Ship Fabricator y requiere Lake. Su creación práctica en Erekir se hace con `/aurora invoca`.
- Se usan costes de investigación apropiados para cada planeta.

## Cuerpo y compatibilidad ambiental
- Aurora conserva el sprite 48x48.
- Se elimina la limitación ambiental heredada que podía impedir su uso en Erekir: la unidad ya no queda bloqueada por `Env.scorching`.

## Minería universal
- Aurora puede considerar todos los ores registrados por Mindustry, incluidos ores añadidos por otros mods.
- Su capacidad de minería se amplía a un tier alto para no fijarla a una lista cerrada de minerales.
- La construcción de infraestructura de extracción ya no está limitada a Mechanical/Pneumatic/Laser/Blast: `drillForItem()` inspecciona los `Drill` registrados, incluyendo `BeamDrill`, `BurstDrill` y drills de otros mods, y escoge el de menor tier que pueda extraer el mineral y esté desbloqueado.
- Se respetan las excepciones `blockedItems` de cada drill.

## Diagnóstico
- `/aurora estado` sigue mostrando dueño, tarea, estado, controlador, recurso y herramienta de extracción.

## Distribución
- Sigue siendo un proyecto fuente JVM. Debe compilarse a `AuroraAICompanionDesktop.jar` antes de instalarlo como mod Java.
- Objetivo: Mindustry 159.7.
