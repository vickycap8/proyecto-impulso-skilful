package ar.com.skilful.servicio.tarifa;

import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;

public final class TarifaGeneral extends PoliticaTarifa {
    public TarifaGeneral() {
        super("Cobro con tarifa general");
    }

    @Override
    public boolean aplica(Cuota cuota, MedioPago medio) {
        return !cuota.isSuscripcionActiva() && !tieneBeneficioAplicable(cuota, medio);
    }

    @Override
    protected String resolverCodigo(Cuota cuota, MedioPago medio) {
        return "GENERAL";
    }
}
