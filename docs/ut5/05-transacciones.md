# Transacciones e integridad

> Una transacción es la respuesta a una pregunta muy concreta: **si esto falla a mitad, ¿qué pasa con lo que ya se había hecho?** En la UT4, con ficheros, la respuesta era «quedaba a medias y lo arreglabas a mano».

## 1. El problema, con un caso real

Vender un producto son tres operaciones:

```java
public Pedido vender(Long productoId, int unidades, String cliente) {
    var producto = productoRepositorio.findById(productoId).orElseThrow(...);
    producto.restarStock(unidades);                        // 1 · descontar
    var pedido = pedidoRepositorio.save(new Pedido(cliente));   // 2 · crear pedido
    lineaRepositorio.save(new Linea(pedido, producto, unidades));// 3 · crear línea
    return pedido;
}
```

Si el paso 3 falla —se cae la conexión, salta una restricción— te queda **el stock descontado y un pedido sin líneas**. Nadie compró nada y faltan unidades del almacén.

Una transacción convierte esos tres pasos en **uno solo**: o los tres, o ninguno.

```java
@Transactional
public Pedido vender(Long productoId, int unidades, String cliente) { ... }
```

Con esa anotación, si algo falla se deshace todo. Y no hay que escribir el `ROLLBACK`.

## 2. ACID en una tabla

| Propiedad | Qué garantiza |
|---|---|
| **Atomicidad** | Todo o nada |
| **Consistencia** | Al terminar, las reglas de integridad se cumplen |
| **Aislamiento** | Las transacciones concurrentes no se ven a medias |
| **Durabilidad** | Lo confirmado sobrevive a un corte de luz |

Ninguna de las cuatro la tenías con ficheros.

## 3. `@Transactional`, y las cuatro trampas

```java
@Service
@Transactional(readOnly = true)      // por defecto para toda la clase: solo lectura
public class PedidoServicio {

    @Transactional                    // este sí escribe
    public Pedido vender(...) { ... }

    public List<Pedido> listar() { ... }   // hereda readOnly = true
}
```

`readOnly = true` no es cosmético: permite a Hibernate saltarse el *dirty checking* y a la base de datos optimizar. Ponlo por defecto y marca solo lo que escribe.

**Trampa 1 · Solo hace rollback con excepciones no comprobadas.**

```java
@Transactional
public void algo() throws IOException {
    repositorio.save(x);
    throw new IOException("falla");     // MAL  NO deshace: IOException es checked
}

@Transactional(rollbackFor = Exception.class)   // BIEN ahora sí
```

Por defecto solo revierte con `RuntimeException` y `Error`. Es una decisión histórica de Spring que sorprende a todo el mundo una vez.

**Trampa 2 · La llamada desde dentro de la misma clase no pasa por el proxy.**

```java
@Service
public class Servicio {
    public void a() { b(); }                 // MAL  b() NO tiene transacción
    @Transactional public void b() { ... }
}
```

Spring implementa `@Transactional` con un *proxy* que envuelve el bean. Si llamas a `b()` desde `a()`, la llamada es interna y no pasa por el proxy. Solución: mover `b()` a otro bean, o inyectarse a sí mismo.

**Trampa 3 · `@Transactional` en un método privado no hace nada.** El proxy solo intercepta métodos públicos. Y no avisa.

**Trampa 4 · Una transacción larga bloquea.** No metas dentro llamadas HTTP a terceros ni envíos de correo: mantienen abierta la conexión y los bloqueos mientras esperas a otro sistema. Guarda primero, notifica después.

### Propagación, lo justo

| Valor | Qué hace |
|---|---|
| `REQUIRED` *(por defecto)* | Se une a la que haya, o crea una |
| `REQUIRES_NEW` | Suspende la actual y abre otra independiente |
| `MANDATORY` | Falla si no hay una en curso |

`REQUIRES_NEW` sirve para lo que debe persistir **aunque el resto falle**: un registro de auditoría, una traza de intento. Con el resto, `REQUIRED` es lo correcto.

## 4. Integridad: que la base de datos también diga que no

Ya validas en el DTO (formato) y en el servicio (negocio). La base de datos es **la tercera red**, y la única que no se puede saltar:

```java
@Table(name = "productos",
       uniqueConstraints = @UniqueConstraint(name = "uk_producto_nombre", columnNames = "nombre"))

@Column(nullable = false, length = 100)
private String nombre;

@ManyToOne(optional = false)
@JoinColumn(name = "pedido_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_linea_pedido"))
private Pedido pedido;
```

**Por qué hace falta si ya validas en el servicio.** Porque tu servicio no es el único que escribe: hay migraciones, importaciones, un script de mantenimiento, otro equipo. La restricción en la base de datos protege el dato de **todos**, incluido tu yo futuro con prisa.

Y porque la comprobación en el servicio tiene una carrera:

```java
if (repositorio.existsByNombre(nombre))         // dos peticiones simultáneas
    throw new ProductoDuplicadoException(nombre);
repositorio.save(producto);                     // ...pasan las dos por aquí
```

Entre el `exists` y el `save` puede colarse otra petición. La restricción `UNIQUE` no falla nunca.

```java
try {
    return repositorio.save(producto);
} catch (DataIntegrityViolationException e) {
    throw new ProductoDuplicadoException(producto.getNombre());   // → 409
}
```

**El cinturón y los tirantes:** compruebas antes para dar un mensaje bonito, y capturas la violación para el caso de carrera.

## 5. Concurrencia: el problema de la actualización perdida

Dos empleados abren el mismo producto con stock 10. Uno vende 3, otro vende 4. Los dos leyeron 10; el primero guarda 7, el segundo guarda 6. **Han desaparecido 3 unidades** y nadie se ha enterado.

La solución barata y estándar es el **bloqueo optimista**:

```java
@Entity
public class Producto {
    @Version
    private Long version;      // Hibernate la gestiona: no la toques
}
```

Con eso, cada `UPDATE` lleva `WHERE id = ? AND version = ?` e incrementa la versión. El segundo en guardar no encuentra fila que actualizar y recibe `OptimisticLockException`.

```java
@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
public ProblemDetail conflicto(Exception e) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
        "Otro usuario ha modificado este producto. Vuelve a cargarlo e inténtalo de nuevo.");
    pd.setTitle("Conflicto de edición");
    return pd;
}
```

Se llama optimista porque **asume que el choque es raro** y solo lo detecta al guardar. El pesimista (`SELECT ... FOR UPDATE`) bloquea la fila desde la lectura: correcto pero caro, y solo se usa cuando la contienda es alta de verdad.

!!! success "Una anotación contra un fallo invisible"
    La actualización perdida es de los peores bugs que existen: no lanza ningún error, no aparece en los logs y solo se descubre cuando el inventario no cuadra meses después. `@Version` cuesta dos líneas.

---

## Pruébalo ahora (35 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

**Parte 1 — Provoca la inconsistencia.** Escribe `vender` **sin** `@Transactional` y lanza una excepción a propósito después de descontar el stock. Consulta la base de datos: stock descontado, pedido a medias. Añade `@Transactional` y repite: todo intacto.

**Parte 2 — La trampa de la checked.** Con `@Transactional`, lanza una `IOException` en vez de una `RuntimeException`. **No revierte.** Añade `rollbackFor = Exception.class` y vuelve a probar.

**Parte 3 — La llamada interna.** Crea `a()` sin anotar que llama a `b()` anotada, y comprueba que no hay transacción. Muévela a otro servicio y verás que ya funciona.

**Parte 4 — La carrera del duplicado.** Con dos terminales, lanza a la vez:
```bash
for i in 1 2 3 4 5; do (curl -s -o /dev/null -X POST localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" -d '{"nombre":"Carrera","categoria":"seguridad","precio":10,"stock":1}' &) ; done
```
Sin restricción `UNIQUE` acabarás con varios «Carrera». Con ella, uno solo y el resto en 409.

**Parte 5 — La actualización perdida.** Sin `@Version`, simula dos ediciones concurrentes del mismo producto y comprueba que se pierde una. Añade `@Version` y verás el 409.

---

## Ejercicios (con solución)

### Ejercicio 1 — ¿Dónde va `@Transactional`?
¿En el repositorio, en el servicio o en el controlador?

??? success "Solución"

    En el <b>servicio</b>. Es la capa que conoce la <b>unidad de trabajo</b>: «vender» son tres operaciones que deben ir juntas, y eso solo lo sabe el negocio. El repositorio no ve más que su propia operación, y el controlador no debería saber nada de persistencia.<br>
    Los métodos de <code>JpaRepository</code> ya son transaccionales por su cuenta, pero cada uno en la suya: eso no te da atomicidad entre varios.


### Ejercicio 2 — No revierte
Un compañero tiene `@Transactional` y aun así se quedan datos a medias. Da tres causas posibles.

??? success "Solución"

    (1) La excepción es <b>checked</b> y no puso <code>rollbackFor</code>.<br>
    (2) El método se llama <b>desde la misma clase</b>, así que el proxy no interviene y no hay transacción.<br>
    (3) El método es <b>privado</b> —o la clase es final— y el proxy no puede interceptarlo.<br>
    Y una cuarta que aparece de vez en cuando: capturó la excepción dentro del método, así que Spring nunca se enteró de que hubo un fallo.


### Ejercicio 3 — Validar dos veces
Si ya compruebas el nombre duplicado en el servicio, ¿por qué poner `UNIQUE` en la tabla?

??? success "Solución"

    Por dos razones. La primera es la <b>condición de carrera</b>: entre el <code>existsByNombre</code> y el <code>save</code> pueden colarse otras peticiones, y las dos pasan la comprobación. La restricción de la base de datos es atómica y no falla.<br>
    La segunda es que <b>tu servicio no es el único que escribe</b>: hay migraciones, importaciones de CSV, scripts de mantenimiento y, con el tiempo, otras aplicaciones. La restricción protege el dato de todos ellos.<br>
    Se hacen las dos cosas: comprobar antes para dar un mensaje claro, y capturar <code>DataIntegrityViolationException</code> para el caso de carrera.


### Ejercicio 4 — Optimista o pesimista
Un sistema de reserva de butacas de teatro. ¿Qué bloqueo usas?

??? success "Solución"

    Depende de la contienda. En reventa de un concierto agotado, donde cien personas van a por la misma butaca en el mismo segundo, el <b>optimista</b> haría que 99 recibieran un error tras rellenar el formulario: mala experiencia. Ahí encaja el <b>pesimista</b> (<code>SELECT ... FOR UPDATE</code>) o un modelo de <b>reserva temporal</b>: se bloquea la butaca 10 minutos mientras el usuario paga.<br>
    En un teatro de barrio con cuatro reservas al día, <b>optimista</b> y a otra cosa: el choque es tan raro que no compensa el coste de bloquear.


### Ejercicio 5 — Diseña la operación
`devolverPedido`: reintegra el stock de cada línea, marca el pedido como devuelto y registra un movimiento de caja. Escribe la firma y explica las decisiones transaccionales.

??? success "Solución"

    ```java
    @Transactional(rollbackFor = Exception.class)
    public Devolucion devolverPedido(Long pedidoId, String motivo) {
        var pedido = pedidoRepositorio.findConLineas(pedidoId)      // JOIN FETCH: sin N+1
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));

        if (pedido.getEstado() == EstadoPedido.DEVUELTO)
            throw new PedidoYaDevueltoException(pedidoId);          // → 409

        pedido.getLineas().forEach(l -> l.getProducto().sumarStock(l.getUnidades()));
        pedido.setEstado(EstadoPedido.DEVUELTO);
        return cajaRepositorio.save(Devolucion.de(pedido, motivo));
    }
    ```
    Decisiones: <b>una sola transacción</b> para las tres cosas, porque reintegrar stock sin registrar la devolución descuadraría el almacén; <b>rollbackFor</b> por si el registro de caja lanza una checked; <b>JOIN FETCH</b> para no provocar un N+1 al recorrer las líneas; y la comprobación de «ya devuelto» <b>antes</b> de tocar nada, para no reintegrar el stock dos veces.<br>
    Lo que <b>no</b> va dentro: enviar el correo de confirmación al cliente. Eso es una llamada externa que alargaría la transacción; se hace después, o con un evento.

