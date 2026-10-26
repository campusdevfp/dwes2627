# 2. Paginación, criterios y el N+1

Tres cosas, y las tres se resuelven con lo que ya trae Spring Data:

| | Qué resuelve |
|:-:|---|
| **2.1** | **Paginar y ordenar** sin escribir `LIMIT` ni `OFFSET` |
| **2.2** | **Filtros opcionales** que se combinan, sin concatenar SQL |
| **2.3** | **El N+1**: traer una relación en una consulta en vez de en N |

---

## 2.1. Paginación y ordenación

```java title="El repositorio: una línea"
Page<Funko> findByDeletedFalse(Pageable pageable);
```

```java title="El servicio"
@Override
@Transactional(readOnly = true)
public Page<FunkoResponse> findAll(Pageable pageable) {
    return repositorio.findByDeletedFalse(pageable).map(mapper::toResponse);
}
```

```java title="El controlador"
@GetMapping
public ResponseEntity<Page<FunkoResponse>> findAll(
        @PageableDefault(size = 20, sort = "id") Pageable pageable) {
    return ResponseEntity.ok(servicio.findAll(sanear(pageable)));
}
```

**Y ya funciona:**

```bash
curl -s "localhost:8080/api/v1/funkos?page=0&size=2&sort=precio,desc" \
  | jq '{total: .totalElements, paginas: .totalPages, nombres: [.content[].nombre]}'
# { "total": 6, "paginas": 3, "nombres": ["Iron Man", "Goku"] }
```

!!! success "Los tres parámetros, que son estándar de Spring"
    | | Qué | Ejemplo |
    |---|---|---|
    | `page` | La página, **empezando en 0** | `?page=2` |
    | `size` | Elementos por página | `?size=20` |
    | `sort` | `campo,dirección`, **repetible** | `?sort=precio,desc&sort=nombre,asc` |

    No hay que parsearlos: `Pageable` los recibe resueltos. Y el `.map(mapper::toResponse)` del servicio **conserva la paginación** (`totalElements`, `totalPages`) y devuelve **DTOs, no entidades**.

### Las tres opciones, y cuál elegir

| | Qué trae | Consultas |
|---|---|:-:|
| `List<T>` | Solo los elementos | **1** |
| `Slice<T>` | Elementos + «¿hay más?» | **1** (pide `size+1`) |
| **`Page<T>`** | Elementos + `totalElements` + `totalPages` | **2** |

**`Page` si la interfaz tiene «página 3 de 47». `Slice` si es un *scroll* infinito con «cargar más»**, y te ahorras el `SELECT count(*)` de cada petición. `List` si de verdad quieres todo.

### Sanear lo que llega: no es paranoia

```java title="El controlador"
/** Lista blanca de campos ordenables y tope de tamaño. */
private Pageable sanear(Pageable p) {
    var permitidos = Set.of("id", "nombre", "precio", "cantidad", "createdAt");
    var orden = p.getSort().stream()
            .filter(o -> permitidos.contains(o.getProperty()))
            .toList();
    return PageRequest.of(p.getPageNumber(),
                          Math.min(p.getPageSize(), 100),
                          orden.isEmpty() ? Sort.by("id") : Sort.by(orden));
}
```

```properties
spring.data.web.pageable.default-page-size=20
spring.data.web.pageable.max-page-size=100
```

!!! danger "Lo que pasa sin esto"
    | Petición | Sin sanear | Saneado |
    |---|---|---|
    | `?sort=inventado` | `PropertyReferenceException` → **500** | 200, ordena por `id` |
    | `?size=1000000` | Carga la tabla entera: **has paginado para nada** | Recorta a 100 |
    | `?sort=passwordHash` | Ordena por un campo que no publicas: **revela el orden relativo de los hashes** | Lo ignora |

    El primero es el que más cae en el examen, porque es un 500 que provoca el cliente con un parámetro.

---

## 2.2. Criterios de búsqueda combinables

El problema: tres filtros **opcionales** que pueden venir en cualquier combinación. Son **ocho** consultas distintas.

=== "❌ Lo que NO se hace"

    ```java
    var sql = new StringBuilder("SELECT * FROM funkos WHERE 1 = 1");
    if (categoria != null) sql.append(" AND categoria = '" + categoria + "'");
    ```

    Concatenar texto SQL con datos del cliente. Y ocho métodos `findByXAndY`, `findByX`, `findByY`… que no escalan: con cuatro filtros son dieciséis.

=== "✅ `Specification`"

    ```java title="El repositorio"
    public interface FunkosRepository
            extends JpaRepository<Funko, Long>, JpaSpecificationExecutor<Funko> { }
    ```

    ```java title="El servicio"
    @Override
    @Transactional(readOnly = true)
    public Page<FunkoResponse> buscar(Optional<String> categoria,
                                      Optional<String> nombre,
                                      Optional<BigDecimal> precioMax,
                                      Pageable pageable) {

        // El filtro base: siempre se aplica
        Specification<Funko> spec = (root, q, cb) -> cb.isFalse(root.get("deleted"));

        spec = categoria.map(c -> spec.and((root, q, cb) ->
                    cb.equal(cb.upper(root.get("categoria").get("nombre")), c.toUpperCase())))
                .orElse(spec);

        spec = nombre.map(n -> spec.and((root, q, cb) ->
                    cb.like(cb.lower(root.get("nombre")), "%" + n.toLowerCase() + "%")))
                .orElse(spec);

        spec = precioMax.map(p -> spec.and((root, q, cb) ->
                    cb.lessThanOrEqualTo(root.get("precio"), p)))
                .orElse(spec);

        return repositorio.findAll(spec, pageable).map(mapper::toResponse);
    }
    ```

!!! success "Cómo se lee una `Specification`"
    ```java
    (root, query, cb) -> cb.equal(root.get("nombre"), "Goku")
    //  │      │      │              │
    //  │      │      │              └── el campo de la entidad
    //  │      │      └── CriteriaBuilder: equal, like, lessThan, isFalse, and, or
    //  │      └── la consulta, casi nunca se usa
    //  └── la entidad: root.get("categoria").get("nombre") navega la relación
    ```

    | Lo que quieres | El método |
    |---|---|
    | `= valor` | `cb.equal(root.get("campo"), v)` |
    | `LIKE '%v%'` | `cb.like(cb.lower(root.get("campo")), "%"+v+"%")` |
    | `<= valor` | `cb.lessThanOrEqualTo(root.get("campo"), v)` |
    | `BETWEEN` | `cb.between(root.get("campo"), a, b)` |
    | `= false` | `cb.isFalse(root.get("campo"))` |
    | Navegar una relación | `root.get("categoria").get("nombre")` |

    **Cada filtro es un objeto que se compone con `.and()`.** Tres filtros opcionales son tres líneas, no ocho métodos — y **en ningún momento se concatena texto SQL**, así que no hay forma de introducir una inyección.

!!! info "Cuándo NO usar `Specification`"
    Con **uno o dos** filtros opcionales, dos métodos derivados son más legibles. `Specification` empieza a pagar a partir de **tres**, y es obligatoria cuando los filtros se combinan libremente.

---

## 2.3. El N+1 y cómo se mide

```java
// El repositorio así:
Page<Funko> findByDeletedFalse(Pageable pageable);
```

```java
// Y el mapeador toca la relación:
public FunkoResponse toResponse(Funko f) {
    return new FunkoResponse(…, f.getCategoria().getNombre(), …);
}
```

Con `?size=20`, eso son **21 consultas**: una para la página y **una por cada funko** para ir a buscar su categoría.

```properties title="Cómo se ve"
spring.jpa.show-sql=true
```

```
Hibernate: select f1_0.id, … from funkos f1_0 where f1_0.is_deleted = false limit ?
Hibernate: select c1_0.id, c1_0.nombre from categorias c1_0 where c1_0.id = ?
Hibernate: select c1_0.id, c1_0.nombre from categorias c1_0 where c1_0.id = ?
Hibernate: select c1_0.id, c1_0.nombre from categorias c1_0 where c1_0.id = ?
…                                          ← y así 20 veces
```

**Contar esas líneas es la forma de corregirlo**, y es lo que se hace en el examen.

### Las dos soluciones, y cuál elegir

=== "`@EntityGraph` ✅ por defecto"

    ```java
    @EntityGraph(attributePaths = {"categoria"})
    Page<Funko> findByDeletedFalse(Pageable pageable);
    ```

    Una línea, se combina con **cualquier** consulta derivada y **funciona con `Pageable`**. De 21 consultas a **2**.

=== "`JOIN FETCH` para controlar el JPQL"

    ```java
    @Query(value      = """
                        SELECT f FROM Funko f JOIN FETCH f.categoria
                         WHERE f.deleted = false
                        """,
           countQuery = "SELECT count(f) FROM Funko f WHERE f.deleted = false")
    Page<Funko> findAllConCategoria(Pageable pageable);
    ```

    **El `countQuery` es obligatorio** cuando devuelves `Page`: sin él, Hibernate intenta contar sobre la consulta con el fetch y falla.

!!! danger "`JOIN FETCH` de una COLECCIÓN con `Pageable` no se puede"
    ```
    HHH90003004: firstResult/maxResults specified with collection fetch; applying in memory
    ```

    Es un **aviso**, no un error, y lo que hace es **traer la tabla entera y paginar en memoria**: justo lo que querías evitar. Con 100.000 filas, la aplicación se cae.

    | | Con `Pageable` |
    |---|---|
    | `@ManyToOne` (una categoría) | **Sí**: `@EntityGraph` o `JOIN FETCH` |
    | `@OneToMany` (una lista) | **No**: carga la lista en otra consulta aparte |

---

## Casos especiales

### `?sort=` con un campo de una relación

```
?sort=categoria.nombre,asc
```

**Funciona**, y Spring Data genera el `JOIN`. Pero ojo con la lista blanca del `sanear`: si solo permites `"nombre"`, esto se descarta en silencio. Si quieres permitirlo, el valor a meter en el `Set` es exactamente `"categoria.nombre"`.

### La página que se pide fuera de rango

```bash
curl -s "localhost:8080/api/v1/funkos?page=500" | jq '{content, totalElements}'
# { "content": [], "totalElements": 6 }
```

**200 con una lista vacía, no 404.** Es correcto: la colección existe, lo que no hay son elementos en esa página. Un 404 aquí sería un error de diseño.

### Paginar sin paginar

```java
Pageable.unpaged()
```

Devuelve un `Page` con todo dentro. Sirve para reutilizar un método paginado desde un sitio que no pagina —un informe, un resolutor de GraphQL en la [UT6](../ut6/02-graphql.md)— pero **no la uses en un endpoint público**: es el `?size=1000000` que acabas de prohibir, escrito por ti.

### `count` que sobra

```java
Page<Funko> page = repositorio.findByDeletedFalse(pageable);
```

Esa línea hace **dos** consultas: la de datos y el `SELECT count(*)`. En una tabla grande, el `count` puede tardar más que los datos.

Si la interfaz solo necesita «siguiente/anterior», cambia el tipo de retorno a **`Slice<Funko>`** y desaparece la segunda consulta. No hay que tocar nada más: el repositorio, el servicio y el controlador son iguales.

### Devolver el `Page` tal cual en el JSON

```json
{ "content": [ … ], "pageable": { … }, "totalElements": 6, "last": false, … }
```

Funciona y es lo que hacemos en la unidad, pero Spring avisa en el log:

```
Serializing PageImpl instances as-is is not supported
```

El motivo es que **la forma de ese JSON no es estable entre versiones de Spring**, así que un cliente que dependa de `pageable.pageNumber` puede romperse al actualizar. En una API real se devuelve un DTO propio de página con los cuatro campos que te interesan. **Para el examen, el `Page` directo vale.**
