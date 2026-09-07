package ar.com.skilful.vista;

import java.awt.*;
import java.sql.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ar.com.skilful.sesion.SesionUsuario;
import ar.com.skilful.conexion.ConexionBD;

public class VentanaSuscripciones extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField campoBusqueda;
    private JTable tablaSuscripciones;
    private DefaultTableModel modeloTabla;

    public VentanaSuscripciones() {
        configurarVentana();
        crearContenido();
        cargarSuscripciones("");
    }

    private void configurarVentana() {
        setTitle("Control de suscripciones - Proyecto Impulso");
        setSize(1180, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void crearContenido() {
        setLayout(new BorderLayout(10, 10));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(25, 25, 25));
        encabezado.setBorder(
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        );

        JLabel titulo = new JLabel("CONTROL DE SUSCRIPCIONES");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel informacion = new JLabel(
            "Seguimiento centralizado de todas las sedes"
        );
        informacion.setForeground(Color.LIGHT_GRAY);

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(informacion, BorderLayout.EAST);

        JPanel panelBusqueda = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );

        panelBusqueda.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 0, 10)
        );

        panelBusqueda.add(
            new JLabel("Buscar por DNI, nombre o apellido:")
        );

        campoBusqueda = new JTextField(25);

        JButton botonBuscar = crearBoton("Buscar");
        JButton botonMostrarTodos = crearBoton("Mostrar todos");

        panelBusqueda.add(campoBusqueda);
        panelBusqueda.add(botonBuscar);
        panelBusqueda.add(botonMostrarTodos);

        modeloTabla = new DefaultTableModel(
            new Object[] {
                "ID", "DNI", "Socio", "Teléfono", "Sede",
                "Plan", "Día", "Permanencia", "Intentos",
                "Último resultado", "Notificación", "Estado"
            }, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaSuscripciones = new JTable(modeloTabla);
        tablaSuscripciones.setRowHeight(25);
        tablaSuscripciones.setAutoResizeMode(
            JTable.AUTO_RESIZE_OFF
        );
        tablaSuscripciones.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        establecerAnchos();

        JScrollPane desplazamiento =
            new JScrollPane(tablaSuscripciones);

        desplazamiento.setBorder(
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        panelBotones.setBorder(
            BorderFactory.createEmptyBorder(5, 10, 15, 15)
        );
        
        JButton botonNuevaSuscripcion = crearBoton("Nueva suscripción");

        JButton botonNuevoIntento = crearBoton("Registrar resultado");

        JButton botonNotificar = crearBoton("Marcar como notificado");
        
        JButton botonFinalizar = crearBoton("Finalizar suscripción");
        
        JButton botonCerrar = crearBoton("Cerrar");

        panelBotones.add(botonNuevaSuscripcion);
        panelBotones.add(botonNuevoIntento);
        panelBotones.add(botonNotificar);
        panelBotones.add(botonFinalizar);
        panelBotones.add(botonCerrar);

        botonBuscar.addActionListener(
            e -> cargarSuscripciones(
                campoBusqueda.getText().trim()
            )
        );

        campoBusqueda.addActionListener(
            e -> cargarSuscripciones(
                campoBusqueda.getText().trim()
            )
        );

        botonMostrarTodos.addActionListener(e -> {
            campoBusqueda.setText("");
            cargarSuscripciones("");
        });
        
        botonNuevaSuscripcion.addActionListener(e -> {
            FormularioSuscripcion formulario =
                new FormularioSuscripcion(
                    this,
                    () -> cargarSuscripciones("")
                );

            formulario.setVisible(true);
        });

        botonNuevoIntento.addActionListener(e -> {

            Object[] opciones = {
                "APROBADO",
                "RECHAZADO",
                "Cancelar"
            };

            int resultado = JOptionPane.showOptionDialog(
                this,
                "Seleccione el resultado del intento de cobro:",
                "Registrar resultado",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
            );

            if (resultado == 0) {
                registrarIntentoAprobado();

            } else if (resultado == 1) {
                registrarIntentoRechazado();
            }
        });

        botonNotificar.addActionListener(
        	    e -> registrarNotificacion()
        );
        
        botonFinalizar.addActionListener(
        	    e -> finalizarSuscripcion()
        	);

        botonCerrar.addActionListener(e -> dispose());

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(panelBusqueda, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(desplazamiento, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void establecerAnchos() {
        int[] anchos = {
            45, 90, 160, 110, 120, 60,
            45, 100, 65, 110, 100, 80
        };

        for (int i = 0; i < anchos.length; i++) {
            tablaSuscripciones.getColumnModel()
                .getColumn(i)
                .setPreferredWidth(anchos[i]);
        }
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

    private void validarSeleccion(String operacion) {
        if (tablaSuscripciones.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar una suscripción."
            );
            return;
        }

        JOptionPane.showMessageDialog(
            this,
            "A continuación incorporaremos la función para "
            + operacion + "."
        );
    }
    
    private void registrarIntentoRechazado() {
        int fila = tablaSuscripciones.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar una suscripción."
            );
            return;
        }

        int idSuscripcion = Integer.parseInt(
            modeloTabla.getValueAt(fila, 0).toString()
        );

        int intentosRealizados = Integer.parseInt(
            modeloTabla.getValueAt(fila, 8).toString()
        );

        if (intentosRealizados >= 5) {
            JOptionPane.showMessageDialog(
                this,
                "La suscripción alcanzó el máximo de cinco intentos. "
                + "El socio deberá abonar mediante un medio alternativo.",
                "Máximo de intentos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int numeroIntento = intentosRealizados + 1;

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Se registrará el intento número "
            + numeroIntento + " como RECHAZADO.\n"
            + "¿Desea continuar?",
            "Registrar rechazo",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        String observacion = JOptionPane.showInputDialog(
            this,
            "Ingrese una observación:",
            "Cobro rechazado por Mercado Pago"
        );

        if (observacion == null) {
            return;
        }

        String sql =
        	    "INSERT INTO intento_cobro "
        	  + "(id_suscripcion, id_cuota, id_usuario, id_sede, "
        	  + "numero_intento, importe, estado, observacion) "
        	  + "SELECT su.id_suscripcion, c.id_cuota, "
        	  + "?, ?, ?, tp.monto, 'RECHAZADO', ? "
        	  + "FROM suscripcion su "
        	  + "INNER JOIN membresia m "
        	  + "ON m.id_membresia = su.id_membresia "
        	  + "INNER JOIN cuota c "
        	  + "ON c.id_membresia = m.id_membresia "
        	  + "AND c.estado IN ('PENDIENTE', 'VENCIDA') "
        	  + "INNER JOIN tarifa_plan tp "
        	  + "ON tp.id_plan = m.id_plan "
        	  + "AND tp.codigo_tarifa = 'SUSCRIPCION' "
        	  + "AND tp.activo = TRUE "
        	  + "WHERE su.id_suscripcion = ? "
        	  + "AND su.estado = 'ACTIVA' "
        	  + "AND CURDATE() >= tp.vigencia_desde "
        	  + "AND (tp.vigencia_hasta IS NULL "
        	  + "OR CURDATE() <= tp.vigencia_hasta) "
        	  + "LIMIT 1";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

        	sentencia.setInt(
        		    1,
        		    SesionUsuario.getIdUsuario()
        		);

        		sentencia.setInt(
        		    2,
        		    SesionUsuario.getIdSede()
        		);

        		sentencia.setInt(3, numeroIntento);
        		sentencia.setString(4, observacion.trim());
        		sentencia.setInt(5, idSuscripcion);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se encontró una cuota pendiente para la suscripción."
                );
                return;
            }

            JOptionPane.showMessageDialog(
                this,
                "El intento " + numeroIntento
                + " fue registrado como rechazado."
            );

            cargarSuscripciones("");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo registrar el intento.\n"
                + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void registrarIntentoAprobado() {

        int fila = tablaSuscripciones.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar una suscripción."
            );
            return;
        }

        int idSuscripcion = Integer.parseInt(
            modeloTabla.getValueAt(fila, 0).toString()
        );

        int intentosRealizados = Integer.parseInt(
            modeloTabla.getValueAt(fila, 8).toString()
        );

        if (intentosRealizados >= 5) {
            JOptionPane.showMessageDialog(
                this,
                "La suscripción alcanzó el máximo de cinco intentos. "
                    + "El socio deberá abonar mediante un medio alternativo.",
                "Máximo de intentos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int numeroIntento = intentosRealizados + 1;

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Se registrará el intento número "
                + numeroIntento + " como APROBADO.\n"
                + "También se registrará el pago de la cuota.\n\n"
                + "¿Desea continuar?",
            "Confirmar cobro aprobado",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        String observacion = JOptionPane.showInputDialog(
            this,
            "Ingrese una observación o referencia:",
            "Cobro aprobado por Mercado Pago"
        );

        if (observacion == null) {
            return;
        }

        String sqlDatos =
            "SELECT c.id_cuota, c.importe_original, " +
            "tp.id_tarifa, tp.monto " +
            "FROM suscripcion su " +
            "INNER JOIN membresia m " +
            "ON m.id_membresia = su.id_membresia " +
            "INNER JOIN cuota c " +
            "ON c.id_membresia = m.id_membresia " +
            "AND c.estado IN ('PENDIENTE', 'VENCIDA') " +
            "INNER JOIN tarifa_plan tp " +
            "ON tp.id_plan = m.id_plan " +
            "AND tp.codigo_tarifa = 'SUSCRIPCION' " +
            "AND tp.activo = TRUE " +
            "WHERE su.id_suscripcion = ? " +
            "AND su.estado = 'ACTIVA' " +
            "AND CURDATE() >= tp.vigencia_desde " +
            "AND (tp.vigencia_hasta IS NULL " +
            "OR CURDATE() <= tp.vigencia_hasta) " +
            "ORDER BY c.periodo " +
            "LIMIT 1 FOR UPDATE";

        String sqlPago =
            "INSERT INTO pago " +
            "(id_cuota, id_medio_pago, id_tarifa_aplicada, " +
            "id_usuario, id_sede, precio_original, descuento, " +
            "recargo, saldo_aplicado, importe_final, " +
            "importe_abonado, saldo_generado, estado) " +
            "SELECT ?, mp.id_medio_pago, ?, ?, ?, ?, ?, " +
            "0, 0, ?, ?, 0, 'REGISTRADO' " +
            "FROM medio_pago mp " +
            "WHERE mp.nombre = 'MERCADO_PAGO' " +
            "AND mp.activo = TRUE";

        String sqlIntento =
            "INSERT INTO intento_cobro " +
            "(id_suscripcion, id_cuota, id_usuario, id_sede, " +
            "id_pago, numero_intento, importe, estado, observacion) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, 'APROBADO', ?)";

        String sqlActualizarCuota =
            "UPDATE cuota " +
            "SET estado = 'PAGADA' " +
            "WHERE id_cuota = ? " +
            "AND estado IN ('PENDIENTE', 'VENCIDA')";

        try (Connection conexion = ConexionBD.conectar()) {

            conexion.setAutoCommit(false);

            try {
                int idCuota;
                int idTarifa;
                double precioOriginal;
                double importeSuscripcion;

                try (PreparedStatement consulta =
                        conexion.prepareStatement(sqlDatos)) {

                    consulta.setInt(1, idSuscripcion);

                    try (ResultSet resultado = consulta.executeQuery()) {

                        if (!resultado.next()) {
                            conexion.rollback();

                            JOptionPane.showMessageDialog(
                                this,
                                "No se encontró una cuota pendiente "
                                    + "para la suscripción."
                            );
                            return;
                        }

                        idCuota = resultado.getInt("id_cuota");
                        idTarifa = resultado.getInt("id_tarifa");
                        precioOriginal =
                            resultado.getDouble("importe_original");
                        importeSuscripcion =
                            resultado.getDouble("monto");
                    }
                }

                double descuento =
                    Math.max(0, precioOriginal - importeSuscripcion);

                long idPago;

                try (PreparedStatement pago = conexion.prepareStatement(
                        sqlPago,
                        java.sql.Statement.RETURN_GENERATED_KEYS
                )) {
                    pago.setInt(1, idCuota);
                    pago.setInt(2, idTarifa);
                    pago.setInt(3, SesionUsuario.getIdUsuario());
                    pago.setInt(4, SesionUsuario.getIdSede());
                    pago.setDouble(5, precioOriginal);
                    pago.setDouble(6, descuento);
                    pago.setDouble(7, importeSuscripcion);
                    pago.setDouble(8, importeSuscripcion);

                    int filasPago = pago.executeUpdate();

                    if (filasPago == 0) {
                        throw new SQLException(
                            "No se encontró el medio de pago MERCADO_PAGO."
                        );
                    }

                    try (ResultSet claves = pago.getGeneratedKeys()) {
                        if (!claves.next()) {
                            throw new SQLException(
                                "No se pudo obtener el identificador del pago."
                            );
                        }

                        idPago = claves.getLong(1);
                    }
                }

                try (PreparedStatement intento =
                        conexion.prepareStatement(sqlIntento)) {

                    intento.setInt(1, idSuscripcion);
                    intento.setInt(2, idCuota);
                    intento.setInt(
                        3,
                        SesionUsuario.getIdUsuario()
                    );
                    intento.setInt(
                        4,
                        SesionUsuario.getIdSede()
                    );
                    intento.setLong(5, idPago);
                    intento.setInt(6, numeroIntento);
                    intento.setDouble(7, importeSuscripcion);
                    intento.setString(8, observacion.trim());

                    intento.executeUpdate();
                }

                try (PreparedStatement actualizar =
                        conexion.prepareStatement(sqlActualizarCuota)) {

                    actualizar.setInt(1, idCuota);

                    if (actualizar.executeUpdate() == 0) {
                        throw new SQLException(
                            "La cuota ya no se encuentra pendiente."
                        );
                    }
                }

                conexion.commit();

                JOptionPane.showMessageDialog(
                    this,
                    "El cobro fue aprobado correctamente.\n"
                        + "La cuota quedó registrada como PAGADA."
                );

                cargarSuscripciones("");

            } catch (SQLException error) {
                conexion.rollback();
                throw error;

            } finally {
                conexion.setAutoCommit(true);
            }

        } catch (SQLException error) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo registrar el cobro aprobado.\n"
                    + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void finalizarSuscripcion() {

        int fila = tablaSuscripciones.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar una suscripción."
            );
            return;
        }

        int idSuscripcion = Integer.parseInt(
            modeloTabla.getValueAt(fila, 0).toString()
        );

        String socio =
            modeloTabla.getValueAt(fila, 2).toString();

        String permanencia =
            modeloTabla.getValueAt(fila, 7).toString();

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Se intentará finalizar la suscripción de:\n"
                + socio + "\n\n"
                + "Permanencia mínima hasta: "
                + permanencia + "\n\n"
                + "¿Desea continuar?",
            "Finalizar suscripción",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        String sql =
            "UPDATE suscripcion " +
            "SET estado = 'FINALIZADA', " +
            "fecha_baja = CURDATE() " +
            "WHERE id_suscripcion = ? " +
            "AND estado = 'ACTIVA' " +
            "AND CURDATE() >= permanencia_hasta";

        try (
            Connection conexion = ConexionBD.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, idSuscripcion);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "La suscripción no puede finalizarse todavía.\n"
                        + "La permanencia mínima se extiende hasta "
                        + permanencia + ".",
                    "Permanencia vigente",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(
                this,
                "La suscripción fue finalizada correctamente."
            );

            cargarSuscripciones("");

        } catch (SQLException error) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo finalizar la suscripción.\n"
                    + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void registrarNotificacion() {
        int fila = tablaSuscripciones.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar una suscripción."
            );
            return;
        }

        String estadoNotificacion =
            modeloTabla.getValueAt(fila, 10).toString();

        if ("ENVIADA".equals(estadoNotificacion)) {
            JOptionPane.showMessageDialog(
                this,
                "El último rechazo ya fue notificado."
            );
            return;
        }

        if (!"PENDIENTE".equals(estadoNotificacion)) {
            JOptionPane.showMessageDialog(
                this,
                "La suscripción no posee un rechazo pendiente "
                + "de notificación."
            );
            return;
        }

        int idSuscripcion = Integer.parseInt(
            modeloTabla.getValueAt(fila, 0).toString()
        );

        String observacion = JOptionPane.showInputDialog(
            this,
            "Ingrese una observación opcional:",
            "Socio notificado por WhatsApp"
        );

        if (observacion == null) {
            return;
        }

        String sql =
        	    "INSERT INTO notificacion "
        	  + "(id_intento, id_usuario, canal, estado, observacion) "
        	  + "SELECT ic.id_intento, ?, "
        	  + "'WHATSAPP', 'ENVIADA', ? "
        	  + "FROM intento_cobro ic "
        	  + "WHERE ic.id_suscripcion = ? "
        	  + "AND ic.estado = 'RECHAZADO' "
        	  + "AND NOT EXISTS ("
        	  + "SELECT 1 FROM notificacion n "
        	  + "WHERE n.id_intento = ic.id_intento"
        	  + ") "
        	  + "ORDER BY ic.numero_intento DESC "
        	  + "LIMIT 1";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

        	sentencia.setInt(
        		    1,
        		    SesionUsuario.getIdUsuario()
        		);

        	sentencia.setString(2, observacion.trim());
        	sentencia.setInt(3, idSuscripcion);

            int filasAfectadas = sentencia.executeUpdate();

            if (filasAfectadas == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se encontró una notificación pendiente."
                );
                return;
            }

            JOptionPane.showMessageDialog(
                this,
                "La notificación por WhatsApp "
                + "fue registrada correctamente."
            );

            cargarSuscripciones("");

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo registrar la notificación.\n"
                + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarSuscripciones(String busqueda) {
        modeloTabla.setRowCount(0);

        String sql =
            "SELECT su.id_suscripcion, so.dni, "
          + "CONCAT(so.apellido, ', ', so.nombre) AS socio, "
          + "so.telefono, se.nombre AS sede, p.nombre AS plan, "
          + "su.dia_cobro, su.permanencia_hasta, su.estado, "
          + "COALESCE(("
          + "SELECT MAX(ic.numero_intento) "
          + "FROM intento_cobro ic "
          + "WHERE ic.id_suscripcion = su.id_suscripcion "
          + "AND ic.id_cuota = c.id_cuota"
          + "), 0) AS intentos, "
          + "COALESCE(("
          + "SELECT ic.estado FROM intento_cobro ic "
          + "WHERE ic.id_suscripcion = su.id_suscripcion "
          + "AND ic.id_cuota = c.id_cuota "
          + "ORDER BY ic.numero_intento DESC LIMIT 1"
          + "), 'SIN INTENTOS') AS ultimo_resultado, "
          + "COALESCE(("
          + "SELECT CASE "
          + "WHEN icu.estado <> 'RECHAZADO' THEN '-' "
          + "WHEN EXISTS ("
          + "SELECT 1 FROM notificacion n "
          + "WHERE n.id_intento = icu.id_intento"
          + ") THEN 'ENVIADA' "
          + "ELSE 'PENDIENTE' END "
          + "FROM intento_cobro icu "
          + "WHERE icu.id_suscripcion = su.id_suscripcion "
          + "AND icu.id_cuota = c.id_cuota "
          + "ORDER BY icu.numero_intento DESC LIMIT 1"
          + "), '-') AS notificacion "
          + "FROM suscripcion su "
          + "INNER JOIN membresia m "
          + "ON m.id_membresia = su.id_membresia "
          + "INNER JOIN socio so "
          + "ON so.id_socio = m.id_socio "
          + "INNER JOIN sede se "
          + "ON se.id_sede = so.id_sede_habitual "
          + "INNER JOIN plan p "
          + "ON p.id_plan = m.id_plan "
          + "LEFT JOIN cuota c "
          + "ON c.id_membresia = m.id_membresia "
          + "AND c.periodo = "
          + "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE) "
          + "WHERE su.estado = 'ACTIVA' "
          + "AND (so.dni LIKE ? OR so.nombre LIKE ? "
          + "OR so.apellido LIKE ?) "
          + "ORDER BY su.dia_cobro, so.apellido";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

            String filtro = "%" + busqueda + "%";

            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    modeloTabla.addRow(new Object[] {
                        resultado.getInt("id_suscripcion"),
                        resultado.getString("dni"),
                        resultado.getString("socio"),
                        resultado.getString("telefono"),
                        resultado.getString("sede"),
                        resultado.getString("plan"),
                        resultado.getInt("dia_cobro"),
                        resultado.getDate("permanencia_hasta"),
                        resultado.getInt("intentos"),
                        resultado.getString("ultimo_resultado"),
                        resultado.getString("notificacion"),
                        resultado.getString("estado")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar las suscripciones.\n"
                + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}
