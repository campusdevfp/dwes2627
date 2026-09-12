package es.iesx.daw.practicas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * UT2 · P6 — Un record que se valida y se normaliza a sí mismo.
 *
 * Todo esto va en el CONSTRUCTOR COMPACTO. Si termina, el objeto es válido.
 */
@DisplayName("UT2 · P6 · Producto validado")
class UT2P6Test {

    @Test
    @DisplayName("rechaza el nombre en blanco")
    void nombreEnBlanco() {
        assertThatThrownBy(() -> new Producto(1, "   ", "seguridad", 10, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rechaza el precio cero o negativo, y el mensaje incluye el valor recibido")
    void precioInvalido() {
        assertThatThrownBy(() -> new Producto(1, "Casco", "seguridad", -5, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("-5");
        assertThatThrownBy(() -> new Producto(1, "Casco", "seguridad", 0, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rechaza el stock negativo")
    void stockNegativo() {
        assertThatThrownBy(() -> new Producto(1, "Casco", "seguridad", 10, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("normaliza el nombre: recorta, colapsa espacios y capitaliza")
    void normalizaNombre() {
        var p = new Producto(1, "  casco   integral  ", "seguridad", 10, 1);
        assertThat(p.nombre()).isEqualTo("Casco integral");
    }

    @Test
    @DisplayName("un producto válido se construye sin lanzar nada")
    void valido() {
        assertThatCode(() -> new Producto(1, "Casco", "seguridad", 49.99, 10))
                .doesNotThrowAnyException();
    }
}
