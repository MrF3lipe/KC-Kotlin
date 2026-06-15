# Kitchen Cabinet — Kotlin (Jetpack Compose)

App Android nativa para gestión de recetas, despensa y lista de la compra.  
Construida con **Kotlin + Jetpack Compose + Room**.

---

## Pantallas

| Pantalla | Descripción |
|----------|-------------|
| **Home** | Lista de recetas con filtros por categoría y chips de color |
| **Detalle** | Imagen hero, ingredientes, pasos, rating, porciones ajustables |
| **Añadir/Editar** | Formulario con cámara/galería y picker de categorías |
| **Favoritos** | Recetas marcadas con ♥ |
| **Despensa** | Inventario agrupado por categoría con cantidades y caducidad |
| **Compras** | Lista de la compra con checkboxes y precio estimado |
| **Buscar** | Filtros por ingrediente, utensilio, dificultad y "solo cocinables" |
| **Cocinar** | Paso a paso con temporizador y pantalla siempre encendida |
| **Herramientas** | Conversor de unidades, escáner de barras, escalar recetas, importar |
| **Plan semanal** | Planificación de comidas por día de la semana |
| **Ajustes** | Idioma (ES/EN), tema (claro/oscuro), recordatorios, backup/restore |

---

## Stack técnico

| Componente | Versión |
|-----------|---------|
| Kotlin | 2.0.21 |
| AGP | 8.7.3 |
| Gradle | 8.9 |
| Compose BOM | 2024.12.01 |
| Room | 2.7.1 |
| Navigation Compose | 2.8.5 |
| compileSdk / targetSdk | 35 (Android 15) |
| minSdk | 26 (Android 8.0) |
| Java | 17 |

---

## Compilar con GitHub Actions (recomendado)

### Primera vez

1. Crea un repositorio en GitHub (puede ser privado)
2. Sube todo el contenido de esta carpeta:
   ```
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/TU_USUARIO/TU_REPO.git
   git push -u origin main
   ```
3. GitHub detecta el archivo `.github/workflows/android.yml` y compila automáticamente

### Descargar el APK

1. Ve a tu repositorio en GitHub
2. Haz clic en la pestaña **Actions**
3. Selecciona el workflow más reciente
4. Al final de la página verás los **Artifacts**:
   - `KitchenCabinet-Kotlin-debug` — APK de depuración (listo para instalar)
   - `KitchenCabinet-Kotlin-release` — APK sin firmar (para distribución)
5. Descarga y descomprime el ZIP del artifact
6. Instala el APK en tu Android: `adb install app-debug.apk`

> El primer build tarda ~5-8 minutos (descarga dependencias).  
> Los builds siguientes tardan ~2-3 minutos gracias al caché de Gradle.

---

## Compilar localmente (Windows)

### Requisitos previos

- **Java 17** (OpenJDK o Temurin) — [descargar aquí](https://adoptium.net/)
- **Android SDK** con Build Tools 35
- Configurar `JAVA_HOME` en variables de entorno

### Pasos

```bash
# 1. Doble clic en:
COMPILAR-LOCAL.bat

# 2. El APK se genera en:
app\build\outputs\apk\debug\app-debug.apk

# 3. Instalarlo en un dispositivo conectado:
INSTALAR-APK.bat
```

El script `COMPILAR-LOCAL.bat` detecta automáticamente:
- Si tienes Gradle local en `C:\android-sdk\gradle\gradle-8.9-bin.zip` → lo usa sin internet
- Si no → descarga Gradle de internet (solo la primera vez)

---

## Estructura del proyecto

```
KC-Kotlin/
├── .github/workflows/android.yml   ← GitHub Actions CI/CD
├── app/
│   └── src/main/
│       ├── java/com/kitchencabinet/
│       │   ├── MainActivity.kt      ← Punto de entrada + NavHost
│       │   ├── data/                ← Room: entidades, DAOs, BD, repositorio
│       │   ├── viewmodel/           ← ViewModels (Recipe, Pantry, Shopping, Settings)
│       │   ├── i18n/                ← Traducciones ES/EN (Translations.kt)
│       │   ├── notification/        ← WorkManager para recordatorios
│       │   └── ui/
│       │       ├── theme/           ← Colores, tipografía, tema Material3
│       │       ├── components/      ← RecipeCard, BottomNavBar, AppShell
│       │       ├── i18n/            ← Strings.kt (localización compose)
│       │       └── screens/         ← 12 pantallas Compose
│       └── res/                     ← Recursos, iconos, temas, file_paths.xml
├── gradle/
│   ├── libs.versions.toml           ← Version catalog centralizado
│   └── wrapper/gradle-wrapper.properties
├── COMPILAR-LOCAL.bat               ← Compilar en Windows
├── INSTALAR-APK.bat                 ← Instalar en dispositivo
├── Errores Encontrados.txt          ← Lista de bugs/feature requests
└── README.md
```

---

## Funcionalidades principales

### Gestión de recetas
- Crear, editar y eliminar recetas con imágenes (cámara o galería)
- Categorías, dificultad, tiempo, porciones ajustables
- Rating con estrellas y contador de veces cocinadas
- Tags y recetas destacadas
- Compartir como imagen, texto o QR

### Despensa
- Inventario agrupado por categoría con emoji
- Control de cantidades (+/-) y disponibilidad
- Fechas de caducidad con recordatorios
- Gestión de categorías personalizadas
- Imágenes persistentes (copiadas a storage interno)

### Búsqueda inteligente
- Filtros por ingrediente, utensilio y dificultad
- Modo "solo cocinables" (con lo que tengo en despensa)
- Agrupación: ✅ Puedes cocinar, 🟡 Casi listas, 📚 Otras

### Cocina
- Modo paso a paso con temporizador
- Pantalla siempre encendida durante cocción
- Finalización automática al último paso

### Herramientas
- Conversor de unidades (g, ml, L, tsp, tbsp, cup, etc.)
- Escáner de código de barras (Google Code Scanner)
- Escalar recetas para diferentes porciones
- Importar recetas desde enlaces codificados

### Datos
- Backup/restore completo en JSON
- Idioma español/inglés con cambio en tiempo real
- Tema claro/oscuro con persistencia
- Recordatorios de caducidad via WorkManager

---

## Datos de ejemplo

La app incluye 15 recetas pre-cargadas al instalar por primera vez:
- Sopa de Tomate Rostizado con Albahaca
- Ensalada Caprese Rústica
- Salsa Base de Tomate y Cebolla
- Pan de Masa Madre Rústico
- Pizza Margarita Clásica
- Avena de la Abuela
- Pasta Casera al Pesto
- Tortilla Española
- Curry de Garbanzos
- Guacamole Clásico
- Delicia de Capas
- Ensalada Rústica de la Huerta
- Smoothie Verde
- Galletas de Avena y Chocolate
- Limonada de Hierbabuena

Los datos se guardan localmente en **Room (SQLite)**, sin necesidad de internet ni backend.

---

## Changelog

### v1.0 (último commit)
- **FIX**: Modo oscuro persiste al cerrar la app
- **FIX**: Botones de idioma y recordatorios ahora funcionan (root cause: UPDATE sin fila existente)
- **FIX**: Cámara no cierra la app (permission request launcher)
- **FIX**: Imágenes de galería/cámara persisten tras reiniciar (copiadas a filesDir/images/)
- **FIX**: Navegación bottom nav con 5 items (Herramientas agregado)
- **FIX**: Multi-tap en Settings ya no rompe la navegación
- **FIX**: Botón Explorar vuelve a Home desde Favoritos
- **FIX**: Botones Edit/Delete junto al título en detalle de receta
- **FIX**: Títulos ajustados (headlineMedium) para evitar bloques invisibles
- **FIX**: Panel de contenido llena toda la pantalla en detalle de receta
- **FIX**: Chips de categorías con colores (primaryContainer)
- **FIX**: Estrellas de rating actualizan al primer toque (optimistic update)
- **FIX**: Botón favoritos actualiza al primer toque (optimistic update)
- **FIX**: Feedback al guardar receta sin título (error state)
- **FIX**: Recetas sin imagen muestran iniciales
- **FIX**: Despensa vacía permite agregar ingredientes
- **FIX**: Mensajes de finalizar cocción corregidos
- **FIX**: Modismos rioplatenses eliminados (voseo → formas estándar)
- **FIX**: Keyboard bounce en despensa (adjustNothing + imePadding)
- **FIX**: Contador de items localizado

---

## Personalizar el package name

Si quieres publicar la app con otro nombre de paquete, cambia `com.kitchencabinet` por el tuyo en:
- `app/build.gradle.kts` → `applicationId` y `namespace`
- `AndroidManifest.xml` → paquete raíz
- Todos los archivos `.kt` → `package` y `import`

---

## Licencia

MIT — usa, modifica y distribuye libremente.
