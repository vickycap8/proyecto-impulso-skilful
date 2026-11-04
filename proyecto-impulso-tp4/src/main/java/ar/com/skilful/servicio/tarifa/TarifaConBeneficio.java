package ar.com.skilful.servicio.tarifa;

import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;

public final class TarifaConBeneficio extends PoliticaTarifa {
    public TarifaConBeneficio() {
        super("Cobro con beneficio vigente");
    }

    @Override
    public boolean aplica(Cuota cuota, MedioPago medio) {
        return !cuota.isSuscripcionActiva() && tieneBeneficioAplicable(cuota, medio);
    }

    @Override
    protected String resolverCodigo(Cuota cuota, MedioPago medio) {
        return cuota.getBeneficioActivo();
    }
}
