package ar.com.skilful.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import ar.com.skilful.dao.BeneficioDAO;

public class VentanaBeneficios extends JFrame {

    private static final long serialVersionUID = 1L;
    private final BeneficioDAO beneficioDAO = new BeneficioDAO();

    private JTextField campoBusqueda;
    private JTable tablaBeneficios;
    private DefaultTableModel modeloTabla;

    public VentanaBeneficios() {
        configurarVentana();
        crearContenido();
        cargarBeneficios("");
    }

    private void configurarVentana() {
        setTitle("Gestión de beneficios - Proyecto Impulso");
        setSize(1050, 570);
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

        JLabel titulo = new JLabel("GESTIÓN DE BENEFICIOS");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        encabezado.add(titulo, BorderLayout.WEST);

        JPanel panelBusqueda = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );

        panelBusqueda.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 0, 10)
        );

        panelBusqueda.add(
            new JLabel("Buscar por DNI, socio o beneficio:")
        );

        campoBusqueda = new JTextField(25);

        JButton botonBuscar = crearBoton("Buscar");
        JButton botonMostrarTodos = crearBoton("Mostrar todos");

        panelBusqueda.add(campoBusqueda);
        panelBusqueda.add(botonBuscar);
        panelBusqueda.add(botonMostrarTodos);

        modeloTabla = new DefaultTableModel(
            new Object[] {
                "ID", "DNI", "Socio", "Beneficio", "Empresa",
                "Presentación", "Vencimiento", "Estado"
            }, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaBeneficios = new JTable(modeloTabla);
        tablaBeneficios.setRowHeight(25);
        tablaBeneficios.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );
        tablaBeneficios.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 12)
        );

        JScrollPane desplazamiento =
            new JScrollPane(tablaBeneficios);

        desplazamiento.setBorder(
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        panelBotones.setBorder(
            BorderFactory.createEmptyBorder(5, 10, 15, 15)
        );

        JButton botonAsignar = crearBoton("Asignar beneficio");
        JButton botonRenovar = crearBoton("Renovar");
        JButton botonFinalizar = crearBoton("Finalizar");
        JButton botonCerrar = crearBoton("Cerrar");

        panelBotones.add(botonAsignar);
        panelBotones.add(botonRenovar);
        panelBotones.add(botonFinalizar);
        panelBotones.add(botonCerrar);

        botonBuscar.addActionListener(
            e -> cargarBeneficios(campoBusqueda.getText().trim())
        );

        campoBusqueda.addActionListener(
            e -> cargarBeneficios(campoBusqueda.getText().trim())
        );

        botonMostrarTodos.addActionListener(e -> {
            campoBusqueda.setText("");
            cargarBeneficios("");
        });

        botonAsignar.addActionListener(e -> {
            FormularioBeneficio formulario =
                new FormularioBeneficio(this);

            formulario.setVisible(true);
        });

        botonRenovar.addActionListener(e -> renovarBeneficio());
        botonFinalizar.addActionListener(e -> finalizarBeneficio());

        botonCerrar.addActionListener(e -> dispose());

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(panelBusqueda, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(desplazamiento, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JButton crearBoton(String texto) {
        JButton boton = new JButton(texto);
        boton.setBackground(new Color(190, 25, 35));
        boton.setForeground(Color.WHITE);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setContentAreaFilled(true);
        boton.setOpaque(true);

        return boton;
    }

    private void validarSeleccion(String operacion) {
        if (tablaBeneficios.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar un beneficio de la tabla."
            );
            return;
        }

        JOptionPane.showMessageDialog(
            this,
            "A continuación incorporaremos la función de "
            + operacion + " el beneficio."
        );
    }
    
    private void renovarBeneficio() {
        int fila = tablaBeneficios.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar un beneficio."
            );
            return;
        }

        int idBeneficio = Integer.parseInt(
            modeloTabla.getValueAt(fila, 0).toString()
        );

        String tipo = modeloTabla.getValueAt(fila, 3).toString();

        if (!"ESTUDIANTE".equalsIgnoreCase(tipo)) {
            JOptionPane.showMessageDialog(
                this,
                "El beneficio por convenio no requiere renovación semestral."
            );
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Confirma la presentación de un nuevo certificado estudiantil?",
            "Renovar beneficio",
            JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            beneficioDAO.renovar(idBeneficio);
            JOptionPane.showMessageDialog(this,
                "Se registró el nuevo certificado.\n"
                + "El beneficio anterior se conserva como FINALIZADO.");
            cargarBeneficios(campoBusqueda.getText().trim());
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this,
                "No se pudo renovar el beneficio.\n" + error.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void finalizarBeneficio() {
        int fila = tablaBeneficios.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar un beneficio."
            );
            return;
        }

        int idBeneficio = Integer.parseInt(
            modeloTabla.getValueAt(fila, 0).toString()
        );

        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Confirma la finalización del beneficio seleccionado?",
            "Finalizar beneficio",
            JOptionPane.YES_NO_OPTION
        );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (!beneficioDAO.finalizar(idBeneficio)) {
                JOptionPane.showMessageDialog(this, "El beneficio seleccionado ya no existe.");
                return;
            }
            JOptionPane.showMessageDialog(this, "El beneficio fue finalizado correctamente.");
            cargarBeneficios("");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "No se pudo completar la operación.\n" + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void cargarBeneficios(String busqueda) {
        modeloTabla.setRowCount(0);

        try {
            for (Object[] fila : beneficioDAO.buscar(busqueda)) {
                modeloTabla.addRow(fila);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los beneficios.\n"
                + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            VentanaBeneficios ventana = new VentanaBeneficios();
            ventana.setVisible(true);
        });
    }
}
