# Aurora AI Companion para Mindustry 159.7

Aurora es una IA compañera inteligente para Mindustry. Este repositorio contiene el código fuente y está configurado con GitHub Actions para compilar automáticamente el `.jar` que puedes usar en Mindustry Android.

## 🚀 Compilación Automática

Cada vez que hagas un `push` a este repositorio, GitHub Actions:

1. ✅ Descarga el código fuente
2. ✅ Ejecuta Gradle para compilar
3. ✅ Genera `AuroraAICompanion.jar`
4. ✅ Lo guarda como **artifact descargable**

### 📥 Cómo descargar el .jar compilado:

1. Ve a la pestaña **"Actions"** en tu repo GitHub
2. Selecciona el workflow más reciente (en verde = exitoso)
3. En la sección "Artifacts" descarga `aurora-jar`
4. Extrae y coloca el `.jar` en Mindustry

## 📁 Estructura del Proyecto

```
Aurora-Mindustry-Miharii/
├── build.gradle.kts        ← Configuración de Gradle (compilación)
├── settings.gradle.kts      ← Configuración de proyectos Gradle
├── gradle.properties        ← Propiedades de Gradle
├── src/
│   ├── assets/             ← Recursos (imágenes, textos, configuración)
│   │   ├── bundles/       ← Archivos de idioma
│   │   └── sprites/       ← Sprites del mod
│   └── aurora/            ← Código fuente Java
│       ├── ai/            ← Sistema de IA
│       ├── brain/         ← Lógica cerebral
│       ├── core/          ← Funciones core
│       ├── systems/       ← Sistemas (combate, minería, etc.)
│       ├── content/       ← Contenido del mod
│       └── [más módulos...]
├── content/               ← Configuración del contenido en HJSON
├── .github/workflows/     ← Workflows de GitHub Actions
│   └── build.yml         ← Workflow de compilación automática
└── icon.png              ← Icono del mod
```

## 🔧 Desarrollo Local

Si quieres compilar localmente:

```bash
# Linux/Mac
./gradlew build

# Windows
gradlew.bat build
```

El `.jar` estará en `build/libs/`

## 📝 Versión Actual

**Aurora v2.0.4.6** - Para Mindustry 159.7

## 📄 Licencia

Ver archivo `LICENSE`

---

**Nota:** Este repositorio utiliza GitHub Actions como servidor de compilación gratuito. No es necesario instalar Java, Gradle ni Android Studio en tu dispositivo.
