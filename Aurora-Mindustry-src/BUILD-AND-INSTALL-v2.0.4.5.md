# Aurora 2.0.4.6: compilación y prueba

## Importante
Aurora es un JVM mod de Java. Mindustry carga el bytecode compilado de un JAR. Este repositorio/ZIP es el proyecto fuente y no debe confundirse con el artefacto ejecutable.

## Compilar

Requisitos:
- JDK 17
- Gradle 8.x
- Internet durante la primera compilación para descargar Mindustry 159.7 y las dependencias

Ejecuta:

```text
./gradlew jar
```

El artefacto esperado es:

```text
build/libs/AuroraAICompanionDesktop.jar
```

También existe un workflow en `.github/workflows/build.yml` para compilar automáticamente en GitHub Actions.

## Prueba en Mindustry 159.7

1. Instala el JAR compilado como JVM mod.
2. Crea una partida personalizada en Serpulo.
3. Construye una Air Factory.
4. Selecciona Aurora en la fábrica.
5. Comprueba que aparece el sprite de 48x48.
6. Fabrica una Aurora.
7. Usa `/aurora estado`.
8. Comprueba que el dueño sea tu jugador.
9. Usa `/aurora mina`.
10. Comprueba que el recurso seleccionado no esté fijado a cobre.
11. Comprueba que la unidad pueda regresar al núcleo y depositar.

## Prueba de campaña

En campaña, Aurora aparece como hija de Air Factory en la rama tecnológica y requiere investigar su nodo. El coste de investigación es 150 cobre + 100 plomo.
