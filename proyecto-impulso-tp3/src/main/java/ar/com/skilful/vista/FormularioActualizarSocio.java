package ar.com.skilful.vista;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;

import ar.com.skilful.conexion.ConexionBD;

public class FormularioActualizarSocio extends JDialog {

    private static final long serialVersionUID = 1L;

    private final int idSocio;
    private final VentanaSocios ventanaSocios;

    private JTextField campoDni;
    private JTextField campoNombre;
    private JTextField campoApellido;
    private JTextField campoFechaNacimiento;
    private JTextField campoTelefono;
    private JTextField campoEmergencia;
    private JTextField campoCorreo;
    private JTextField campoDomicilio;
    private JComboBox<String> comboSede;

    public FormularioActualizarSocio(
            VentanaSocios ventanaSocios,
            int idSocio) {

        super(ventanaSocios, "Actualizar socio", true);

        this.ventanaSocios = ventanaSocios;
        this.idSocio = idSocio;

        configurarVentana();
        crearContenido();
        cargarDatos();
    }

    private void configurarVentana() {
        setSize(560, 570);
        setLocationRelativeTo(ventanaSocios);
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

        JLabel titulo = new JLabel("ACTUALIZAR SOCIO");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        encabezado.add(titulo);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(
            BorderFactory.createEmptyBorder(20, 25, 10, 25)
        );

        campoDni = new JTextField(25);
        campoNombre = new JTextField(25);
        campoApellido = new JTextField(25);
        campoFechaNacimiento = new JTextField(25);
        campoTelefono = new JTextField(25);
        campoEmergencia = new JTextField(25);
        campoCorreo = new JTextField(25);
        campoDomicilio = new JTextField(25);

        comboSede = new JComboBox<>(new String[] {
            "Alvear 4167",
            "9 de Julio 2742",
            "Irigoyen Freyre 2646",
            "Ituzaingó 2053"
        });

        agregarCampo(formulario, "DNI:", campoDni, 0);
        agregarCampo(formulario, "Nombre:", campoNombre, 1);
        agregarCampo(formulario, "Apellido:", campoApellido, 2);

        agregarCampo(
            formulario,
            "Fecha de nacimiento (AAAA-MM-DD):",
            campoFechaNacimiento,
            3
        );

        agregarCampo(formulario, "Teléfono:", campoTelefono, 4);

        agregarCampo(
            formulario,
            "Teléfono de emergencia:",
            campoEmergencia,
            5
        );

        agregarCampo(formulario, "Correo electrónico:", campoCorreo, 6);
        agregarCampo(formulario, "Domicilio:", campoDomicilio, 7);
        agregarCampo(formulario, "Sede habitual:", comboSede, 8);

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        panelBotones.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 15, 20)
        );

        JButton botonGuardar = crearBoton("Guardar cambios");
        JButton botonCancelar = crearBoton("Cancelar");

        botonGuardar.addActionListener(e -> actualizarSocio());
        botonCancelar.addActionListener(e -> dispose());

        panelBotones.add(botonGuardar);
        panelBotones.add(botonCancelar);

        add(encabezado, BorderLayout.NORTH);
        add(formulario, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void agregarCampo(
            JPanel panel,
            String etiqueta,
            Component componente,
            int fila) {

        GridBagConstraints restricciones = new GridBagConstraints();
        restricciones.insets = new Insets(6, 5, 6, 5);
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

    private void cargarDatos() {
        String sql =
            "SELECT id_sede_habitual, dni, nombre, apellido, "
          + "fecha_nacimiento, telefono, telefono_emergencia, "
          + "correo, domicilio "
          + "FROM socio WHERE id_socio = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idSocio);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (resultado.next()) {
                    campoDni.setText(resultado.getString("dni"));
                    campoNombre.setText(resultado.getString("nombre"));
                    campoApellido.setText(resultado.getString("apellido"));

                    campoFechaNacimiento.setText(
                        resultado.getDate("fecha_nacimiento").toString()
                    );

                    campoTelefono.setText(resultado.getString("telefono"));
                    campoEmergencia.setText(
                        resultado.getString("telefono_emergencia")
                    );
                    campoCorreo.setText(resultado.getString("correo"));
                    campoDomicilio.setText(resultado.getString("domicilio"));

                    int idSede = resultado.getInt("id_sede_habitual");
                    comboSede.setSelectedIndex(idSede - 1);
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los datos.\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void actualizarSocio() {
        if (!validarCampos()) {
            return;
        }

        String sql =
            "UPDATE socio SET id_sede_habitual = ?, dni = ?, "
          + "nombre = ?, apellido = ?, fecha_nacimiento = ?, "
          + "telefono = ?, telefono_emergencia = ?, correo = ?, "
          + "domicilio = ? "
          + "WHERE id_socio = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, comboSede.getSelectedIndex() + 1);
            sentencia.setString(2, campoDni.getText().trim());
            sentencia.setString(3, campoNombre.getText().trim());
            sentencia.setString(4, campoApellido.getText().trim());

            sentencia.setDate(
                5,
                java.sql.Date.valueOf(
                    campoFechaNacimiento.getText().trim()
                )
            );

            sentencia.setString(6, campoTelefono.getText().trim());
            sentencia.setString(7, campoEmergencia.getText().trim());
            sentencia.setString(8, campoCorreo.getText().trim());
            sentencia.setString(9, campoDomicilio.getText().trim());
            sentencia.setInt(10, idSocio);

            sentencia.executeUpdate();

            JOptionPane.showMessageDialog(
                this,
                "Los datos del socio fueron actualizados correctamente."
            );

            ventanaSocios.refrescarSocios();
            dispose();

        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(
                this,
                "La fecha debe escribirse con el formato AAAA-MM-DD.",
                "Fecha incorrecta",
                JOptionPane.WARNING_MESSAGE
            );

        } catch (SQLException e) {
            String mensaje = e.getMessage();

            if (mensaje != null
                    && mensaje.toLowerCase().contains("duplicate")) {
                mensaje = "Ya existe otro socio con ese DNI.";
            }

            JOptionPane.showMessageDialog(
                this,
                "No se pudo actualizar el socio.\n" + mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private boolean validarCampos() {
        if (campoDni.getText().trim().isEmpty()
                || campoNombre.getText().trim().isEmpty()
                || campoApellido.getText().trim().isEmpty()
                || campoFechaNacimiento.getText().trim().isEmpty()
                || campoTelefono.getText().trim().isEmpty()
                || campoCorreo.getText().trim().isEmpty()
                || campoDomicilio.getText().trim().isEmpty()) {

            JOptionPane.showMessageDialog(
                this,
                "Complete todos los campos obligatorios.",
                "Datos incompletos",
                JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        if (!campoDni.getText().trim().matches("\\d{7,9}")) {
            JOptionPane.showMessageDialog(
                this,
                "El DNI debe contener entre 7 y 9 números.",
                "DNI incorrecto",
                JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        if (!campoCorreo.getText().trim().matches(
                "^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {

            JOptionPane.showMessageDialog(
                this,
                "Ingrese un correo electrónico válido.",
                "Correo incorrecto",
                JOptionPane.WARNING_MESSAGE
            );

            return false;
        }

        return true;
    }
}