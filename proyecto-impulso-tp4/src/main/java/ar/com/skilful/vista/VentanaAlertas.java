package ar.com.skilful.vista;

import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ar.com.skilful.dao.AlertaDAO;

public class VentanaAlertas extends JFrame {

    private static final long serialVersionUID = 1L;

    private final AlertaDAO alertaDAO = new AlertaDAO();
    private DefaultTableModel modeloCertificados;
    private DefaultTableModel modeloNotificaciones;
    private DefaultTableModel modeloResumen;

    public VentanaAlertas() {
        configurarVentana();
        crearContenido();
        actualizarAlertas();
    }

    private void configurarVentana() {
        setTitle("Alertas e informes - Proyecto Impulso");
        setSize(1000, 600);
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

        JLabel titulo = new JLabel("ALERTAS E INFORMES");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        JButton botonActualizar = crearBoton("Actualizar");
        botonActualizar.addActionListener(
            e -> actualizarAlertas()
        );

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(botonActualizar, BorderLayout.EAST);

        JTabbedPane pestanas = new JTabbedPane();

        modeloCertificados = crearModelo(new Object[] {
            "ID socio", "DNI", "Socio",
            "Vencimiento", "Días restantes", "Sede"
        });

        modeloNotificaciones = crearModelo(new Object[] {
            "ID intento", "DNI", "Socio", "Teléfono",
            "Intento", "Fecha", "Importe", "Sede"
        });

        modeloResumen = crearModelo(new Object[] {
            "Fecha", "ID sede", "Sede", "Total",
            "Aprobados", "Rechazados", "Pendientes",
            "Importe aprobado"
        });

        JTable tablaCertificados =
            crearTabla(modeloCertificados);

        JTable tablaNotificaciones =
            crearTabla(modeloNotificaciones);

        JTable tablaResumen =
            crearTabla(modeloResumen);

        pestanas.addTab(
            "Certificados próximos a vencer",
            crearPanelTabla(
                tablaCertificados,
                "Certificados estudiantiles que vencen "
                + "durante los próximos 30 días."
            )
        );

        pestanas.addTab(
            "Notificaciones pendientes",
            crearPanelTabla(
                tablaNotificaciones,
                "Cobros rechazados que todavía no fueron "
                + "notificados al socio."
            )
        );

        pestanas.addTab(
            "Resumen diario",
            crearPanelTabla(
                tablaResumen,
                "Cantidad y resultado de los intentos "
                + "registrados por fecha y sede."
            )
        );

        JButton botonCerrar = crearBoton("Cerrar");
        botonCerrar.addActionListener(e -> dispose());

        JPanel panelInferior = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        panelInferior.setBorder(
            BorderFactory.createEmptyBorder(5, 10, 15, 15)
        );

        panelInferior.add(botonCerrar);

        add(encabezado, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private DefaultTableModel crearModelo(Object[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
    }

    private JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setRowHeight(25);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        tabla.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 12)
        );

        return tabla;
    }

    private JPanel crearPanelTabla(
            JTable tabla,
            String descripcion) {

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        );

        JLabel etiqueta = new JLabel(descripcion);
        etiqueta.setBorder(
            BorderFactory.createEmptyBorder(0, 0, 10, 0)
        );

        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        return panel;
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

    private void actualizarAlertas() {
        cargarCertificados();
        cargarNotificaciones();
        cargarResumen();
    }

    private void cargarCertificados() {
        try {
            cargarFilas(modeloCertificados, alertaDAO.certificadosProximos());
        } catch (SQLException e) {
            mostrarError("No se cargaron los certificados.", e);
        }
    }

    private void cargarNotificaciones() {
        try {
            cargarFilas(modeloNotificaciones, alertaDAO.notificacionesPendientes());
        } catch (SQLException e) {
            mostrarError("No se cargaron las notificaciones.", e);
        }
    }

    private void cargarResumen() {
        try {
            cargarFilas(modeloResumen, alertaDAO.resumenDiarioIntentos());
        } catch (SQLException e) {
            mostrarError("No se cargó el resumen diario.", e);
        }
    }

    private void cargarFilas(DefaultTableModel modelo, ArrayList<Object[]> filas) {
        modelo.setRowCount(0);
        for (Object[] fila : filas) {
            modelo.addRow(fila);
        }
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
