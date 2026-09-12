package es.iesx.daw.practicas;

import es.iesx.daw.practicas.modelo.Producto;
import es.iesx.daw.practicas.repositorio.ProductoRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.assertj.core.api.Assertions.*;

/** UT4 · P6 — Repositorio en memoria. */
@SpringBootTest
@DisplayName("UT4 · P6 · Repositorio en memoria")
class UT4P6Test {

    @Autowired ProductoRepositorio repositorio;

    @Test @DisplayName("la precarga deja 5 productos")
    void precarga() {
        assertThat(repositorio.buscarTodos()).hasSize(5);
    }

    @Test @DisplayName("buscarPorId encuentra el que existe y devuelve vacío con el que no")
    void buscarPorId() {
        assertThat(repositorio.buscarPorId(1)).isPresent();
        assertThat(repositorio.buscarPorId(9999)).isEmpty();
    }

    @Test @DisplayName("buscarPorNombre localiza por nombre exacto")
    void buscarPorNombre() {
        assertThat(repositorio.buscarPorNombre("Casco")).isPresent();
        assertThat(repositorio.buscarPorNombre("Nolohay")).isEmpty();
    }

    @Test @DisplayName("buscarTodos NO devuelve la colección interna modificable")
    void listaInmutable() {
        assertThatThrownBy(() -> repositorio.buscarTodos().clear())
                .as("si devuelves la lista interna, quien la reciba puede vaciarte el almacén")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test @DirtiesContext @DisplayName("guardar asigna id cuando llega a null")
    void guardarAsignaId() {
        var creado = repositorio.guardar(new Producto(null, "Nuevo", "accesorios", 9.99, 1));
        assertThat(creado.id()).isNotNull().isGreaterThan(5);
        assertThat(repositorio.buscarPorId(creado.id())).isPresent();
    }

    @Test @DirtiesContext @DisplayName("borrar devuelve true si existía y false si no")
    void borrar() {
        assertThat(repositorio.borrar(1)).isTrue();
        assertThat(repositorio.borrar(9999)).isFalse();
    }
}
