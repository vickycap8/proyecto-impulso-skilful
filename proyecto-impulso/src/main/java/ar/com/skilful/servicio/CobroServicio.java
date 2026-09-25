package ar.com.skilful.servicio;

import ar.com.skilful.dao.CuotaDAO;
import ar.com.skilful.dao.PagoDAO;
import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;
import ar.com.skilful.modelo.Pago;
import ar.com.skilful.modelo.ResultadoCobro;
import ar.com.skilful.modelo.Tarifa;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.util.List;

public class CobroServicio {

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final CuotaDAO cuotaDAO;
    private final PagoDAO pagoDAO;

    public CobroServicio() {
        this(new CuotaDAO(), new PagoDAO());
    }

    public CobroServicio(CuotaDAO cuotaDAO, PagoDAO pagoDAO) {
        this.cuotaDAO = cuotaDAO;
        this.pagoDAO = pagoDAO;
    }

    public List<MedioPago> listarMediosPagoPresenciales()
            throws SQLException {
        return cuotaDAO.listarMediosPagoPresenciales();
    }

    public Cuota buscarCuotaPendiente(int idSocio) throws SQLException {
        return cuotaDAO.buscarPendientePorSocio(idSocio);
    }

    public BigDecimal obtenerSaldoSocio(int idSocio) throws SQLException {
        return cuotaDAO.obtenerSaldoSocio(idSocio);
    }

    public String determinarCodigoTarifa(
            Cuota cuota,
            MedioPago medioPago) {

        if (cuota.isSuscripcionActiva()) {
            if (cuota.getIntentosSuscripcion() < 5) {
                throw new IllegalStateException(
                    "La cuota debe gestionarse desde Control de suscripciones."
                );
            }

            String medio = medioPago.getNombre();
            if (!"EFECTIVO".equals(medio) && !"TRANSFERENCIA".equals(medio)) {
                throw new IllegalArgumentException(
                    "Tras cinco rechazos, la tarifa de suscripción solo puede "
                    + "abonarse en efectivo o transferencia."
                );
            }
            return "SUSCRIPCION";
        }

        if (medioPago.isAdmiteBeneficio()
                && cuota.getBeneficioActivo() != null
                && !"NINGUNO".equals(cuota.getBeneficioActivo())) {
            return cuota.getBeneficioActivo();
        }

        return "GENERAL";
    }

    public Tarifa buscarTarifa(int idPlan, String codigoTarifa)
            throws SQLException {
        return cuotaDAO.buscarTarifaVigente(idPlan, codigoTarifa);
    }

    public ResultadoCobro calcular(
            BigDecimal precioOriginal,
            BigDecimal tarifaAplicada,
            BigDecimal recargoPorcentaje,
            BigDecimal saldoDisponible,
            boolean aplicarSaldo) {

        validarImporte(precioOriginal, "precio original");
        validarImporte(tarifaAplicada, "tarifa aplicada");

        BigDecimal porcentaje = valorNoNulo(recargoPorcentaje);
        BigDecimal saldo = valorNoNulo(saldoDisponible);

        if (porcentaje.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El recargo no puede ser negativo.");
        }
        if (saldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo.");
        }

        BigDecimal descuento = precioOriginal.subtract(tarifaAplicada)
            .max(BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal recargo = tarifaAplicada
            .multiply(porcentaje)
            .divide(CIEN, 2, RoundingMode.HALF_UP);

        BigDecimal importeFinal = tarifaAplicada
            .add(recargo)
            .setScale(2, RoundingMode.HALF_UP);

        BigDecimal saldoAplicado = aplicarSaldo
            ? saldo.min(importeFinal).setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalAbonar = importeFinal
            .subtract(saldoAplicado)
            .setScale(2, RoundingMode.HALF_UP);

        return new ResultadoCobro(
            descuento, recargo, importeFinal, saldoAplicado, totalAbonar
        );
    }

    public long registrarPago(Pago pago) throws SQLException {
        if (pago == null) {
            throw new IllegalArgumentException("Debe informar el pago.");
        }
        if (pago.getIdUsuario() <= 0 || pago.getIdSede() <= 0) {
            throw new IllegalStateException(
                "No existe una sesión activa con una sede seleccionada."
            );
        }
        validarImporte(pago.getImporteAbonado(), "importe abonado");
        if (pago.getImporteAbonado().compareTo(
                pago.getImporteFinal().subtract(pago.getSaldoAplicado())) < 0) {
            throw new IllegalArgumentException(
                "El importe abonado no puede ser menor al total."
            );
        }
        return pagoDAO.registrar(pago);
    }

    private void validarImporte(BigDecimal importe, String nombre) {
        if (importe == null || importe.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                "El " + nombre + " debe ser un valor válido."
            );
        }
    }

    private BigDecimal valorNoNulo(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
