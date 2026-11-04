package ar.com.skilful.vista;

import ar.com.skilful.dao.HistorialCobrosDAO;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

public class VentanaHistorialCobros extends JFrame {

    private final HistorialCobrosDAO historialDAO = new HistorialCobrosDAO();

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
        try {
            for (Object[] fila : historialDAO.buscar(busqueda)) {
                for (int i = 6; i <= 9; i++) {
                    fila[i] = formatoMoneda((BigDecimal) fila[i]);
                }
                modeloTabla.addRow(fila);
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

    private String formatoMoneda(BigDecimal importe) {
        return "$ " + importe
            .setScale(2, RoundingMode.HALF_UP)
            .toPlainString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaHistorialCobros().setVisible(true);
        });
    }
}
