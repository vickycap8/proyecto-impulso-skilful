package ar.com.skilful.vista;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.DateTimeException;

import javax.swing.*;

import ar.com.skilful.dao.SocioDAO;
import ar.com.skilful.modelo.Socio;

public class FormularioActualizarSocio extends JDialog {

    private static final long serialVersionUID = 1L;

    private final int idSocio;
    private final VentanaSocios ventanaSocios;
    private final SocioDAO socioDAO = new SocioDAO();

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
        try {
            Socio socio = socioDAO.buscarPorId(idSocio);
            if (socio == null) {
                JOptionPane.showMessageDialog(this, "No se encontró el socio.");
                dispose();
                return;
            }
            campoDni.setText(socio.getDni());
            campoNombre.setText(socio.getNombre());
            campoApellido.setText(socio.getApellido());
            campoFechaNacimiento.setText(socio.getFechaNacimiento() == null
                    ? "" : socio.getFechaNacimiento().toString());
            campoTelefono.setText(socio.getTelefono());
            campoEmergencia.setText(socio.getTelefonoEmergencia());
            campoCorreo.setText(socio.getCorreo());
            campoDomicilio.setText(socio.getDomicilio());
            comboSede.setSelectedIndex(socio.getIdSedeHabitual() - 1);
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

        try {
            Socio socio = new Socio();
            socio.setId(idSocio);
            socio.setIdSedeHabitual(comboSede.getSelectedIndex() + 1);
            socio.setDni(campoDni.getText().trim());
            socio.setNombre(campoNombre.getText().trim());
            socio.setApellido(campoApellido.getText().trim());
            socio.setFechaNacimiento(LocalDate.parse(campoFechaNacimiento.getText().trim()));
            socio.setTelefono(campoTelefono.getText().trim());
            socio.setTelefonoEmergencia(campoEmergencia.getText().trim());
            socio.setCorreo(campoCorreo.getText().trim());
            socio.setDomicilio(campoDomicilio.getText().trim());
            if (!socioDAO.actualizar(socio)) {
                JOptionPane.showMessageDialog(this, "No se encontró el socio para actualizar.");
                return;
            }

            JOptionPane.showMessageDialog(
                this,
                "Los datos del socio fueron actualizados correctamente."
            );

            ventanaSocios.refrescarSocios();
            dispose();

        } catch (DateTimeException e) {
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
