package ar.com.skilful.excepcion;

/** Operación de cobro impedida por una regla comercial, no por un fallo de SQL. */
public class ReglaCobroException extends IllegalStateException {
    private static final long serialVersionUID = 1L;

    public ReglaCobroException(String mensaje) {
        super(mensaje);
    }
}
