package es.iesx.daw.practicas;

import es.iesx.daw.practicas.datos.Articulo;
import es.iesx.daw.practicas.datos.ArticuloRepositorio;
import es.iesx.daw.practicas.datos.Categoria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * UT5 · P2 — De la memoria a la base de datos.
 *
 * El test es el enunciado. Para ponerlo en verde tienes que:
 *   1. Anotar Articulo como entidad, con clave generada y tipos correctos.
 *   2. Hacer que ArticuloRepositorio extienda JpaRepository<Articulo, Long>.
 *   3. Resolver con @Query la consulta que no sale del nombre del método.
 *
 * Si el proyecto no arranca con "No default constructor for entity",
 * has convertido en @Entity algo que es un record. Vuelve al tema 2.
 */
@DataJpaTest
@DisplayName("UT5 · P2 · De la memoria a la base de datos")
class UT5P2Test {

    @Autowired
    ArticuloRepositorio repo;

    private Articulo nuevo(String nombre, Categoria cat, String precio, int stock) {
        return new Articulo(nombre, cat, new BigDecimal(precio), stock);
    }

    @Test
    @DisplayName("la clave la genera la base de datos, no tú")
    void guardaYGeneraId() {
        var a = repo.save(nuevo("Patinete", Categoria.MOVILIDAD, "299.00", 4));

        assertThat(a.getId()).isNotNull();
        assertThat(repo.findById(a.getId())).isPresent();
    }

    @Test
    @DisplayName("el precio conserva los decimales: BigDecimal, no double")
    void elPrecioNoPierdeDecimales() {
        var a = repo.save(nuevo("Candado", Categoria.ACCESORIOS, "19.95", 15));

        assertThat(repo.findById(a.getId()).orElseThrow().getPrecio())
                .as("con double aquí saldría 19.949999809265137")
                .isEqualByComparingTo(new BigDecimal("19.95"));
    }

    @Test
    @DisplayName("consulta derivada: findByCategoria")
    void buscaPorCategoria() {
        repo.save(nuevo("Patinete", Categoria.MOVILIDAD, "299.00", 4));
        repo.save(nuevo("Bici", Categoria.MOVILIDAD, "899.00", 2));
        repo.save(nuevo("Casco", Categoria.SEGURIDAD, "35.50", 20));

        assertThat(repo.findByCategoria(Categoria.MOVILIDAD))
                .extracting(Articulo::getNombre)
                .containsExactlyInAnyOrder("Patinete", "Bici");
    }

    @Test
    @DisplayName("consulta derivada: búsqueda parcial sin distinguir mayúsculas")
    void buscaPorNombreParcial() {
        repo.save(nuevo("Luz LED trasera", Categoria.ACCESORIOS, "12.00", 30));
        repo.save(nuevo("Casco", Categoria.SEGURIDAD, "35.50", 20));

        assertThat(repo.findByNombreContainingIgnoreCase("led")).hasSize(1);
    }

    @Test
    @DisplayName("consulta derivada: por debajo de un precio y ordenado")
    void buscaPorPrecioOrdenado() {
        repo.save(nuevo("Bici", Categoria.MOVILIDAD, "899.00", 2));
        repo.save(nuevo("Candado", Categoria.ACCESORIOS, "19.95", 15));
        repo.save(nuevo("Casco", Categoria.SEGURIDAD, "35.50", 20));

        assertThat(repo.findByPrecioLessThanOrderByPrecioAsc(new BigDecimal("50.00")))
                .extracting(Articulo::getNombre)
                .containsExactly("Candado", "Casco");
    }

    @Test
    @DisplayName("la que no sale del nombre: JPQL con @Query")
    void bajoMinimos() {
        repo.save(nuevo("Bici", Categoria.MOVILIDAD, "899.00", 2));
        repo.save(nuevo("Luz LED", Categoria.ACCESORIOS, "12.00", 30));

        assertThat(repo.bajoMinimos(5))
                .extracting(Articulo::getNombre)
                .containsExactly("Bici");
    }

    @Test
    @DisplayName("el enumerado se guarda por su nombre, no por su posición")
    void elEnumeradoSeGuardaComoTexto() {
        var a = repo.saveAndFlush(nuevo("Casco", Categoria.SEGURIDAD, "35.50", 20));

        // Si esto falla, tienes @Enumerated(ORDINAL) —el valor por defecto—
        // y el día que alguien reordene Categoria, los datos mentirán.
        assertThat(repo.findByCategoria(Categoria.SEGURIDAD))
                .as("recuperado por el mismo valor con el que se guardó")
                .extracting(Articulo::getId)
                .containsExactly(a.getId());
    }

    @Test
    @DisplayName("el nombre es obligatorio en la base de datos, no solo en Java")
    void elNombreEsObligatorio() {
        assertThatThrownBy(() -> repo.saveAndFlush(nuevo(null, Categoria.REPUESTOS, "10.00", 1)))
                .as("hace falta @Column(nullable = false) sobre el nombre")
                .isInstanceOf(Exception.class);
    }
}
