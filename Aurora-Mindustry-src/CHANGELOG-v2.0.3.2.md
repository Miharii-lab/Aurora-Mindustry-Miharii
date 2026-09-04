# Aurora AI Companion 2.0.3.2

## Estabilización
- Corregida la interpretación de `ItemModule.getFlowRate()`: muestras negativas ya no se convierten en consumo de cobre inventado.
- Mejorado el análisis conservador de recursos para evitar decisiones basadas en un falso flujo de salida.
- Corregida la exploración de sectores para no recorrer una fila/columna adicional fuera del mapa.
- Los planos defensivos bloqueados ya no se eliminan: se rotan al final de la cola para que Aurora pueda avanzar con otros pasos y reintentarlos después.
- La construcción prueba la posición original, una búsqueda local en expansión y materiales alternativos cuando el plano es una pared de cobre.
- Se evita abandonar una misión completa por un único plano temporalmente imposible.
- Se mantienen los datos de guardado de la propuesta con el formato compatible de la versión anterior.

## Objetivo de esta versión
Esta versión prioriza estabilidad y capacidad de recuperación antes de añadir nuevas funciones de aprendizaje. Aurora debe intentar cumplir una misión de varias maneras, conservar los pasos imposibles y volver a intentarlos cuando cambien los recursos o el terreno.

## Nota de verificación
El proyecto fue auditado estáticamente. La compilación completa contra las dependencias reales de Mindustry 159.7 debe verificarse en un entorno con Gradle/dependencias disponibles.
