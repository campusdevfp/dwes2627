# Preparar el examen — UT9

**:material-laptop: Examen práctico · 100 % · 2 sesiones**

## Formato

Se te entrega una aplicación funcionando —con su base de datos, su API y su web— y **dos fuentes externas**: un fichero de datos abiertos y una API pública con clave. Tienes que integrarlas.

Es el examen más parecido a un encargo real de todo el módulo: el enunciado dice *qué* se quiere, no *cómo*.

## Qué se pide

1. **Ingesta** del fichero: limpieza, normalización, descarte contado y carga **idempotente**.
2. **Ejecución programada** más un endpoint manual protegido, con registro de cada ejecución.
3. **Consumo en vivo** de la API externa, con *timeouts*, DTO propio y **valor de reserva**.
4. **Caché** con un tiempo justificado en el README.
5. **Exportación** del listado a PDF o Excel, con la librería aislada tras una interfaz propia.
6. **Vista analítica**: al menos tres agregaciones calculadas **en la base de datos**, una de ellas cruzando las dos fuentes.
7. **Tests**: éxito, error y *timeout* de la API externa, más el de idempotencia.
8. **README** con la tabla de fuentes, licencias y variables de entorno.

## Rúbrica

| Criterio | Peso | Qué se mira |
|---|---:|---|
| 1. Arranca y navega *(eliminatorio)* | 10 % | `docker compose up` o `mvn spring-boot:run`, y las páginas responden |
| 2. **Resiliencia** | 20 % | **La aplicación funciona con las fuentes externas apagadas**; *timeouts*, reserva, reintentos |
| 3. **Ingesta idempotente** | 20 % | Ejecutarla dos veces no duplica; limpieza y descartes contados |
| 4. Consumo del servicio externo | 15 % | Clave fuera del código, DTO propio, caché justificada |
| 5. Librería aislada | 10 % | Interfaz propia; se puede cambiar la implementación tocando una clase |
| 6. Analítica en la base de datos | 15 % | `group by` en la consulta, no `findAll()` + `stream()`; cruce de fuentes |
| 7. Tests y documentación | 10 % | `mvn test` verde **sin red**; README con fuentes y licencias |

**Eliminatorio:** si no arranca, la nota máxima es 4.

## Cómo se corrige el criterio 2

Literalmente así, y no admite discusión:

```bash
# se corta la salida a internet
sudo iptables -A OUTPUT -p tcp --dport 443 -j REJECT
```

Se navega la aplicación entera. **Todo debe verse** salvo el recuadro que dependía de la API, que muestra «información no disponible». Un error 500 en cualquier página es un cero en este criterio, aunque con conexión funcione de maravilla.

Y el criterio 3:

```bash
curl -X POST localhost:8080/admin/ingesta -H "..." && \
psql -c "select count(*) from comercio"
curl -X POST localhost:8080/admin/ingesta -H "..." && \
psql -c "select count(*) from comercio"
```

Los dos números tienen que coincidir.

## Errores que más cuestan

1. **La clave de la API en el `application.yml`.** Fallo de seguridad grave, y además queda en el historial de Git para siempre.
2. **Sin `readTimeout`.** Una API lenta deja tu aplicación colgada. Se demuestra con un servidor que no responde.
3. **La página revienta si el tercero no contesta.** Es el error conceptual central de la unidad.
4. **Ingesta que duplica.** Casi siempre por no tener clave natural ni restricción de unicidad.
5. **`split(";")` sin el `-1`.** Se comen las columnas vacías del final y los datos quedan desplazados, sin dar ningún error.
6. **No contar los descartes.** «Faltan 300 comercios» sin log es imposible de explicar.
7. **`findAll()` + `stream()` para agregar.** Funciona con 50 filas y muere con 200.000.
8. **Reintentar operaciones no idempotentes.** Un POST reintentado tres veces puede cobrar tres veces.
9. **Usar la estructura JSON del tercero como modelo propio.** El día que cambien un campo, se rompen cincuenta sitios.
10. **Tests que llaman de verdad a la API.** Lentos, gastan cuota y fallan cuando el tercero está de mantenimiento.
11. **Librería sin aislar.** `OpenPDF` esparcido por cinco clases.
12. **README sin licencias de las fuentes.** Es criterio explícito y cuesta cinco minutos.

## Cómo prepararte

1. Rehaz la **F4** del reto sobre otro CSV público. Es el 20 % de la nota y es puro oficio: cuanto más lo hagas, más rápido sale.
2. Ejecuta la prueba del `/etc/hosts` contra tu proyecto. Si alguna página falla, ahí tienes el trabajo pendiente.
3. Desconecta el wifi y lanza `mvn test`.
4. Escribe el README **antes** del examen, con la tabla vacía. El día de la prueba solo tendrás que rellenarla.
