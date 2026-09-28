package ar.com.skilful.vista;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ar.com.skilful.sesion.SesionUsuario;
import ar.com.skilful.servicio.SuscripcionServicio;
import ar.com.skilful.conexion.ConexionBD;

public class VentanaSuscripciones extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final int COL_ID = 0;
    private static final int COL_SOCIO = 2;
    private static final int COL_PERIODO = 3;
    private static final int COL_ESTADO_CUOTA = 4;
    private static final int COL_PERMANENCIA = 9;
    private static final int COL_ID_CUOTA = 14;
    private static final int COL_ID_INTENTO = 15;

    private JTextField campoBusqueda;
    private JTable tablaSuscripciones;
    private DefaultTableModel modeloTabla;
    private final SuscripcionServicio suscripcionServicio;

    public VentanaSuscripciones() {
        suscripcionServicio = new SuscripcionServicio();
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
            "Seleccione la fila del período que desea gestionar"
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
                "ID", "DNI", "Socio", "Período", "Estado cuota",
                "Teléfono", "Sede", "Plan", "Día", "Permanencia",
                "Intentos", "Último resultado", "Notificación", "Estado",
                "ID cuota", "ID intento"
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

        // Identificadores del registro seleccionado: no dependen del orden visible.
        tablaSuscripciones.removeColumn(tablaSuscripciones.getColumnModel().getColumn(15));
        tablaSuscripciones.removeColumn(tablaSuscripciones.getColumnModel().getColumn(14));
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
            45, 90, 160, 90, 100, 110, 120, 70,
            45, 100, 65, 125, 105, 80
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

    private int filaSeleccionada() {
        int fila = tablaSuscripciones.getSelectedRow();
        return fila < 0 ? -1 : tablaSuscripciones.convertRowIndexToModel(fila);
    }

    private int entero(int fila, int columna) {
        return ((Number) modeloTabla.getValueAt(fila, columna)).intValue();
    }

    private void registrarIntentoRechazado() {
        registrarResultadoDesdePantalla(false);
    }

    private void registrarIntentoAprobado() {
        registrarResultadoDesdePantalla(true);
    }

    private void registrarResultadoDesdePantalla(boolean aprobado) {
        int fila = filaSeleccionada();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione la suscripción y el período.");
            return;
        }
        int idCuota = entero(fila, COL_ID_CUOTA);
        String estado = String.valueOf(modeloTabla.getValueAt(fila, COL_ESTADO_CUOTA));
        if (idCuota == 0 || !("PENDIENTE".equals(estado) || "VENCIDA".equals(estado))) {
            JOptionPane.showMessageDialog(this,
                "Seleccione una cuota PENDIENTE o VENCIDA.");
            return;
        }
        String resultado = aprobado ? "APROBADO" : "RECHAZADO";
        String periodo = String.valueOf(modeloTabla.getValueAt(fila, COL_PERIODO));
        if (JOptionPane.showConfirmDialog(this,
                "Socio: " + modeloTabla.getValueAt(fila, COL_SOCIO)
                + "\nPeríodo: " + periodo + "\nResultado: " + resultado
                + (aprobado ? "\nTambién se registrará el pago de esta cuota." : "")
                + "\n¿Confirma el resultado consultado en Mercado Pago?",
                "Registrar resultado", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String observacion = JOptionPane.showInputDialog(this,
            "Ingrese una observación o referencia:", "Cobro " + resultado + " por Mercado Pago");
        if (observacion == null) {
            return;
        }
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            int numero;
            try {
                numero = registrarResultado(conexion, entero(fila, COL_ID), idCuota,
                    SesionUsuario.getIdUsuario(), SesionUsuario.getIdSede(),
                    aprobado, observacion.trim());
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
            JOptionPane.showMessageDialog(this, "Intento " + numero + " registrado como "
                + resultado + " para el período " + periodo + "."
                + (aprobado ? "\nLa cuota quedó PAGADA." : ""));
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                "No se pudo registrar el resultado", JOptionPane.ERROR_MESSAGE);
        }
        cargarSuscripciones(campoBusqueda.getText().trim());
    }

    /** Opera sobre la cuota indicada; el llamador confirma o revierte la transacción. */
    static int registrarResultado(Connection conexion, int idSuscripcion, int idCuota,
            int idUsuario, int idSede, boolean aprobado, String observacion) throws SQLException {
        exigirTransaccion(conexion);
        if (observacion != null && observacion.length() > 250) {
            throw new SQLException("La observación admite hasta 250 caracteres.");
        }
        String sqlDatos = "SELECT c.importe_original, tp.id_tarifa, tp.monto "
            + "FROM suscripcion su JOIN membresia m ON m.id_membresia = su.id_membresia "
            + "JOIN cuota c ON c.id_membresia = m.id_membresia "
            + "JOIN tarifa_plan tp ON tp.id_plan = m.id_plan "
            + "AND tp.codigo_tarifa = 'SUSCRIPCION' AND tp.activo = TRUE "
            + "WHERE su.id_suscripcion = ? AND c.id_cuota = ? "
            + "AND su.estado = 'ACTIVA' AND c.estado IN ('PENDIENTE', 'VENCIDA') "
            + "AND CURDATE() >= tp.vigencia_desde "
            + "AND (tp.vigencia_hasta IS NULL OR CURDATE() <= tp.vigencia_hasta) "
            + "ORDER BY tp.vigencia_desde DESC, tp.id_tarifa DESC LIMIT 1 FOR UPDATE";
        int idTarifa;
        BigDecimal precioOriginal;
        BigDecimal importe;
        try (PreparedStatement ps = conexion.prepareStatement(sqlDatos)) {
            ps.setInt(1, idSuscripcion);
            ps.setInt(2, idCuota);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("La cuota seleccionada no está pendiente, "
                        + "no pertenece a esta suscripción activa o no hay tarifa vigente.");
                }
                idTarifa = rs.getInt("id_tarifa");
                precioOriginal = rs.getBigDecimal("importe_original");
                importe = rs.getBigDecimal("monto");
            }
        }
        // Se vuelve a consultar MySQL, sin confiar en un contador viejo de la tabla.
        int ultimoNumero = 0;
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT numero_intento, estado FROM intento_cobro "
                + "WHERE id_suscripcion = ? AND id_cuota = ? FOR UPDATE")) {
            ps.setInt(1, idSuscripcion);
            ps.setInt(2, idCuota);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if ("APROBADO".equals(rs.getString("estado"))) {
                        throw new SQLException("Esta cuota ya tiene un intento aprobado.");
                    }
                    ultimoNumero = Math.max(ultimoNumero, rs.getInt("numero_intento"));
                }
            }
        }
        if (ultimoNumero >= 5) {
            throw new SQLException("Esta cuota alcanzó el máximo de cinco intentos. "
                + "Corresponde registrar el pago por un medio alternativo.");
        }
        long idPago = 0;
        if (aprobado) {
            String sqlPago = "INSERT INTO pago (id_cuota, id_medio_pago, id_tarifa_aplicada, "
                + "id_usuario, id_sede, precio_original, descuento, recargo, saldo_aplicado, "
                + "importe_final, importe_abonado, saldo_generado, estado) "
                + "SELECT ?, mp.id_medio_pago, ?, ?, ?, ?, ?, 0, 0, ?, ?, 0, 'REGISTRADO' "
                + "FROM medio_pago mp WHERE mp.nombre = 'MERCADO_PAGO' AND mp.activo = TRUE";
            try (PreparedStatement ps = conexion.prepareStatement(sqlPago, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCuota);
                ps.setInt(2, idTarifa);
                ps.setInt(3, idUsuario);
                ps.setInt(4, idSede);
                ps.setBigDecimal(5, precioOriginal);
                ps.setBigDecimal(6, precioOriginal.subtract(importe).max(BigDecimal.ZERO));
                ps.setBigDecimal(7, importe);
                ps.setBigDecimal(8, importe);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("No se encontró el medio de pago MERCADO_PAGO activo.");
                }
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    if (!claves.next()) {
                        throw new SQLException("No se pudo obtener el identificador del pago.");
                    }
                    idPago = claves.getLong(1);
                }
            }
        }
        int numero = ultimoNumero + 1;
        try (PreparedStatement ps = conexion.prepareStatement(
                "INSERT INTO intento_cobro (id_suscripcion, id_cuota, id_usuario, id_sede, "
                + "id_pago, numero_intento, importe, estado, observacion) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setInt(1, idSuscripcion);
            ps.setInt(2, idCuota);
            ps.setInt(3, idUsuario);
            ps.setInt(4, idSede);
            if (aprobado) {
                ps.setLong(5, idPago);
            } else {
                ps.setNull(5, Types.BIGINT);
            }
            ps.setInt(6, numero);
            ps.setBigDecimal(7, importe);
            ps.setString(8, aprobado ? "APROBADO" : "RECHAZADO");
            ps.setString(9, observacion);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("No se pudo registrar el intento.");
            }
        }
        if (aprobado) {
            try (PreparedStatement ps = conexion.prepareStatement(
                    "UPDATE cuota SET estado = 'PAGADA' "
                    + "WHERE id_cuota = ? AND estado IN ('PENDIENTE', 'VENCIDA')")) {
                ps.setInt(1, idCuota);
                if (ps.executeUpdate() != 1) {
                    throw new SQLException("La cuota ya no está pendiente. Se revierte el cobro.");
                }
            }
        }
        return numero;
    }

    private static void exigirTransaccion(Connection conexion) throws SQLException {
        if (conexion.getAutoCommit()) {
            throw new SQLException("La operación requiere una transacción.");
        }
    }

    private void finalizarSuscripcion() {

        int fila = filaSeleccionada();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar una suscripción."
            );
            return;
        }

        int idSuscripcion = Integer.parseInt(
            modeloTabla.getValueAt(fila, COL_ID).toString()
        );

        String socio =
            modeloTabla.getValueAt(fila, COL_SOCIO).toString();

        String permanencia =
            modeloTabla.getValueAt(fila, COL_PERMANENCIA).toString();

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

        try {
            if (!suscripcionServicio.finalizar(idSuscripcion)) {
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
        int fila = filaSeleccionada();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione la suscripción y el período.");
            return;
        }
        long idIntento = ((Number) modeloTabla.getValueAt(fila, COL_ID_INTENTO)).longValue();
        if (idIntento == 0) {
            JOptionPane.showMessageDialog(this,
                "El último intento de este período no tiene una notificación pendiente.");
            return;
        }
        String observacion = JOptionPane.showInputDialog(this,
            "Período: " + modeloTabla.getValueAt(fila, COL_PERIODO)
            + "\nRegistre la notificación después de contactar al socio por WhatsApp."
            + "\nObservación:", "Socio notificado por WhatsApp");
        if (observacion == null) {
            return;
        }
        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);
            try {
                registrarNotificacion(conexion, idIntento, entero(fila, COL_ID),
                    entero(fila, COL_ID_CUOTA), SesionUsuario.getIdUsuario(), observacion.trim());
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
            JOptionPane.showMessageDialog(this, "La notificación quedó registrada.");
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                "No se pudo registrar la notificación", JOptionPane.ERROR_MESSAGE);
        }
        cargarSuscripciones(campoBusqueda.getText().trim());
    }

    /** Registra el contacto manual referido al intento exacto que mostraba la fila. */
    static void registrarNotificacion(Connection conexion, long idIntento,
            int idSuscripcion, int idCuota, int idUsuario, String observacion) throws SQLException {
        exigirTransaccion(conexion);
        if (observacion != null && observacion.length() > 250) {
            throw new SQLException("La observación admite hasta 250 caracteres.");
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT id_intento FROM intento_cobro WHERE id_intento = ? "
                + "AND id_suscripcion = ? AND id_cuota = ? AND estado = 'RECHAZADO' FOR UPDATE")) {
            ps.setLong(1, idIntento);
            ps.setInt(2, idSuscripcion);
            ps.setInt(3, idCuota);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("El intento no corresponde al rechazo seleccionado.");
                }
            }
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "SELECT estado FROM notificacion WHERE id_intento = ? FOR UPDATE")) {
            ps.setLong(1, idIntento);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (!"PENDIENTE".equals(rs.getString("estado"))) {
                        throw new SQLException("Este intento ya fue notificado.");
                    }
                }
            }
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "UPDATE notificacion SET estado = 'ENVIADA', id_usuario = ?, "
                + "fecha_notificacion = CURRENT_TIMESTAMP, canal = 'WHATSAPP', observacion = ? "
                + "WHERE id_intento = ? AND estado = 'PENDIENTE'")) {
            ps.setInt(1, idUsuario);
            ps.setString(2, observacion);
            ps.setLong(3, idIntento);
            if (ps.executeUpdate() > 0) {
                return;
            }
        }
        try (PreparedStatement ps = conexion.prepareStatement(
                "INSERT INTO notificacion (id_intento, id_usuario, canal, estado, observacion) "
                + "VALUES (?, ?, 'WHATSAPP', 'ENVIADA', ?)")) {
            ps.setLong(1, idIntento);
            ps.setInt(2, idUsuario);
            ps.setString(3, observacion);
            ps.executeUpdate();
        }
    }

    private void cargarSuscripciones(String busqueda) {
        modeloTabla.setRowCount(0);
        String sql = "SELECT su.id_suscripcion, so.dni, "
            + "CONCAT(so.apellido, ', ', so.nombre) AS socio, so.telefono, "
            + "se.nombre AS sede, p.nombre AS plan, su.dia_cobro, "
            + "su.permanencia_hasta, su.estado, c.id_cuota, "
            + "DATE_FORMAT(c.periodo, '%Y-%m') AS periodo, c.estado AS estado_cuota, "
            + "COALESCE(ic.numero_intento, 0) AS intentos, "
            + "COALESCE(ic.estado, 'SIN INTENTOS') AS ultimo_resultado, "
            + "CASE WHEN ic.estado = 'RECHAZADO' THEN "
            + "CASE WHEN EXISTS (SELECT 1 FROM notificacion n "
            + "WHERE n.id_intento = ic.id_intento AND n.estado IN ('ENVIADA', 'CONFIRMADA')) "
            + "THEN 'ENVIADA' ELSE 'PENDIENTE' END ELSE '-' END AS notificacion, "
            + "ic.id_intento FROM suscripcion su "
            + "JOIN membresia m ON m.id_membresia = su.id_membresia "
            + "JOIN socio so ON so.id_socio = m.id_socio "
            + "JOIN sede se ON se.id_sede = so.id_sede_habitual "
            + "JOIN plan p ON p.id_plan = m.id_plan "
            + "LEFT JOIN cuota c ON c.id_membresia = m.id_membresia "
            + "LEFT JOIN intento_cobro ic ON ic.id_suscripcion = su.id_suscripcion "
            + "AND ic.id_cuota = c.id_cuota AND ic.numero_intento = ("
            + "SELECT MAX(ultimo.numero_intento) FROM intento_cobro ultimo "
            + "WHERE ultimo.id_suscripcion = su.id_suscripcion AND ultimo.id_cuota = c.id_cuota) "
            + "WHERE su.estado = 'ACTIVA' "
            + "AND (so.dni LIKE ? OR so.nombre LIKE ? OR so.apellido LIKE ?) "
            + "ORDER BY so.apellido, so.nombre, su.id_suscripcion, c.periodo DESC";
        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            String filtro = "%" + busqueda + "%";
            ps.setString(1, filtro);
            ps.setString(2, filtro);
            ps.setString(3, filtro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String notificacion = rs.getString("notificacion");
                    int idCuota = rs.getInt("id_cuota");
                    modeloTabla.addRow(new Object[] {
                        rs.getInt("id_suscripcion"), rs.getString("dni"), rs.getString("socio"),
                        idCuota == 0 ? "SIN CUOTA" : rs.getString("periodo"),
                        idCuota == 0 ? "-" : rs.getString("estado_cuota"),
                        rs.getString("telefono"), rs.getString("sede"), rs.getString("plan"),
                        rs.getInt("dia_cobro"), rs.getDate("permanencia_hasta"),
                        rs.getInt("intentos"), rs.getString("ultimo_resultado"),
                        notificacion, rs.getString("estado"), idCuota,
                        "PENDIENTE".equals(notificacion) ? rs.getLong("id_intento") : 0L
                    });
                }
            }
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this,
                "No se pudieron cargar las suscripciones.\n" + error.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
