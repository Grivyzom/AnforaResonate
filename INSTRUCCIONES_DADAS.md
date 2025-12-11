1. El cliente solicita que a traves del archivo "config.yml" el usuario pueda cambiar el nombre, descripción e incluso activar/desactivar un efecto de encantamiento que se oculta que el flag de hide enchant.
2. El cliente solicita que añadas un sistema de "help" o "ayuda" donde se añadan automaticamente todos los comandos que se vayan añadiendo, osea, un sistema de help escalable.
3. El cliente solicita que se añada un sistema de auto completado a través del tab que sea un sistema escalable y moderno.
4. El cliente ha solicitado que añadas capas de seguridad a mi "Ánfora Resonante" para evitar duplicaciones, y que un usuario pueda "recrear" una "Ánfora Resonante" de alguna manera, cada "Ánfora Resonante" debe ser única.


El cliente ha solictado a lo sigueinte:

Añade una mesa de administracion, esta mesa debe ser el "atril" la cual debe permitir al usuario hacer el siguiente flujo:

Con clic derecho sobre el atril de administracion - Acceder a GUI de administracion
Con clic izquierdo 

Solicitud de Mejoras y Placeholders
1. Implementación de un ID Universal (Global ID)

Se solicita que cada ánfora colocada en el mundo tenga asignado un ID Universal único (hash o código único).Propósito: 

Permitir que el staff pueda rastrear, ubicar y gestionar una ánfora específica independientemente de quién sea el dueño.Nota: Este ID debe ser diferente al "ID local" que ve el usuario (ej. Ánfora #1, #2), y debe ser irrepetible en todo el servidor.

2. Sistema de Placeholders (PlaceholderAPI)Se requiere ofrecer una variedad de placeholders para interactuar con los datos de las ánforas. A continuación, se detallan los requerimientos:

%anfora_total_exp%
Descripción: Devuelve la suma total de experiencia almacenada en todas las ánforas del usuario.
Ejemplo: Brocolito usa /papi parse me %anfora_total_exp% => El sistema devuelve el valor total (ej. "5000").

%anfora_exp_<id_local>%
Descripción: Muestra la experiencia actual almacenada en la ánfora del usuario con el ID local especificado.
Ejemplo: Brocolito usa /papi parse me %anfora_exp_1% => El sistema devuelve la experiencia de su ánfora personal #1.

%anfora_ubi_<id_local>%
Descripción: Muestra las coordenadas (Mundo, X, Y, Z) de la ánfora del usuario con el ID local especificado.
Ejemplo: Brocolito usa /papi parse me %anfora_ubi_1% => El sistema devuelve la ubicación (ej. "World, 100, 64, -200").

%anfora_nivel_<id_local>%
Descripción: Muestra el nivel actual de la ánfora del usuario con el ID local especificado.
Ejemplo: Brocolito usa /papi parse me %anfora_nivel_1% => El sistema devuelve el nivel de su ánfora personal #1.

%anfora_owner_<id_universal>%
Descripción: Muestra el nombre del propietario de una ánfora específica basándose en su ID Universal (Global).
Ejemplo: Brocolito usa /papi parse me %anfora_owner_2d1f65hj% => El sistema busca la ánfora con ID global 2d1f65hj y devuelve el nombre del dueño (ej. "Steve").