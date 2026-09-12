# UT9 — Aplicaciones web híbridas

**34 h · 34 sesiones · Trimestre 2.º** · Evaluación: :material-laptop: **examen práctico (100 %)** · Peso: **10 %** del módulo, más el **10 % de la FFE**, que se evidencia en el proyecto final de esta unidad

> **RA9:** Desarrolla aplicaciones web híbridas seleccionando y utilizando tecnologías, frameworks servidor y repositorios heterogéneos de información.

Última unidad del módulo, y la que más se parece a un trabajo real. Hasta ahora **todo lo que había en tu aplicación lo habías escrito tú**, y todos los datos los había metido alguien a mano. Eso, en una empresa, no pasa casi nunca.

Aquí tu aplicación se abre al mundo: consume servicios ajenos, ingiere datos públicos, incorpora librerías de terceros y cruza todo eso para responder preguntas que **ninguna de las fuentes sabía responder por separado**.

!!! info "El hilo de la unidad"
    Una idea recorre las 34 sesiones: **reutilizar es una ventaja enorme y una dependencia peligrosa**. Al terminar, tu aplicación tiene que seguir funcionando —peor, pero funcionando— aunque todas las fuentes externas estén caídas. Eso es lo que separa una integración profesional de una demo de clase.


## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Explicar qué es una **aplicación híbrida** y qué se gana y se pierde al montarla.
- [ ] **Consumir servicios de terceros** con tolerancia a fallos, tiempos de espera y credenciales fuera del código.
- [ ] Construir **tu propio repositorio de datos** a partir de fuentes ajenas, limpiando y validando la entrada.
- [ ] Incorporar **librerías** externas evaluando licencia, mantenimiento y riesgo.
- [ ] Sacar **conocimiento** de los datos y presentarlo en un cuadro de mando.
- [ ] **Probar, documentar y desplegar** la aplicación completa.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Qué es una aplicación híbrida · el ejemplo del portal inmobiliario | [1. Aplicaciones híbridas](01-aplicaciones-hibridas/) §1–2 |
| **S2** | El precio de reutilizar: dependencia, licencias, casos reales | [1. Aplicaciones híbridas](01-aplicaciones-hibridas/) §3 |
| **S3** | Consumir, ingerir o incorporar: cómo se decide | [1. Aplicaciones híbridas](01-aplicaciones-hibridas/) §4 |
| **S4** | Dónde buscar datos abiertos · formatos y el CSV español | [1. Aplicaciones híbridas](01-aplicaciones-hibridas/) §5–7 |
| **S5** | Cliente HTTP bien montado: claves fuera y *timeouts* | [2. Servicios externos](02-consumir-servicios-externos/) §1 |
| **S6** | DTO propio, no el del tercero | [2. Servicios externos](02-consumir-servicios-externos/) §2 |
| **S7** | Valor de reserva y reintentos con espera creciente | [2. Servicios externos](02-consumir-servicios-externos/) §3 |
| **S8** | Cortacircuitos: dejar de insistir | [2. Servicios externos](02-consumir-servicios-externos/) §3 |
| **S9** | Caché: menos latencia y menos factura | [2. Servicios externos](02-consumir-servicios-externos/) §4 |
| **S10** | Cuotas, 429 y buenos modales | [2. Servicios externos](02-consumir-servicios-externos/) §5–7 |
| **S11** | Componer varias fuentes en paralelo | [2. Servicios externos](02-consumir-servicios-externos/) §6 |
| **S12** | Taller: montar el cliente HTTP del proyecto propio | [Batería de ejercicios](ejercicios/) |
| **S13** | Taller: tolerancia a fallos sobre ese cliente | [Batería de ejercicios](ejercicios/) |
| **S14** | Ingerir: por qué y las tres fases | [3. Tu repositorio](03-crear-tu-repositorio/) §1–3 |
| **S15** | Transformar datos sucios de verdad | [3. Tu repositorio](03-crear-tu-repositorio/) §4 |
| **S16** | Normalizar categorías · la trampa de `split` | [3. Tu repositorio](03-crear-tu-repositorio/) §4 |
| **S17** | Cargar sin duplicar: idempotencia y *upsert* | [3. Tu repositorio](03-crear-tu-repositorio/) §5 |
| **S18** | Qué hacer con lo que desaparece del origen | [3. Tu repositorio](03-crear-tu-repositorio/) §5 |
| **S19** | Automatizar con `@Scheduled` · registrar cada ejecución | [3. Tu repositorio](03-crear-tu-repositorio/) §6–7 |
| **S20** | Taller: ingerir un fichero abierto real de principio a fin | [Batería de ejercicios](ejercicios/) |
| **S21** | Taller: dejar la carga automatizada y con registro | [Batería de ejercicios](ejercicios/) |
| **S22** | Elegir una dependencia con criterio · licencias | [4. Librerías](04-librerias-y-dependencias/) §1–3 |
| **S23** | Seguridad de las dependencias · Log4Shell | [4. Librerías](04-librerias-y-dependencias/) §4 |
| **S24** | PDF, Excel, QR y mapas | [4. Librerías](04-librerias-y-dependencias/) §5 |
| **S25** | Aislar la librería detrás de una interfaz propia | [4. Librerías](04-librerias-y-dependencias/) §6 |
| **S26** | Dato, información, conocimiento · agregar en SQL | [5. Analítica](05-analitica-y-cuadros-de-mando/) §1–3 |
| **S27** | Taller: incorporar una librería y aislarla tras una interfaz | [Batería de ejercicios](ejercicios/) |
| **S28** | Cruzar fuentes · el cuadro de mando | [5. Analítica](05-analitica-y-cuadros-de-mando/) §4–5 |
| **S29** | Cuándo esto deja de valer: OLTP, OLAP y *big data* | [5. Analítica](05-analitica-y-cuadros-de-mando/) §6–7 |
| **S30** | Probar sin internet · documentar · desplegar | [6. Probar y desplegar](06-probar-documentar-desplegar/) |
| **S31** | Laboratorio libre: cerrar la integración y el README | [Reto con tests](practicas/) |
| **S32** | Proyecto integrador: cerrar la aplicación híbrida completa | [Reto con tests](practicas/) |
| **S33** | :material-laptop: **Examen práctico de RA9 (100 %)** · 1.ª sesión | [Preparar el examen](examen/) |
| **S34** | :material-laptop: **Examen práctico de RA9 (100 %)** · 2.ª sesión | — |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, 2 sesiones. Se te da una aplicación funcionando y un conjunto de datos abiertos, y tienes que integrarlos: ingerir el fichero de forma idempotente, consumir una API en vivo con su plan B, exportar a PDF o Excel y montar una vista analítica. Rúbrica en la [página de preparación](examen/).

!!! danger "El criterio que decide la nota"
    **Que la aplicación aguante con las fuentes externas apagadas.** Se corrige literalmente así: se corta la red hacia el exterior y se navega la aplicación. Si da un error 500, ese criterio es cero por muy bien que funcione con conexión.

## Material

- [Reto con tests](practicas/) — el censo de comercios de un municipio, de principio a fin.
- [Batería de ejercicios](ejercicios/) — 12 ejercicios resueltos sobre otro dominio.
- [Chuleta](chuleta/) — cliente HTTP, resiliencia, ingesta y analítica en una página.
- [Preparar el examen](examen/) — rúbrica y errores que más cuestan.

## Antes de la S1

- [ ] Tu proyecto de la UT8 arranca, con su API y su web.
- [ ] Date de alta en [AEMET OpenData](https://opendata.aemet.es/centrodedescargas/altaUsuario) — la clave tarda unos minutos en llegar.
- [ ] Echa un vistazo a [datos.gob.es](https://datos.gob.es) y busca un conjunto de datos de tu municipio.
- [ ] Docker funcionando: en la S25 se despliega todo junto.
