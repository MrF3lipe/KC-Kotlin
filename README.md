# Kitchen Cabinet — Kotlin (Jetpack Compose)

App Android nativa para gestión de recetas, despensa y lista de la compra.  
Construida con **Kotlin + Jetpack Compose + Room**.

---

## Pantallas

| Pantalla | Descripción |
|----------|-------------|
| **Home** | Lista de recetas con filtros por categoría |
| **Detalle** | Ingredientes, pasos y botón Cocinar |
| **Añadir/Editar** | Formulario completo de receta |
| **Favoritos** | Recetas marcadas con ♥ |
| **Despensa** | Inventario con cantidades y fechas de caducidad |
| **Compras** | Lista de la compra con checkboxes |
| **Buscar** | Búsqueda por título y descripción |
| **Cocinar** | Paso a paso con pantalla siempre encendida |

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
│       │   ├── viewmodel/           ← ViewModels (Recipe, Pantry, Shopping)
│       │   └── ui/
│       │       ├── theme/           ← Colores, tipografía, tema Material3
│       │       ├── components/      ← RecipeCard, BottomNavBar
│       │       └── screens/         ← 8 pantallas Compose
│       └── res/                     ← Recursos, iconos, temas
├── gradle/
│   ├── libs.versions.toml           ← Version catalog centralizado
│   └── wrapper/gradle-wrapper.properties
├── COMPILAR-LOCAL.bat               ← Compilar en Windows
├── INSTALAR-APK.bat                 ← Instalar en dispositivo
└── README.md
```

---

## Datos de ejemplo

La app incluye 5 recetas pre-cargadas al instalar por primera vez:
- Spaghetti Carbonara
- Chicken Stir Fry
- Chocolate Brownie
- Greek Salad
- Vegetable Soup

Los datos se guardan localmente en **Room (SQLite)**, sin necesidad de internet ni backend.

---

## Personalizar el package name

Si quieres publicar la app con otro nombre de paquete, cambia `com.kitchencabinet` por el tuyo en:
- `app/build.gradle.kts` → `applicationId` y `namespace`
- `AndroidManifest.xml` → paquete raíz
- Todos los archivos `.kt` → `package` y `import`

---

## Licencia

MIT — usa, modifica y distribuye libremente.
