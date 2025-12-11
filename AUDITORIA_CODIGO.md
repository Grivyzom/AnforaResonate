### Auditoría del Código de AnforaResonante

**Resumen General:**
El problema más grave y urgente de tu complemento es un **fallo de diseño arquitectónico**: todas las operaciones de guardado y carga de datos (a la base de datos o a los archivos YAML) se realizan de forma **síncrona**. Esto significa que bloquean el hilo principal del servidor, lo que inevitablemente causará **lag y falta de respuesta** a medida que más jugadores usen el complemento.

---

### Hallazgos Detallados:

**1. Fallo Arquitectónico Crítico: Operaciones de Datos Síncronas**
- **Descripción:** Clases como `AnforaDataManager` y `PlayerDataManager` llaman directamente a los métodos de guardado/carga (`saveAnfora`, `loadAnfora`, etc.) desde los `listeners` de eventos y los comandos. El servidor de Minecraft tiene un único hilo principal para gestionar todas las acciones importantes (movimiento de jugadores, ejecución de comandos, etc.). Cuando realizas una operación de I/O (entrada/salida) en este hilo, el servidor se congela hasta que la operación termina.
- **Ubicación:** `src/main/java/gc/grivyzom/AnforaXP/data/AnforaDataManager.java`, `src/main/java/gc/grivyzom/AnforaXP/data/PlayerDataManager.java`
- **Impacto:** Muy alto. Causa de lag severo y puede hacer que el servidor se bloquee o se caiga.

**2. Implementación de `YamlStorage` Ineficiente**
- **Descripción:** El sistema de guardado en YAML (`YamlStorage`) reescribe el archivo completo (`player-data.yml` o `anfora-data.yml`) cada vez que se modifica un solo dato. Las operaciones de disco son lentas, y hacer esto constantemente para cada cambio (por ejemplo, cada vez que un jugador gana XP para el ánfora) es extremadamente ineficiente.
- **Ubicación:** `src/main/java/gc/grivyzom/AnforaXP/data/YamlStorage.java`
- **Impacto:** Alto. Contribuye significativamente al lag, especialmente si se usa como sistema de guardado principal.

**3. Fallos en `SqliteStorage`**
- **Descripción:** La implementación de SQLite tiene dos problemas importantes:
    1.  **No es segura para hilos (thread-safe):** Utiliza un único objeto de `Connection` para todas las operaciones. Si varias operaciones ocurren al mismo tiempo, esto puede llevar a la **corrupción de la base de datos**.
    2.  **Bajo rendimiento:** Las tablas de la base de datos se crean sin **índices**. Sin índices en columnas como `ownerUUID` o `uniqueId`, las búsquedas en la base de datos serán muy lentas a medida que la cantidad de datos crezca.
- **Ubicación:** `src/main/java/gc/grivyzom/AnforaXP/data/SqliteStorage.java`
- **Impacto:** Medio-Alto. Riesgo de corrupción de datos y rendimiento deficiente.

**4. Buen Ejemplo: `MariaDbStorage`**
- **Descripción:** En contraste, la implementación de `MariaDbStorage` es de alta calidad. Utiliza un *pool* de conexiones (HikariCP) para gestionar las conexiones de forma eficiente y segura, usa `PreparedStatement` para prevenir inyección SQL y crea los índices necesarios para un buen rendimiento.
- **Ubicación:** `src/main/java/gc/grivyzom/AnforaXP/data/MariaDbStorage.java`
- **Impacto:** Positivo. Sirve como un excelente modelo para corregir las otras implementaciones.

---

### Recomendaciones:

Para solucionar estos problemas, te recomiendo los siguientes pasos, en orden de prioridad:

**1. Implementar Operaciones Asíncronas:**
- **Acción:** Refactorizar `AnforaDataManager` y `PlayerDataManager`. Todas las llamadas a los métodos del `StorageEngine` (`storage.save...`, `storage.load...`) deben envolverse en una tarea asíncrona.
- **Ejemplo de Código:**
  ```java
  public void saveAnfora(AnforaData anfora) {
      // Guardar en caché localmente (síncrono)
      anforasByUUID.put(anfora.getUniqueId(), anfora);
      // Guardar en la base de datos (asíncrono)
      Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
          storage.saveAnfora(anfora);
      });
  }
  ```

**2. Mejorar `YamlStorage`:**
- **Acción:** Modificar los métodos `save...` en `YamlStorage` para que solo guarden los cambios en la memoria (en el objeto `YamlConfiguration`). La escritura real al disco debe ser gestionada únicamente por la tarea `AutoSaveTask`, que puede guardar todos los cambios pendientes cada ciertos minutos.

**3. Corregir `SqliteStorage`:**
- **Acción:**
    1.  Reemplazar la conexión única con un *pool* de conexiones (HikariCP, que ya está en tus dependencias).
    2.  Añadir sentencias `CREATE INDEX` en el método `createTables` para las columnas que se usan en búsquedas (`ownerUUID`, `uniqueId`).

---
---

### Segunda Auditoría (10-12-2025) - Post-Refactorización

**Resumen General:**
La primera fase de refactorización fue un éxito parcial. Se corrigieron los problemas más graves relacionados con la escritura de datos (haciéndola asíncrona) y la ineficiencia de los sistemas de almacenamiento. Sin embargo, la auditoría revela que **la carga de datos sigue siendo completamente síncrona**, lo que representa el siguiente cuello de botella crítico para el rendimiento del servidor.

---

### Hallazgos Detallados (Segunda Auditoría):

**1. Carga Síncrona de Datos en `PlayerJoinEvent`**
- **Descripción:** El problema más urgente ahora reside en `PlayerConnectionListener`. Cuando un jugador entra al servidor, el `listener` llama a `playerDataManager.loadPlayer()` y `anforaDataManager.getAnforasByOwner()` directamente en el hilo principal. Estas operaciones leen de la base de datos o de archivos, bloqueando el servidor y causando un pico de lag visible cada vez que un jugador se conecta.
- **Ubicación:** `src/main/java/gc/grivyzom/AnforaXP/listeners/PlayerConnectionListener.java`
- **Impacto:** Alto. Causa de lag notable en el servidor durante el inicio de sesión de los jugadores.

**2. Operaciones de Lectura Síncronas en toda la Capa de Datos**
- **Descripción:** El problema es sistémico. Todos los métodos que leen o consultan datos en `AnforaDataManager` y `PlayerDataManager` son bloqueantes (síncronos). Aunque has dejado comentarios indicando que eres consciente de ello, es crucial solucionarlo. La solución moderna y recomendada es usar `CompletableFuture` para manejar los resultados de las operaciones asíncronas.
- **Ubicación:** `src/main/java/gc/grivyzom/AnforaXP/data/AnforaDataManager.java`, `src/main/java/gc/grivyzom/AnforaXP/data/PlayerDataManager.java`
- **Impacto:** Alto. Cualquier llamada a estos métodos desde el hilo principal puede causar lag si los datos no están en caché.

**3. Búsqueda de Ánforas por Ubicación Ineficiente**
- **Descripción:** El método `AnforaDataManager.getAnforaIdByLocation` es muy ineficiente. Itera sobre todas las ánforas cargadas para encontrar una que coincida con la ubicación. Esto se vuelve progresivamente más lento a medida que se añaden más ánforas.
- **Ubicación:** `src/main/java/gc/grivyzom/AnforaXP/data/AnforaDataManager.java`
- **Impacto:** Medio. Puede causar picos de lag en operaciones específicas como romper o interactuar con ánforas.

---

### Recomendaciones (Segunda Auditoría):

**1. Refactorizar la Carga de Datos para que sea Asíncrona:**
- **Acción:** Es la tarea más compleja pero más importante. Los métodos de carga (`loadPlayer`, `getAnforasByOwner`, etc.) deben ser modificados para devolver un `CompletableFuture`. El código que los llama (como `PlayerConnectionListener`) deberá ser adaptado para manejar esta asincronía.
- **Ejemplo de Código (conceptual):**
  ```java
  // En PlayerDataManager.java
  public CompletableFuture<PlayerData> loadPlayerAsync(UUID uuid) {
      return CompletableFuture.supplyAsync(() -> {
          return storage.loadPlayerData(uuid);
      });
  }

  // En PlayerConnectionListener.java
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
      Player player = event.getPlayer();
      playerDataManager.loadPlayerAsync(player.getUniqueId()).thenAccept(playerData -> {
          // Este código se ejecuta cuando los datos están listos
          // Es importante asegurarse de que si se modifica algo de Bukkit,
          // se haga en el hilo principal.
          Bukkit.getScheduler().runTask(plugin, () -> {
              player.sendMessage("¡Tus datos han sido cargados!");
          });
      });
  }
  ```

**2. Optimizar la Búsqueda por Ubicación:**
- **Acción:** Añadir un nuevo método `getAnforaByLocation(Location location)` a la interfaz `StorageEngine`. Cada implementación (`MariaDbStorage`, `SqliteStorage`, `YamlStorage`) deberá implementarlo de forma optimizada (con una consulta SQL `WHERE` para las bases de datos, por ejemplo).