# 3. POO en Java

Cuatro herramientas y un ejemplo largo al final. Nada más.

!!! tip "Todo esto se prueba en `jshell`"
    Abre una terminal y escribe `jshell`. Es un Java interactivo: escribes una línea, pulsa Intro y ves el resultado. Sin proyecto, sin `main`, sin compilar.

    ```
    $ jshell
    |  Welcome to JShell -- Version 25
    jshell> 2 + 2
    $1 ==> 4
    ```

    Para salir, `/exit`. Para borrar todo y empezar de cero, `/reset`. **Los ejemplos de esta página están pensados para copiarlos ahí y ver qué pasa.**

---

## 1. Una clase es un molde

Una **clase** describe cómo son las cosas de un tipo; un **objeto** es una de esas cosas.

```java
jshell> class Alumno {
   ...>     String nombre;
   ...>     int nota;
   ...> }
|  created class Alumno

jshell> var ana = new Alumno()
ana ==> Alumno@76ed5528

jshell> ana.nombre = "Ana"
$3 ==> "Ana"

jshell> ana.nota = 8
$4 ==> 8

jshell> ana.nombre + " saca " + ana.nota
$5 ==> "Ana saca 8"
```

`new Alumno()` construye un objeto. `ana` no *es* el objeto: es una **referencia** que apunta a él. Eso explica esto, que sorprende siempre:

```java
jshell> var otra = ana
otra ==> Alumno@76ed5528

jshell> otra.nota = 3
$7 ==> 3

jshell> ana.nota
$8 ==> 3        // ¡cambió también!
```

**Hay un solo objeto y dos referencias apuntándolo.** No se ha copiado nada. Es la causa de la mitad de los errores raros de un principiante.

### El constructor

Crear el objeto y rellenarlo campo a campo es incómodo y permite dejarlo a medias. El **constructor** obliga a dar los datos desde el principio:

```java
jshell> class Alumno {
   ...>     String nombre;
   ...>     int nota;
   ...>     Alumno(String nombre, int nota) {
   ...>         this.nombre = nombre;
   ...>         this.nota = nota;
   ...>     }
   ...> }

jshell> var bruno = new Alumno("Bruno", 7)
jshell> bruno.nombre
$3 ==> "Bruno"
```

`this.nombre` es el campo del objeto; `nombre` a secas es el parámetro. Se llaman igual **a propósito**, y `this` los distingue.

!!! warning "En cuanto escribes un constructor, pierdes el que venía gratis"
    Con el constructor de arriba, `new Alumno()` **ya no compila**. Java te regala un constructor vacío solo si no defines ninguno.

---

## 2. Por qué `private`

Los campos de arriba son públicos: cualquiera puede escribir lo que quiera.

```java
jshell> bruno.nota = -47
$5 ==> -47          // nadie ha protestado
```

Una nota de −47 no existe. El objeto está en un estado imposible y **el error aparecerá mucho después**, en otro sitio, cuando alguien calcule una media.

La solución es cerrar los campos y dejar solo puertas controladas:

```java
jshell> class Alumno {
   ...>     private final String nombre;      // (1)
   ...>     private int nota;
   ...>
   ...>     Alumno(String nombre, int nota) {
   ...>         if (nombre == null || nombre.isBlank())
   ...>             throw new IllegalArgumentException("Nombre obligatorio");
   ...>         this.nombre = nombre;
   ...>         setNota(nota);                // (2)
   ...>     }
   ...>
   ...>     String getNombre() { return nombre; }
   ...>     int getNota()      { return nota; }
   ...>
   ...>     void setNota(int nota) {
   ...>         if (nota < 0 || nota > 10)
   ...>             throw new IllegalArgumentException("Nota fuera de rango: " + nota);
   ...>         this.nota = nota;
   ...>     }
   ...> }

jshell> new Alumno("Carla", 25)
|  Exception java.lang.IllegalArgumentException: Nota fuera de rango: 25
```

1. `final` significa que **no cambia nunca** después de construirse. El nombre de un alumno no cambia; la nota sí.
2. El constructor llama al *setter* para no repetir la validación en dos sitios.

**El error salta en el momento exacto en que alguien intenta hacer la barbaridad**, con un mensaje que dice qué pasó. Eso es toda la ventaja de `private`.

!!! danger "El *getter* que se cuela"
    Un *getter* que devuelve algo modificable deshace la protección:

    ```java
    class Grupo {
        private final List<Alumno> alumnos = new ArrayList<>();
        List<Alumno> getAlumnos() { return alumnos; }   // ← fuga
    }
    ```

    `grupo.getAlumnos().clear()` vacía la lista interna desde fuera. Se arregla devolviendo una copia inmutable: `return List.copyOf(alumnos);`

---

## 3. `record`: cuando los datos son los datos

Escribir constructor, *getters*, `equals`, `hashCode` y `toString` para una clase que solo transporta datos son cincuenta líneas que no aportan nada. Un `record` las escribe por ti:

```java
jshell> record Punto(int x, int y) { }
|  created record Punto

jshell> var p = new Punto(3, 4)
p ==> Punto[x=3, y=4]              // toString automático

jshell> p.x()                      // getter: se llama como el campo
$3 ==> 3

jshell> new Punto(3, 4).equals(new Punto(3, 4))
$4 ==> true                        // equals automático
```

Compáralo con una clase normal:

```java
jshell> class PuntoClase {
   ...>     int x, y;
   ...>     PuntoClase(int x, int y) { this.x = x; this.y = y; }
   ...> }

jshell> new PuntoClase(3, 4).equals(new PuntoClase(3, 4))
$6 ==> false        // ← compara referencias, no contenido
```

**Ese `false` es el error que más se sufre.** Sin `equals`, dos objetos con los mismos datos son «distintos», y entonces `lista.contains(...)` no encuentra nada y `mapa.get(...)` devuelve `null`.

### Validar en un `record`

```java
jshell> record Producto(String nombre, double precio) {
   ...>     Producto {                                     // (1)
   ...>         if (precio < 0) throw new IllegalArgumentException("Precio negativo");
   ...>         if (nombre == null || nombre.isBlank())
   ...>             throw new IllegalArgumentException("Nombre vacío");
   ...>     }
   ...>     double precioConIva() { return precio * 1.21; } // (2)
   ...> }

jshell> new Producto("Casco", 35).precioConIva()
$8 ==> 42.35

jshell> new Producto("Casco", -5)
|  Exception java.lang.IllegalArgumentException: Precio negativo
```

1. **Constructor compacto**: sin paréntesis. Se ejecuta antes de asignar los campos.
2. Un `record` puede tener métodos como cualquier clase.

!!! info "Cuándo `record` y cuándo clase"
    | | Usa |
    |---|---|
    | El objeto **no cambia** nunca: un punto, un DTO, una línea de un CSV | **`record`** |
    | El objeto **cambia de estado**: un pedido que pasa a ENVIADO, un carrito | **Clase** |

    Un `record` es inmutable: no hay `setX`. Si necesitas «cambiarlo», se devuelve uno nuevo:

    ```java
    jshell> record Pedido(int id, String estado) {
       ...>     Pedido con(String nuevo) { return new Pedido(id, nuevo); }
       ...> }

    jshell> new Pedido(1, "PENDIENTE").con("ENVIADO")
    $10 ==> Pedido[id=1, estado=ENVIADO]
    ```

---

## 4. `enum`: conjuntos cerrados

Cuando algo solo puede tomar unos valores concretos, un `String` es una mala idea:

```java
var estado = "ENVAIDO";     // compila. Y está mal escrito.
```

Un `enum` cierra el conjunto y el compilador vigila:

```java
jshell> enum Estado { PENDIENTE, PAGADO, ENVIADO, ENTREGADO }

jshell> var e = Estado.ENVIADO
e ==> ENVIADO

jshell> Estado.valueOf("ENVAIDO")
|  Exception java.lang.IllegalArgumentException: No enum constant Estado.ENVAIDO
```

Y en Java un `enum` **es una clase**: puede llevar datos y métodos dentro.

```java
jshell> enum Estado {
   ...>     PENDIENTE("Pendiente de pago", false),
   ...>     PAGADO("Pagado", false),
   ...>     ENVIADO("En reparto", true),
   ...>     ENTREGADO("Entregado", true);
   ...>
   ...>     private final String descripcion;
   ...>     private final boolean enCamino;
   ...>
   ...>     Estado(String descripcion, boolean enCamino) {
   ...>         this.descripcion = descripcion;
   ...>         this.enCamino = enCamino;
   ...>     }
   ...>
   ...>     String descripcion() { return descripcion; }
   ...>     boolean enCamino()   { return enCamino; }
   ...> }

jshell> Estado.ENVIADO.descripcion()
$14 ==> "En reparto"

jshell> for (var s : Estado.values()) System.out.println(s + " → " + s.descripcion());
PENDIENTE → Pendiente de pago
PAGADO → Pagado
ENVIADO → En reparto
ENTREGADO → Entregado
```

El dato vive **donde vive el concepto**, en vez de repartirse por quince `switch` distintos.

---

## 5. Interfaces: el contrato

Una **interfaz** dice *qué* se puede hacer, sin decir *cómo*. Es lo más importante de esta página y la base de todo lo que viene desde la UT4.

```java
jshell> interface Notificador {
   ...>     void enviar(String destinatario, String mensaje);
   ...> }

jshell> class NotificadorCorreo implements Notificador {
   ...>     public void enviar(String destinatario, String mensaje) {
   ...>         System.out.println("[correo a " + destinatario + "] " + mensaje);
   ...>     }
   ...> }

jshell> class NotificadorSms implements Notificador {
   ...>     public void enviar(String destinatario, String mensaje) {
   ...>         System.out.println("[SMS a " + destinatario + "] " + mensaje);
   ...>     }
   ...> }
```

Y ahora lo que importa: **un método que funciona con cualquiera de los dos**.

```java
jshell> void avisar(Notificador n, String quien) {
   ...>     n.enviar(quien, "Tu pedido ha salido");
   ...> }

jshell> avisar(new NotificadorCorreo(), "ana@iesx.es")
[correo a ana@iesx.es] Tu pedido ha salido

jshell> avisar(new NotificadorSms(), "600123456")
[SMS a 600123456] Tu pedido ha salido
```

`avisar` **no sabe ni le importa** cuál le han pasado. Eso es **polimorfismo**, y su utilidad práctica es esta: el día que aparezca `NotificadorWhatsApp`, `avisar` no se toca.

!!! reto "La pregunta que hay que saber contestar"
    *«¿Para qué sirve una interfaz?»* La respuesta que puntúa no es «para desacoplar». Es:

    1. **Para poder cambiar la implementación sin tocar a quien la usa.**
    2. **Para poder probar.** En un test le pasas un `Notificador` de mentira que solo apunta lo que le piden, y compruebas el servicio sin mandar un solo correo.

    Ese segundo punto es el que se olvida y el que más vale.

---

## 6. Ejemplo largo: un sistema de pedidos, paso a paso

Ahora todo junto. Construimos un sistema de pedidos en **cinco pasos**, y en cada uno se ve por qué hace falta la pieza siguiente. Puedes teclearlo entero en `jshell`.

### Paso 1 · Los datos

Un pedido no cambia de forma; las líneas tampoco. **Records.**

```java
record Linea(String producto, int unidades, double precioUnidad) {
    Linea {
        if (unidades <= 0) throw new IllegalArgumentException("Unidades: " + unidades);
        if (precioUnidad < 0) throw new IllegalArgumentException("Precio negativo");
    }
    double importe() { return unidades * precioUnidad; }
}
```

```java
jshell> new Linea("Casco", 2, 35.0).importe()
$3 ==> 70.0
```

### Paso 2 · El estado, cerrado

```java
enum Estado { PENDIENTE, PAGADO, ENVIADO, ENTREGADO, CANCELADO }
```

### Paso 3 · La clase que sí cambia

El pedido **cambia de estado**, así que aquí no vale un `record`: es una clase con los campos privados y las reglas dentro.

```java
class Pedido {
    private final int id;
    private final String cliente;
    private final List<Linea> lineas = new ArrayList<>();
    private Estado estado = Estado.PENDIENTE;

    Pedido(int id, String cliente) {
        this.id = id;
        this.cliente = cliente;
    }

    void anadir(Linea linea) {
        if (estado != Estado.PENDIENTE) {                      // (1)
            throw new IllegalStateException("No se toca un pedido " + estado);
        }
        lineas.add(linea);
    }

    double total() {
        double suma = 0;
        for (var l : lineas) suma += l.importe();
        return suma;
    }

    void pagar() {
        if (estado != Estado.PENDIENTE)
            throw new IllegalStateException("Ya estaba " + estado);
        if (lineas.isEmpty())
            throw new IllegalStateException("Un pedido vacío no se puede pagar");
        estado = Estado.PAGADO;
    }

    void enviar() {
        if (estado != Estado.PAGADO)
            throw new IllegalStateException("Hay que pagar antes de enviar");
        estado = Estado.ENVIADO;
    }

    int id()            { return id; }
    String cliente()    { return cliente; }
    Estado estado()     { return estado; }
    List<Linea> lineas() { return List.copyOf(lineas); }        // (2)
}
```

1. **La regla está en el objeto**, no en quien lo usa. Nadie puede añadir líneas a un pedido ya enviado, se llame desde donde se llame.
2. Copia inmutable: sin esto, `pedido.lineas().clear()` vaciaría el pedido desde fuera.

Pruébalo:

```java
jshell> var p = new Pedido(1, "ana@iesx.es")
jshell> p.anadir(new Linea("Casco", 2, 35.0))
jshell> p.anadir(new Linea("Candado", 1, 25.0))
jshell> p.total()
$8 ==> 95.0

jshell> p.enviar()
|  Exception java.lang.IllegalStateException: Hay que pagar antes de enviar

jshell> p.pagar()
jshell> p.enviar()
jshell> p.estado()
$12 ==> ENVIADO

jshell> p.anadir(new Linea("Luces", 1, 15.0))
|  Exception java.lang.IllegalStateException: No se toca un pedido ENVIADO
```

**Fíjate en lo que acaba de pasar:** el objeto se defiende solo. No hace falta que quien lo use se acuerde de comprobar nada.

### Paso 4 · La interfaz aparece cuando hace falta

Ahora piden avisar al cliente cuando el pedido se envía. La tentación es escribir el correo dentro de `enviar()`:

```java
void enviar() {
    …
    estado = Estado.ENVIADO;
    System.out.println("[correo a " + cliente + "] Tu pedido ha salido");   // NO
}
```

Y eso trae tres problemas: `Pedido` pasa a saber de correo; no se puede cambiar a SMS sin tocarlo; y **no se puede probar sin enviar correos de verdad**.

La solución es depender de una **interfaz** y que te la den desde fuera:

```java
interface Notificador {
    void enviar(String destinatario, String mensaje);
}

class NotificadorCorreo implements Notificador {
    public void enviar(String destinatario, String mensaje) {
        System.out.println("[correo a " + destinatario + "] " + mensaje);
    }
}
```

```java
class ServicioPedidos {
    private final Notificador notificador;

    ServicioPedidos(Notificador notificador) {      // (1)
        this.notificador = notificador;
    }

    void enviarPedido(Pedido pedido) {
        pedido.enviar();
        notificador.enviar(pedido.cliente(),
                "Tu pedido " + pedido.id() + " ha salido. Total: " + pedido.total() + " €");
    }
}
```

1. **Se recibe por el constructor.** Nunca `new NotificadorCorreo()` aquí dentro: eso volvería a atarlo.

```java
jshell> var servicio = new ServicioPedidos(new NotificadorCorreo())
jshell> var p2 = new Pedido(2, "bruno@iesx.es")
jshell> p2.anadir(new Linea("Bici", 1, 450.0))
jshell> p2.pagar()
jshell> servicio.enviarPedido(p2)
[correo a bruno@iesx.es] Tu pedido 2 ha salido. Total: 450.0 €
```

### Paso 5 · Y ahora se ve para qué servía

Cambiar a SMS: **no se toca ni `Pedido` ni `ServicioPedidos`**.

```java
jshell> class NotificadorSms implements Notificador {
   ...>     public void enviar(String d, String m) { System.out.println("[SMS " + d + "] " + m); }
   ...> }

jshell> new ServicioPedidos(new NotificadorSms()).enviarPedido(p3)
[SMS 600123456] Tu pedido 3 ha salido. Total: 95.0 €
```

Y probarlo **sin enviar nada**, que es lo que de verdad importa:

```java
jshell> class NotificadorFalso implements Notificador {
   ...>     final List<String> enviados = new ArrayList<>();
   ...>     public void enviar(String d, String m) { enviados.add(d + ": " + m); }
   ...> }

jshell> var falso = new NotificadorFalso()
jshell> var p4 = new Pedido(4, "carla@iesx.es")
jshell> p4.anadir(new Linea("Luces", 2, 15.0))
jshell> p4.pagar()
jshell> new ServicioPedidos(falso).enviarPedido(p4)

jshell> falso.enviados
$25 ==> [carla@iesx.es: Tu pedido 4 ha salido. Total: 30.0 €]
```

**Ese `falso` es la razón de ser de la interfaz.** Sin ella, para comprobar que el aviso sale bien habría que mandar un correo de verdad y mirar la bandeja de entrada.

!!! success "Lo que hay que llevarse de este ejemplo"
    | Pieza | Por qué está |
    |---|---|
    | `record Linea` | Datos que no cambian |
    | `enum Estado` | Conjunto cerrado, con el compilador vigilando |
    | `class Pedido` | **Cambia de estado**, y guarda sus propias reglas |
    | `interface Notificador` | Para poder cambiar el cómo y **para poder probar** |
    | Constructor de `ServicioPedidos` | Recibe lo que necesita; no lo crea |

    Esa última fila tiene nombre: **inyección de dependencias**, hecha a mano. En la UT4 la hace Spring por ti, y ahora ya sabes qué problema resuelve.

---

## Pruébalo ahora (12 min)

Con `jshell` abierto:

1. Teclea el `record Linea` del paso 1 e intenta crear una con `0` unidades.
2. Crea un `Pedido`, añádele dos líneas y llama a `enviar()` **antes** de pagar. Lee el mensaje.
3. Añade un estado `DEVUELTO` al `enum` y un método `devolver()` que solo funcione si está `ENTREGADO`.
4. Escribe un `NotificadorDoble` que implemente `Notificador` y **mande por los dos canales**, sin tocar `ServicioPedidos`.

El punto 4 es el que demuestra que lo has entendido: si has tenido que modificar `ServicioPedidos`, vuelve al paso 4.

---

## Ejercicios (con solución)

??? success "E1 · ¿Por qué `false`?"

    ```java
    record P(int x) {}
    class C { int x; C(int x) { this.x = x; } }

    System.out.println(new P(1).equals(new P(1)));   // ?
    System.out.println(new C(1).equals(new C(1)));   // ?
    ```

    **`true` y `false`.** El `record` genera un `equals` que compara los campos; la clase hereda el de `Object`, que compara **referencias** — es decir, si son literalmente el mismo objeto en memoria.

    Consecuencia práctica: con la clase, `lista.contains(new C(1))` devuelve `false` aunque la lista tenga un `C(1)`, y `mapa.get(new C(1))` devuelve `null`.

??? success "E2 · La fuga del getter"

    ```java
    class Grupo {
        private final List<String> nombres = new ArrayList<>();
        void anadir(String n) { nombres.add(n); }
        List<String> getNombres() { return nombres; }
    }

    var g = new Grupo();
    g.anadir("Ana");
    g.getNombres().clear();
    ```

    La lista interna **se vacía**. El `private` no ha servido de nada porque el *getter* entrega la lista de verdad.

    ```java
    List<String> getNombres() { return List.copyOf(nombres); }
    ```

    Ahora `clear()` lanza `UnsupportedOperationException`.

??? success "E3 · ¿Record o clase?"

    Decide para cada uno: (a) una coordenada GPS · (b) una partida de ajedrez en curso · (c) una fila leída de un CSV · (d) un carrito de la compra · (e) el resultado de un cálculo

    | | Cuál | Por qué |
    |---|---|---|
    | (a) Coordenada | **record** | No cambia |
    | (b) Partida | **clase** | El tablero cambia en cada jugada |
    | (c) Fila del CSV | **record** | Es un dato de transporte |
    | (d) Carrito | **clase** | Se añaden y quitan cosas |
    | (e) Resultado | **record** | Se calcula una vez |

    La regla: **¿el objeto cambia después de crearse?** Si sí, clase.

??? success "E4 · El `enum` con datos"

    Escribe un `enum Prioridad` con `BAJA`, `MEDIA`, `ALTA` y `CRITICA`, cada una con las horas máximas de respuesta, y un método que diga si hay que avisar fuera de horario.

    ```java
    enum Prioridad {
        BAJA(72), MEDIA(24), ALTA(4), CRITICA(1);

        private final int horas;
        Prioridad(int horas) { this.horas = horas; }

        int horas() { return horas; }
        boolean urgente() { return horas <= 4; }
    }
    ```

    ```java
    jshell> Prioridad.ALTA.urgente()
    $3 ==> true
    ```

    Sin el `enum`, ese `horas <= 4` acabaría repetido en cuatro sitios distintos del programa, y el día que cambie la política habría que encontrarlos todos.

??? success "E5 · La interfaz que falta"

    Este servicio no se puede probar sin escribir en el disco. Arréglalo.

    ```java
    class ServicioInformes {
        void generar(List<String> lineas) {
            var texto = String.join("\n", lineas);
            Files.writeString(Path.of("informe.txt"), texto);   // atado al disco
        }
    }
    ```

    ```java
    interface Destino {
        void escribir(String contenido);
    }

    class DestinoFichero implements Destino {
        private final Path ruta;
        DestinoFichero(Path ruta) { this.ruta = ruta; }
        public void escribir(String c) {
            try { Files.writeString(ruta, c); }
            catch (IOException e) { throw new UncheckedIOException(e); }
        }
    }

    class ServicioInformes {
        private final Destino destino;
        ServicioInformes(Destino destino) { this.destino = destino; }

        void generar(List<String> lineas) {
            destino.escribir(String.join("\n", lineas));
        }
    }
    ```

    Y ahora el test, sin tocar el disco:

    ```java
    class DestinoMemoria implements Destino {
        String ultimo;
        public void escribir(String c) { ultimo = c; }
    }

    var m = new DestinoMemoria();
    new ServicioInformes(m).generar(List.of("uno", "dos"));
    // m.ultimo == "uno\ndos"
    ```

    **El patrón es siempre el mismo**, y es el de toda la asignatura: lo que depende del exterior —disco, red, base de datos— se pone detrás de una interfaz y se recibe por constructor.
