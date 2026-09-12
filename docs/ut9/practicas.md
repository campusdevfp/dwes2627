# Reto de la unidad — UT9

> **Reto `ut9-integracion-base`**, en GitHub Classroom. Una aplicación completa más un CSV y una API externa simulada, con tests que exigen ingesta idempotente y resiliencia.
>
> Arranca desde una base que funciona: lo que te saliera mal en la unidad anterior no te condiciona.

| Fase | Sesiones | Qué haces |
|---|---|---|
| F1 · Elegir las fuentes | S3–S4 | Buscar datos, leer licencias, comprobar cuotas |
| F2 · La primera API en vivo | S5–S7 | AEMET con *timeouts*, DTO propio y plan B |
| F3 · Que no se caiga | S8–S11 | Cortacircuitos, caché, composición en paralelo |
| F4 · Ingerir el censo | S12–S16 | ETL completo, idempotente |
| F5 · Automatizar | S17 | `@Scheduled`, registro de ejecuciones, panel de ingestas |
| F6 · Librerías | S18–S21 | PDF, Excel, QR, mapa con Leaflet |
| F7 · Analítica | S22–S24 | Cruce con renta, cuadro de mando |
| F8 · Cerrar | S25 | Tests sin internet, README, Docker |

---

## F1 — Elegir las fuentes (S3–S4)

Busca en [datos.gob.es](https://datos.gob.es) el censo de comercios de un municipio y los datos de renta por barrio. Rellena esta tabla **antes** de escribir código:

| Fuente | Formato | Licencia | Cuota | Frecuencia de cambio | ¿Consumir o ingerir? |
|---|---|---|---|---|---|
| | | | | | |

??? success "Comprobación"

    Si has puesto «ingerir» para el censo y «consumir» para el tiempo, has entendido el tema 1. Si has puesto «consumir» para el censo, pregúntate cuánto tardaría en responder una página que pide 4.000 registros a un servidor ajeno en cada visita.

    **Del profesor:** descarga el CSV y ábrelo con un editor de texto, no con Excel. Vas a ver el `;`, la codificación rara y las filas vacías **antes** de que te exploten en la cara. Ese vistazo de dos minutos ahorra una tarde.


## F2 — La primera API en vivo (S5–S7)

Cliente de AEMET con clave en variable de entorno, `connectTimeout` y `readTimeout`, DTO propio y `Optional` cuando falle. Muestra el tiempo en la ficha del comercio.

??? success "Comprobación"

    ```bash
    grep -rn "api_key\|apiKey" src/main/resources/   # no debe aparecer ninguna clave
    ```

    La prueba de fuego, y hazla ahora aunque parezca pronto:

    ```bash
    # simula que AEMET no existe
    sudo sh -c 'echo "127.0.0.1 opendata.aemet.es" >> /etc/hosts'
    ```

    Carga la ficha de un comercio. **Tiene que verse todo menos el recuadro del tiempo.** Si sale un error 500, tu página depende de un tercero para funcionar, y eso es el fallo que más cuesta en el examen.

    Recuerda quitar la línea del `/etc/hosts` después.


## F3 — Que no se caiga (S8–S11)

Añade reintentos con espera creciente, cortacircuitos y caché de 30 minutos. Si tu ficha llama a más de una API, compón en paralelo.

??? success "Comprobación"

    - Con el `/etc/hosts` trucado, mira el log: deben verse **3 intentos** con esperas de 0,5 s, 1 s y 2 s, y después el valor de reserva.
    - Recarga la misma ficha diez veces seguidas. Con la caché puesta debe haber **una** llamada, no diez. Se comprueba en el log del interceptor.
    - Deja el cortacircuitos abierto y comprueba que deja de intentarlo: las peticiones siguientes responden al instante, sin esperar el *timeout*.

    **Pregunta para clase:** ¿pondrías `@Retryable` en un endpoint que registra una matrícula? ¿Por qué no?


## F4 — Ingerir el censo (S12–S16)

ETL completo del CSV: limpiar espacios, convertir decimales, normalizar actividades, descartar filas inválidas contándolas, deduplicar y cargar con *upsert*.

??? success "Comprobación"

    La única que importa:

    ```bash
    curl -X POST localhost:8080/admin/ingesta   # primera vez
    curl -X POST localhost:8080/admin/ingesta   # segunda vez
    ```

    ```sql
    select count(*) from comercio;
    ```

    **El número tiene que ser idéntico.** Si crece, tu ingesta no es idempotente y el examen se te va a atragantar.

    Y comprueba el registro: debe decir cuántos entraron, cuántos se actualizaron y cuántos se descartaron **y por qué**. Una ingesta silenciosa es una caja negra.


## F5 — Automatizar (S17)

`@Scheduled` semanal de madrugada, endpoint manual protegido para administradores, tabla `EjecucionIngesta` y una página en el panel que muestre el historial.

??? success "Comprobación"

    Pon temporalmente `@Scheduled(fixedDelay = 20000)` y comprueba que se ejecuta sola, que registra la ejecución y que **no duplica**. Después devuélvelo al `cron` semanal.

    Comprueba también que el endpoint manual está protegido: sin sesión de administrador debe dar 403.

    **Pregunta:** si mañana despliegas tres réplicas de esta aplicación, ¿qué pasa con la ingesta del lunes a las 3:30?


## F6 — Librerías (S18–S21)

Ficha del comercio en PDF, listado exportable a Excel, QR con el enlace a la ficha y mapa con Leaflet. Todo detrás de interfaces propias.

??? success "Comprobación"

    ```bash
    mvn dependency:tree | head -40
    mvn dependency-check:check
    ```

    Anota en el README la licencia de cada librería que has metido. Si alguna es AGPL, cámbiala y explica por qué en una línea.

    Prueba de arquitectura: **cambia OpenPDF por otra librería de PDF.** Si tocas más de una clase, no aislaste bien. Es el mismo ejercicio mental que hiciste en la UT5 al cambiar el repositorio en memoria por JPA.

    Y comprueba el mapa: la atribución de OpenStreetMap tiene que estar visible. Quitarla incumple la licencia.


## F7 — Analítica (S22–S24)

Cruza comercios con renta por barrio. Cuadro de mando con 4 indicadores, una serie temporal, un ranking y exportación.

??? success "Comprobación"

    Con `show-sql` activado, carga el panel y **cuenta las consultas**. Si ves un `select * from comercio` seguido de cálculos en Java, vuelve al §2 del tema 5.

    Cada indicador debe llevar su comparación: «312 comercios» no informa; «312, un 8 % más que el año pasado» sí.

    **Del profesor:** cuando tengas el panel, busca una conclusión de verdad y escríbela en una frase. Si no encuentras ninguna, el panel no sirve todavía — y eso también es un resultado.


## F8 — Cerrar (S25)

Tests con `MockRestServiceServer` (éxito, error y *timeout*), test de idempotencia, README con la tabla de fuentes, diagrama y `compose.yaml`.

??? success "Comprobación"

    ```bash
    # desconecta el wifi y ejecuta
    mvn test
    ```

    **Todo en verde sin internet.** Un test que necesita red no es un test: es una comprobación manual disfrazada.

    ```bash
    docker compose up --build
    ```

    Desde cero, en una máquina limpia, con solo el README delante. Si hace falta que tú expliques algo por voz, el README está incompleto.


---

## Al terminar el módulo tienes

```
UT2/UT3  el lenguaje y los datos
UT4      capas
UT5      base de datos
UT6      API
UT7      seguridad
UT8      interfaz web
UT9      el mundo exterior
```

Una aplicación que consulta servicios ajenos, mantiene su propio repositorio de datos públicos, exporta documentos, analiza lo que guarda **y sobrevive a que todo lo de fuera se caiga**.

Eso es, con bastante exactitud, el trabajo de un desarrollador de servidor.
