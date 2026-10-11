package ar.com.skilful.servicio.tarifa;

import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;
import ar.com.skilful.excepcion.ReglaCobroException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Usa referencias a la abstracción para ejecutar las reglas polimórficamente. */
public final class SelectorPoliticaTarifa {
    private final List<PoliticaTarifa> politicas;

    public SelectorPoliticaTarifa() {
        politicas = Collections.unmodifiableList(Arrays.asList(
            new TarifaSuscripcion(), new TarifaConBeneficio(), new TarifaGeneral()));
    }

    public String seleccionarCodigo(Cuota cuota, MedioPago medio) {
        PoliticaTarifa.validarContexto(cuota, medio);
        for (PoliticaTarifa politica : politicas) {
            if (politica.aplica(cuota, medio)) {
                return politica.seleccionar(cuota, medio);
            }
        }
        throw new ReglaCobroException("No se encontró una política de tarifa aplicable.");
    }
}
