package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductoDao {

    private final String url, usuario, clave;

    public ProductoDao(String url, String usuario, String clave) {
        this.url = url; this.usuario = usuario; this.clave = clave;
    }

    private Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, usuario, clave);
    }

    // ── crear el esquema ───────────────────────────────────────────────
    public void crearTabla() {
        var sql = """
                CREATE TABLE IF NOT EXISTS producto (
                    id      BIGINT AUTO_INCREMENT PRIMARY KEY,
                    codigo  VARCHAR(20)   NOT NULL UNIQUE,
                    nombre  VARCHAR(100)  NOT NULL,
                    precio  DECIMAL(10,2) NOT NULL,
                    stock   INT           NOT NULL DEFAULT 0
                )""";

        try (var con = conectar(); var st = con.createStatement()) {    //
            st.execute(sql);
        } catch (SQLException e) {
            throw new AccesoDatosException("No se pudo crear la tabla", e);
        }
    }

    // ── C · insertar ───────────────────────────────────────────────────
    public Producto insertar(Producto p) {
        var sql = "INSERT INTO producto (codigo, nombre, precio, stock) VALUES (?, ?, ?, ?)";

        try (var con = conectar();
             var ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {  //

            ps.setString(1, p.codigo());
            ps.setString(2, p.nombre());
            ps.setBigDecimal(3, p.precio());
            ps.setInt(4, p.stock());
            ps.executeUpdate();

            try (var claves = ps.getGeneratedKeys()) {
                claves.next();
                long id = claves.getLong(1);
                return new Producto(id, p.codigo(), p.nombre(), p.precio(), p.stock());
            }
        } catch (SQLIntegrityConstraintViolationException e) {          //
            throw new CodigoDuplicadoException(p.codigo(), e);
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al insertar " + p.codigo(), e);
        }
    }

    // ── R · listar y buscar ────────────────────────────────────────────
    public List<Producto> listar() {
        var sql = "SELECT id, codigo, nombre, precio, stock FROM producto ORDER BY codigo";

        try (var con = conectar();
             var ps = con.prepareStatement(sql);
             var rs = ps.executeQuery()) {

            var lista = new ArrayList<Producto>();
            while (rs.next()) lista.add(aProducto(rs));                 //
            return List.copyOf(lista);

        } catch (SQLException e) {
            throw new AccesoDatosException("Error al listar", e);
        }
    }

    public Optional<Producto> buscarPorCodigo(String codigo) {
        var sql = "SELECT id, codigo, nombre, precio, stock FROM producto WHERE codigo = ?";

        try (var con = conectar(); var ps = con.prepareStatement(sql)) {
            ps.setString(1, codigo);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(aProducto(rs)) : Optional.empty();   //
            }
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al buscar " + codigo, e);
        }
    }

    // ── U · actualizar ─────────────────────────────────────────────────
    public boolean actualizarStock(String codigo, int stock) {
        var sql = "UPDATE producto SET stock = ? WHERE codigo = ?";

        try (var con = conectar(); var ps = con.prepareStatement(sql)) {
            ps.setInt(1, stock);
            ps.setString(2, codigo);
            return ps.executeUpdate() == 1;                             //
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al actualizar " + codigo, e);
        }
    }

    // ── D · borrar ─────────────────────────────────────────────────────
    public boolean borrar(String codigo) {
        try (var con = conectar();
             var ps = con.prepareStatement("DELETE FROM producto WHERE codigo = ?")) {
            ps.setString(1, codigo);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new AccesoDatosException("Error al borrar " + codigo, e);
        }
    }

    private static Producto aProducto(ResultSet rs) throws SQLException {
        return new Producto(rs.getLong("id"), rs.getString("codigo"),
                rs.getString("nombre"), rs.getBigDecimal("precio"),
                rs.getInt("stock"));
    }

    public class AccesoDatosException extends RuntimeException {

        public AccesoDatosException(String mensaje, Throwable causa) {

            super(mensaje, causa);                 // ← la causa, SIEMPRE

        }

    }



    public class CodigoDuplicadoException extends RuntimeException {

        private final String codigo;

        public CodigoDuplicadoException(String codigo, Throwable causa) {

            super("Ya existe un producto con código " + codigo, causa);

            this.codigo = codigo;

        }

        public String codigo() { return codigo; }

    }
}