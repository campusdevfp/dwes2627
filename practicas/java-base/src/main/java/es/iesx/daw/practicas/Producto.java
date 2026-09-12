package es.iesx.daw.practicas;

/**
 * UT2 · P5 — Convierte esta clase en un record y añade el comportamiento
 * que piden los tests de UT2P5Test.
 *
 * Los tests te dicen exactamente qué firma esperan. Léelos antes de tocar nada.
 */
public record Producto(Integer id, String nombre, String categoria, double precio, int stock) {

    // TODO UT2·P6 — constructor compacto: valida y normaliza

    public boolean hayStock(int unidades) {
        return false;   // TODO UT2·P5
    }

    public Producto conStock(int nuevoStock) {
        return null;    // TODO UT2·P5
    }
}
