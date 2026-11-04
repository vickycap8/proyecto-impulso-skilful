package ar.com.skilful.servicio;

import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;
import ar.com.skilful.modelo.ResultadoCobro;

import java.math.BigDecimal;

/**
 * Pruebas unitarias ejecutables sin conexión a MySQL.
 *
 * Se ejecutan con la opción -ea de Java para comprobar la lógica monetaria
 * antes de integrarla con la interfaz y la base de datos.
 */
public final class CobroServicioPrueba {

    private static int pruebasEjecutadas;

    private CobroServicioPrueba() {
    }

    public static void main(String[] args) {
        CobroServicio servicio = new CobroServicio(null, null);

        probarTarifaEstudiante(servicio);
        probarTarifaGeneralCuandoElMedioNoAdmiteBeneficio(servicio);
        probarRecargoTarjeta(servicio);
        probarAplicacionParcialDeSaldo(servicio);
        probarAplicacionTotalDeSaldo(servicio);
        probarBloqueoDeSuscripcion(servicio);

        System.out.println(
            "Pruebas de CobroServicio superadas: " + pruebasEjecutadas
        );
    }

    private static void probarTarifaEstudiante(CobroServicio servicio) {
        Cuota cuota = cuotaSinSuscripcion("ESTUDIANTE");
        MedioPago efectivo = medio("EFECTIVO", "0", true);

        verificarIgual(
            "ESTUDIANTE",
            servicio.determinarCodigoTarifa(cuota, efectivo),
            "Debe aplicar la tarifa estudiantil al pagar en efectivo"
        );
    }

    private static void probarTarifaGeneralCuandoElMedioNoAdmiteBeneficio(
            CobroServicio servicio) {

        Cuota cuota = cuotaSinSuscripcion("CONVENIO");
        MedioPago tarjeta = medio("TARJETA_CREDITO", "10", false);

        verificarIgual(
            "GENERAL",
            servicio.determinarCodigoTarifa(cuota, tarjeta),
            "No debe aplicar el convenio con un medio no habilitado"
        );
    }

    private static void probarRecargoTarjeta(CobroServicio servicio) {
        ResultadoCobro resultado = servicio.calcular(
            dinero("20000"),
            dinero("20000"),
            dinero("10"),
            BigDecimal.ZERO,
            false
        );

        verificarIgual(
            dinero("22000.00"),
            resultado.getImporteFinal(),
            "Debe calcular el 10 % de recargo"
        );
    }

    private static void probarAplicacionParcialDeSaldo(CobroServicio servicio) {
        ResultadoCobro resultado = servicio.calcular(
            dinero("20000"),
            dinero("15000"),
            BigDecimal.ZERO,
            dinero("3000"),
            true
        );

        verificarIgual(
            dinero("12000.00"),
            resultado.getTotalAbonar(),
            "Debe descontar el saldo disponible"
        );
    }

    private static void probarAplicacionTotalDeSaldo(CobroServicio servicio) {
        ResultadoCobro resultado = servicio.calcular(
            dinero("10000"),
            dinero("7500"),
            BigDecimal.ZERO,
            dinero("9000"),
            true
        );

        verificarIgual(
            dinero("0.00"),
            resultado.getTotalAbonar(),
            "El saldo aplicado no debe superar el importe final"
        );
    }

    private static void probarBloqueoDeSuscripcion(CobroServicio servicio) {
        Cuota cuota = cuotaSinSuscripcion("NINGUNO");
        cuota.setSuscripcionActiva(true);
        cuota.setIntentosSuscripcion(3);

        boolean bloqueado = false;
        try {
            servicio.determinarCodigoTarifa(
                cuota,
                medio("EFECTIVO", "0", true)
            );
        } catch (IllegalStateException esperado) {
            bloqueado = true;
        }

        verificarIgual(
            Boolean.TRUE,
            Boolean.valueOf(bloqueado),
            "Debe bloquear el pago presencial antes del quinto intento"
        );
    }

    private static Cuota cuotaSinSuscripcion(String beneficio) {
        Cuota cuota = new Cuota();
        cuota.setBeneficioActivo(beneficio);
        cuota.setSuscripcionActiva(false);
        cuota.setIntentosSuscripcion(0);
        return cuota;
    }

    private static MedioPago medio(
            String nombre,
            String recargo,
            boolean admiteBeneficio) {

        return new MedioPago(
            1, nombre, dinero(recargo), admiteBeneficio
        );
    }

    private static BigDecimal dinero(String valor) {
        return new BigDecimal(valor);
    }

    private static void verificarIgual(
            Object esperado,
            Object obtenido,
            String mensaje) {

        pruebasEjecutadas++;
        if (!esperado.equals(obtenido)) {
            throw new AssertionError(
                mensaje + ". Esperado: " + esperado + ", obtenido: " + obtenido
            );
        }
    }
}
