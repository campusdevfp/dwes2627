package org.example;

import java.math.BigDecimal;

public record Producto(Long id, String codigo, String nombre,
                       BigDecimal precio, int stock) {

    public Producto {
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código obligatorio");
        if (precio == null || precio.signum() < 0)
            throw new IllegalArgumentException("Precio no válido");
        if (stock < 0)
            throw new IllegalArgumentException("Stock negativo");
    }

    public static Producto nuevo(String codigo, String nombre,
                                 String precio, int stock) {          //
        return new Producto(null, codigo, nombre, new BigDecimal(precio), stock);
    }
}