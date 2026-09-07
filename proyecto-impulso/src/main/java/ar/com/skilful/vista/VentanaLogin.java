package ar.com.skilful.vista;

import java.awt.*;
import java.security.MessageDigest;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;

import ar.com.skilful.conexion.ConexionBD;
import ar.com.skilful.sesion.SesionUsuario;
import ar.com.skilful.servicio.GeneradorCuotas;

public class VentanaLogin extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField campoUsuario;
    private JPasswordField campoContrasena;
    private JButton botonIngresar;

    public VentanaLogin() {
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

        String sql =
            "SELECT u.id_usuario, u.nombre_completo, "
          + "u.nombre_usuario, u.clave_hash, u.clave_salt, "
          + "u.clave_iteraciones, r.nombre AS rol "
          + "FROM usuario u "
          + "INNER JOIN rol r ON r.id_rol = u.id_rol "
          + "WHERE u.nombre_usuario = ? "
          + "AND u.activo = TRUE "
          + "AND r.activo = TRUE";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

            sentencia.setString(1, nombreUsuario);

            try (ResultSet resultado = sentencia.executeQuery()) {
                if (!resultado.next()) {
                    mostrarAccesoIncorrecto();
                    return;
                }

                boolean claveValida = verificarContrasena(
                    contrasena,
                    resultado.getString("clave_salt"),
                    resultado.getString("clave_hash"),
                    resultado.getInt("clave_iteraciones")
                );

                if (!claveValida) {
                    mostrarAccesoIncorrecto();
                    return;
                }

                int idUsuario = resultado.getInt("id_usuario");
                String nombreCompleto =
                    resultado.getString("nombre_completo");
                String rol = resultado.getString("rol");

                SedeItem sede = seleccionarSede(
                    conexion,
                    idUsuario
                );

                if (sede == null) {
                    return;
                }

                SesionUsuario.iniciarSesion(
                	    idUsuario,
                	    nombreCompleto,
                	    nombreUsuario,
                	    rol,
                	    sede.getId(),
                	    sede.getNombre()
                	);

                	try {
                	    GeneradorCuotas.generarCuotasMesActual();

                	} catch (SQLException errorCuotas) {
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
            }

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

    private boolean verificarContrasena(
            char[] contrasena,
            String saltBase64,
            String hashBase64,
            int iteraciones) throws Exception {

        byte[] salt = Base64.getDecoder().decode(saltBase64);
        byte[] hashEsperado =
            Base64.getDecoder().decode(hashBase64);

        PBEKeySpec especificacion = new PBEKeySpec(
            contrasena,
            salt,
            iteraciones,
            hashEsperado.length * 8
        );

        SecretKeyFactory fabrica = SecretKeyFactory.getInstance(
            "PBKDF2WithHmacSHA256"
        );

        byte[] hashCalculado =
            fabrica.generateSecret(especificacion).getEncoded();

        especificacion.clearPassword();

        return MessageDigest.isEqual(
            hashEsperado,
            hashCalculado
        );
    }

    private SedeItem seleccionarSede(
            Connection conexion,
            int idUsuario) throws SQLException {

        List<SedeItem> sedes = new ArrayList<>();

        String sql =
            "SELECT s.id_sede, s.nombre "
          + "FROM usuario_sede us "
          + "INNER JOIN sede s ON s.id_sede = us.id_sede "
          + "WHERE us.id_usuario = ? "
          + "AND us.activo = TRUE "
          + "AND s.activo = TRUE "
          + "ORDER BY s.nombre";

        try (PreparedStatement sentencia =
                conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idUsuario);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    sedes.add(
                        new SedeItem(
                            resultado.getInt("id_sede"),
                            resultado.getString("nombre")
                        )
                    );
                }
            }
        }

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

        return (SedeItem) seleccion;
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

    private static class SedeItem {

        private final int id;
        private final String nombre;

        public SedeItem(int id, String nombre) {
            this.id = id;
            this.nombre = nombre;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        @Override
        public String toString() {
            return nombre;
        }
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
