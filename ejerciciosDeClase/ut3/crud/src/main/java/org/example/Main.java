package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main() {

        // Conexión H2
        //var dao = new ProductoDao("jdbc:h2:./datos/tienda", "sa", "");
        var dao = new ProductoDao(

                "jdbc:mysql://localhost:3307/tienda?serverTimezone=Europe/Madrid",   //

                "alumno", "alumno");

        dao.crearTabla();

        System.out.println("=== INSERTAR ===");
        var casco = dao.insertar(Producto.nuevo("SEG-01", "Casco", "35.00", 12));
        dao.insertar(Producto.nuevo("SEG-02", "Candado", "25.00", 0));
        dao.insertar(Producto.nuevo("MOV-01", "Patinete", "120.00", 4));
        System.out.println("  Insertado con id " + casco.id());

        System.out.println("\n=== DUPLICADO ===");
        try {
            dao.insertar(Producto.nuevo("SEG-01", "Casco repetido", "40.00", 1));
        } catch (ProductoDao.CodigoDuplicadoException e) {
            System.out.println("  " + e.getMessage());
        }

        System.out.println("\n=== LISTAR ===");
        dao.listar().forEach(p ->
                System.out.printf("  %-8s %-12s %8s €  stock %d%n",
                        p.codigo(), p.nombre(), p.precio(), p.stock()));

        System.out.println("\n=== BUSCAR ===");
        System.out.println("  SEG-01 → " +
                dao.buscarPorCodigo("SEG-01").map(Producto::nombre).orElse("no está"));
        System.out.println("  NO-HAY → " +
                dao.buscarPorCodigo("NO-HAY").map(Producto::nombre).orElse("no está"));

        System.out.println("\n=== ACTUALIZAR ===");
        System.out.println("  SEG-02 a 30 unidades: " + dao.actualizarStock("SEG-02", 30));
        System.out.println("  NO-HAY a 5 unidades:  " + dao.actualizarStock("NO-HAY", 5));

        System.out.println("\n=== BORRAR ===");
        System.out.println("  MOV-01: " + dao.borrar("MOV-01"));
        System.out.println("  Quedan " + dao.listar().size() + " productos");

        System.out.println("\n=== INYECCIÓN SQL ===");
        System.out.println("  Buscando \"x' OR '1'='1\" → " +
                dao.buscarPorCodigo("x' OR '1'='1").isPresent());       //
    }
}
