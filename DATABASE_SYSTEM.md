# Arquitectura del Sistema de Almacenamiento de Datos

> **ESTADO DE IMPLEMENTACIÓN:**
> - **YAML (`YamlStorage`):** ¡Completo y funcional!
> - **SQLite (`SqliteStorage`):** ¡Completo y funcional!
> - **MySQL (`MySqlStorage`):** ¡Completo y funcional!

Este documento detalla la arquitectura del sistema de persistencia de datos del plugin, que ha sido diseñado para ser modular, flexible y extensible.

## 1. Patrón de Diseño: Strategy

El núcleo del sistema se basa en el **Patrón de Diseño Strategy**. Este patrón nos permite definir una familia de algoritmos (en nuestro caso, diferentes formas de guardar y leer datos), encapsular cada uno de ellos y hacerlos intercambiables.

-   **Contexto (`AnforaDataManager`, `PlayerDataManager`):** Las clases que necesitan guardar datos. No saben *cómo* se guardan los datos, solo que tienen un objeto que puede hacerlo.
-   **Estrategia (`StorageEngine`):** Una interfaz que define un conjunto común de operaciones para todas las estrategias (e.g., `savePlayerData`, `loadAnfora`).
-   **Estrategias Concretas (`YamlStorage`, `MySqlStorage`, `SqliteStorage`):** Clases que implementan la interfaz `StorageEngine`. Cada una contiene la lógica específica para un método de almacenamiento.

Gracias a este patrón, el plugin puede cambiar de YAML a MySQL con solo modificar una línea en `config.yml`, sin necesidad de alterar el código de lógica de negocio.

## 2. Flujo de Inicialización

El sistema se inicializa en la clase `anforaMain` siguiendo un orden estricto para asegurar que las dependencias se resuelvan correctamente:

1.  **`anforaMain` lee `config.yml`**: Carga la configuración principal.
2.  **`anforaMain` crea `DatabaseManager`**: `DatabaseManager` lee la clave `database-type` de `config.yml` para decidir qué motor de base de datos se debe usar (`DatabaseType` enum).
3.  **`anforaMain` crea `DataStorageProvider`**: Se le pasa la instancia de `DatabaseManager`. `DataStorageProvider` actúa como una fábrica que, basándose en el `DatabaseType`, crea una instancia de la estrategia concreta (`new YamlStorage()`, `new MySqlStorage()`, etc.).
4.  **`anforaMain` obtiene el `StorageEngine`**: Llama a `dataStorageProvider.getActiveStorage()` para obtener el motor de almacenamiento activo.
5.  **`anforaMain` crea los Data Managers**: Crea `PlayerDataManager` y `AnforaDataManager`, inyectándoles la instancia del `StorageEngine` en su constructor.

A partir de este punto, si `PlayerDataManager` necesita guardar datos, simplemente llama a `storage.savePlayerData(...)`, y el patrón Strategy se encarga de dirigir esa llamada a la implementación correcta (YAML, MySQL, etc.).

## 3. Diagrama de Clases (Simplificado)

```
[anforaMain]
     |
     | 1. crea
     v
[DatabaseManager]
(determina tipo)
     |
     | 2. usado por
     v
[DataStorageProvider] --(crea)--> [StorageEngine] (Interfaz)
     |
     | 3. obtiene                        ^ (implementan)
     v                                   |
[StorageEngine] (instancia)     /--------|--------\
     |
     | 4. inyectado en      v          v          v
     |                   [YamlStorage] [MySqlStorage] [SqliteStorage]
     |
     \----------------------> [PlayerDataManager]
     |                        (usa StorageEngine)
     |
     \----------------------> [AnforaDataManager]
                              (usa StorageEngine)
```

## 4. Componentes Clave

-   **`anforaMain`**: Orquestador. Inicia y conecta todos los componentes.
-   **`DatabaseManager`**: El "cerebro" que decide *qué* estrategia usar.
-   **`DataStorageProvider`**: La "fábrica" que construye la estrategia elegida.
-   **`StorageEngine`**: La "interfaz común" o "contrato" que todas las estrategias deben cumplir. Es la clave del desacoplamiento.
-   **`YamlStorage`, `MySqlStorage`, `SqliteStorage`**: Los "trabajadores". Cada uno sabe hacer su trabajo específico.
-   **`PlayerDataManager`, `AnforaDataManager`**: Los "clientes" o "consumidores". Usan el sistema de almacenamiento para realizar su trabajo, sin preocuparse por los detalles de implementación.

## 5. Cómo Extender el Sistema (Ej: Añadir PostgreSQL)

La arquitectura actual hace que añadir un nuevo tipo de almacenamiento sea muy sencillo:

1.  **Crear la Estrategia Concreta:**
    -   Crea una nueva clase `PostgreSqlStorage.java` en el paquete `data`.
    -   Haz que implemente la interfaz `StorageEngine`: `public class PostgreSqlStorage implements StorageEngine { ... }`.
    -   Implementa todos los métodos requeridos (`savePlayerData`, `loadPlayerData`, etc.) con la lógica específica para PostgreSQL.

2.  **Actualizar el `DatabaseManager`:**
    -   Añade `POSTGRESQL` al enum `DatabaseType`.
    -   Añade un nuevo `case "postgresql":` en el `switch` del método `loadDatabaseType`.

3.  **Actualizar el `DataStorageProvider`:**
    -   Añade un nuevo `case POSTGRESQL:` en el `switch` del constructor.
    -   Dentro del `case`, instancia tu nueva clase: `this.activeStorage = new PostgreSqlStorage(...);`.

4.  **Actualizar la Configuración:**
    -   Añade la opción `postgresql` a los comentarios de `config.yml`.
    -   Añade una nueva sección `postgresql:` en `databases.yml` con los campos de conexión necesarios (host, puerto, etc.).

¡Y eso es todo! El resto del plugin podrá usar PostgreSQL sin necesidad de ninguna modificación adicional.

## 6. Sistema de Failover (Respaldo Automático)

Para aumentar la robustez del plugin, se ha implementado un sistema de respaldo automático en el `DataStorageProvider`.

### 6.1. Propósito

El objetivo es garantizar que el plugin siga siendo funcional incluso si la base de datos principal configurada (MySQL o SQLite) no puede iniciarse debido a un error de conexión, configuración incorrecta o archivos corruptos.

### 6.2. Mecanismo

1.  **Intento de Conexión Primaria:** El sistema intenta inicializar el motor de almacenamiento SQL (`MySqlStorage` o `SqliteStorage`) según lo especificado en `config.yml`.
2.  **Detección de Errores:** Las clases de almacenamiento SQL están diseñadas para lanzar una `SQLException` si fallan durante su inicialización (ej: no se pueden conectar a la base de datos).
3.  **Captura y Fallback:** El `DataStorageProvider` envuelve la creación de estos motores en un bloque `try-catch`.
    -   Si se captura una `SQLException`, el sistema sabe que la base de datos primaria ha fallado.
4.  **Notificación y Respaldo:**
    -   Se registra un **mensaje de error de nivel `SEVERE`** en la consola. Este mensaje informa al administrador del servidor sobre el fallo de la base de datos primaria y le notifica que se procederá a usar YAML.
    -   Inmediatamente después, el sistema crea una instancia de `YamlStorage` y la establece como el motor de almacenamiento activo para la sesión actual.

### 6.3. Beneficios

-   **Continuidad del Servicio:** El plugin puede seguir funcionando, leyendo y guardando datos (en formato YAML) en lugar de desactivarse o causar errores durante el juego.
-   **Prevención de Pérdida de Datos:** Los datos de la sesión actual no se pierden, ya que se guardan en el archivo YAML de respaldo.
-   **Diagnóstico Claro:** El administrador del servidor recibe un mensaje claro y detallado que le permite diagnosticar y solucionar el problema con la base de datos principal sin que el servidor se vea interrumpido.
