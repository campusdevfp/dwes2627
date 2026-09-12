package es.iesx.daw.practicas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UT3 · P10 — El bucle lento.
 *
 * Además de dar el resultado correcto, tiene que ser rápido: el último test
 * falla si has dejado dos bucles anidados.
 */
@DisplayName("UT3 · P10 · Índice en vez de bucle anidado")
class UT3P10Test {

    @Test
    @DisplayName("suma solo las ventas cuyo producto está en el catálogo")
    void total() {
        var catalogo = List.of(
                new Producto(1, "Patinete", "movilidad", 299.0, 4),
                new Producto(2, "Casco", "seguridad", 35.0, 20));
        var ventas = List.of(
                new Venta("Ana", "Patinete", 2, 299.0),
                new Venta("Ana", "Casco", 1, 35.0),
                new Venta("Ana", "Desconocido", 5, 10.0));

        assertThat(new Informes().totalConCatalogo(ventas, catalogo)).isEqualTo(633.0);
    }

    @Test
    @DisplayName("con 3.000 productos y 5.000 ventas termina en menos de un segundo")
    void rendimiento() {
        var catalogo = new ArrayList<Producto>();
        for (int i = 1; i <= 3000; i++)
            catalogo.add(new Producto(i, "P" + i, "cat", 10.0, 1));

        var ventas = new ArrayList<Venta>();
        for (int i = 0; i < 5000; i++)
            ventas.add(new Venta("v", "P" + (i % 3000 + 1), 1, 10.0));

        long t0 = System.nanoTime();
        double total = new Informes().totalConCatalogo(ventas, catalogo);
        long ms = (System.nanoTime() - t0) / 1_000_000;

        assertThat(total).isEqualTo(50000.0);
        assertThat(ms)
                .as("con dos bucles anidados esto tarda muchísimo: construye un índice antes")
                .isLessThan(1000);
    }
}
