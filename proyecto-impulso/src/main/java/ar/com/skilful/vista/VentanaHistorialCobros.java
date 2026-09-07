package ar.com.skilful.vista;

import ar.com.skilful.conexion.ConexionBD;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VentanaHistorialCobros extends JFrame {

    private final JTextField campoBusqueda;
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;

    public VentanaHistorialCobros() {
        setTitle("Historial de cobros - Proyecto Impulso");
        setSize(1250, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        campoBusqueda = new JTextField(30);

        String[] columnas = {
            "Fecha", "Período", "DNI", "Socio", "Plan",
            "Medio de pago", "Importe final", "Saldo aplicado",
            "Importe abonado", "Saldo generado", "Estado",
            "Sede", "Registrado por"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setRowHeight(24);

        crearContenido();
        cargarHistorial("");
    }

    private void crearContenido() {
        setLayout(new BorderLayout());

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(24, 24, 24));
        encabezado.setBorder(
            BorderFactory.createEmptyBorder(18, 18, 18, 18)
        );

        JLabel titulo = new JLabel("HISTORIAL DE COBROS");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        encabezado.add(titulo, BorderLayout.WEST);

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.add(encabezado, BorderLayout.NORTH);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        panelBusqueda.add(
            new JLabel("Buscar por DNI o socio:")
        );
        panelBusqueda.add(campoBusqueda);

        JButton botonBuscar = crearBoton("Buscar");
        JButton botonMostrarTodos = crearBoton("Mostrar todos");

        panelBusqueda.add(botonBuscar);
        panelBusqueda.add(botonMostrarTodos);

        panelSuperior.add(panelBusqueda, BorderLayout.SOUTH);
        add(panelSuperior, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(tabla);
        add(scroll, BorderLayout.CENTER);

        configurarAnchos();

        JPanel panelInferior = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        panelInferior.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        );

        JButton botonCerrar = crearBoton("Cerrar");
        panelInferior.add(botonCerrar);
        add(panelInferior, BorderLayout.SOUTH);

        botonBuscar.addActionListener(
            e -> cargarHistorial(campoBusqueda.getText().trim())
        );

        botonMostrarTodos.addActionListener(e -> {
            campoBusqueda.setText("");
            cargarHistorial("");
        });

        campoBusqueda.addActionListener(
            e -> cargarHistorial(campoBusqueda.getText().trim())
        );

        botonCerrar.addActionListener(e -> dispose());
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);

        boton.setUI(
            new javax.swing.plaf.basic.BasicButtonUI()
        );

        boton.setBackground(new Color(198, 20, 36));
        boton.setForeground(Color.WHITE);
        boton.setOpaque(true);
        boton.setContentAreaFilled(true);
        boton.setBorderPainted(false);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setPreferredSize(new Dimension(115, 28));

        return boton;
    }

    private void configurarAnchos() {
        int[] anchos = {
            145, 90, 90, 170, 80, 110, 100,
            110, 110, 110, 90, 120, 110
        };

        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel()
                 .getColumn(i)
                 .setPreferredWidth(anchos[i]);
        }
    }

    private void cargarHistorial(String busqueda) {
        modeloTabla.setRowCount(0);

        String sql =
            "SELECT fecha_pago, periodo, dni, socio, plan, " +
            "medio_pago, importe_final, saldo_aplicado, " +
            "importe_abonado, saldo_generado, estado_pago, " +
            "sede, registrado_por " +
            "FROM vw_historial_pagos " +
            "WHERE dni LIKE ? OR socio LIKE ? " +
            "ORDER BY fecha_pago DESC";

        try (
        	Connection conexion = ConexionBD.conectar();
            PreparedStatement consulta = conexion.prepareStatement(sql)
        ) {
            String filtro = "%" + busqueda + "%";

            consulta.setString(1, filtro);
            consulta.setString(2, filtro);

            try (ResultSet resultado = consulta.executeQuery()) {
                while (resultado.next()) {
                    modeloTabla.addRow(new Object[] {
                        resultado.getTimestamp("fecha_pago"),
                        resultado.getDate("periodo"),
                        resultado.getString("dni"),
                        resultado.getString("socio"),
                        resultado.getString("plan"),
                        resultado.getString("medio_pago"),
                        formatoMoneda(
                            resultado.getDouble("importe_final")
                        ),
                        formatoMoneda(
                            resultado.getDouble("saldo_aplicado")
                        ),
                        formatoMoneda(
                            resultado.getDouble("importe_abonado")
                        ),
                        formatoMoneda(
                            resultado.getDouble("saldo_generado")
                        ),
                        resultado.getString("estado_pago"),
                        resultado.getString("sede"),
                        resultado.getString("registrado_por")
                    });
                }
            }

        } catch (SQLException error) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo cargar el historial:\n"
                    + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private String formatoMoneda(double importe) {
        return String.format("$ %.2f", importe);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaHistorialCobros().setVisible(true);
        });
    }
}