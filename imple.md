# Sistema de Guardado/Carga de Partida con Gson

## Descripción

Implementar un sistema de guardado y carga de partida que serialice el estado completo del juego (jugador, enemigo, misión activa) a un archivo JSON usando la librería **Gson**. Se añadirán botones "Guardar Partida 💾" y "Cargar Partida 📂" a la interfaz gráfica.

## Retos Técnicos

> [!IMPORTANT]
> **Polimorfismo con Gson**: El proyecto usa herencia extensiva (`Personaje` → `Guerrero`/`Mago`/`Arquero`/`Luchador`, `Items` → `ItemEquipable`, `Mision` → `MisionCombate`/`MisionSobrevivir`/`MisionEliminarBoss`). Gson por defecto **no preserva el tipo concreto** al serializar. Se necesita un `RuntimeTypeAdapterFactory` o un adaptador custom para cada jerarquía polimórfica.

> [!IMPORTANT]
> **Dependencia Gson**: El proyecto actualmente NO tiene Gson en el classpath. Se necesita descargar el JAR y agregarlo al `.classpath` de Eclipse, además de declarar `requires com.google.gson;` en `module-info.java`.

## Propuesta de Cambios

### 1. Dependencia: Agregar Gson al proyecto

#### [MODIFY] [.classpath](file:///c:/Users/ronal/Desktop/RPG_Journey/.classpath)
- Agregar una entrada `<classpathentry kind="lib" ...>` apuntando al JAR de Gson descargado en `lib/gson-2.11.0.jar`.

#### [NEW] `lib/gson-2.11.0.jar`
- Descargar el JAR de Gson desde Maven Central y colocarlo en una carpeta `lib/` en la raíz del proyecto.

#### [MODIFY] [module-info.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/module-info.java)
- Agregar `requires com.google.gson;` al módulo.

---

### 2. Modelo de datos para serialización (DTO)

#### [NEW] [EstadoPartida.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/com/rpg/persistencia/EstadoPartida.java)
Clase DTO (Data Transfer Object) que encapsula todo el estado serializable de la partida:
- `tipoJugador` (String): clase concreta del jugador ("Guerrero", "Mago", etc.)
- `jugador` (Personaje): el personaje del jugador con todos sus atributos
- `tipoEnemigo` (String): "Enemigo" o "JefeDragon"
- `enemigo` (Personaje): el enemigo actual
- `tipoMision` (String): tipo de misión activa
- `mision` (Mision): datos de la misión activa
- `fechaGuardado` (String): timestamp del guardado

---

### 3. Adaptadores de tipo para Gson (manejo de polimorfismo)

#### [NEW] [AdaptadorPersonaje.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/com/rpg/persistencia/AdaptadorPersonaje.java)
`JsonSerializer<Personaje>` + `JsonDeserializer<Personaje>` custom que:
- Al serializar: incluye un campo `"tipo"` con la clase concreta y todos los campos del personaje.
- Al deserializar: lee `"tipo"` para instanciar la clase correcta (`Guerrero`, `Mago`, `Arquero`, `Luchador`, `Enemigo`, `JefeDragon`) y restaurar los campos internos (incluyendo `mana`/`manaMax` del Mago, `cooldown` del Guerrero/Arquero/Luchador, etc.).

#### [NEW] [AdaptadorItems.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/com/rpg/persistencia/AdaptadorItems.java)
`JsonSerializer<Items>` + `JsonDeserializer<Items>` para distinguir entre `Items` y `ItemEquipable`.

#### [NEW] [AdaptadorMision.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/com/rpg/persistencia/AdaptadorMision.java)
`JsonSerializer<Mision>` + `JsonDeserializer<Mision>` para distinguir entre `MisionCombate`, `MisionSobrevivir` y `MisionEliminarBoss`.

---

### 4. Servicio de persistencia

#### [NEW] [ServicioGuardado.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/com/rpg/persistencia/ServicioGuardado.java)
Clase que centraliza la lógica de guardado/carga:
- `guardarPartida(Personaje jugador, Enemigo enemigo, Mision mision, String rutaArchivo)`: serializa el estado a JSON y lo escribe en disco.
- `cargarPartida(String rutaArchivo)`: lee el JSON y devuelve un `EstadoPartida` reconstruido.
- Configura el `Gson` con los adaptadores custom y `setPrettyPrinting()`.

---

### 5. Integración con el Controlador

#### [MODIFY] [ControladorCombate.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/com/rpg/controlador/ControladorCombate.java)
- Agregar métodos `guardarPartida(String ruta)` y `cargarPartida(String ruta)`.
- `guardarPartida` delega al `ServicioGuardado` y notifica a la vista.
- `cargarPartida` usa `ServicioGuardado`, reemplaza el estado interno (jugador, enemigo, misión) con los datos cargados, y actualiza la UI.

---

### 6. Integración con la Vista (Botones)

#### [MODIFY] [VentanaJuego.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/com/rpg/gui/VentanaJuego.java)
- Agregar dos botones: `btnGuardar` ("Guardar 💾") y `btnCargar` ("Cargar 📂").
- Expandir el `GridLayout` del panel de botones de `(1, 6, ...)` a `(1, 8, ...)`.
- `btnGuardar`: abre un `JFileChooser` en modo "guardar" con filtro `.json`, luego llama `controlador.guardarPartida(ruta)`.
- `btnCargar`: abre un `JFileChooser` en modo "abrir" con filtro `.json`, luego llama `controlador.cargarPartida(ruta)`.

---

### 7. Setters necesarios para reconstrucción

Para que Gson pueda reconstruir los objetos, se necesitan algunos setters o constructores sin argumentos en las clases del modelo. Se agregarán de forma mínima:

#### [MODIFY] [Personaje.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/com/rpg/logica/personaje/Personaje.java)
- Agregar constructor protegido sin argumentos (para Gson).

#### [MODIFY] [Mision.java](file:///c:/Users/ronal/Desktop/RPG_Journey/src/com/rpg/mision/Mision.java)
- Los campos `nombre` y `descripcion` son `final`. El adaptador custom reconstruirá las misiones usando sus constructores existentes, así que no se modifica esta clase.

## Open Questions

> [!IMPORTANT]
> **Ubicación del archivo de guardado**: ¿Prefieres que el guardado se haga siempre en una ruta fija (ej: `saves/partida.json` dentro de la carpeta del proyecto), o prefieres que se abra un `JFileChooser` para que el usuario elija dónde guardar/cargar? **Propuesta actual: JFileChooser** para máxima flexibilidad.

## Plan de Verificación

### Verificación Manual
1. Iniciar el juego, subir de nivel, equipar ítems, avanzar misión.
2. Presionar "Guardar 💾" → verificar que se genera un archivo `.json` con el estado completo.
3. Cerrar y reabrir el juego.
4. Presionar "Cargar 📂" → verificar que el estado se restaura correctamente (nivel, HP, inventario, equipamiento, misión).
5. Verificar que cada clase de personaje (Guerrero, Mago, Arquero, Luchador) se serializa/deserializa correctamente.
