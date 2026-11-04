package ar.com.skilful.servicio;

import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;
import ar.com.skilful.modelo.ResultadoCobro;
import ar.com.skilful.excepcion.ReglaCobroException;
import ar.com.skilful.servicio.tarifa.*;
import java.math.BigDecimal;

/** Regresión y ejemplos de POO. Ejecutar como Java Application, sin MySQL. */
public final class PoliticasCobroPrueba {
    private static final CobroServicio SERVICIO = new CobroServicio(null, null);
    private static int aprobadas;

    private PoliticasCobroPrueba() { }

    public static void main(String[] args) {
        caso("P01 Sin beneficio: GENERAL", () -> tarifa("GENERAL", "NINGUNO", false, 0, "EFECTIVO", true));
        caso("P02 Beneficio nulo: GENERAL", () -> tarifa("GENERAL", null, false, 0, "EFECTIVO", true));
        caso("P03 Estudiante y efectivo: ESTUDIANTE", () -> tarifa("ESTUDIANTE", "ESTUDIANTE", false, 0, "EFECTIVO", true));
        caso("P04 Convenio y efectivo: CONVENIO", () -> tarifa("CONVENIO", "CONVENIO", false, 0, "EFECTIVO", true));
        caso("P05 Estudiante y transferencia: GENERAL", () -> tarifa("GENERAL", "ESTUDIANTE", false, 0, "TRANSFERENCIA", false));
        caso("P06 Convenio y crédito: GENERAL", () -> tarifa("GENERAL", "CONVENIO", false, 0, "TARJETA_CREDITO", false));
        caso("P07 Suscripción con 4 intentos: bloqueada", () -> excepcion(ReglaCobroException.class,
            () -> SERVICIO.determinarCodigoTarifa(cuota("NINGUNO", true, 4), medio("EFECTIVO", true))));
        caso("P08 Suscripción con 5 intentos y efectivo: SUSCRIPCION", () -> tarifa("SUSCRIPCION", "NINGUNO", true, 5, "EFECTIVO", true));
        caso("P09 Suscripción con 5 intentos y transferencia: SUSCRIPCION", () -> tarifa("SUSCRIPCION", "NINGUNO", true, 5, "TRANSFERENCIA", false));
        caso("P10 Suscripción y crédito: bloqueada", () -> excepcion(ReglaCobroException.class,
            () -> SERVICIO.determinarCodigoTarifa(cuota("NINGUNO", true, 5), medio("TARJETA_CREDITO", false))));
        caso("P11 Suscripción prioritaria: no acumula beneficio", () -> tarifa("SUSCRIPCION", "ESTUDIANTE", true, 5, "EFECTIVO", true));
        caso("P12 Cuota nula: error de validación", () -> excepcion(IllegalArgumentException.class,
            () -> SERVICIO.determinarCodigoTarifa(null, medio("EFECTIVO", true))));
        caso("P13 Medio nulo: error de validación", () -> excepcion(IllegalArgumentException.class,
            () -> SERVICIO.determinarCodigoTarifa(cuota("NINGUNO", false, 0), null)));
        caso("P14 Política de beneficio incompatible: bloqueada", () -> excepcion(ReglaCobroException.class,
            () -> new TarifaConBeneficio().seleccionar(cuota("NINGUNO", false, 0), medio("EFECTIVO", true))));
        caso("P15 Crédito: 20000 + 10% = 22000", () -> {
            ResultadoCobro r = SERVICIO.calcular(d("20000"), d("20000"), d("10"), d("0"), false);
            dinero("2000", r.getRecargo()); dinero("22000", r.getTotalAbonar());
        });
        caso("P16 Tarifa 15000, saldo 3000: total 12000", () -> {
            ResultadoCobro r = SERVICIO.calcular(d("20000"), d("15000"), d("0"), d("3000"), true);
            dinero("5000", r.getDescuento()); dinero("3000", r.getSaldoAplicado()); dinero("12000", r.getTotalAbonar());
        });
        caso("P17 Tarifa 7500, saldo 9000: aplica 7500 y total 0", () -> {
            ResultadoCobro r = SERVICIO.calcular(d("10000"), d("7500"), d("0"), d("9000"), true);
            dinero("7500", r.getSaldoAplicado()); dinero("0", r.getTotalAbonar());
        });
        caso("P18 Saldo -1: error de validación", () -> excepcion(IllegalArgumentException.class,
            () -> SERVICIO.calcular(d("100"), d("100"), d("0"), d("-1"), true)));
        caso("P19 Recargo -10: error de validación", () -> excepcion(IllegalArgumentException.class,
            () -> SERVICIO.calcular(d("100"), d("100"), d("-10"), d("0"), false)));
        caso("P20 Despacho polimórfico: GENERAL, ESTUDIANTE, SUSCRIPCION", () -> {
            PoliticaTarifa[] reglas = {new TarifaGeneral(), new TarifaConBeneficio(), new TarifaSuscripcion()};
            Cuota[] cuotas = {cuota("NINGUNO", false, 0), cuota("ESTUDIANTE", false, 0), cuota("NINGUNO", true, 5)};
            String[] esperados = {"GENERAL", "ESTUDIANTE", "SUSCRIPCION"};
            for (int i = 0; i < reglas.length; i++) {
                igual(esperados[i], reglas[i].seleccionar(cuotas[i], medio("EFECTIVO", true)));
                if (reglas[i].getDescripcion().isEmpty()) throw new AssertionError("Constructor sin descripción");
            }
        });
        System.out.println("Pruebas de políticas de cobro superadas: " + aprobadas + "/20.");
    }

    private static void caso(String nombre, Runnable prueba) {
        prueba.run();
        aprobadas++;
        System.out.println("OK " + nombre);
    }

    private static void tarifa(String esperado, String beneficio, boolean suscripcion,
            int intentos, String nombreMedio, boolean admiteBeneficio) {
        igual(esperado, SERVICIO.determinarCodigoTarifa(
            cuota(beneficio, suscripcion, intentos), medio(nombreMedio, admiteBeneficio)));
    }

    private static Cuota cuota(String beneficio, boolean suscripcion, int intentos) {
        Cuota c = new Cuota();
        c.setBeneficioActivo(beneficio);
        c.setSuscripcionActiva(suscripcion);
        c.setIntentosSuscripcion(intentos);
        return c;
    }

    private static MedioPago medio(String nombre, boolean admiteBeneficio) {
        return new MedioPago(1, nombre, BigDecimal.ZERO, admiteBeneficio);
    }

    private static BigDecimal d(String valor) { return new BigDecimal(valor); }

    private static void dinero(String esperado, BigDecimal obtenido) {
        if (d(esperado).compareTo(obtenido) != 0) {
            throw new AssertionError("Esperado: " + esperado + "; obtenido: " + obtenido);
        }
    }

    private static void igual(String esperado, String obtenido) {
        if (!esperado.equals(obtenido)) {
            throw new AssertionError("Esperado: " + esperado + "; obtenido: " + obtenido);
        }
    }

    private static void excepcion(Class<? extends RuntimeException> tipo, Runnable accion) {
        try { accion.run(); }
        catch (RuntimeException e) {
            if (tipo.isInstance(e)) return;
            throw new AssertionError("Excepción inesperada: " + e, e);
        }
        throw new AssertionError("Se esperaba " + tipo.getSimpleName());
    }
}
