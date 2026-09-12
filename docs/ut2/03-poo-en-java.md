# POO en Java: clases, records e interfaces

> La programación orientada a objetos es la forma en que Java organiza el código — y la base de todo Spring. Aquí va desde la clase clásica hasta los **records**, que te ahorrarán cientos de líneas en las APIs.

## 1. Clases y objetos

Una **clase** es el molde; un **objeto**, cada ejemplar creado con ese molde.

``` { .java .numerado }
public class Producto {
    // 1. Atributos (estado) — private: encapsulación
    private String nombre;
    private double precio;

    // 2. Constructor: cómo se crea
    public Producto(String nombre, double precio) {
        this.nombre = nombre;     // this = "el objeto actual"
        this.precio = precio;
    }

    // 3. Métodos (comportamiento)
    public double precioConIva() {
        return precio * 1.21;
    }

    // 4. Getters: acceso controlado
    public String getNombre() { return nombre; }
    public double getPrecio() { return precio; }
}
```

```java
var patinete = new Producto("Patinete", 120.0);
IO.println(patinete.getNombre());        // Patinete
IO.println(patinete.precioConIva());     // 145.2
```

## 2. Encapsulación: por qué `private`

Los atributos van `private` y se accede por métodos. No es burocracia: es **control**.

```java
public void setPrecio(double precio) {
    if (precio < 0) throw new IllegalArgumentException("Precio negativo");
    this.precio = precio;
}
```

Si el atributo fuera público, cualquiera podría poner un precio negativo y romper las reglas del negocio. **Encapsular = proteger los invariantes de tu objeto.**

| Modificador | Quién puede acceder |
|---|---|
| `private` | Solo la propia clase (por defecto para atributos) |
| `protected` | La clase y sus hijas |
| *(sin modificador)* | Clases del mismo paquete |
| `public` | Todo el mundo (métodos de la API pública) |

## 3. Records: clases de datos en una línea 

Java 16+ trae los **records**: para objetos que solo transportan datos, todo el código repetitivo desaparece.

```java
public record Producto(String nombre, double precio) {}
```

Esa línea te da **gratis**: constructor, getters (`nombre()`, `precio()`), `equals()`, `hashCode()` y `toString()`. Y los objetos son **inmutables** (no se pueden modificar tras crearlos), lo que evita una familia entera de bugs.

```java
var p = new Producto("Patinete", 120.0);
IO.println(p.nombre());       // Patinete   (ojo: sin "get")
IO.println(p);                // Producto[nombre=Patinete, precio=120.0]
IO.println(p.equals(new Producto("Patinete", 120.0)));   // true
```

Puedes añadir validación con un **constructor compacto**:

```java
public record Producto(String nombre, double precio) {
    public Producto {
        if (precio < 0) throw new IllegalArgumentException("Precio negativo");
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Sin nombre");
    }
}
```

!!! success "Los usarás constantemente"
    En las APIs REST, los **DTO** (los objetos que viajan como JSON) serán records. Compara: 4 líneas frente a las ~50 de una clase clásica con getters, `equals` y `toString`.

**¿Clase o record?** Record si el objeto **solo transporta datos** y es inmutable (DTOs, respuestas de API, coordenadas). Clase si tiene **comportamiento y estado que cambia** (un servicio, una entidad que se modifica).

## 4. Enums: conjuntos cerrados de valores

```java
public enum Estado { PENDIENTE, ENVIADO, ENTREGADO, CANCELADO }

var estado = Estado.PENDIENTE;
var mensaje = switch (estado) {
    case PENDIENTE -> "Preparando tu pedido";
    case ENVIADO   -> "En camino";
    case ENTREGADO -> "Entregado";
    case CANCELADO -> "Cancelado";
};
```

Mejor que usar Strings ("pendiente", "Pendiente", "PENDIENTE"…): el compilador **garantiza** que solo existen esos valores y que cubres todos los casos en el `switch`.

## 5. Herencia: reutilizar comportamiento

```java
public class Empleado {
    protected String nombre;
    public Empleado(String nombre) { this.nombre = nombre; }
    public double salario() { return 1200; }
}

public class Jefe extends Empleado {
    public Jefe(String nombre) { super(nombre); }   // llama al constructor padre

    @Override
    public double salario() { return 2500; }        // redefine
}
```

`@Override` no es obligatorio, pero **póntelo siempre**: si te equivocas en el nombre del método, el compilador te avisa.

:material-alert: **Úsala con cabeza.** La herencia acopla mucho: un cambio en el padre afecta a todos los hijos. La regla profesional es *"composición sobre herencia"*: antes de heredar, pregúntate si no es mejor que tu clase **use** otra en vez de **ser** otra.

## 6. Interfaces y polimorfismo 

Una **interfaz** es un contrato: dice *qué* se puede hacer, no *cómo*.

```java
public interface Notificador {
    void enviar(String mensaje);       // sin cuerpo: es un contrato
}

public class NotificadorEmail implements Notificador {
    public void enviar(String mensaje) { IO.println("[correo] " + mensaje); }
}

public class NotificadorSms implements Notificador {
    public void enviar(String mensaje) { IO.println("[SMS] " + mensaje); }
}
```

El **polimorfismo** es que puedas tratarlos igual:

```java
List<Notificador> canales = List.of(new NotificadorEmail(), new NotificadorSms());
for (var c : canales) c.enviar("Tu pedido ha salido");
```

Esto es **exactamente** el principio DIP de SOLID que viste en la UT1, y la base de la **inyección de dependencias** de Spring: tu servicio dependerá de la interfaz `Notificador`, y Spring decidirá qué implementación inyectar. Cambiar de email a SMS no tocará ni una línea del servicio.

**Sealed** (Java 17+) cierra la jerarquía cuando quieres controlar quién implementa:

```java
public sealed interface Pago permits Tarjeta, Bizum, Transferencia {}
```

---

## Pruébalo ahora (12 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Crea `Tienda.java` con esto y ejecútalo:

```java
record Producto(String nombre, double precio) {
    Producto {
        if (precio < 0) throw new IllegalArgumentException("Precio negativo");
    }
    double precioConIva() { return precio * 1.21; }
}

void main() {
    var p1 = new Producto("Patinete", 120.0);
    var p2 = new Producto("Patinete", 120.0);

    IO.println(p1);                    // toString gratis
    IO.println(p1.equals(p2));         // equals gratis → true
    IO.println(p1.precioConIva());

    // Descomenta y mira la excepción:
    // var malo = new Producto("Roto", -5);
}
```

Fíjate: **un record puede tener métodos**. Lo que no puede es cambiar sus datos.

---

## Ejercicios (con solución)

### Ejercicio 1 — De clase a record
Convierte esta clase en un record y di cuántas líneas te ahorras:
```java
public class Coordenada {
    private final double lat, lon;
    public Coordenada(double lat, double lon) { this.lat = lat; this.lon = lon; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    @Override public String toString() { return "Coordenada[" + lat + ", " + lon + "]"; }
    @Override public boolean equals(Object o) { /* 8 líneas más */ return false; }
}
```

??? success "Solución"

    ```java
    public record Coordenada(double lat, double lon) {}
    ```
    De ~15 líneas a 1. Ojo: los getters pasan a ser <code>lat()</code> y <code>lon()</code>, sin <code>get</code>.


### Ejercicio 2 — ¿Qué imprime?
```java
record Punto(int x, int y) {}

void main() {
    var a = new Punto(1, 2);
    var b = new Punto(1, 2);
    IO.println(a == b);
    IO.println(a.equals(b));
}
```

??? success "Solución"

    <code>false</code> y <code>true</code>. Son dos objetos distintos en memoria (por eso <code>==</code> es false), pero el record genera un <code>equals()</code> que compara los <b>valores</b> de sus campos. Es la misma trampa del <code>==</code> con Strings.


### Ejercicio 3 — Diseña con interfaces
Una tienda debe poder cobrar con **tarjeta**, **Bizum** y **transferencia**. Diseña la interfaz y una implementación, y escribe el bucle que cobra 50 € por los tres métodos. ¿Qué principio SOLID estás aplicando?

??? success "Solución"

    ```java
    interface MetodoPago {
        void cobrar(double importe);
    }

    class Tarjeta implements MetodoPago {
        public void cobrar(double importe) { IO.println("Cobrados " + importe + " € con tarjeta"); }
    }
    class Bizum implements MetodoPago {
        public void cobrar(double importe) { IO.println("Bizum de " + importe + " €"); }
    }
    class Transferencia implements MetodoPago {
        public void cobrar(double importe) { IO.println("Transferencia bancaria de " + importe + " €"); }
    }

    void main() {
        List<MetodoPago> metodos = List.of(new Tarjeta(), new Bizum(), new Transferencia());
        for (var m : metodos) m.cobrar(50);
    }
    ```
    Aplicas <b>OCP</b> (añadir un método de pago nuevo no obliga a tocar el código existente) y <b>DIP</b> (dependes de la abstracción <code>MetodoPago</code>, no de clases concretas). Es el patrón que Spring automatiza con inyección de dependencias.


### Ejercicio 4 — ¿Clase o record?
Decide para cada caso: (a) la respuesta JSON de la API con id, nombre y precio · (b) un carrito al que se añaden y quitan productos · (c) las coordenadas de un punto en un mapa · (d) el servicio que calcula descuentos.

??? success "Solución"

    (a) <b>Record</b> (DTO inmutable) · (b) <b>Clase</b> (su estado cambia) · (c) <b>Record</b> (valor inmutable) · (d) <b>Clase</b> (tiene comportamiento, será un <code>@Service</code> de Spring).

