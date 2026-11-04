package ar.com.skilful.servicio.tarifa;

import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;
import ar.com.skilful.excepcion.ReglaCobroException;

/** Abstracción de una regla de selección. Los precios permanecen en MySQL. */
public abstract class PoliticaTarifa {
    private final String descripcion;

    protected PoliticaTarifa(String descripcion) {
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La política debe tener una descripción.");
        }
        this.descripcion = descripcion;
    }

    public final String getDescripcion() {
        return descripcion;
    }

    public abstract boolean aplica(Cuota cuota, MedioPago medio);

    /** Valida el contexto común y delega el comportamiento específico. */
    public final String seleccionar(Cuota cuota, MedioPago medio) {
        validarContexto(cuota, medio);
        if (!aplica(cuota, medio)) {
            throw new ReglaCobroException("La política no corresponde a esta operación.");
        }
        return resolverCodigo(cuota, medio);
    }

    protected abstract String resolverCodigo(Cuota cuota, MedioPago medio);

    static void validarContexto(Cuota cuota, MedioPago medio) {
        if (cuota == null || medio == null) {
            throw new IllegalArgumentException("Debe seleccionar una cuota y un medio de pago.");
        }
    }

    protected final boolean tieneBeneficioAplicable(Cuota cuota, MedioPago medio) {
        return medio.isAdmiteBeneficio() && cuota.getBeneficioActivo() != null
            && !"NINGUNO".equals(cuota.getBeneficioActivo());
    }
}
