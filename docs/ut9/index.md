# UT9 — Aplicaciones web híbridas

**22 h · 22 sesiones · Trimestre 2.º** · Evaluación: :material-laptop: **examen práctico (100 %)** · Peso: **10 %** del módulo, más el **10 % de la FFE**, que se evidencia en el proyecto final de esta unidad

> **RA9:** Desarrolla aplicaciones web híbridas seleccionando y utilizando tecnologías, frameworks servidor y repositorios heterogéneos de información.

Última unidad del módulo, y la que más se parece a un trabajo real. Hasta ahora **todo lo que había en tu aplicación lo habías escrito tú**, y todos los datos los había metido alguien a mano. Eso, en una empresa, no pasa casi nunca.

Aquí tu aplicación se abre al mundo: consume servicios ajenos, ingiere datos públicos, incorpora librerías de terceros y cruza todo eso para responder preguntas que **ninguna de las fuentes sabía responder por separado**.

!!! info "El hilo de la unidad"
    Una idea recorre las 34 sesiones: **reutilizar es una ventaja enorme y una dependencia peligrosa**. Al terminar, tu aplicación tiene que seguir funcionando —peor, pero funcionando— aunque todas las fuentes externas estén caídas. Eso es lo que separa una integración profesional de una demo de clase.


!!! reto "Esta unidad se da con el proyecto del trimestre"
    Desde enero no hay prácticas sueltas: hay **un producto**, [Pulso de Madrid](../proyecto/index.md), que se construye en equipos de tres. Esta unidad son los dos últimos retos: **«las fuentes mienten y se caen»** y **«¿y esto qué nos dice?»**.

    Lee primero **[el proyecto](../proyecto/index.md)** y **[los seis retos](../proyecto/retos.md)**: el calendario de abajo son las sesiones de R5 y R6.

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
| **S1** | Qué es una aplicación híbrida · el precio de reutilizar: dependencia y licencias | [1. Aplicaciones híbridas](01-aplicaciones-hibridas.md) §1–3 |
| **S2** | Consumir, ingerir o incorporar · dónde buscar datos abiertos y el CSV español | [1. Aplicaciones híbridas](01-aplicaciones-hibridas.md) §4–7 |
| **S3** | Cliente HTTP bien montado: claves fuera, *timeouts* y **DTO propio** | [2. Servicios externos](02-consumir-servicios-externos.md) §1–2 |
| **S4** | Tolerancia a fallos: valor de reserva, reintentos y **cortacircuitos** | [2. Servicios externos](02-consumir-servicios-externos.md) §3 |
| **S5** | Caché, cuotas, 429 y componer varias fuentes en paralelo | [2. Servicios externos](02-consumir-servicios-externos.md) §4–7 |
| **S6** | Taller: montar el cliente HTTP del proyecto propio | [Batería de ejercicios](ejercicios.md) |
| **S7** | Taller: tolerancia a fallos sobre ese cliente | [Batería de ejercicios](ejercicios.md) |
| **S8** | Ingerir: por qué, y las tres fases | [3. Tu repositorio](03-crear-tu-repositorio.md) §1–3 |
| **S9** | Transformar datos sucios de verdad · normalizar categorías y la trampa de `split` | [3. Tu repositorio](03-crear-tu-repositorio.md) §4 |
| **S10** | Cargar sin duplicar: **idempotencia** y *upsert* · lo que desaparece del origen | [3. Tu repositorio](03-crear-tu-repositorio.md) §5 |
| **S11** | Automatizar con `@Scheduled` · registrar cada ejecución | [3. Tu repositorio](03-crear-tu-repositorio.md) §6–7 |
| **S12** | Taller: ingerir un fichero abierto real de principio a fin | [Batería de ejercicios](ejercicios.md) |
| **S13** | Taller: dejar la carga automatizada y con registro | [Batería de ejercicios](ejercicios.md) |
| **S14** | Elegir una dependencia con criterio · licencias · **seguridad y Log4Shell** | [4. Librerías](04-librerias-y-dependencias.md) §1–4 |
| **S15** | PDF, Excel, QR y mapas · **aislar la librería tras una interfaz propia** | [4. Librerías](04-librerias-y-dependencias.md) §5–6 |
| **S16** | Taller: incorporar una librería y aislarla tras una interfaz | [Batería de ejercicios](ejercicios.md) |
| **S17** | Dato, información, conocimiento · **agregar en SQL** | [5. Analítica](05-analitica-y-cuadros-de-mando.md) §1–3 |
| **S18** | Cruzar fuentes · el cuadro de mando · cuándo esto deja de valer | [5. Analítica](05-analitica-y-cuadros-de-mando.md) §4–7 |
| **S19** | Probar sin internet · documentar · desplegar | [6. Probar y desplegar](06-probar-documentar-desplegar.md) |
| **S20** | Proyecto integrador: cerrar la aplicación híbrida completa y el README | [Reto con tests](practicas.md) |
| **S21** | :material-laptop: **Examen práctico de RA9 (100 %)** · 1.ª sesión | [Preparar el examen](examen.md) |
| **S22** | :material-laptop: **Examen práctico de RA9 (100 %)** · 2.ª sesión | — |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**, 2 sesiones. Se te da una aplicación funcionando y un conjunto de datos abiertos, y tienes que integrarlos: ingerir el fichero de forma idempotente, consumir una API en vivo con su plan B, exportar a PDF o Excel y montar una vista analítica. Rúbrica en la [página de preparación](examen.md).

!!! danger "El criterio que decide la nota"
    **Que la aplicación aguante con las fuentes externas apagadas.** Se corrige literalmente así: se corta la red hacia el exterior y se navega la aplicación. Si da un error 500, ese criterio es cero por muy bien que funcione con conexión.

## Material

- [Reto con tests](practicas.md) — el censo de comercios de un municipio, de principio a fin.
- [Batería de ejercicios](ejercicios.md) — 12 ejercicios resueltos sobre otro dominio.
- [Chuleta](chuleta.md) — cliente HTTP, resiliencia, ingesta y analítica en una página.
- [Preparar el examen](examen.md) — rúbrica y errores que más cuestan.

## Antes de la S1

- [ ] Tu proyecto de la UT8 arranca, con su API y su web.
- [ ] Date de alta en [AEMET OpenData](https://opendata.aemet.es/centrodedescargas/altaUsuario) — la clave tarda unos minutos en llegar.
- [ ] Echa un vistazo a [datos.gob.es](https://datos.gob.es) y busca un conjunto de datos de tu municipio.
- [ ] Docker funcionando: en la S25 se despliega todo junto.
