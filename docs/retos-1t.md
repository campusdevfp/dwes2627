# Los retos del 1.er trimestre

En enero el curso se organiza en [un proyecto por sprints](proyecto/index.md). Antes de eso, en UT1–UT3, **no**: sería un error. No se puede construir un producto con un lenguaje que todavía no se domina, y perderíamos en montaje las semanas que hacen falta para llegar a Spring con base.

Lo que sí hay desde el primer día es **la misma forma de pensar, en pequeño**.

!!! reto "Qué es un micro-reto"
    Cada sesión abre con **una pregunta incómoda de 25 minutos**, no con un tema. Trabajas en pareja, entregas algo concreto y se pone en común. Después —y solo después— se explica lo que hacía falta.

    | | Micro-reto (UT1–UT3) | Reto de sprint (UT7–UT9) |
    |---|---|---|
    | **Dura** | 25 min | 2 semanas |
    | **Se hace** | En pareja | En equipo de tres |
    | **Entrega** | Una respuesta, un `curl`, un fichero | Un producto que funciona |
    | **Lo resuelve** | Un ejercicio de la batería | Decisiones de diseño vuestras |

La diferencia importante: en el micro-reto **la solución existe y está en la batería**. No se te pide inventar arquitectura, se te pide **descubrir el problema antes de que te den la respuesta**. Eso es lo que hace que la respuesta se quede.

## Por qué este orden y no al revés

Es tentador dar la teoría y luego el ejercicio. Funciona peor, y la razón es sencilla: cuando ya te han contado la solución, el ejercicio es aplicar una receta. Cuando te has estrellado tú primero durante quince minutos, la explicación **cae en un hueco que ya existe**.

Cuesta más y es más incómodo. Por eso se avisa: **es normal no saber resolverlo al empezar**. No estás evaluado en el micro-reto; estás evaluado en el test del final de la unidad, y lo que hoy no te sale es exactamente lo que entonces sabrás.

---

## UT1 · Arquitecturas y tecnologías · 6 sesiones

Aquí casi todo se resuelve **con la consola y el navegador**. No se programa todavía, y aun así se teclea todos los días.

| Sesión | El micro-reto | Se resuelve con | Lo que queda claro |
|:-:|---|:-:|---|
| **S1** | *«Escribe todo lo que ocurre entre que pulsas Intro y ves la página. Sin mirar nada. Después ábrelo con `F12` y cuenta las peticiones.»* | [E2](ut1/ejercicios.md) · [E4](ut1/ejercicios.md) | Que una página son decenas de peticiones, no una |
| **S2** | *«Aquí tenéis seis situaciones. ¿Qué código devolvéis en cada una? Aviso: en dos os vais a equivocar.»* | [E23](ut1/ejercicios.md) · [E15](ut1/ejercicios.md) | Que el código de estado **es** la respuesta, y el 401/403 |
| **S3** | *«La página tarda 3 segundos. ¿Dónde se van? Traedme el número, no la sensación.»* | [E16](ut1/ejercicios.md) | `curl -w`, y que «va lento» no es un diagnóstico |
| **S4** | *«Pedid la misma URL y conseguid que os devuelva dos formatos distintos.»* | [E17](ut1/ejercicios.md) · [E18](ut1/ejercicios.md) | Negociación de contenido y caché, sin haberlas nombrado |
| **S5** | *«Aquí tenéis un JWT. ¿Qué sabéis de este usuario? Cinco minutos.»* | [E25](ut1/ejercicios.md) | Que un JWT **no está cifrado**. Nadie lo olvida |
| **S6** | *«Diseñad las rutas de una biblioteca en veinte minutos. Después las comparamos todas en la pizarra.»* | [E24](ut1/ejercicios.md) · [E29](ut1/ejercicios.md) | Que hay decisiones de diseño, no una única forma |

!!! tip "El de la S5 es el que más rinde"
    Cuando alguien decodifica el token con `base64 -d` y ve el correo de otra persona en claro, la clase se queda en silencio. Es la mejor explicación de seguridad de todo el trimestre, y no la das tú.

---

## UT2 · Java moderno · 9 sesiones

En Java el micro-reto casi siempre tiene la misma forma: **predice qué imprime, ejecútalo, explica la diferencia**. Es además exactamente el formato del examen.

| Sesión | El micro-reto | Se resuelve con | Lo que queda claro |
|:-:|---|:-:|---|
| **S2** | *«¿Cuánto es `7/2` y cuánto `7/2.0`? Apuéstalo antes de ejecutar.»* | [E1](ut2/ejercicios.md) | La división entera, de una vez |
| **S3** | *«Escribe una ficha de ocho líneas sin usar un solo `\n`.»* | [E13](ut2/ejercicios.md) | Bloques de texto, por necesidad |
| **S4** | *«Este `Equipo` dice ser inmutable. Rompedlo.»* | [E23](ut2/ejercicios.md) · [E4](ut2/ejercicios.md) | `final` protege la referencia, no el contenido |
| **S5** | *«Añade un tipo de evento nuevo y consigue que el compilador te obligue a tratarlo.»* | [E16](ut2/ejercicios.md) | `sealed` + `switch` sin `default` |
| **S6** | *«Ordena por puntos y desempata por nombre. Aviso: el 80 % lo hará mal.»* | [E17](ut2/ejercicios.md) | Dónde va el `.reversed()` |
| **S7** | *«Media y máximo anotador recorriendo la lista una sola vez.»* | [E19](ut2/ejercicios.md) | `teeing`, y que hay más que `forEach` |
| **S8** | *«Este `catch` está vacío. Provocad el fallo y contad qué veis.»* | [E22](ut2/ejercicios.md) | Por qué tragarse una excepción es lo peor |
| **S9** | *«Aquí hay un método de 2010. Tenéis 25 minutos.»* | [E27](ut2/ejercicios.md) | La unidad entera, en un ejercicio |

---

## UT3 · Datos, ficheros y JSON · 9 sesiones

La UT3 se examina con un [test práctico](ut3/autoevaluacion.md) sobre fragmentos de código, así que el micro-reto tiene aquí un papel extra: **generar las preguntas**.

| Sesión | El micro-reto | Se resuelve con | Lo que queda claro |
|:-:|---|:-:|---|
| **S1** | *«Buscad 10.000 veces en una lista de 100.000 y luego en un mapa. Traed los dos tiempos.»* | [E2](ut3/ejercicios.md) | Que elegir la estructura **es** el trabajo |
| **S2** | *«Agrupad por marca y por tipo, y que salga ordenado. Los dos niveles.»* | [E5](ut3/ejercicios.md) | Dónde va el `TreeMap::new` |
| **S3** | *«Este CSV tiene cinco trampas. Encontradlas antes de programar nada.»* | [E9](ut3/ejercicios.md) | Que los datos reales vienen sucios |
| **S4** | *«Contad los descartes y decid por qué se descartó cada uno.»* | [E10](ut3/ejercicios.md) | Vale un 20 % del examen él solo |
| **S5** | *«Vuestro JSON escribe la fecha como `[2026,3,14]`. Arregladlo.»* | [E16](ut3/ejercicios.md) | El módulo de `java.time` en Jackson |
| **S6** | *«Leed un JSON ajeno que trae tres campos que no esperáis, sin que reviente.»* | [E18](ut3/ejercicios.md) · [E20](ut3/ejercicios.md) | `path` frente a `get` |
| **S7** | *«`fecha.plusDays(7)` no cambia nada. ¿Por qué?»* | [E21](ut3/ejercicios.md) | Inmutabilidad, otra vez y para siempre |
| **S8** | *«Cambiad el origen de CSV a JSON tocando una sola línea.»* | [E27](ut3/ejercicios.md) · [E26](ut3/ejercicios.md) | Para qué sirve una interfaz |
| **S9** | *«Escribid tres preguntas de examen a partir de vuestros ejercicios.»* | [E30](ut3/ejercicios.md) | El puente entre hacer y reconocer |

---

## Los tres retos largos

Además de los micro-retos, cada unidad tiene **un reto de dos sesiones** que se parece ya a lo que se hará en enero: enunciado abierto, en parejas, y puesta en común.

=== "UT1 · La autopsia de una web"

    > Elegid **dos webs muy distintas** —un periódico y una aplicación tipo panel— y desmontadlas con las DevTools y con `curl`. **Decidme cuál hace SSR y cuál CSR, y demostradlo**; cuántas peticiones lanza cada una y de qué tipo; qué se está cacheando y cómo lo sabéis.

    No hay que programar nada. Hay que **leer lo que ya está pasando**, que es la mitad del trabajo de un desarrollador de servidor y no se enseña en ningún tema.

    **Se entrega:** una página. Las dos tablas de peticiones, la evidencia del SSR o CSR (`Ctrl+U`) y una captura de un `304`.

=== "UT2 · El código heredado"

    > Aquí tenéis **una clase de 200 líneas escrita en 2010** que funciona y que nadie quiere tocar. Vuestro trabajo: dejarla funcionando igual, escrita en Java 25 y **con tests que demuestren que sigue haciendo lo mismo**.

    La restricción es la que lo hace un reto: **primero los tests, después el refactor**. Si refactorizáis antes de tener red, no sabréis si lo habéis roto.

    **Se entrega:** la clase nueva, los tests en verde, y una lista de lo que cambiasteis y por qué.

=== "UT3 · El taller de bicis"

    > `datos/bicis.csv`, 300 filas sucias de verdad. **Cargadlo contando los descartes por motivo, sacad cuatro informes, exportadlo a JSON y dejadlo todo detrás de una interfaz** para que mañana se pueda cambiar el origen.

    Es el [E29](ut3/ejercicios.md) con más datos y sin guion paso a paso. Y es, literalmente, lo que hace una aplicación de verdad antes de tener base de datos.

    **Se entrega:** el proyecto y **tres preguntas de test** escritas por vosotros sobre los errores que cometisteis.

!!! info "Y desde la UT4 ya cambia"
    UT4, UT5 y UT6 tienen sus propios **retos** ([UT4](ut4/retos.md) · [UT5](ut5/retos.md) · [UT6](ut6/retos.md)): dos resueltos que se construyen en clase y **uno que se entrega**, con la misma rúbrica que el examen práctico. Es el escalón intermedio entre esto y el proyecto de enero.

---

## Resumen

| | UT1–UT3 | UT4–UT6 | UT7–UT9 |
|---|---|---|---|
| **Formato** | Micro-retos de 25 min + 1 reto largo | Reto con tests por unidad | Proyecto en sprints |
| **Agrupación** | Parejas | Parejas o individual | Equipos de tres |
| **Sabes que acabaste cuando** | Lo has puesto en común | Los tests están en verde | La demo funciona |
| **Examen** | Test | Práctico | Práctico |

La progresión es deliberada: **de la pregunta corta al producto**. Nadie monta una plataforma en enero si en septiembre no se ha acostumbrado a que le pregunten primero y le expliquen después.
