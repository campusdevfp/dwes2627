package es.iesx.daw.practicas.datos;

import java.math.BigDecimal;

/**
 * UT5 · P2 — Conviértelo en una entidad JPA.
 *
 * Está a propósito sin anotar. Lo que tienes que decidir:
 *   · @Entity y el nombre de la tabla
 *   · @Id y cómo se genera la clave
 *   · qué columnas son obligatorias y con qué longitud
 *   · precio con precisión y escala (es dinero)
 *   · categoria guardada por su NOMBRE, no por su posición
 *
 * Fíjate en que NO es un record. Pregúntate por qué antes de seguir:
 * la respuesta está en el tema 2 y cae en el examen.
 */
public class Articulo {

    private Long id;
    private String nombre;
    private Categoria categoria;
    private BigDecimal precio;
    private int stock;

    /** JPA lo necesita. No lo borres. */
    protected Articulo() { }

    public Articulo(String nombre, Categoria categoria, BigDecimal precio, int stock) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.precio = precio;
        this.stock = stock;
    }

    public Long getId()            { return id; }
    public String getNombre()      { return nombre; }
    public Categoria getCategoria(){ return categoria; }
    public BigDecimal getPrecio()  { return precio; }
    public int getStock()          { return stock; }

    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public void setStock(int stock)          { this.stock = stock; }
}
