# Aurora AI Companion 2.0.4.0

## Enfoque
Versión centrada en inteligencia adaptativa, aprendizaje local y misiones resilientes.

### Aprendizaje
- Aurora registra intentos y resultados de estrategias de construcción y recuperación.
- La memoria de aprendizaje influye en el orden de alternativas futuras.
- El aprendizaje queda guardado con el estado de Aurora.

### Misiones y planos
- Los planos bloqueados permanecen pendientes.
- Se buscan posiciones alternativas en radios crecientes.
- Se prueban bloques alternativos cuando la misión lo permite.
- La cola de construcción tiene en cuenta recursos ya comprometidos por otros planos.
- Si falta un recurso para el siguiente paso, Aurora identifica el requisito faltante, busca una fuente y cambia temporalmente a recuperación.
- Al completar la recuperación, vuelve a la misión original en lugar de abandonarla.
- Los taladros de recursos pasan por el mismo sistema de construcción adaptativo.

### Comunicación
- El texto de Aurora usa una firma visual rosa pastel: `[#F6C1D8]`.
- Se mantiene el texto del contenido en blanco para conservar legibilidad.

### Compatibilidad
- `minGameVersion: 159.7`.
- El chunk de guardado mantiene el nombre `aurora.state.v2`, pero incrementa su versión interna para incluir aprendizaje y recuperación.
