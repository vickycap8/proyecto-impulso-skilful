package ar.com.skilful.vista;

import java.awt.*;
import ar.com.skilful.sesion.SesionUsuario;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;

import javax.swing.*;

import ar.com.skilful.conexion.ConexionBD;

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

    public VentanaCobros() {
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

        botonRegistrar.addActionListener(e ->
            JOptionPane.showMessageDialog(
                this,
                "El cálculo es correcto. En el siguiente paso "
                + "incorporaremos el registro definitivo del pago."
            )
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
        String sql =
            "SELECT id_medio_pago, nombre, recargo_porcentaje, "
          + "admite_beneficio "
          + "FROM medio_pago WHERE activo = TRUE "
          + "AND nombre <> 'MERCADO_PAGO' "
          + "ORDER BY id_medio_pago";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                 conexion.prepareStatement(sql);
             ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                comboMedioPago.addItem(
                    new MedioPago(
                        resultado.getInt("id_medio_pago"),
                        resultado.getString("nombre"),
                        resultado.getBigDecimal(
                            "recargo_porcentaje"
                        ),
                        resultado.getBoolean("admite_beneficio")
                    )
                );
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

        String sql =
            "SELECT c.id_cuota, m.id_plan, p.nombre AS plan, "
          + "c.periodo, c.fecha_vencimiento, c.estado AS estado_cuota, "
          + "c.importe_original, "
          + "COALESCE(tb.nombre, 'NINGUNO') AS beneficio, "
          + "EXISTS ("
          + "SELECT 1 FROM suscripcion su "
          + "WHERE su.id_membresia = m.id_membresia "
          + "AND su.estado = 'ACTIVA'"
          + ") AS tiene_suscripcion, "
          + "COALESCE(("
          + "SELECT MAX(ic.numero_intento) "
          + "FROM suscripcion su "
          + "INNER JOIN intento_cobro ic "
          + "ON ic.id_suscripcion = su.id_suscripcion "
          + "WHERE su.id_membresia = m.id_membresia "
          + "AND su.estado = 'ACTIVA' "
          + "AND ic.id_cuota = c.id_cuota"
          + "), 0) AS intentos_suscripcion "
          + "FROM membresia m "
          + "INNER JOIN plan p ON p.id_plan = m.id_plan "
          + "INNER JOIN cuota c ON c.id_membresia = m.id_membresia "
          + "LEFT JOIN beneficio_socio bs "
          + "ON bs.id_socio = m.id_socio "
          + "AND bs.estado = 'ACTIVO' "
          + "AND (bs.fecha_vencimiento IS NULL "
          + "OR bs.fecha_vencimiento >= CURDATE()) "
          + "LEFT JOIN tipo_beneficio tb "
          + "ON tb.id_tipo_beneficio = bs.id_tipo_beneficio "
          + "WHERE m.id_socio = ? "
          + "AND m.estado = 'ACTIVA' "
          + "AND c.estado IN ('PENDIENTE', 'VENCIDA') "
          + "ORDER BY c.periodo "
          + "LIMIT 1";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSocio);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                	boolean tieneSuscripcion =
                		    resultado.getBoolean("tiene_suscripcion");

                		int intentosSuscripcion =
                		    resultado.getInt("intentos_suscripcion");

                		pagoPresencialBloqueado =
                		    tieneSuscripcion && intentosSuscripcion < 5;

                		pagoAlternativoSuscripcion =
                			    tieneSuscripcion;
                    idCuota = resultado.getInt("id_cuota");
                    idPlan = resultado.getInt("id_plan");
                    precioOriginal = resultado.getBigDecimal(
                        "importe_original"
                    );
                    beneficioActivo = resultado.getString(
                        "beneficio"
                    );
                    
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

                    campoPlan.setText(resultado.getString("plan"));
                    campoPeriodo.setText(
                        resultado.getDate("periodo").toString()
                    );               
                    campoVencimiento.setText(
                    	    resultado.getDate(
                    	        "fecha_vencimiento"
                    	    ).toString()
                    );
                    campoEstadoCuota.setText(
                    	    resultado.getString("estado_cuota")
                    );
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
            }

        } catch (SQLException e) {
            mostrarError(
                "No se pudo consultar la cuota pendiente.",
                e
            );
        }
    }
    
    private void cargarSaldoDisponible() {
        String sql =
            "SELECT saldo_disponible "
          + "FROM vw_saldo_socio "
          + "WHERE id_socio = ?";

        saldoDisponible = BigDecimal.ZERO;

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSocio);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    saldoDisponible = resultado.getBigDecimal(
                        "saldo_disponible"
                    );
                }
            }

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

        String codigoTarifa = "GENERAL";

        if (pagoAlternativoSuscripcion) {
            String nombreMedio = medio.getNombre();

            if (!"EFECTIVO".equals(nombreMedio)
                    && !"TRANSFERENCIA".equals(nombreMedio)) {

                idTarifaAplicada = -1;
                campoDescuento.setText("-");
                campoRecargo.setText("-");
                campoImporteFinal.setText("-");
                campoSaldoAplicado.setText("-");
                campoTotalAbonar.setText("-");

                JOptionPane.showMessageDialog(
                    this,
                    "Después de cinco rechazos, la tarifa de suscripción "
                    + "solo puede conservarse pagando en efectivo "
                    + "o transferencia.",
                    "Medio de pago no permitido",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            codigoTarifa = "SUSCRIPCION";

        } else if (medio.isAdmiteBeneficio()
                && !"NINGUNO".equals(beneficioActivo)) {

            codigoTarifa = beneficioActivo;
        }

        String sql =
            "SELECT id_tarifa, monto "
          + "FROM tarifa_plan "
          + "WHERE id_plan = ? "
          + "AND codigo_tarifa = ? "
          + "AND activo = TRUE "
          + "AND CURDATE() >= vigencia_desde "
          + "AND (vigencia_hasta IS NULL "
          + "OR CURDATE() <= vigencia_hasta) "
          + "ORDER BY vigencia_desde DESC LIMIT 1";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPlan);
            sentencia.setString(2, codigoTarifa);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "No existe una tarifa vigente para "
                        + codigoTarifa + "."
                    );
                    return;
                }

                idTarifaAplicada = resultado.getInt("id_tarifa");

                BigDecimal tarifaAplicada =
                    resultado.getBigDecimal("monto");

                BigDecimal descuento =
                    precioOriginal.subtract(tarifaAplicada);

                if (descuento.compareTo(BigDecimal.ZERO) < 0) {
                    descuento = BigDecimal.ZERO;
                }

                BigDecimal recargo = tarifaAplicada
                    .multiply(medio.getRecargo())
                    .divide(
                        new BigDecimal("100"),
                        2,
                        RoundingMode.HALF_UP
                    );
                
                descuentoCalculado = descuento;
                recargoCalculado = recargo;

                importeFinal = tarifaAplicada.add(recargo);

                campoDescuento.setText(formatoMoneda(descuento));
                campoRecargo.setText(formatoMoneda(recargo));
                campoImporteFinal.setText(
                    formatoMoneda(importeFinal)
                );
                actualizarAplicacionSaldo();
            }

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

        Connection conexion = null;

        try {
            conexion = ConexionBD.conectar();
            conexion.setAutoCommit(false);

            int idUsuario = SesionUsuario.getIdUsuario();
            int idSede = SesionUsuario.getIdSede();

            if (idUsuario == 0 || idSede == 0) {
                throw new SQLException(
                    "No existe una sesión activa con una sede seleccionada."
                );
            }

            String sqlPago =
                "INSERT INTO pago "
              + "(id_cuota, id_medio_pago, id_tarifa_aplicada, "
              + "id_usuario, id_sede, precio_original, descuento, "
              + "recargo, saldo_aplicado, importe_final, "
              + "importe_abonado, saldo_generado, estado) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
              + "'REGISTRADO')";

            long idPago;

            try (PreparedStatement sentencia = conexion.prepareStatement(
                    sqlPago,
                    Statement.RETURN_GENERATED_KEYS)) {

                sentencia.setInt(1, idCuota);
                sentencia.setInt(2, medio.getId());
                sentencia.setInt(3, idTarifaAplicada);
                sentencia.setInt(4, idUsuario);
                sentencia.setInt(5, idSede);
                sentencia.setBigDecimal(6, precioOriginal);
                sentencia.setBigDecimal(7, descuentoCalculado);
                sentencia.setBigDecimal(8, recargoCalculado);
                sentencia.setBigDecimal(9, saldoAplicado);
                sentencia.setBigDecimal(10, importeFinal);
                sentencia.setBigDecimal(11, importeAbonado);
                sentencia.setBigDecimal(12, saldoGenerado);

                sentencia.executeUpdate();

                try (ResultSet claves = sentencia.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new SQLException(
                            "No se pudo obtener el identificador del pago."
                        );
                    }

                    idPago = claves.getLong(1);
                }
            }

            String sqlCuota =
                "UPDATE cuota SET estado = 'PAGADA' "
              + "WHERE id_cuota = ?";

            try (PreparedStatement sentencia =
                    conexion.prepareStatement(sqlCuota)) {

                sentencia.setInt(1, idCuota);
                sentencia.executeUpdate();
            }
            
            if (saldoAplicado.compareTo(BigDecimal.ZERO) > 0) {
                String sqlDebito =
                    "INSERT INTO movimiento_saldo "
                  + "(id_socio, id_pago, tipo, monto, descripcion) "
                  + "VALUES (?, ?, 'DEBITO', ?, "
                  + "'Saldo aplicado al pago de cuota')";

                try (PreparedStatement sentencia =
                        conexion.prepareStatement(sqlDebito)) {

                    sentencia.setInt(1, idSocio);
                    sentencia.setLong(2, idPago);
                    sentencia.setBigDecimal(3, saldoAplicado);
                    sentencia.executeUpdate();
                }
            }

            if (saldoGenerado.compareTo(BigDecimal.ZERO) > 0) {
                String sqlSaldo =
                    "INSERT INTO movimiento_saldo "
                  + "(id_socio, id_pago, tipo, monto, descripcion) "
                  + "VALUES (?, ?, 'CREDITO', ?, "
                  + "'Saldo generado por cobro de cuota')";

                try (PreparedStatement sentencia =
                        conexion.prepareStatement(sqlSaldo)) {

                    sentencia.setInt(1, idSocio);
                    sentencia.setLong(2, idPago);
                    sentencia.setBigDecimal(3, saldoGenerado);
                    sentencia.executeUpdate();
                }
            }

            conexion.commit();

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

        } catch (SQLException e) {
            if (conexion != null) {
                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    System.out.println(
                        "No se pudo revertir la operación."
                    );
                }
            }

            mostrarError("No se pudo registrar el pago.", e);

        } finally {
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException e) {
                    System.out.println(
                        "No se pudo cerrar la conexión."
                    );
                }
            }
        }
    }
    
    private void actualizarAplicacionSaldo() {
        if (checkUsarSaldo.isSelected()) {
            saldoAplicado = saldoDisponible.min(importeFinal);
        } else {
            saldoAplicado = BigDecimal.ZERO;
        }

        totalAbonar = importeFinal.subtract(saldoAplicado);

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
        beneficioActivo = null;
        precioOriginal = BigDecimal.ZERO;
        importeFinal = BigDecimal.ZERO;
        
        descuentoCalculado = BigDecimal.ZERO;
        recargoCalculado = BigDecimal.ZERO;
        
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

    private static class MedioPago {
    	
    	public String getNombre() {
    	    return nombre;
    	}

        private final int id;
        private final String nombre;
        private final BigDecimal recargo;
        private final boolean admiteBeneficio;

        public MedioPago(
                int id,
                String nombre,
                BigDecimal recargo,
                boolean admiteBeneficio) {

            this.id = id;
            this.nombre = nombre;
            this.recargo = recargo;
            this.admiteBeneficio = admiteBeneficio;
        }

        public int getId() {
            return id;
        }

        public BigDecimal getRecargo() {
            return recargo;
        }

        public boolean isAdmiteBeneficio() {
            return admiteBeneficio;
        }

        @Override
        public String toString() {
            return nombre.replace("_", " ");
        }
    }
}