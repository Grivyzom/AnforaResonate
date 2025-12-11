# Anfora Resonante

Anfora Resonante es un plugin de Spigot que introduce un nuevo bloque personalizado, el "Ánfora Resonante", diseñado para funcionar como un banco de experiencia (XP). Los jugadores pueden colocar estas ánforas para almacenar su XP de forma segura. El plugin cuenta con un sistema de almacenamiento de datos robusto y flexible, compatible con YAML, SQLite y MySQL, e incluye un sistema de failover para garantizar la máxima disponibilidad.

## ✨ Características Principales

-   **Banco de Experiencia:** La función principal es permitir a los jugadores almacenar su experiencia en un bloque físico en el mundo.
-   **Ítem Personalizado y Configurable:** El "Anfora Resonante" es un ítem único cuya apariencia (nombre, descripción y efecto de brillo) es totalmente personalizable por los administradores a través de `config.yml`.
-   **Persistencia de Datos en el Ítem:** Cuando un ánfora se rompe, el ítem resultante **conserva el nivel, la experiencia y el nombre del propietario** que contenía, permitiendo a los jugadores mover sus bancos de XP sin perder datos.
-   **Sistema de Propietarios:** Cada ánfora colocada pertenece a un jugador. **Solo el propietario puede romper y recuperar su ánfora**. Cualquier intento de romper un ánfora ajena es cancelado, protegiendo la experiencia de los jugadores.
-   **Protección contra Explosiones:** Las ánforas no son destruidas por explosiones (ej. Creepers, TNT). En su lugar, se sueltan como un ítem, conservando toda su experiencia y datos.
-   **Sistema Anti-Duplicación:** Incluye un sistema de seguimiento de UUIDs para prevenir la duplicación de ánforas, garantizando que cada ánfora sea única en el servidor.
-   **Interacción con Ánforas:**
    -   **Shift + Clic Derecho:** Permite al propietario **extraer toda la experiencia** almacenada en el ánfora y transferirla a su barra de experiencia.
    -   **Shift + Clic Izquierdo:** Permite al propietario **depositar toda su experiencia** en el ánfora.
    -   **Clic Derecho (sin Shift):** Abre una interfaz gráfica (GUI) que permite gestionar la experiencia del ánfora, mejorarla y ver su información.
-   **Almacenamiento Flexible:** Soporte para múltiples backends de base de datos:
    -   **YAML:** Ideal para servidores pequeños o para empezar.
    -   **SQLite:** Almacenamiento en un único archivo, perfecto para la mayoría de servidores.
    -   **MySQL:** Soporte para bases de datos externas, ideal para redes de servidores o cargas pesadas.
-   **Failover Automático:** Si por alguna razón el plugin no puede conectarse a la base de datos SQL (SQLite o MySQL) configurada, cambiará automáticamente a YAML para esa sesión, evitando la pérdida de datos y manteniendo el plugin funcional.
-   **Comandos Intuitivos:** Incluye un sistema de ayuda y autocompletado para facilitar su uso a los administradores.

## ⌨️ Comandos

A continuación se detallan los comandos disponibles y sus respectivos permisos.

| Comando                                       | Descripción                                                                 | Permiso                         |
| --------------------------------------------- | --------------------------------------------------------------------------- | ------------------------------- |
| `/anfora help`                                | Muestra todos los comandos disponibles para el jugador.                     | `anforaresonante.player.help`   |
| `/anfora list`                                | Muestra la ubicación de las ánforas que el jugador tiene colocadas.         | `anforaresonante.player.list`   |
| `/anfora give <jugador> [cant] [nivel]`       | Entrega a un jugador una cantidad de ánforas del nivel especificado.        | `anforaresonante.admin.give`    |
| `/anfora setlevel <nivel> [uuid]`             | Establece el nivel de un ánfora. Si no se provee UUID, afecta a la de la mano. | `anforaresonante.admin.setlevel`|
| `/anfora setexp <xp> [uuid]`                  | Establece la experiencia de un ánfora. Si no se provee UUID, afecta a la de la mano. | `anforaresonante.admin.setxp` |
| `/anfora reload`                              | Recarga los archivos de configuración del plugin.                           | `anforaresonante.admin.reload`  |

El sistema de autocompletado (Tab) sugiere subcomandos, nombres de jugadores, cantidades y niveles.

##  Placeholders

### Placeholders Internos

Estos placeholders pueden ser usados en el archivo `messages.yml` para mostrar información dinámica.

| Placeholder          | Descripción                                             |
| -------------------- | ------------------------------------------------------- |
| `{player}`           | Muestra el nombre del jugador objetivo.                 |
| `{owner}`            | Muestra el nombre del propietario de un ánfora.         |
| `{amount}`           | La cantidad de ítems o experiencia.                     |
| `{anfora_count}`     | El número de ánforas.                                   |
| `{xp}`               | La cantidad de puntos de experiencia.                   |
| `{command}`          | El nombre de un comando.                                |
| `{description}`      | La descripción de un comando.                           |
| `{number}`           | Un número genérico usado en mensajes de error.          |
| `{level}`            | El nivel de un ánfora.                                  |
| `{world}`            | El nombre del mundo donde se encuentra un ánfora.       |
| `{x}`, `{y}`, `{z}`  | Las coordenadas de un ánfora.                           |
| `{current_capacity}` | La experiencia actual almacenada en un ánfora.          |
| `{max_capacity}`     | La capacidad máxima de experiencia de un ánfora.        |
| `{exp_amount}`       | Una cantidad específica de experiencia.                 |
| `{uuid}`             | El Identificador Único de un ánfora.                    |
| `{max_level}`        | El nivel máximo que un ánfora puede alcanzar.           |

### Placeholders de PlaceholderAPI

Si tienes PlaceholderAPI instalado, puedes usar los siguientes placeholders en cualquier otro plugin compatible (ej. scoreboards, chat).

**Placeholders Generales del Jugador:**

| Placeholder              | Descripción                                                        |
| ------------------------ | ------------------------------------------------------------------ |
| `%anfora_count%`         | Muestra el número total de ánforas que posee el jugador.           |
| `%anfora_total_exp%`     | Muestra la suma de la experiencia en TODAS las ánforas del jugador.|
| `%anfora_nearest_ubi%`   | Muestra las coordenadas del ánfora más cercana al jugador.         |

**Placeholders Específicos:**

| Placeholder                | Descripción                                                 |
| -------------------------- | ----------------------------------------------------------- |
| `%anfora_owner_<uuid>%`    | Muestra el nombre del propietario de un ánfora específica usando su UUID (el que se ve en el ítem). |

**Placeholders por ID Local (Avanzado):**

Estos placeholders requieren un ID numérico local del ánfora, que corresponde al orden en que fueron creadas por un jugador. Su uso es complejo y está pensado para configuraciones avanzadas.

| Placeholder                        | Descripción                                                       |
| ---------------------------------- | ----------------------------------------------------------------- |
| `%anfora_exp_<id>`                 | Experiencia del ánfora con el ID local especificado.              |
| `%anfora_maxexp_<id>`              | Experiencia máxima del ánfora con el ID local especificado.       |
| `%anfora_exppercentage_<id>`       | Porcentaje de experiencia del ánfora con el ID local especificado.|
| `%anfora_expleft_<id>`             | Experiencia restante para el siguiente nivel.                     |
| `%anfora_ubi_<id>`                 | Ubicación del ánfora con el ID local especificado.                |
| `%anfora_nivel_<id>`               | Nivel del ánfora con el ID local especificado.                    |

## 🖥️ Interfaz Gráfica (GUI)

Al hacer clic derecho en un ánfora, se abre una interfaz de 3 filas con la siguiente disposición:

-   **Fila Superior:**
    -   En el centro, una **Estrella del Nether** permite al jugador mejorar el ánfora (función futura).

-   **Fila Central:**
    -   A la izquierda, tres tintes rojos para **depositar 1, 5 o 10 niveles** de experiencia.
    -   En el centro, una **Perla de Ender** muestra la experiencia actual y el nivel del ánfora.
    -   A la derecha, tres tintes verdes para **retirar 1, 5 o 10 niveles** de experiencia.

-   **Fila Inferior:**
    -   En el centro, un **Libro** muestra información detallada sobre el ánfora, como el nombre del propietario, el nivel y la capacidad.

## 🔊 Efectos de Sonido

-   **Abrir Ánfora:** Al abrir la GUI del ánfora, se reproduce el sonido de un huevo de gallina.
-   **Clic en GUI:** Al hacer clic en los botones de depósito o retiro, se reproduce un sonido de clic.

## ⚙️ Configuración (`config.yml`)

El archivo `config.yml` te permite personalizar el plugin.

### Selección de Base de Datos
```yaml
# Motor de base de datos disponible:
# - yaml (por defecto)
# - sqlite
# - mysql
database-type: yaml
```

### Personalización del Ítem "Anfora Resonante"
```yaml
# --- Configuración del Ítem Anfora Resonante ---
anfora-item:
  # Nombre del ítem. Acepta códigos de color con '&'.
  name: "&6Anfora Resonante"
  
  # Descripción del ítem. Se muestra debajo del nombre.
  lore:
    - "&eContiene la esencia de la experiencia."
    - "&7Colócala en el suelo para usarla."
    
  # Si es true, el ítem brillará como si estuviera encantado.
  hide-enchant: true
  
  # El encantamiento que causa el brillo (si hide-enchant es true).
  enchantment: "UNBREAKING"
  
  # El nivel de dicho encantamiento.
  enchantment-level: 1
```

## 📝 Para Desarrolladores

-   **Arquitectura:** El plugin utiliza el patrón de diseño **Strategy** para cambiar fácilmente entre los diferentes motores de almacenamiento (`StorageEngine`).
-   **Identificación de Ítems:** Los ítems personalizados se identifican mediante `PersistentDataContainer` (PDC). La clave principal es `anfora_resonante: "true"`.
-   **Unicidad de Ítems:** Cada ánfora tiene un `UUID` único (`anfora_unique_id`) que persiste a través de su ciclo de vida (ítem -> bloque -> ítem) para prevenir la duplicación. El nivel, la experiencia y el nombre del propietario también se guardan en el PDC del ítem (`anfora_level`, `anfora_experience`, `anfora_owner_name`).
-   **Gestor de UUIDs (`AnforaUUIDManager`):** Se ha implementado un gestor central que mantiene un registro de todas las ánforas colocadas en el mundo. Este sistema previene la colocación de ánforas duplicadas al verificar el `UUID` único de cada ítem contra un caché cargado al iniciar el servidor.
