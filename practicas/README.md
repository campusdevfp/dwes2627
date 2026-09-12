# Proyectos base con tests

Aquí no se corrige «a ojo» ni se pregunta si está bien. **Se ejecutan los tests.**

```
practicas/
├── java-base/          UT2 y UT3 — Java puro, sin frameworks
├── spring-base/        UT4, UT6 y UT7 — Spring Boot
└── nuevo-ejercicio.py  (profesor) crea el esqueleto de una práctica nueva
```

Los dos proyectos **crecen contigo**: en `spring-base` la UT4 monta las capas, la UT6 pone la API encima y la UT7 la protege. No se empieza de cero cada unidad, y por eso las decisiones que tomes en la UT4 se pagan o se cobran después.

## Cómo se trabaja

1. Abres el proyecto que toque.
2. Buscas la práctica del día: cada una tiene su clase de test, con **los tests en rojo**.
3. Escribes el código hasta que se ponen **verdes**.
4. Cuando están todos en verde, la práctica está terminada. Sin ambigüedad.

```bash
cd practicas/java-base
mvn test                       # todos
mvn test -Dtest='UT2*'         # solo los de la UT2
mvn test -Dtest='UT2P3Test'    # solo la práctica 3
./verificar.sh UT2             # resumen legible de qué falta
```

## Por qué los tests son el enunciado

El test dice **exactamente** qué se espera: qué método, qué firma, qué devuelve, qué lanza y con qué mensaje. Leerlo antes de programar es la mitad del trabajo, y es lo que se hace en cualquier empresa cuando te llega una especificación.

Además te da tres cosas que ningún corrector humano puede darte:

- **Respuesta inmediata**: ejecutas y sabes.
- **Respuesta objetiva**: pasa o no pasa, sin interpretación.
- **Respuesta a cualquier hora**: el domingo por la noche también.

!!! tip "Lee el test antes de escribir código"
    Un test que espera `ProductoDuplicadoException` con el nombre dentro del mensaje te está diciendo la firma, el tipo de excepción y hasta el texto. No adivines: ábrelo.

!!! warning "Verde no siempre es terminado"
    Los tests comprueban el **comportamiento**, no el diseño. Puedes tener todo en verde con el código metido a martillazos en una sola clase. Los ejercicios que piden separar capas llevan además tests de **arquitectura**, que sí lo comprueban.

## Si un test falla y no entiendes por qué

Lee el mensaje entero: AssertJ dice qué esperaba y qué recibió.

```
expected: "Casco integral"
 but was: "  casco integral  "
```

Ahí está el fallo: falta normalizar. El test no te dice cómo arreglarlo, te dice qué está mal, que es justo lo que necesitas.


---

## Para el profesor: añadir una práctica

```bash
python3 nuevo-ejercicio.py --estado                            # qué prácticas tienen tests
python3 nuevo-ejercicio.py UT3 P7 "Inventario de una carpeta"  # crea UT3P7Test.java
python3 nuevo-ejercicio.py UT6 P6 "Filtros combinables"        # deduce que va a spring-base
```

Genera la clase con la plantilla de cuatro tests —caso normal, caso límite, entrada inválida y sin efectos colaterales— marcados con `fail("test sin escribir")`. La numeración es **la misma que en el banco**: el P7 del RA3 es `UT3P7Test`.

Guía completa en `repo-profesor/docs/programacion/crear-ejercicios.md`.
