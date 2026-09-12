package es.iesx.daw.practicas.excepcion;

import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** UT6 · P4 — Traduce las excepciones de dominio a ProblemDetail. */
@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ProblemDetail noEncontrado(ProductoNoEncontradoException e) { return null; }  // TODO 404

    @ExceptionHandler(ProductoDuplicadoException.class)
    public ProblemDetail duplicado(ProductoDuplicadoException e) { return null; }        // TODO 409
}
