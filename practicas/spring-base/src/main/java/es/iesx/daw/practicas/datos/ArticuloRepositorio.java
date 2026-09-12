package es.iesx.daw.practicas.datos;

import java.math.BigDecimal;
import java.util.List;

/**
 * UT5 · P2 — Haz que extienda JpaRepository<Articulo, Long> y añade las
 * consultas que faltan. No escribas ninguna implementación: no hace falta.
 *
 * Las firmas están dadas porque los tests dependen de ellas. Tu trabajo es
 * que Spring Data sepa traducirlas — y saber cuáles se traducen solas y
 * cuáles necesitan @Query.
 */
public interface ArticuloRepositorio /* TODO: extends JpaRepository<Articulo, Long> */ {

    List<Articulo> findByCategoria(Categoria categoria);

    List<Articulo> findByNombreContainingIgnoreCase(String texto);

    List<Articulo> findByPrecioLessThanOrderByPrecioAsc(BigDecimal maximo);

    /** TODO: esta no sale del nombre. Necesita @Query con JPQL. */
    List<Articulo> bajoMinimos(int minimo);
}
