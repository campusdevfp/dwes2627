# Examen práctico de la UT4 — cómo es y cómo prepararlo

**100 % de la nota de la unidad · Desarrollo en ordenador · 2 sesiones (110 min)**

> Es tu primer examen práctico del curso. Aquí tienes exactamente **qué se te va a pedir, con qué se te va a puntuar y cómo prepararte**. Sin sorpresas.

!!! tip "Se examina con chuleta"
    En el examen se te entrega impresa la **[chuleta de los prácticos del 1.er trimestre](../chuleta-examen-1t.md)**, y no puedes usar nada más.

    Está publicada desde septiembre: **entrena con ella**. Trae sintaxis, no criterio — lo que se evalúa no viene en la hoja.

## Formato

- Recibes un enunciado y un proyecto esqueleto (Spring Initializr con las dependencias ya puestas y algún fichero de partida).
- Trabajas en tu equipo, con tu IDE. Puedes consultar la web del curso, tus prácticas y la documentación oficial de Spring.
- No puedes
    usar asistentes de IA durante la prueba, ni copiar de un compañero.
- Entregas el proyecto comprimido (o el push a tu repositorio, según se indique).

## Qué se pide (tipo)

Construir una pequeña API por capas para un dominio nuevo. Por ejemplo:

!!! example "Enunciado de ejemplo — Biblioteca"
    Desarrolla la gestión de **libros** de una biblioteca:

    1. Modelo
        Libro
        con id, título, autor, ISBN, año y ejemplares disponibles.
    2. Repositorio en memoria con su interfaz y una implementación (`@Repository`).
    3. Servicio (`@Service`) con: listar, obtener por id (404 si no existe), crear, y prestar un libro (decrementa ejemplares; si no quedan, error de conflicto).
    4. Controlador REST (`@RestController`) con los endpoints correspondientes y los códigos HTTP correctos (200, 201, 204, 404, 409).
    5. DTO de entrada y de salida (el de salida no expone el ISBN interno) con validación (`@Valid`): título obligatorio, año positivo, ejemplares ≥ 0.
    6. Manejo global de errores con `@RestControllerAdvice` devolviendo `ProblemDetail`.
    7. Dos tests — uno del servicio con Mockito (caso de error) y uno del controlador con `@WebMvcTest`.

## Rúbrica de corrección

| Criterio | Peso | Qué se valora |
|---|---|---|
| **1. Funciona** | 25 % | El proyecto compila, arranca y los endpoints responden lo que se pide |
| **2. Arquitectura por capas** | 20 % | Controlador → servicio → repositorio, sin saltos ni lógica fuera de sitio |
| **3. Inyección de dependencias** | 15 % | Por constructor, contra interfaces, con los estereotipos correctos |
| **4. DTO y validación** | 15 % | DTO de entrada/salida, `@Valid`, no se expone el modelo interno |
| **5. Errores y códigos HTTP** | 15 % | Excepciones de dominio + `@RestControllerAdvice`; 200/201/204/404/409 bien usados |
| **6. Tests** | 10 % | Al menos dos tests con sentido, que pasen |

**Cada criterio se puntúa 0–10** y la nota es la media ponderada. Se supera con **≥ 5**.

### Cómo se puntúa cada criterio

| Nota | Significa |
|---|---|
| 9–10 | Impecable, como lo haría un profesional |
| 7–8 | Correcto con detalles menores |
| 5–6 | Cumple lo básico, con carencias |
| 3–4 | Intentado pero incorrecto |
| 0–2 | Ausente o no funciona |

!!! danger "Lo que hunde la nota"
    - No compila
        → criterio 1 a 0, y arrastra al resto. Entrega algo que arranque aunque falte una funcionalidad.
    - Lógica de negocio en el controlador
        → criterio 2 muy penalizado. Es el error que más veo.
    - Devolver el modelo en vez del DTO
        → criterio 4.
    - Devolver 200 en un error, o 500 por no controlar la excepción
        → criterio 5.
    - Tests que no compilan
        → arrastran el criterio 6 a 0 y pueden impedir que el proyecto arranque.

## Cómo prepararlo

**Haz el proyecto de aula completo** ([prácticas](../practicas/)). El examen es "lo mismo con otro dominio": si has construido la tienda de principio a fin, la biblioteca te sale en 90 minutos.

Autochequeo antes del examen — ¿sabes hacer todo esto **sin mirar**?

- [ ] Crear un proyecto en Initializr con Web + Validation.
- [ ] Escribir un record de modelo y su repositorio con interfaz + `@Repository`.
- [ ] Inyectar por constructor sin usar `@Autowired` en campos.
- [ ] Escribir los cinco endpoints CRUD con sus códigos (`@ResponseStatus`).
- [ ] Crear DTO de entrada con `@NotBlank`, `@Positive`, `@Min` y activarlos con `@Valid`.
- [ ] Escribir una excepción de dominio y su manejador en `@RestControllerAdvice` con `ProblemDetail`.
- [ ] Un test de servicio con `@Mock` + `when(...)` + `assertThrows`.
- [ ] Un test de controlador con `@WebMvcTest` + `mockMvc.perform(...)`.

Si alguna casilla se te resiste, esa es tu práctica pendiente.

## Estrategia durante el examen

1. Lee el enunciado entero
    antes de escribir nada y localiza los criterios de la rúbrica en él.
2. Construye de abajo arriba: modelo → repositorio → servicio → controlador. Arranca y prueba cada capa según la terminas.
3. Prueba con curl
    a medida que avanzas; no dejes las pruebas para el final.
4. Cuando el CRUD funcione, añade DTO, validación y errores (50 % de la nota está ahí).
5. Los tests, al final pero no de cualquier manera
    : dos tests que pasen valen más que cinco a medias.
6. Reserva 10 minutos para: comprobar que compila limpio, que arranca y que la entrega está completa.

> **Consejo final:** el examen no premia escribir mucho código, premia **poner cada cosa en su sitio**. Un CRUD sencillo con capas limpias, DTO, errores bien mapeados y dos tests saca mejor nota que una aplicación grande con todo mezclado en el controlador.
