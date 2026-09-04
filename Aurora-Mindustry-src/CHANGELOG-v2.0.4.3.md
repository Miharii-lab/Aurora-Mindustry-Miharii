# Aurora AI Companion 2.0.4.3

## Estabilización: dueño, cerebro y cuerpo

### Propiedad y control
- Aurora recibe automáticamente un dueño al ser creada o cargada si todavía no tiene uno.
- El dueño nunca se sobreescribe al cargar una partida ni se reemplaza silenciosamente si el jugador propietario está temporalmente ausente.
- En equipos aliados con varios jugadores se usa la Aurora más cercana como propietario inicial; `/aurora dueña <jugador>` sigue permitiendo reasignarla.
- Los comandos buscan primero Auroras que pertenecen al jugador, evitando ordenar accidentalmente la Aurora de otro aliado.
- El `UnitType.controller` inicial crea `AuroraAI`, por lo que el cerebro no queda sustituido por `CommandAI` simplemente porque la unidad sea controlable por el jugador. El control directo del jugador sigue siendo posible cuando Mindustry cambia el controlador a `Player`.

### Minería sin identidad de cobre
- `AuroraTask.MINE_COPPER` pasa a llamarse `MINE_RESOURCE` manteniendo el mismo ordinal para no romper estados guardados.
- Aurora selecciona entre sus recursos minables el que tenga menor stock y una fuente de mineral disponible.
- El comando `/aurora mina` ya no la define como minera de cobre.
- Los mensajes autónomos hablan de reforzar el recurso que necesite la base.

### Sprite
- `aurora.png` preparado a 48x48 píxeles manteniendo el diseño pixel-art de la versión anterior.
- Se conserva la identidad rosa pastel, núcleo luminoso, sensores y propulsores.

### Limpieza
- Versiones, log de carga y documentación actualizados a 2.0.4.3.
- Se conservan los chunks de guardado existentes para compatibilidad con partidas anteriores.

### Compatibilidad
- Objetivo: Mindustry 159.7.
- No se elimina ningún sistema de memoria, aprendizaje, construcción, reparación, combate o exploración de 2.0.4.2.
