# Aurora 2.0.4.6: compilación y prueba

## Importante
Aurora es un JVM mod de Java. Este ZIP contiene el proyecto fuente. Mindustry necesita el JAR compilado para ejecutar `AuroraMod` y habilitar el cerebro, propietario, memoria y comandos. La definición HJSON permite que el cuerpo físico y la receta sean visibles como contenido aunque la parte Java no se haya ejecutado.

## Compilar
Requisitos: JDK 17 y Gradle 8.x.

```text
./gradlew jar
```

Artefacto esperado:

```text
build/libs/AuroraAICompanionDesktop.jar
```

## Prueba offline recomendada
1. Instala el JAR compilado en Mindustry 159.7.
2. Crea una partida personalizada de Serpulo.
3. Escribe `/aurora invoca`.
4. Aurora debe aparecer junto al jugador y tenerlo como dueño.
5. Escribe `/aurora estado`.
6. Prueba `/aurora mina` cerca de cobre y después cerca de otro mineral disponible.
7. Verifica que el taladro sugerido cambia según dureza y disponibilidad.
8. Prueba control manual de Aurora y luego deja el control para verificar que vuelve a su IA.

## Investigación
- Serpulo: Investigación -> Air Factory -> Aurora.
- Erekir: Investigación -> Ship Fabricator -> Aurora.
- En partidas personalizadas, la investigación no es necesaria para `/aurora invoca`.

## Si Aurora vuelve a no aparecer
Revisa primero que estés instalando `AuroraAICompanionDesktop.jar`, no el ZIP `-source`. Un mod JVM requiere el bytecode compilado.
