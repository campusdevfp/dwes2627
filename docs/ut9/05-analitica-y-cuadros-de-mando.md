# Analítica: sacar conocimiento de los datos

El criterio *g)* del RA9 es el más llamativo: *«librerías relacionadas con Big Data e inteligencia de negocios, para incorporar análisis e inteligencia de datos proveniente de repositorios»*.

Vamos a hacerlo con honestidad. **No vas a montar un clúster de Spark en un módulo de 26 horas.** Lo que sí vas a hacer —y es lo que de verdad se pide en una empresa mediana— es convertir tus datos en información útil, saber cuándo tu enfoque deja de escalar, y conocer el vocabulario del mundo grande.

## 1. Dato, información, conocimiento

```
DATO           "Comercio 4271, actividad BAR, barrio Centro, alta 2024-03-11"
INFORMACIÓN    "En Centro hay 312 bares, el 22 % del total del municipio"
CONOCIMIENTO   "Centro tiene 4 veces más bares por habitante que la media,
                y el crecimiento se ha frenado desde 2023"
DECISIÓN       "No abrimos ahí; miramos Arganzuela, que crece un 18 % anual"
```

Tu aplicación lleva todo el curso guardando datos. La analítica es subir esos tres escalones. **Un listado no es analítica; una tabla ordenada tampoco.**

## 2. Agregar en la base de datos, no en Java

La regla que decide el rendimiento:

```java
// MAL  Trae 200.000 filas a memoria para contarlas
var todos = repo.findAll();
var porBarrio = todos.stream()
    .collect(groupingBy(Comercio::getBarrio, counting()));

// BIEN La base de datos devuelve 21 filas
@Query("""
       select c.barrio as barrio, count(c) as total, avg(c.empleados) as media
       from Comercio c where c.activo = true
       group by c.barrio order by count(c) desc
       """)
List<ResumenBarrio> resumenPorBarrio();
```

```java
public interface ResumenBarrio {
    String getBarrio();
    Long getTotal();
    Double getMedia();
}
```

Es una **proyección por interfaz**, de la UT5. La diferencia con 200.000 registros es de segundos a milisegundos, y de cientos de megas de memoria a ninguno.

!!! danger "El `findAll()` seguido de `stream()` es el error del tema"
    Funciona con los 50 registros de tus pruebas y tumba la aplicación con los datos reales. En el examen se comprueba mirando el SQL del log: si aparece un `select` sin `group by` seguido de un cálculo en Java, es cero en ese criterio.

## 3. Las cinco métricas que hay que saber calcular

``` { .sql .numerado }
-- 1. Conteo con agrupación
select barrio, count(*) from comercio group by barrio;

-- 2. Serie temporal: evolución por mes
select date_trunc('month', alta) as mes, count(*)
from comercio group by mes order by mes;

-- 3. Distribución: media, mínimo, máximo, desviación
select actividad, avg(empleados), min(empleados), max(empleados), stddev(empleados)
from comercio group by actividad;

-- 4. Ranking con ventana
select nombre, barrio, empleados,
       rank() over (partition by barrio order by empleados desc) as puesto
from comercio;

-- 5. Comparación con el periodo anterior
select mes, total, lag(total) over (order by mes) as mes_anterior,
       round(100.0 * (total - lag(total) over (order by mes)) / lag(total) over (order by mes), 1) as variacion
from (select date_trunc('month', alta) mes, count(*) total from comercio group by 1) t;
```

Las dos últimas usan **funciones de ventana**, que son la herramienta más infrautilizada de SQL. `rank()`, `lag()`, `lead()` y `sum() over` resuelven en una consulta lo que en Java son treinta líneas y un bucle anidado.

!!! tip "La media miente"
    La renta media de un barrio con un millonario y nueve mileuristas son 109.000 €. La **mediana** son 12.000. Cuando presentes datos a alguien, la mediana suele ser más honesta, y saber decirlo cuenta en el examen.

## 4. Cruzar fuentes: el valor de verdad

Aquí se junta todo lo de la unidad. Tus comercios (tema 3) cruzados con la renta por barrio del INE (tema 2):

```java
@Query("""
       select b.nombre as barrio,
              count(c) as comercios,
              b.rentaMedia as renta,
              count(c) * 1000.0 / b.habitantes as porMilHab
       from Comercio c join c.barrio b
       where c.activo = true
       group by b.nombre, b.rentaMedia, b.habitantes
       order by porMilHab desc
       """)
List<DensidadComercial> densidad();
```

Esa consulta responde a algo que **ninguna de las dos fuentes sabía por separado**. Es la definición de aplicación híbrida del tema 1, hecha realidad en SQL.

## 5. Pintar el cuadro de mando

Con Thymeleaf de la UT8 y Chart.js:

```java
@GetMapping("/panel")
public String panel(Model modelo) {
    modelo.addAttribute("porBarrio", servicio.resumenPorBarrio());
    modelo.addAttribute("evolucion", servicio.altasPorMes(24));
    modelo.addAttribute("kpis", servicio.indicadores());
    return "analitica/panel";
}
```

```html
<div class="kpis">
    <div class="kpi"><span th:text="${kpis.total}">0</span><small>comercios activos</small></div>
    <div class="kpi"><span th:text="|${kpis.variacion} %|">0 %</span><small>frente al año pasado</small></div>
</div>

<canvas id="grafico"></canvas>
<script th:inline="javascript">
  const datos = /*[[${evolucion}]]*/ [];
  new Chart(document.getElementById('grafico'), {
    type: 'line',
    data: { labels: datos.map(d => d.mes),
            datasets: [{ label: 'Altas por mes', data: datos.map(d => d.total) }] }
  });
</script>
```

**Cuatro reglas para que un panel sirva de algo:**

1. **Pocos indicadores.** Cuatro o cinco. Un panel con veinte números no se mira.
2. **Siempre una comparación.** «312 bares» no dice nada; «312, un 8 % más que el año pasado» sí.
3. **El gráfico adecuado**: línea para el tiempo, barras para comparar, tabla para el detalle. Nunca un gráfico de tarta con quince porciones.
4. **Que se pueda exportar.** El PDF y el Excel del tema 4 aparecen aquí: quien toma decisiones quiere llevárselo a una reunión.

## 6. Cuándo esto deja de valer

Todo lo anterior funciona hasta unos cuantos millones de filas en una base de datos relacional bien indexada. Que es más de lo que va a tener el 90 % de los proyectos en los que trabajes.

Cuando se queda corto, aparecen tres problemas y sus soluciones:

| Problema | Se llama | Se resuelve con |
|---|---|---|
| Las consultas analíticas ralentizan la aplicación | Competencia OLTP/OLAP | Réplica de solo lectura, o un almacén separado |
| Los datos no caben en una máquina | Volumen | Procesamiento distribuido (Spark, Flink) |
| Hay que reaccionar en segundos, no en horas | Velocidad | *Streaming* (Kafka) |

**OLTP frente a OLAP** es la distinción que más se pregunta:

| | OLTP | OLAP |
|---|---|---|
| Para qué | Operar: pedidos, altas | Analizar: informes, paneles |
| Consultas | Muchas, pequeñas, por clave | Pocas, enormes, con agregación |
| Ejemplo | «Dame el pedido 4271» | «Ventas por provincia y trimestre» |
| Almacén | PostgreSQL, MySQL | Almacén de datos, columnar |

Y el vocabulario que conviene reconocer aunque no lo uses:

- **Las cinco uves del *big data***: volumen, velocidad, variedad, veracidad, valor.
- **Lago de datos** (*data lake*): todo crudo, se estructura al leer. **Almacén** (*data warehouse*): estructurado al escribir.
- **Lote frente a flujo**: procesar cada noche, o según llega.
- **Spark**: el motor distribuido de referencia. **Kafka**: el bus de eventos.

!!! tip "Cómo responder a esto en un examen o una entrevista"
    La respuesta buena no es soltar la lista de herramientas. Es: *«con este volumen, PostgreSQL con índices y funciones de ventana sobra; si creciera a X, separaría la analítica en una réplica; solo pasaría a un motor distribuido si los datos no caben en una máquina»*.

    Eso demuestra criterio. Decir «usaría Spark» para 40.000 filas demuestra lo contrario.

## 7. Nunca sin cuidar los datos personales

Al agregar información de personas, dos reglas del RGPD que se comprueban en la rúbrica:

- **Minimización**: si te basta el código postal, no guardes la dirección exacta.
- **Anonimización**: en un panel, nunca datos individuales identificables. Y cuidado con los grupos pequeños: «media de sueldos del departamento» con dos personas es revelar el sueldo del otro.

Un umbral sencillo y eficaz: **no muestres agregados de menos de cinco individuos.**

## Pruébalo ahora (20 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Las cinco consultas de analítica, sobre tu tabla de comercios. Ejecútalas en PostgreSQL y compara con lo que tardaría hacerlo en Java.

``` { .sql .numerado title="1 · Cuántos por categoría" }
SELECT actividad,
       COUNT(*)                                             AS total,
       ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER (), 1)   AS porcentaje
FROM comercio
WHERE activo
GROUP BY actividad
ORDER BY total DESC;
```

``` { .sql .numerado title="2 · Evolución mensual de altas" }
SELECT date_trunc('month', alta) AS mes,
       COUNT(*)                  AS altas
FROM comercio
WHERE alta >= CURRENT_DATE - INTERVAL '12 months'
GROUP BY mes
ORDER BY mes;
```

``` { .sql .numerado title="3 · Variación respecto al mes anterior, con lag()" }
WITH por_mes AS (
    SELECT date_trunc('month', alta) AS mes, COUNT(*) AS altas
    FROM comercio
    GROUP BY mes
)
SELECT mes,
       altas,
       lag(altas) OVER (ORDER BY mes)                       AS mes_anterior,
       altas - lag(altas) OVER (ORDER BY mes)                AS variacion
FROM por_mes
ORDER BY mes;
```

``` { .sql .numerado title="4 · Media y mediana, para ver la diferencia" }
SELECT actividad,
       ROUND(AVG(superficie), 1)                                       AS media,
       PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY superficie)         AS mediana,
       MIN(superficie), MAX(superficie), COUNT(*)
FROM comercio
WHERE activo
GROUP BY actividad
HAVING COUNT(*) >= 5              -- no publicar grupos diminutos
ORDER BY media DESC;
```

``` { .sql .numerado title="5 · Los 10 barrios con más comercios por cada 1.000 habitantes" }
SELECT b.nombre,
       COUNT(c.id)                                        AS comercios,
       b.habitantes,
       ROUND(1000.0 * COUNT(c.id) / NULLIF(b.habitantes, 0), 2) AS por_mil
FROM barrio b
LEFT JOIN comercio c ON c.barrio_id = b.id AND c.activo
GROUP BY b.id, b.nombre, b.habitantes
ORDER BY por_mil DESC NULLS LAST
LIMIT 10;
```

Y ahora **mide la diferencia**, que es de lo que va el tema:

```sql
EXPLAIN ANALYZE SELECT actividad, COUNT(*) FROM comercio GROUP BY actividad;
```

Compáralo con la versión en Java que hace lo mismo:

```java
// Esto trae TODAS las filas a memoria para contarlas
List<Comercio> todos = repositorio.findAll();
Map<String, Long> conteo = todos.stream()
        .collect(groupingBy(Comercio::getActividad, counting()));
```

Con 4.000 filas las dos van rápido. Prueba con 400.000 y verás por qué se agrega en SQL.

---

## Ejercicios (con solución)

### Ejercicio 1 — Dato, información, conocimiento

Sube la escalera con un ejemplo propio.

??? success "Solución"

    - **Dato**: `Panadería La Espiga · Calle Mayor 3 · alta 2019-03-12`. Un hecho suelto. No dice nada por sí mismo.
    - **Información**: *«El barrio Centro tiene 47 panaderías, un 12 % del comercio de alimentación del municipio.»* Datos agregados y puestos en contexto. Ya responde a una pregunta.
    - **Conocimiento**: *«Las panaderías del Centro han caído un 30 % en cinco años, mientras las de barrios periféricos crecen un 15 %. La apertura de dos grandes superficies en 2022 coincide con el inicio de la caída.»* Ahora hay una tendencia, una comparación y una hipótesis. **Con esto se decide algo.**

    La escalera importa porque **la mayoría de los cuadros de mando se quedan en el segundo escalón** y lo llaman analítica. Un contador grande que dice «4.312 comercios» es información, y no sirve para tomar ninguna decisión.

    La pregunta que hay que hacerle a cada gráfico: **¿qué haría alguien distinto después de verlo?** Si la respuesta es «nada», sobra.

### Ejercicio 2 — Agregar en SQL

¿Por qué agregar en SQL y no con *streams*? ¿Cómo se detecta el fallo al corregir?

??? success "Solución"

    Porque un `findAll()` **se trae todas las filas a la memoria de la aplicación** para contarlas allí. Con 4.000 filas no se nota; con 400.000 y varios usuarios a la vez, se agota la memoria.

    Tres motivos concretos:

    1. **Red y memoria.** `SELECT actividad, COUNT(*) ... GROUP BY` devuelve 8 filas. `findAll()` devuelve 400.000 objetos con todos sus campos.
    2. **La base de datos tiene índices y estadísticas**, y treinta años de optimizador. Tu `groupingBy` recorre una lista.
    3. **Puede paralelizar y usar disco** si el conjunto no cabe. Tu JVM se queda sin montón y se cae.

    **Cómo se detecta al corregir:** buscando `findAll()` seguido de `.stream()`. Es la firma del fallo. Y en la aplicación, activando el registro de SQL:

    ```yaml
    spring.jpa.show-sql: true
    ```

    Si para pintar un panel de seis gráficos salen seis `SELECT * FROM comercio`, está mal. Deberían ser seis consultas con `GROUP BY` que devuelven unas pocas filas cada una.

### Ejercicio 3 — Evolución mensual de altas

Escribe la consulta que dé la evolución mensual de altas del último año.

??? success "Solución"

    ```sql
    SELECT date_trunc('month', alta) AS mes,
           COUNT(*)                  AS altas
    FROM comercio
    WHERE alta >= CURRENT_DATE - INTERVAL '12 months'
    GROUP BY mes
    ORDER BY mes;
    ```

    `date_trunc('month', fecha)` lleva cualquier día al día 1 de su mes, que es lo que permite agrupar.

    **El detalle que se falla:** los meses sin ninguna alta **no salen**. El gráfico se dibuja con los meses seguidos y engaña, porque une marzo con mayo como si abril no hubiera existido.

    Se arregla generando la serie de meses y uniéndola por la izquierda:

    ```sql
    WITH meses AS (
        SELECT generate_series(date_trunc('month', CURRENT_DATE - INTERVAL '11 months'),
                               date_trunc('month', CURRENT_DATE),
                               INTERVAL '1 month') AS mes
    )
    SELECT m.mes, COUNT(c.id) AS altas
    FROM meses m
    LEFT JOIN comercio c ON date_trunc('month', c.alta) = m.mes
    GROUP BY m.mes
    ORDER BY m.mes;
    ```

    Ahora los meses vacíos salen con **0**, que es la verdad.

### Ejercicio 4 — `lag()`

¿Para qué sirve y qué calcularías con ella?

??? success "Solución"

    `lag()` es una **función de ventana**: deja ver el valor de la **fila anterior** sin hacer una subconsulta ni traerse nada a Java.

    ```sql
    lag(altas) OVER (ORDER BY mes)
    ```

    Sirve para todo lo que sea comparar con el periodo anterior:

    - **Variación absoluta**: `altas - lag(altas) OVER (ORDER BY mes)`
    - **Variación porcentual**:

      ```sql
      ROUND(100.0 * (altas - lag(altas) OVER (ORDER BY mes))
                  / NULLIF(lag(altas) OVER (ORDER BY mes), 0), 1) AS variacion_pct
      ```

      El `NULLIF` evita la división por cero cuando el mes anterior fue 0.
    - **Detectar saltos raros**: un mes que triplica al anterior suele ser un error de la ingesta, no un milagro comercial.

    La primera fila da `NULL` en `lag`, porque no hay anterior. Hay que tratarlo, o el gráfico saldrá con un hueco.

    Su pareja es `lead()`, que mira hacia adelante, y `sum() OVER (ORDER BY mes)`, que da el acumulado.

### Ejercicio 5 — Media y mediana

Diferencia, con un ejemplo donde la media engañe.

??? success "Solución"

    - **Media**: se suma todo y se divide. Le afectan mucho los valores extremos.
    - **Mediana**: se ordenan y se coge el del medio. La mitad está por debajo y la mitad por encima.

    El ejemplo: superficies de los comercios de una calle, en metros cuadrados.

    ``` { .text .sinajuste }
    40, 45, 50, 55, 60, 8.000        (el último es un centro comercial)
    ```

    - **Media**: 1.375 m². Ningún comercio de la calle se parece a eso.
    - **Mediana**: 52,5 m². Esa sí describe la calle.

    En cuanto hay un valor muy grande —y en datos reales siempre lo hay— **la media deja de describir a nadie**. El caso clásico es el sueldo: la media sube con cuatro directivos, la mediana no.

    En SQL:

    ```sql
    AVG(superficie)                                          AS media,
    PERCENTILE_CONT(0.5) WITHIN GROUP (ORDER BY superficie)  AS mediana
    ```

    **Publica las dos.** Que se parezcan quiere decir que el reparto es sano; que se separen mucho es información en sí misma.

### Ejercicio 6 — OLTP y OLAP

??? success "Solución"

    | | OLTP | OLAP |
    |---|---|---|
    | Para qué | El día a día de la aplicación | Analizar |
    | Operaciones | Muchas, pequeñas, con escrituras | Pocas, enormes, de lectura |
    | Ejemplo | «Guarda esta reseña» | «Comercios por barrio y año» |
    | Se optimiza para | Escribir rápido sin duplicar | Leer y agregar mucho |
    | Diseño | Normalizado | Desnormalizado, por columnas |
    | Cuántas filas toca | Una o pocas | Millones |

    Tu aplicación es **OLTP**: PostgreSQL con las tablas normalizadas de la UT5.

    El problema aparece cuando el cuadro de mando empieza a lanzar consultas de 30 segundos **contra la misma base de datos** que atiende a los usuarios. Los análisis pesados ralentizan la aplicación.

    Las salidas, por orden de coste:

    1. **Vistas materializadas** que se refrescan de noche. Barato y suele bastar.
    2. **Una réplica de solo lectura** para los informes.
    3. **Un almacén analítico aparte**, que ya es otro proyecto.

    En este módulo casi siempre basta la primera. Saber que existe la escalera es lo que se pregunta.

### Ejercicio 7 — La media de un equipo de dos

Un panel muestra la media de sueldos de un equipo de dos personas. ¿Qué problema hay?

??? success "Solución"

    Que **no es un dato agregado: es un dato personal**. Si sé la media de dos y sé mi sueldo, sé el del otro con una resta.

    Es **reidentificación por agregado pequeño**, y no hace falta que el grupo sea de dos: con cuatro o cinco y algún dato de contexto, también se deduce.

    Lo que hay que hacer:

    1. **Umbral mínimo de grupo.** No publicar celdas con menos de 5 individuos:

       ```sql
       GROUP BY equipo
       HAVING COUNT(*) >= 5
       ```

    2. **Agrupar los grupos pequeños** en un «Otros» en vez de suprimirlos a secas.
    3. **Cuidado con el cruce.** Si publicas media por equipo y media por antigüedad, alguien puede cruzar las dos tablas y llegar al individuo aunque cada tabla por separado cumpliera el umbral.

    Y la regla general del tema: **si el resultado permite señalar a una persona, no es analítica, es un dato personal**, con todo lo que eso implica legalmente.

### Ejercicio 8 — Spark para 40.000 registros

Un compañero propone Spark. ¿Qué le respondes?

??? success "Solución"

    Que **40.000 registros caben en la memoria de un móvil**, y que PostgreSQL los agrega en milisegundos.

    Spark está pensado para conjuntos que no caben en una máquina, repartidos en un grupo de servidores. Para 40.000 filas, el tiempo de arrancar el trabajo ya es mayor que el de hacer la consulta entera en SQL.

    Lo que se paga por meterlo sin necesitarlo:

    - Un sistema más que instalar, configurar, actualizar y vigilar.
    - Otro lenguaje y otro modelo mental para el equipo.
    - Depurar se vuelve mucho más difícil.
    - Y el proyecto deja de poder ejecutarse en el portátil de un alumno.

    **Cuándo sí:** cuando los datos no caben en una máquina, o cuando el cálculo tarda horas y se puede repartir. Como referencia grosera, a partir de decenas de millones de filas empieza a tener sentido plantearlo.

    La regla: **empieza por lo simple y sube solo cuando midas que no llega.** Y «no llega» significa un número, no una intuición.
