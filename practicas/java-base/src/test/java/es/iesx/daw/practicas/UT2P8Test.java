package es.iesx.daw.practicas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** UT2 · P8 — Informes con streams. */
@DisplayName("UT2 · P8 · Informes con streams")
class UT2P8Test {

    Informes informes;
    List<Venta> ventas;

    @BeforeEach
    void datos() {
        informes = new Informes();
        ventas = List.of(
                new Venta("Ana",   "Patinete", 2, 120.0),
                new Venta("Bruno", "Casco",    5,  35.0),
                new Venta("Ana",   "Casco",    3,  35.0),
                new Venta("Carla", "Bici",     1, 450.0),
                new Venta("Bruno", "Patinete", 1, 120.0));
    }

    @Test
    @DisplayName("facturación por vendedor, con las claves ordenadas alfabéticamente")
    void facturacion() {
        var r = informes.facturacionPorVendedor(ventas);
        assertThat(r).containsExactly(
                java.util.Map.entry("Ana",   345.0),
                java.util.Map.entry("Bruno", 295.0),
                java.util.Map.entry("Carla", 450.0));
    }

    @Test
    @DisplayName("producto estrella por unidades vendidas")
    void estrella() {
        assertThat(informes.productoEstrella(ventas)).contains("Casco");   // 8 unidades
    }

    @Test
    @DisplayName("con la lista vacía, el producto estrella está vacío (no null, no excepción)")
    void estrellaSinVentas() {
        assertThat(informes.productoEstrella(List.of())).isEmpty();
    }

    @Test
    @DisplayName("media de unidades por venta")
    void media() {
        assertThat(informes.mediaUnidades(ventas)).isEqualTo(2.4);
        assertThat(informes.mediaUnidades(List.of())).as("sin ventas, 0").isEqualTo(0.0);
    }
}
