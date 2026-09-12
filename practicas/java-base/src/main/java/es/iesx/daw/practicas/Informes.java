package es.iesx.daw.practicas;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/** UT2 · P8 y UT3 · P2 — informes con streams. */
public class Informes {

    /** Facturación total de cada vendedor. Claves ordenadas alfabéticamente. */
    public Map<String, Double> facturacionPorVendedor(List<Venta> ventas) {
        return null;   // TODO
    }

    /** Producto con más unidades vendidas. Vacío si no hay ventas. */
    public Optional<String> productoEstrella(List<Venta> ventas) {
        return Optional.empty();   // TODO
    }

    /** Media de unidades por venta. 0 si no hay ventas. */
    public double mediaUnidades(List<Venta> ventas) {
        return 0;   // TODO
    }

    /**
     * UT3 · P10 — Sin bucles anidados: construye un índice antes de recorrer.
     * Devuelve el importe total de las ventas cuyo producto está en el catálogo.
     */
    public double totalConCatalogo(List<Venta> ventas, List<Producto> catalogo) {
        return 0;   // TODO
    }
}
