package ar.com.skilful.servicio.tarifa;

import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;
import ar.com.skilful.excepcion.ReglaCobroException;

/** Regla para pagar por un medio alternativo tras cinco intentos rechazados. */
public final class TarifaSuscripcion extends PoliticaTarifa {
    private static final int LIMITE_INTENTOS = 5;

    public TarifaSuscripcion() {
        super("Pago alternativo de suscripción");
    }

    @Override
    public boolean aplica(Cuota cuota, MedioPago medio) {
        return cuota.isSuscripcionActiva();
    }

    @Override
    protected String resolverCodigo(Cuota cuota, MedioPago medio) {
        if (cuota.getIntentosSuscripcion() < LIMITE_INTENTOS) {
            throw new ReglaCobroException(
                "La cuota debe gestionarse desde Control de suscripciones.");
        }
        if (!"EFECTIVO".equals(medio.getNombre())
                && !"TRANSFERENCIA".equals(medio.getNombre())) {
            throw new ReglaCobroException(
                "Tras cinco rechazos, la tarifa de suscripción solo puede "
                + "abonarse en efectivo o transferencia.");
        }
        return "SUSCRIPCION";
    }
}
