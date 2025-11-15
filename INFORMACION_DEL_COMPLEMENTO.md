# Anfora Resonante

Anfora Resonante es un plugin de Spigot que introduce un nuevo bloque personalizado, el "Ánfora Resonante", diseñado para funcionar como un banco de experiencia (XP). Los jugadores pueden colocar estas ánforas para almacenar su XP de forma segura. El plugin cuenta con un sistema de almacenamiento de datos robusto y flexible, compatible con YAML, SQLite y MySQL, e incluye un sistema de failover para garantizar la máxima disponibilidad.

## ✨ Características Principales

-   **Banco de Experiencia:** La función principal es permitir a los jugadores almacenar su experiencia en un bloque físico en el mundo.
-   **Ítem Personalizado y Configurable:** El "Anfora Resonante" es un ítem único cuya apariencia (nombre, descripción y efecto de brillo) es totalmente personalizable por los administradores a través de `config.yml`.
-   **Persistencia de Datos en el Ítem:** Cuando un ánfora se rompe, el ítem resultante **conserva el nivel, la experiencia y el nombre del propietario** que contenía, permitiendo a los jugadores mover sus bancos de XP sin perder datos.
-   **Sistema de Propietarios:** Cada ánfora colocada pertenece a un jugador. **Solo el propietario puede romper y recuperar su ánfora**. Cualquier intento de romper un ánfora ajena es cancelado, protegiendo la experiencia de los jugadores.
-   **Interacción con Ánforas:**
    -   **Shift + Clic Derecho:** Permite al propietario **extraer toda la experiencia** almacenada en el ánfora y transferirla a su barra de experiencia.
    -   **Clic Derecho (sin Shift):** Abre una interfaz gráfica (GUI) que permite gestionar la experiencia del ánfora, mejorarla y ver su información.
-   **Almacenamiento Flexible:** Soporte para múltiples backends de base de datos:
    -   **YAML:** Ideal para servidores pequeños o para empezar.
    -   **SQLite:** Almacenamiento en un único archivo, perfecto para la mayoría de servidores.
    -   **MySQL:** Soporte para bases de datos externas, ideal para redes de servidores o cargas pesadas.
-   **Failover Automático:** Si por alguna razón el plugin no puede conectarse a la base de datos SQL (SQLite o MySQL) configurada, cambiará automáticamente a YAML para esa sesión, evitando la pérdida de datos y manteniendo el plugin funcional.
-   **Comandos Intuitivos:** Incluye un sistema de ayuda y autocompletado para facilitar su uso a los administradores.

## ⌨️ Comandos

| Comando                               | Descripción                                       | Permiso                  |
| ------------------------------------- | ------------------------------------------------- | ------------------------ |
| `/anfora give <jugador> [cantidad]`   | Da un ánfora nueva (Nivel 1, 0 XP) a un jugador.  | `anforaxp.admin.give`    |
| `/anfora help`                        | Muestra todos los comandos disponibles para ti.   | `anforaxp.command.help`  |

El sistema de autocompletado (Tab) sugiere subcomandos, nombres de jugadores en línea y cantidades.

## 🖥️ Interfaz Gráfica (GUI)

Al hacer clic derecho en un ánfora, se abre una interfaz de 3 filas con la siguiente disposición:

-   **Fila Superior:**
    -   En el centro, una **Estrella del Nether** permite al jugador mejorar el ánfora.

-   **Fila Central:**
    -   A la izquierda, tres tintes rojos para **depositar 1, 5 o 10 niveles** de experiencia.
    -   En el centro, una **Perla de Ender** muestra la experiencia actual y el nivel del ánfora.
    -   A la derecha, tres tintes verdes para **retirar 1, 5 o 10 niveles** de experiencia.

-   **Fila Inferior:**
    -   En el centro, un **Libro** muestra información detallada sobre el ánfora, como el nombre del propietario, el nivel y la capacidad.

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

### Próximos Pasos Críticos
1.  **Implementar la lógica de los Listeners restantes** (`AnforaExplosionListener`, etc.) para definir cómo se añade/retira XP en otras situaciones.
2.  **Implementar un sistema de rastreo de `UUID`s** para validar la unicidad de las ánforas en tiempo real y prevenir duplicaciones avanzadas.
3.  **Sincronizar Versiones** entre `pom.xml` y `plugin.yml`.
