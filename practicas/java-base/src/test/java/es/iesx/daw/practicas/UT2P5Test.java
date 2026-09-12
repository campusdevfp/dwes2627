package es.iesx.daw.practicas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UT2 · P5 — De clase a record.
 *
 * Qué se te pide: que Producto sea un record con los componentes
 * (id, nombre, categoria, precio, stock) y dos métodos de negocio.
 */
@DisplayName("UT2 · P5 · Producto como record")
class UT2P5Test {

    @Test
    @DisplayName("los accesores del record devuelven lo que se pasó al construirlo")
    void accesores() {
        var p = new Producto(1, "Casco", "seguridad", 49.99, 10);
        assertThat(p.id()).isEqualTo(1);
        assertThat(p.nombre()).isEqualTo("Casco");
        assertThat(p.precio()).isEqualTo(49.99);
        assertThat(p.stock()).isEqualTo(10);
    }

    @Test
    @DisplayName("dos productos con los mismos valores son iguales (equals por valor)")
    void igualdadPorValor() {
        var a = new Producto(1, "Casco", "seguridad", 49.99, 10);
        var b = new Producto(1, "Casco", "seguridad", 49.99, 10);
        assertThat(a).isEqualTo(b);
        assertThat(a).hasSameHashCodeAs(b);
    }

    @Test
    @DisplayName("hayStock: true si hay suficientes, incluido el caso justo")
    void hayStock() {
        var p = new Producto(1, "Casco", "seguridad", 49.99, 10);
        assertThat(p.hayStock(5)).isTrue();
        assertThat(p.hayStock(10)).as("el caso justo también cuenta").isTrue();
        assertThat(p.hayStock(11)).isFalse();
    }

    @Test
    @DisplayName("conStock devuelve una copia nueva y NO modifica el original")
    void conStock() {
        var p = new Producto(1, "Casco", "seguridad", 49.99, 10);
        var q = p.conStock(3);

        assertThat(q.stock()).isEqualTo(3);
        assertThat(q.nombre()).isEqualTo("Casco");
        assertThat(p.stock()).as("el record es inmutable: el original no cambia").isEqualTo(10);
    }
}
