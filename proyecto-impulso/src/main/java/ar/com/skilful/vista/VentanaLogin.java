package ar.com.skilful.vista;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

import ar.com.skilful.modelo.Sede;
import ar.com.skilful.modelo.Usuario;
import ar.com.skilful.sesion.SesionUsuario;
import ar.com.skilful.servicio.AutenticacionServicio;
import ar.com.skilful.servicio.GeneradorCuotas;

public class VentanaLogin extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField campoUsuario;
    private JPasswordField campoContrasena;
    private JButton botonIngresar;
    private final AutenticacionServicio autenticacionServicio;

    public VentanaLogin() {
        autenticacionServicio = new AutenticacionServicio();
        configurarVentana();
        crearContenido();
    }

    private void configurarVentana() {
        setTitle("Iniciar sesión - Proyecto Impulso");
        setSize(480, 380);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
    }

    private void crearContenido() {
        setLayout(new BorderLayout());

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(25, 25, 25));
        encabezado.setBorder(
            BorderFactory.createEmptyBorder(25, 25, 25, 25)
        );

        JLabel titulo = new JLabel(
            "<html><b>PROYECTO IMPULSO</b><br>"
            + "<span style='font-size:11px'>Sistema de gestión Skilful</span>"
            + "</html>"
        );

        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 23));
        encabezado.add(titulo);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(
            BorderFactory.createEmptyBorder(25, 45, 15, 45)
        );

        campoUsuario = new JTextField(20);
        campoContrasena = new JPasswordField(20);

        agregarCampo(formulario, "Usuario:", campoUsuario, 0);
        agregarCampo(formulario, "Contraseña:", campoContrasena, 1);

        botonIngresar = crearBoton("Ingresar");
        JButton botonSalir = crearBoton("Salir");

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.CENTER, 10, 10)
        );

        panelBotones.add(botonIngresar);
        panelBotones.add(botonSalir);

        botonIngresar.addActionListener(e -> iniciarSesion());
        botonSalir.addActionListener(e -> System.exit(0));
        campoContrasena.addActionListener(e -> iniciarSesion());

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
        restricciones.insets = new Insets(10, 5, 10, 5);
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
        boton.setFont(new Font("Arial", Font.BOLD, 13));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);
        boton.setPreferredSize(new Dimension(120, 35));

        return boton;
    }

    private void iniciarSesion() {
        String nombreUsuario = campoUsuario.getText().trim();
        char[] contrasena = campoContrasena.getPassword();

        if (nombreUsuario.isEmpty() || contrasena.length == 0) {
            JOptionPane.showMessageDialog(
                this,
                "Ingrese el usuario y la contraseña.",
                "Datos incompletos",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        botonIngresar.setEnabled(false);

        try {
            Usuario usuario = autenticacionServicio.autenticar(
                nombreUsuario,
                contrasena
            );

            if (usuario == null) {
                mostrarAccesoIncorrecto();
                return;
            }

            Sede sede = seleccionarSede(usuario.getId());

            if (sede == null) {
                return;
            }

            SesionUsuario.iniciarSesion(
                usuario.getId(),
                usuario.getNombreCompleto(),
                usuario.getNombreUsuario(),
                usuario.getRol(),
                sede.getId(),
                sede.getNombre()
            );

            try {
                GeneradorCuotas.generarCuotasMesActual();

            } catch (Exception errorCuotas) {
                JOptionPane.showMessageDialog(
                    this,
                    "La sesión se inició, pero no fue posible "
                        + "actualizar las cuotas del mes.\n"
                        + errorCuotas.getMessage(),
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE
                );
            }

            VentanaPrincipal ventana = new VentanaPrincipal();

            ventana.setVisible(true);
            dispose();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo iniciar sesión.\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );

        } finally {
            Arrays.fill(contrasena, '\0');
            botonIngresar.setEnabled(true);
        }
    }

    private Sede seleccionarSede(int idUsuario) throws Exception {
        List<Sede> sedes =
            autenticacionServicio.listarSedesAutorizadas(idUsuario);

        if (sedes.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "El usuario no posee ninguna sede habilitada."
            );
            return null;
        }

        if (sedes.size() == 1) {
            return sedes.get(0);
        }

        Object seleccion = JOptionPane.showInputDialog(
            this,
            "Seleccione la sede donde trabajará:",
            "Sede actual",
            JOptionPane.QUESTION_MESSAGE,
            null,
            sedes.toArray(),
            sedes.get(0)
        );

        return (Sede) seleccion;
    }

    private void mostrarAccesoIncorrecto() {
        JOptionPane.showMessageDialog(
            this,
            "Usuario o contraseña incorrectos.",
            "Acceso denegado",
            JOptionPane.WARNING_MESSAGE
        );

        campoContrasena.setText("");
        campoContrasena.requestFocus();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception e) {
            System.out.println(
                "No se pudo aplicar el estilo del sistema."
            );
        }

        SwingUtilities.invokeLater(() -> {
            VentanaLogin ventana = new VentanaLogin();
            ventana.setVisible(true);
        });
    }
}
