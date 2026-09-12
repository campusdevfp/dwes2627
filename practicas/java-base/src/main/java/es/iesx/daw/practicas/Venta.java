package es.iesx.daw.practicas;

/** Dado. No lo modifiques. */
public record Venta(String vendedor, String producto, int unidades, double precio) {
    public double importe() { return unidades * precio; }
}
