package es.iesx.daw.practicas;

import es.iesx.daw.practicas.excepcion.*;
import es.iesx.daw.practicas.modelo.Producto;
import es.iesx.daw.practicas.servicio.ProductoServicio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.*;

/** UT4 · P7 — Reglas de negocio en el servicio. */
@SpringBootTest
@DisplayName("UT4 · P7 · Servicio")
class UT4P7Test {

    @Autowired ProductoServicio servicio;

    @Test @DisplayName("obtener de un id inexistente lanza ProductoNoEncontradoException")
    void obtenerInexistente() {
        assertThatThrownBy(() -> servicio.obtener(9999))
                .isInstanceOf(ProductoNoEncontradoException.class)
                .hasMessageContaining("9999");
    }

    @Test @DirtiesContext @DisplayName("crear con nombre repetido lanza ProductoDuplicadoException")
    void crearDuplicado() {
        assertThatThrownBy(() -> servicio.crear(new Producto(null, "Casco", "seguridad", 10.0, 1)))
                .isInstanceOf(ProductoDuplicadoException.class)
                .hasMessageContaining("Casco");
    }

    @Test @DirtiesContext @DisplayName("crear con nombre nuevo guarda y devuelve con id")
    void crearCorrecto() {
        var p = servicio.crear(new Producto(null, "Timbre", "accesorios", 8.5, 12));
        assertThat(p.id()).isNotNull();
        assertThat(servicio.obtener(p.id()).nombre()).isEqualTo("Timbre");
    }

    @Test @DirtiesContext @DisplayName("restarStock descuenta cuando hay suficiente")
    void restarStock() {
        var p = servicio.restarStock(2, 5);          // Casco tiene 20
        assertThat(p.stock()).isEqualTo(15);
    }

    @Test @DisplayName("restarStock sin existencias lanza excepción y NO guarda nada")
    void restarStockSinExistencias() {
        int antes = servicio.obtener(4).stock();     // Bici tiene 2
        assertThatThrownBy(() -> servicio.restarStock(4, 99))
                .isInstanceOf(RuntimeException.class);
        assertThat(servicio.obtener(4).stock())
                .as("si validas después de descontar, el stock queda mal")
                .isEqualTo(antes);
    }
}
