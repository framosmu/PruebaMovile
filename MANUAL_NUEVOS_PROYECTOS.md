# Manual Paso a Paso: Creación y Duplicación de Nuevos Proyectos con AndroidWrapper y Omnis Studio

Este manual detalla el proceso completo para utilizar **AndroidWrapper** como una *Plantilla Maestra (Template)*, duplicarla para nuevos clientes o aplicaciones independientes, sustituir librerías de Omnis Studio (`.lbs` y `.omh`), y gestionarlo mediante Git/GitHub.

---

## 1. Introducción y Arquitectura "Plantilla Maestra"

El proyecto **AndroidWrapper** está diseñado como una *Plantilla Maestra (Template)*. Cuando vayas a crear una nueva aplicación independiente para un cliente o uso distinto (por ejemplo, *AppInventario*, *AppVentas* o *AppMantenimiento*), la mejor práctica profesional es **duplicar la plantilla en un nuevo repositorio independiente en GitHub**.

De esta forma, cada aplicación cliente tendrá su propio identificador único de Android (`applicationId`), su propia librería de Omnis Studio (`.lbs` y `.omh`), su propia configuración de servidor y sus propios canales de notificaciones Firebase, sin interferir con otros proyectos.

---

## 2. Paso 1: Duplicar la Plantilla y Crear el Nuevo Repositorio

Sigue estos comandos en la Terminal para duplicar la estructura del proyecto en una nueva carpeta local:

```bash
# 1. Copia la carpeta del proyecto a una nueva ubicación (ejemplo: AppInventario)
cp -R "/Users/framosmu/Documents/Cursos Omnis/Web_Movile_Apps/Wrappers/AndroidWrapper" "/Users/framosmu/Documents/Cursos Omnis/Web_Movile_Apps/Wrappers/AppInventario"

# 2. Entra a la nueva carpeta y reinicia el historial de Git
cd "/Users/framosmu/Documents/Cursos Omnis/Web_Movile_Apps/Wrappers/AppInventario"
rm -rf .git
git init

# 3. Vincula el nuevo repositorio de GitHub creado para esta app
git remote add origin https://github.com/framosmu/AppInventario.git
```

---

## 3. Paso 2: Sustitución de la Librería Omnis Studio (.lbs y JSON .omh)

Para reemplazar la librería de prueba por la nueva librería de tu proyecto:

### A. Sustitución del archivo binario `.lbs`
1. Ve a la carpeta `libraries/` de tu nuevo proyecto local.
2. Elimina el archivo anterior (ej: `PruebaMovile.lbs`).
3. Copia tu nuevo archivo de librería compilado de Omnis Studio (ej: `Inventario.lbs`) dentro de `libraries/`.

### B. Exportación del código fuente JSON (`.omh`)
1. En **Omnis Studio**, abre el *Studio Browser*.
2. Haz clic derecho sobre tu nueva librería (ej: `Inventario`) y selecciona **Export Lib to JSON**.
3. Guarda la carpeta exportada dentro de la carpeta `omnis_json/` de tu repositorio.
4. Gracias a la regla del archivo `.gitattributes` que ya incluye el proyecto, todos los archivos `*.omh` mantendrán automáticamente el formato de salto de línea `crlf` requerido por Omnis.

### C. Actualización del archivo `settings.xml`
Abre el archivo `OmnisWrapper/src/main/assets/settings/settings.xml` y actualiza los nombres de tu nueva librería:

```xml
<!-- Configuración de la nueva librería -->
<setting id="AppSCAFName" dataType="string" displayName="@string/appscaf">
    <value>Inventario</value>
</setting>
<setting id="OfflineFormName" dataType="string" displayName="@string/offlineformname">
    <value>FormInventario</value>
</setting>
<setting id="LocalDBName" dataType="string" displayName="@string/localdatabase">
    <value>inventario.db</value>
</setting>
```

---

## 4. Paso 3: Personalización Detallada de la App Android

### 3.1. Cómo y Dónde Cambiar el ID de la Aplicación (`applicationId`)
El **Application ID** es el identificador único de tu aplicación en Android y en Google Play Store (ej: `net.omnis.appinventario`).
* **Ubicación del archivo:** `OmnisWrapper/build.gradle`
* **Línea exacta a modificar:** Dentro del bloque `defaultConfig { ... }`, busca la línea:
  ```groovy
  defaultConfig {
      applicationId "net.omnis.androidwrapper" // <-- Cambiar esta cadena
      ...
  }
  ```
* **Cambio a realizar:** Sustituye la cadena por tu nuevo identificador único (ej: `applicationId "net.omnis.appinventario"`).
* **Sincronización:** Al guardar el archivo en Android Studio, haz clic en la barra amarilla superior que dice **Sync Now** (o el icono del elefante).

---

### 3.2. Cómo y Dónde Cambiar el Nombre Visible de la App (`app_name`)
Es el nombre que se muestra debajo del icono en el escritorio del teléfono:
* **Archivos a modificar:**
  1. En español: `OmnisWrapper/src/main/res/values-es-rES/strings.xml`
  2. En inglés: `OmnisWrapper/src/main/res/values/strings.xml`
* **Línea exacta:** `<string name="app_name">App Inventario</string>`

---

### 3.3. Configuración de Firebase y Notificaciones Push
1. Ve a la consola de Firebase (`console.firebase.google.com`) y crea un proyecto para la nueva app.
2. Añade una aplicación Android indicando exactamente el mismo `applicationId` configurado en el paso 3.1 (`net.omnis.appinventario`).
3. Descarga el archivo `google-services.json` generado por Firebase.
4. **Reemplaza el archivo en la ruta:** `OmnisWrapper/google-services.json`.

---

### 3.4. Reemplazar Logotipo e Iconos Corporativos
Para cambiar el logotipo por el de tu nuevo cliente, reemplaza las imágenes en las carpetas de recursos `OmnisWrapper/src/main/res/drawable-*/icon.png` y `splash_icon.png`.

---

## 5. Paso 4: Cómo Abrir e Importar el Proyecto desde GitHub

Si tú u otro desarrollador descargan el repositorio desde GitHub para continuar trabajando:

### Opción A: Abrir directamente la librería `.lbs`
1. Clona el repositorio con `git clone https://github.com/framosmu/AppInventario.git`.
2. En Omnis Studio, ve a **File > Open Library** y abre el archivo `libraries/Inventario.lbs`.

### Opción B: Reconstruir/Importar la librería desde los archivos JSON (`.omh`)
1. En Omnis Studio, abre el *Studio Browser*.
2. Selecciona la opción **Create Library from JSON** (o *Rebuild from JSON*).
3. Selecciona la carpeta `omnis_json/Inventario`. Omnis Studio reconstruirá automáticamente el archivo `.lbs` a partir del código fuente guardado en Git.

---

> **Nota importante:** Cada vez que realices modificaciones en el código de tu librería dentro de Omnis Studio, recuerda hacer clic derecho en el *Studio Browser* y elegir **Update JSON tree** antes de hacer el Commit/Push a GitHub para mantener el código fuente totalmente sincronizado.
