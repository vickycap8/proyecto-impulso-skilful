package ar.com.skilful.vista;

import java.awt.*;
import ar.com.skilful.sesion.SesionUsuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;

import javax.swing.*;

import ar.com.skilful.modelo.Cuota;
import ar.com.skilful.modelo.MedioPago;
import ar.com.skilful.modelo.Pago;
import ar.com.skilful.modelo.ResultadoCobro;
import ar.com.skilful.modelo.Tarifa;
import ar.com.skilful.servicio.CobroServicio;

public class VentanaCobros extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField campoSocio;
    private JTextField campoPlan;
    private JTextField campoPeriodo;
    private JTextField campoVencimiento;
    private JTextField campoPrecioOriginal;
    private JTextField campoBeneficio;
    private JTextField campoDescuento;
    private JTextField campoRecargo;
    private JTextField campoImporteFinal;
    
    private JTextField campoEstadoCuota;
    private JTextField campoSaldoDisponible;
    private JTextField campoSaldoAplicado;
    private JTextField campoTotalAbonar;
    private JCheckBox checkUsarSaldo;

    private BigDecimal saldoDisponible = BigDecimal.ZERO;
    private BigDecimal saldoAplicado = BigDecimal.ZERO;
    private BigDecimal totalAbonar = BigDecimal.ZERO;

    private JComboBox<MedioPago> comboMedioPago;
    
    private boolean pagoAlternativoSuscripcion = false;
    private boolean pagoPresencialBloqueado;

    private int idSocio = -1;
    private int idCuota = -1;
    private int idPlan = -1;
    private int idTarifaAplicada = -1;

    private String beneficioActivo;
    private BigDecimal precioOriginal = BigDecimal.ZERO;
    private BigDecimal importeFinal = BigDecimal.ZERO;
    private BigDecimal descuentoCalculado = BigDecimal.ZERO;
    private BigDecimal recargoCalculado = BigDecimal.ZERO;
    private BigDecimal montoTarifaAplicada = BigDecimal.ZERO;
    private Cuota cuotaActual;
    private final CobroServicio cobroServicio;

    public VentanaCobros() {
        cobroServicio = new CobroServicio();
        configurarVentana();
        crearContenido();
        cargarMediosPago();
    }

    private void configurarVentana() {
        setTitle("Registro de cobro - Proyecto Impulso");
        setSize(720, 730);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void crearContenido() {
        setLayout(new BorderLayout());

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(25, 25, 25));
        encabezado.setBorder(
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        );

        JLabel titulo = new JLabel("REGISTRAR COBRO DE CUOTA");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 21));
        encabezado.add(titulo);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(
            BorderFactory.createEmptyBorder(25, 35, 15, 35)
        );

        campoSocio = crearCampoLectura();
        campoPlan = crearCampoLectura();
        campoPeriodo = crearCampoLectura();
        campoVencimiento = crearCampoLectura();
        campoEstadoCuota = crearCampoLectura();
        campoPrecioOriginal = crearCampoLectura();
        campoBeneficio = crearCampoLectura();
        campoDescuento = crearCampoLectura();
        campoRecargo = crearCampoLectura();
        campoImporteFinal = crearCampoLectura();
        campoSaldoDisponible = crearCampoLectura();
        campoSaldoAplicado = crearCampoLectura();
        campoTotalAbonar = crearCampoLectura();

        checkUsarSaldo = new JCheckBox("Aplicar saldo disponible");
        checkUsarSaldo.setEnabled(false);

        campoTotalAbonar.setFont(
            new Font("Arial", Font.BOLD, 17)
        );

        campoImporteFinal.setFont(
            new Font("Arial", Font.BOLD, 17)
        );

        comboMedioPago = new JComboBox<>();

        JButton botonBuscarSocio = crearBoton("Buscar socio");

        JPanel panelSocio = new JPanel(new BorderLayout(5, 0));
        panelSocio.add(campoSocio, BorderLayout.CENTER);
        panelSocio.add(botonBuscarSocio, BorderLayout.EAST);

        agregarCampo(formulario, "Socio:", panelSocio, 0);
        agregarCampo(formulario, "Plan:", campoPlan, 1);
        agregarCampo(formulario, "Periodo:", campoPeriodo, 2);
        agregarCampo(formulario, "Vencimiento:", campoVencimiento, 3);
        agregarCampo(formulario, "Estado de la cuota:", campoEstadoCuota, 4);
        agregarCampo(formulario, "Precio general:", campoPrecioOriginal, 5);
        agregarCampo(formulario, "Beneficio activo:", campoBeneficio, 6);
        agregarCampo(formulario, "Medio de pago:", comboMedioPago, 7);
        agregarCampo(formulario, "Descuento:", campoDescuento, 8);
        agregarCampo(formulario, "Recargo:", campoRecargo, 9);
        agregarCampo(formulario, "Importe calculado:", campoImporteFinal, 10);
        agregarCampo(formulario, "Saldo disponible:", campoSaldoDisponible, 11);
        agregarCampo(formulario, "", checkUsarSaldo, 12);
        agregarCampo(formulario, "Saldo aplicado:", campoSaldoAplicado, 13);
        agregarCampo(formulario, "TOTAL A ABONAR:", campoTotalAbonar, 14);

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        panelBotones.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 15, 20)
        );

        JButton botonRegistrar = crearBoton("Registrar pago");
        JButton botonHistorial =
        	    crearBoton("Historial de cobros");
        JButton botonCerrar = crearBoton("Cerrar");

        botonRegistrar.addActionListener(e -> registrarPago());

        panelBotones.add(botonRegistrar);
        panelBotones.add(botonHistorial);
        panelBotones.add(botonCerrar);

        botonBuscarSocio.addActionListener(e -> buscarSocio());

        comboMedioPago.addActionListener(e -> {
            if (idCuota != -1) {
                calcularImporte();
            }
        });
        
        checkUsarSaldo.addActionListener(
        	    e -> actualizarAplicacionSaldo()
        	);

        botonHistorial.addActionListener(e -> {
            new VentanaHistorialCobros().setVisible(true);
        });

        botonCerrar.addActionListener(e -> dispose());

        add(encabezado, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        
    }

    private JTextField crearCampoLectura() {
        JTextField campo = new JTextField(25);
        campo.setEditable(false);
        campo.setBackground(Color.WHITE);
        return campo;
    }

    private void agregarCampo(
            JPanel panel,
            String etiqueta,
            Component componente,
            int fila) {

        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(7, 5, 7, 5);
        restricciones.anchor = GridBagConstraints.WEST;

        restricciones.gridx = 0;
        restricciones.gridy = fila;
        panel.add(new JLabel(etiqueta), restricciones);

        restricciones.gridx = 1;
        restricciones.fill = GridBagConstraints.HORIZONTAL;
        restricciones.weightx = 1;
        panel.add(componente, restricciones);
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(new Color(190, 25, 35));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        return boton;
    }

    private void cargarMediosPago() {
        try {
            for (MedioPago medio
                    : cobroServicio.listarMediosPagoPresenciales()) {
                comboMedioPago.addItem(medio);
            }

        } catch (SQLException e) {
            mostrarError(
                "No se pudieron cargar los medios de pago.",
                e
            );
        }
    }

    private void buscarSocio() {
        SelectorSocio selector = new SelectorSocio(this);

        if (selector.mostrarSelector()) {
            idSocio = selector.getIdSocioSeleccionado();
            campoSocio.setText(selector.getDescripcionSocio());
            cargarCuotaPendiente();
        }
    }

    private void cargarCuotaPendiente() {
        limpiarDatosCuota();

        try {
            cuotaActual = cobroServicio.buscarCuotaPendiente(idSocio);

            if (cuotaActual != null) {
                boolean tieneSuscripcion = cuotaActual.isSuscripcionActiva();
                int intentosSuscripcion = cuotaActual.getIntentosSuscripcion();

                pagoPresencialBloqueado =
                    tieneSuscripcion && intentosSuscripcion < 5;

                pagoAlternativoSuscripcion = tieneSuscripcion;
                idCuota = cuotaActual.getId();
                idPlan = cuotaActual.getIdPlan();
                precioOriginal = cuotaActual.getImporteOriginal();
                beneficioActivo = cuotaActual.getBeneficioActivo();
                    
                    if (pagoPresencialBloqueado) {

                        campoBeneficio.setText(
                            "SUSCRIPCIÓN ACTIVA - "
                                + intentosSuscripcion
                                + " DE 5 INTENTOS"
                        );

                    } else if (pagoAlternativoSuscripcion) {

                        campoBeneficio.setText(
                            "TARIFA DE SUSCRIPCIÓN - 5 RECHAZOS"
                        );

                    } else {

                        campoBeneficio.setText(beneficioActivo);
                    }

                    campoPlan.setText(cuotaActual.getNombrePlan());
                    campoPeriodo.setText(cuotaActual.getPeriodo().toString());
                    campoVencimiento.setText(
                        cuotaActual.getFechaVencimiento().toString()
                    );
                    campoEstadoCuota.setText(cuotaActual.getEstado());
                    campoPrecioOriginal.setText(
                    	    formatoMoneda(precioOriginal)
                    );

                    cargarSaldoDisponible();

                    calcularImporte();

                    if (pagoPresencialBloqueado) {

                        JOptionPane.showMessageDialog(
                            this,
                            "La cuota pertenece a una suscripción activa.\n"
                                + "Debe registrarse desde el control "
                                + "de suscripciones.\n"
                                + "Intentos actuales: "
                                + intentosSuscripcion + " de 5.",
                            "Pago presencial no habilitado",
                            JOptionPane.WARNING_MESSAGE
                        );
                    }

            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "El socio no posee una cuota pendiente "
                    + "en una membresía activa."
                );
            }

        } catch (SQLException e) {
            mostrarError(
                "No se pudo consultar la cuota pendiente.",
                e
            );
        }
    }
    
    private void cargarSaldoDisponible() {
        saldoDisponible = BigDecimal.ZERO;

        try {
            saldoDisponible = cobroServicio.obtenerSaldoSocio(idSocio);

            campoSaldoDisponible.setText(
                formatoMoneda(saldoDisponible)
            );

            checkUsarSaldo.setSelected(false);
            checkUsarSaldo.setEnabled(
                saldoDisponible.compareTo(BigDecimal.ZERO) > 0
            );

        } catch (SQLException e) {
            mostrarError(
                "No se pudo consultar el saldo disponible.",
                e
            );
        }
    }

    private void calcularImporte() {
        MedioPago medio =
            (MedioPago) comboMedioPago.getSelectedItem();

        if (medio == null || idCuota == -1) {
            return;
        }

        try {
            String codigoTarifa =
                cobroServicio.determinarCodigoTarifa(cuotaActual, medio);

            Tarifa tarifa = cobroServicio.buscarTarifa(idPlan, codigoTarifa);

            if (tarifa == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "No existe una tarifa vigente para " + codigoTarifa + "."
                );
                return;
            }

            idTarifaAplicada = tarifa.getId();
            montoTarifaAplicada = tarifa.getMonto();
            actualizarAplicacionSaldo();

        } catch (IllegalArgumentException | IllegalStateException e) {
            idTarifaAplicada = -1;
            campoDescuento.setText("-");
            campoRecargo.setText("-");
            campoImporteFinal.setText("-");
            campoSaldoAplicado.setText("-");
            campoTotalAbonar.setText("-");
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Operación no permitida",
                JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException e) {
            mostrarError("No se pudo calcular el importe.", e);
        }
    }
    
    private void registrarPago() {
    	if (pagoPresencialBloqueado) {

    	    JOptionPane.showMessageDialog(
    	        this,
    	        "El pago presencial todavía no está habilitado.\n"
    	            + "El cobro debe registrarse desde "
    	            + "Control de suscripciones.",
    	        "Operación no permitida",
    	        JOptionPane.WARNING_MESSAGE
    	    );

    	    return;
    	}
        if (idCuota == -1 || idTarifaAplicada == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar un socio con una cuota pendiente."
            );
            return;
        }

        String entrada = JOptionPane.showInputDialog(
            this,
            "Importe calculado: " + formatoMoneda(importeFinal)
            + "\nSaldo aplicado: " + formatoMoneda(saldoAplicado)
            + "\nTotal a abonar: " + formatoMoneda(totalAbonar)
            + "\n\nIngrese el importe abonado:",
            totalAbonar.toPlainString()
        );

        if (entrada == null) {
            return;
        }

        BigDecimal importeAbonado;

        try {
            importeAbonado = new BigDecimal(
                entrada.trim().replace(",", ".")
            );
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this,
                "Ingrese un importe numérico válido."
            );
            return;
        }

        if (importeAbonado.compareTo(totalAbonar) < 0) {
            JOptionPane.showMessageDialog(
                this,
                "El importe abonado no puede ser menor al total."
            );
            return;
        }

        BigDecimal saldoGenerado =
        	    importeAbonado.subtract(totalAbonar);

        int confirmacion = JOptionPane.showConfirmDialog(
        	    this,
        	    "Importe calculado: " + formatoMoneda(importeFinal)
        	    + "\nSaldo aplicado: " + formatoMoneda(saldoAplicado)
        	    + "\nTotal a abonar: " + formatoMoneda(totalAbonar)
        	    + "\nImporte abonado: " + formatoMoneda(importeAbonado)
        	    + "\nNuevo saldo generado: " + formatoMoneda(saldoGenerado)
        	    + "\n\n¿Confirma el registro del pago?",
        	    "Confirmar cobro",
        	    JOptionPane.YES_NO_OPTION
        	);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        guardarPago(importeAbonado, saldoGenerado);
    }

    private void guardarPago(
            BigDecimal importeAbonado,
            BigDecimal saldoGenerado) {

        MedioPago medio =
            (MedioPago) comboMedioPago.getSelectedItem();

        try {
            int idUsuario = SesionUsuario.getIdUsuario();
            int idSede = SesionUsuario.getIdSede();

            Pago pago = new Pago();
            pago.setIdSocio(idSocio);
            pago.setIdCuota(idCuota);
            pago.setIdMedioPago(medio.getId());
            pago.setIdTarifaAplicada(idTarifaAplicada);
            pago.setIdUsuario(idUsuario);
            pago.setIdSede(idSede);
            pago.setPrecioOriginal(precioOriginal);
            pago.setDescuento(descuentoCalculado);
            pago.setRecargo(recargoCalculado);
            pago.setSaldoAplicado(saldoAplicado);
            pago.setImporteFinal(importeFinal);
            pago.setImporteAbonado(importeAbonado);
            pago.setSaldoGenerado(saldoGenerado);

            cobroServicio.registrarPago(pago);

            JOptionPane.showMessageDialog(
                this,
                "El pago fue registrado correctamente."
                + (saldoGenerado.compareTo(BigDecimal.ZERO) > 0
                    ? "\nSe generó un saldo a favor de "
                      + formatoMoneda(saldoGenerado) + "."
                    : "")
            );

            campoSocio.setText("");
            limpiarDatosCuota();

        } catch (IllegalArgumentException | IllegalStateException e) {
            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Operación no permitida",
                JOptionPane.WARNING_MESSAGE
            );
        } catch (SQLException e) {
            mostrarError("No se pudo registrar el pago.", e);
        }
    }
    
    private void actualizarAplicacionSaldo() {
        MedioPago medio = (MedioPago) comboMedioPago.getSelectedItem();
        if (medio == null || idTarifaAplicada == -1) {
            return;
        }

        ResultadoCobro resultado = cobroServicio.calcular(
            precioOriginal,
            montoTarifaAplicada,
            medio.getRecargoPorcentaje(),
            saldoDisponible,
            checkUsarSaldo.isSelected()
        );

        descuentoCalculado = resultado.getDescuento();
        recargoCalculado = resultado.getRecargo();
        importeFinal = resultado.getImporteFinal();
        saldoAplicado = resultado.getSaldoAplicado();
        totalAbonar = resultado.getTotalAbonar();

        campoDescuento.setText(formatoMoneda(descuentoCalculado));
        campoRecargo.setText(formatoMoneda(recargoCalculado));
        campoImporteFinal.setText(formatoMoneda(importeFinal));

        campoSaldoAplicado.setText(
            formatoMoneda(saldoAplicado)
        );

        campoTotalAbonar.setText(
            formatoMoneda(totalAbonar)
        );
    }

    private void limpiarDatosCuota() {
        idCuota = -1;
        idPlan = -1;
        idTarifaAplicada = -1;
        cuotaActual = null;
        beneficioActivo = null;
        precioOriginal = BigDecimal.ZERO;
        importeFinal = BigDecimal.ZERO;
        
        descuentoCalculado = BigDecimal.ZERO;
        recargoCalculado = BigDecimal.ZERO;
        montoTarifaAplicada = BigDecimal.ZERO;
        
        saldoDisponible = BigDecimal.ZERO;
        saldoAplicado = BigDecimal.ZERO;
        totalAbonar = BigDecimal.ZERO;
        
        pagoAlternativoSuscripcion = false;
        pagoPresencialBloqueado = false;

        campoEstadoCuota.setText("");
        campoSaldoDisponible.setText("");
        campoSaldoAplicado.setText("");
        campoTotalAbonar.setText("");

        checkUsarSaldo.setSelected(false);
        checkUsarSaldo.setEnabled(false);
        
        campoPlan.setText("");
        campoPeriodo.setText("");
        campoVencimiento.setText("");
        campoPrecioOriginal.setText("");
        campoBeneficio.setText("");
        campoDescuento.setText("");
        campoRecargo.setText("");
        campoImporteFinal.setText("");
    }

    private String formatoMoneda(BigDecimal monto) {
        return "$ " + monto.setScale(
            2,
            RoundingMode.HALF_UP
        ).toPlainString();
    }

    private void mostrarError(String mensaje, SQLException e) {
        JOptionPane.showMessageDialog(
            this,
            mensaje + "\n" + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

}
