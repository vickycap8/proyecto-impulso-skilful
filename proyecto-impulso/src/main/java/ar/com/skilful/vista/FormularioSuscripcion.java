package ar.com.skilful.vista;

import ar.com.skilful.conexion.ConexionBD;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class FormularioSuscripcion extends JDialog {

    private static final long serialVersionUID = 1L;

    private int idSocio = -1;
    private int idMembresia = -1;

    private JTextField campoSocio;
    private JTextField campoPlan;
    private JTextField campoTarifa;
    private JTextField campoFechaInicio;
    private JTextField campoPermanencia;
    private JSpinner campoDiaCobro;

    private final Runnable accionAlGuardar;

    public FormularioSuscripcion(
            Window ventanaPadre,
            Runnable accionAlGuardar
    ) {
        super(
            ventanaPadre,
            "Nueva suscripción - Proyecto Impulso",
            ModalityType.APPLICATION_MODAL
        );

        this.accionAlGuardar = accionAlGuardar;

        configurarVentana();
        crearContenido();
    }

    private void configurarVentana() {
        setSize(680, 460);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void crearContenido() {
        setLayout(new BorderLayout());

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(25, 25, 25));
        encabezado.setBorder(
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        );

        JLabel titulo = new JLabel("NUEVA SUSCRIPCIÓN");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        encabezado.add(titulo, BorderLayout.WEST);
        add(encabezado, BorderLayout.NORTH);

        campoSocio = crearCampoLectura();
        campoPlan = crearCampoLectura();
        campoTarifa = crearCampoLectura();
        campoFechaInicio = crearCampoLectura();
        campoPermanencia = crearCampoLectura();

        LocalDate fechaInicio = LocalDate.now();

        campoFechaInicio.setText(fechaInicio.toString());
        campoPermanencia.setText(
            fechaInicio.plusMonths(6).toString()
        );

        campoDiaCobro = new JSpinner(
            new SpinnerNumberModel(
                fechaInicio.getDayOfMonth(),
                1,
                31,
                1
            )
        );

        JButton botonBuscarSocio =
            crearBoton("Buscar socio");

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(
            BorderFactory.createEmptyBorder(25, 25, 20, 25)
        );

        GridBagConstraints restricciones =
            new GridBagConstraints();

        restricciones.insets = new Insets(7, 7, 7, 7);
        restricciones.fill = GridBagConstraints.HORIZONTAL;

        agregarFila(
            formulario,
            restricciones,
            0,
            "Socio:",
            campoSocio,
            botonBuscarSocio
        );

        agregarFila(
            formulario,
            restricciones,
            1,
            "Plan activo:",
            campoPlan,
            null
        );

        agregarFila(
            formulario,
            restricciones,
            2,
            "Tarifa de suscripción:",
            campoTarifa,
            null
        );

        agregarFila(
            formulario,
            restricciones,
            3,
            "Fecha de inicio:",
            campoFechaInicio,
            null
        );

        agregarFila(
            formulario,
            restricciones,
            4,
            "Permanencia hasta:",
            campoPermanencia,
            null
        );

        agregarFila(
            formulario,
            restricciones,
            5,
            "Día de cobro:",
            campoDiaCobro,
            null
        );

        add(formulario, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        panelBotones.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 15, 15)
        );

        JButton botonGuardar =
            crearBoton("Crear suscripción");

        JButton botonCancelar =
            crearBoton("Cancelar");

        panelBotones.add(botonGuardar);
        panelBotones.add(botonCancelar);

        add(panelBotones, BorderLayout.SOUTH);

        botonBuscarSocio.addActionListener(
            e -> seleccionarSocio()
        );

        botonGuardar.addActionListener(
            e -> registrarSuscripcion()
        );

        botonCancelar.addActionListener(
            e -> dispose()
        );
    }

    private JTextField crearCampoLectura() {
        JTextField campo = new JTextField();
        campo.setEditable(false);
        campo.setBackground(Color.WHITE);
        return campo;
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);

        boton.setUI(
            new javax.swing.plaf.basic.BasicButtonUI()
        );

        boton.setBackground(new Color(190, 25, 35));
        boton.setForeground(Color.WHITE);
        boton.setFont(
            new Font("Arial", Font.BOLD, 12)
        );
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setPreferredSize(
            new Dimension(145, 30)
        );

        return boton;
    }

    private void agregarFila(
            JPanel panel,
            GridBagConstraints restricciones,
            int fila,
            String etiqueta,
            Component campo,
            JButton boton
    ) {
        restricciones.gridy = fila;

        restricciones.gridx = 0;
        restricciones.weightx = 0;
        panel.add(new JLabel(etiqueta), restricciones);

        restricciones.gridx = 1;
        restricciones.weightx = 1;
        panel.add(campo, restricciones);

        restricciones.gridx = 2;
        restricciones.weightx = 0;

        if (boton != null) {
            panel.add(boton, restricciones);
        } else {
            panel.add(
                Box.createHorizontalStrut(145),
                restricciones
            );
        }
    }

    private void seleccionarSocio() {
        SelectorSocio selector = new SelectorSocio(this);

        if (!selector.mostrarSelector()) {
            return;
        }

        idSocio = selector.getIdSocioSeleccionado();

        campoSocio.setText(
            selector.getDescripcionSocio()
        );

        cargarMembresiaActiva();
    }

    private void cargarMembresiaActiva() {
        idMembresia = -1;
        campoPlan.setText("");
        campoTarifa.setText("");

        String sql =
            "SELECT m.id_membresia, p.nombre AS plan, " +
            "tp.monto AS tarifa_suscripcion, " +
            "EXISTS (" +
            "SELECT 1 FROM suscripcion su " +
            "WHERE su.id_membresia = m.id_membresia " +
            "AND su.estado = 'ACTIVA'" +
            ") AS ya_suscripto " +
            "FROM membresia m " +
            "INNER JOIN plan p " +
            "ON p.id_plan = m.id_plan " +
            "INNER JOIN tarifa_plan tp " +
            "ON tp.id_plan = p.id_plan " +
            "AND tp.codigo_tarifa = 'SUSCRIPCION' " +
            "AND tp.activo = TRUE " +
            "WHERE m.id_socio = ? " +
            "AND m.estado = 'ACTIVA' " +
            "AND CURDATE() >= tp.vigencia_desde " +
            "AND (tp.vigencia_hasta IS NULL " +
            "OR CURDATE() <= tp.vigencia_hasta) " +
            "ORDER BY m.fecha_inicio DESC, " +
            "tp.vigencia_desde DESC " +
            "LIMIT 1";

        try (
            Connection conexion = ConexionBD.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, idSocio);

            try (ResultSet resultado =
                    sentencia.executeQuery()) {

                if (!resultado.next()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "El socio no posee una membresía activa "
                            + "con tarifa de suscripción.",
                        "Membresía no disponible",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if (resultado.getBoolean("ya_suscripto")) {
                    JOptionPane.showMessageDialog(
                        this,
                        "El socio ya posee una suscripción activa.",
                        "Suscripción existente",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                idMembresia =
                    resultado.getInt("id_membresia");

                campoPlan.setText(
                    resultado.getString("plan")
                );

                campoTarifa.setText(
                    String.format(
                        "$ %.2f",
                        resultado.getDouble(
                            "tarifa_suscripcion"
                        )
                    )
                );
            }

        } catch (SQLException error) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo consultar la membresía.\n"
                    + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void registrarSuscripcion() {
        if (idSocio == -1 || idMembresia == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar un socio "
                    + "con una membresía activa.",
                "Datos incompletos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int diaCobro =
            ((Number) campoDiaCobro.getValue()).intValue();

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Se creará la suscripción para:\n"
                + campoSocio.getText() + "\n\n"
                + "Plan: " + campoPlan.getText() + "\n"
                + "Tarifa: " + campoTarifa.getText() + "\n"
                + "Día de cobro: " + diaCobro + "\n"
                + "Permanencia hasta: "
                + campoPermanencia.getText() + "\n\n"
                + "¿Desea continuar?",
            "Confirmar suscripción",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        String sqlSuscripcion =
            "INSERT INTO suscripcion " +
            "(id_membresia, fecha_inicio, " +
            "permanencia_hasta, dia_cobro, estado) " +
            "SELECT ?, CURDATE(), " +
            "DATE_ADD(CURDATE(), INTERVAL 6 MONTH), " +
            "?, 'ACTIVA' " +
            "WHERE NOT EXISTS (" +
            "SELECT 1 FROM suscripcion " +
            "WHERE id_membresia = ? " +
            "AND estado = 'ACTIVA'" +
            ")";

        String sqlCuota =
            "INSERT INTO cuota " +
            "(id_membresia, id_tarifa_base, periodo, " +
            "fecha_emision, fecha_vencimiento, " +
            "importe_original, estado) " +
            "SELECT m.id_membresia, tp.id_tarifa, " +
            "CAST(DATE_FORMAT(CURDATE(), '%Y-%m-01') AS DATE), " +
            "CURDATE(), " +
            "DATE_ADD(CURDATE(), INTERVAL 4 DAY), " +
            "tp.monto, 'PENDIENTE' " +
            "FROM membresia m " +
            "INNER JOIN tarifa_plan tp " +
            "ON tp.id_plan = m.id_plan " +
            "AND tp.codigo_tarifa = 'GENERAL' " +
            "AND tp.activo = TRUE " +
            "WHERE m.id_membresia = ? " +
            "AND CURDATE() >= tp.vigencia_desde " +
            "AND (tp.vigencia_hasta IS NULL " +
            "OR CURDATE() <= tp.vigencia_hasta) " +
            "ON DUPLICATE KEY UPDATE " +
            "id_membresia = VALUES(id_membresia)";

        try (Connection conexion = ConexionBD.conectar()) {
            conexion.setAutoCommit(false);

            try (
                PreparedStatement suscripcion =
                    conexion.prepareStatement(
                        sqlSuscripcion
                    );
                PreparedStatement cuota =
                    conexion.prepareStatement(sqlCuota)
            ) {
                suscripcion.setInt(1, idMembresia);
                suscripcion.setInt(2, diaCobro);
                suscripcion.setInt(3, idMembresia);

                if (suscripcion.executeUpdate() == 0) {
                    throw new SQLException(
                        "El socio ya posee una suscripción activa."
                    );
                }

                cuota.setInt(1, idMembresia);

                cuota.executeUpdate();

                conexion.commit();

            } catch (SQLException error) {
                conexion.rollback();
                throw error;

            } finally {
                conexion.setAutoCommit(true);
            }

            JOptionPane.showMessageDialog(
                this,
                "La suscripción fue creada correctamente.\n"
                    + "La permanencia mínima es de seis meses."
            );

            if (accionAlGuardar != null) {
                accionAlGuardar.run();
            }

            dispose();

        } catch (SQLException error) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo crear la suscripción.\n"
                    + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}