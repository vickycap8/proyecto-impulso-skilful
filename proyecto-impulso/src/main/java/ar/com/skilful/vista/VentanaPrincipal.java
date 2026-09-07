package ar.com.skilful.vista;

import ar.com.skilful.sesion.SesionUsuario;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class VentanaPrincipal extends JFrame {

    private static final long serialVersionUID = 1L;

    private final Color colorPrincipal = new Color(25, 25, 25);
    private final Color colorSecundario = new Color(190, 25, 35);
    private final Color colorFondo = new Color(242, 242, 242);

    public VentanaPrincipal() {
        configurarVentana();
        crearContenido();
    }

    private void configurarVentana() {
        setTitle("Proyecto Impulso - Skilful");
        setSize(900, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 500));
    }

    private void crearContenido() {
        setLayout(new BorderLayout());

        JPanel encabezado = crearEncabezado();
        JPanel menu = crearMenu();
        JPanel centro = crearPanelCentral();

        add(encabezado, BorderLayout.NORTH);
        add(menu, BorderLayout.WEST);
        add(centro, BorderLayout.CENTER);
    }

    private JPanel crearEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(colorPrincipal);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 25, 18, 25));

        JLabel titulo = new JLabel("PROYECTO IMPULSO");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel sede = new JLabel(
        	    "Usuario: " + SesionUsuario.getNombreUsuario()
        	    + " | Rol: " + SesionUsuario.getRol()
        	    + " | Sede actual: " + SesionUsuario.getNombreSede()
        );
        sede.setForeground(Color.LIGHT_GRAY);
        sede.setFont(new Font("Arial", Font.PLAIN, 14));

        panel.add(titulo, BorderLayout.WEST);
        panel.add(sede, BorderLayout.EAST);

        return panel;
    }

    private JPanel crearMenu() {
        JPanel panel = new JPanel(new GridLayout(7, 1, 5, 10));
        panel.setBackground(colorPrincipal);
        panel.setPreferredSize(new Dimension(220, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JButton botonInicio = crearBoton("Inicio");
        JButton botonSocios = crearBoton("Socios");
        JButton botonCobros = crearBoton("Cobros");
        JButton botonBeneficios = crearBoton("Beneficios");
        JButton botonSuscripciones = crearBoton("Suscripciones");
        JButton botonAlertas = crearBoton("Alertas");
        JButton botonSalir = crearBoton("Cerrar sesión");

        botonSocios.addActionListener(e -> {
            VentanaSocios ventanaSocios = new VentanaSocios();
            ventanaSocios.setVisible(true);
        });
        botonCobros.addActionListener(e -> {
            VentanaCobros ventanaCobros = new VentanaCobros();
            ventanaCobros.setVisible(true);
        });
        botonBeneficios.addActionListener(e -> {
            VentanaBeneficios ventanaBeneficios =
                new VentanaBeneficios();

            ventanaBeneficios.setVisible(true);
        });
        botonSuscripciones.addActionListener(e -> {
            VentanaSuscripciones ventanaSuscripciones =
                new VentanaSuscripciones();

            ventanaSuscripciones.setVisible(true);
        });
        botonAlertas.addActionListener(e -> {
            VentanaAlertas ventanaAlertas =
                new VentanaAlertas();

            ventanaAlertas.setVisible(true);
        });

        botonSalir.addActionListener(e -> {
            int opcion = JOptionPane.showConfirmDialog(
                this,
                "¿Desea cerrar la sesión actual?",
                "Cerrar sesión",
                JOptionPane.YES_NO_OPTION
            );

            if (opcion == JOptionPane.YES_OPTION) {
                SesionUsuario.cerrarSesion();

                VentanaLogin login = new VentanaLogin();
                login.setVisible(true);

                dispose();
            }
        });
        
        botonSocios.setEnabled(
        	    SesionUsuario.puedeGestionarSocios()
        );

        botonCobros.setEnabled(
        	    SesionUsuario.puedeRegistrarCobros()
        );

        botonBeneficios.setEnabled(
        	    SesionUsuario.puedeGestionarBeneficios()
        );

        botonSuscripciones.setEnabled(
        	    SesionUsuario.puedeGestionarSuscripciones()
        );

        botonAlertas.setEnabled(
        	    SesionUsuario.puedeConsultarAlertas()
        );

        panel.add(botonInicio);
        panel.add(botonSocios);
        panel.add(botonCobros);
        panel.add(botonBeneficios);
        panel.add(botonSuscripciones);
        panel.add(botonAlertas);
        panel.add(botonSalir);

        return panel;
    }

    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(colorFondo);
        panel.setBorder(BorderFactory.createEmptyBorder(60, 50, 60, 50));

        JLabel bienvenida = new JLabel(
                "<html><div style='text-align:center;'>"
                + "<h1>Bienvenido a Proyecto Impulso</h1>"
                + "<p>Gestión de socios, beneficios, cobranzas y suscripciones de Skilful.</p>"
                + "<p>Seleccione una opción del menú para comenzar.</p>"
                + "</div></html>",
                SwingConstants.CENTER
        );

        bienvenida.setFont(new Font("Arial", Font.PLAIN, 16));
        bienvenida.setForeground(new Color(50, 50, 50));

        panel.add(bienvenida, BorderLayout.CENTER);

        return panel;
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 14));
        boton.setForeground(Color.WHITE);
        boton.setBackground(colorSecundario);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        return boton;
    }

    private void mostrarMensaje(String funcion) {
        JOptionPane.showMessageDialog(
                this,
                funcion + " se incorporará en la siguiente etapa.",
                "Proyecto Impulso",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        VentanaLogin.main(args);
    }
}