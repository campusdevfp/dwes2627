package es.iesx.daw.practicas.modelo;

/** Dado. No lo modifiques. */
public record Producto(Integer id, String nombre, String categoria, double precio, int stock) {
    public Producto conId(Integer nuevo) { return new Producto(nuevo, nombre, categoria, precio, stock); }
    public Producto conStock(int nuevo)  { return new Producto(id, nombre, categoria, precio, nuevo); }
    public boolean hayStock(int u)       { return stock >= u; }
}
