package ar.com.skilful.vista;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ar.com.skilful.conexion.ConexionBD;

public class SelectorSocio extends JDialog {

    private static final long serialVersionUID = 1L;

    private JTextField campoBusqueda;
    private JTable tablaSocios;
    private DefaultTableModel modeloTabla;

    private int idSocioSeleccionado = -1;
    private String descripcionSocio;
    private boolean seleccionConfirmada;

    public SelectorSocio(Window ventanaPadre) {
        super(
            ventanaPadre,
            "Buscar socio",
            ModalityType.APPLICATION_MODAL
        );

        configurarVentana();
        crearContenido();
    }

    private void configurarVentana() {
        setSize(750, 450);
        setLocationRelativeTo(getOwner());
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    }

    private void crearContenido() {
        setLayout(new BorderLayout(10, 10));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(25, 25, 25));
        encabezado.setBorder(
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        );

        JLabel titulo = new JLabel("BUSCAR SOCIO");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        encabezado.add(titulo);

        JPanel panelBusqueda = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );

        panelBusqueda.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 0, 10)
        );

        panelBusqueda.add(
            new JLabel("DNI, nombre o apellido:")
        );

        campoBusqueda = new JTextField(25);
        JButton botonBuscar = crearBoton("Buscar");

        panelBusqueda.add(campoBusqueda);
        panelBusqueda.add(botonBuscar);

        modeloTabla = new DefaultTableModel(
            new Object[] {
                "ID", "DNI", "Apellido", "Nombre", "Sede"
            }, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaSocios = new JTable(modeloTabla);
        tablaSocios.setRowHeight(25);
        tablaSocios.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane desplazamiento =
            new JScrollPane(tablaSocios);

        desplazamiento.setBorder(
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        panelBotones.setBorder(
            BorderFactory.createEmptyBorder(5, 10, 15, 15)
        );

        JButton botonSeleccionar = crearBoton("Seleccionar");
        JButton botonCancelar = crearBoton("Cancelar");

        panelBotones.add(botonSeleccionar);
        panelBotones.add(botonCancelar);

        botonBuscar.addActionListener(e -> buscarSocios());
        campoBusqueda.addActionListener(e -> buscarSocios());

        botonSeleccionar.addActionListener(
            e -> confirmarSeleccion()
        );

        botonCancelar.addActionListener(e -> dispose());

        tablaSocios.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evento) {
                if (evento.getClickCount() == 2) {
                    confirmarSeleccion();
                }
            }
        });

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
        boton.setOpaque(true);

        return boton;
    }

    private void buscarSocios() {
        String busqueda = campoBusqueda.getText().trim();

        if (busqueda.length() < 2) {
            JOptionPane.showMessageDialog(
                this,
                "Ingrese al menos dos caracteres para buscar.",
                "Búsqueda incompleta",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        modeloTabla.setRowCount(0);

        String sql =
            "SELECT so.id_socio, so.dni, so.apellido, "
          + "so.nombre, se.nombre AS sede "
          + "FROM socio so "
          + "INNER JOIN sede se "
          + "ON se.id_sede = so.id_sede_habitual "
          + "WHERE so.activo = TRUE "
          + "AND (so.dni LIKE ? OR so.nombre LIKE ? "
          + "OR so.apellido LIKE ?) "
          + "ORDER BY so.apellido, so.nombre "
          + "LIMIT 50";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia =
                 conexion.prepareStatement(sql)) {

            String filtro = "%" + busqueda + "%";

            sentencia.setString(1, filtro);
            sentencia.setString(2, filtro);
            sentencia.setString(3, filtro);

            try (ResultSet resultado = sentencia.executeQuery()) {
                while (resultado.next()) {
                    modeloTabla.addRow(new Object[] {
                        resultado.getInt("id_socio"),
                        resultado.getString("dni"),
                        resultado.getString("apellido"),
                        resultado.getString("nombre"),
                        resultado.getString("sede")
                    });
                }
            }

            if (modeloTabla.getRowCount() == 0) {
                JOptionPane.showMessageDialog(
                    this,
                    "No se encontraron socios coincidentes."
                );
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo realizar la búsqueda.\n"
                + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void confirmarSeleccion() {
        int fila = tablaSocios.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Seleccione un socio de la tabla."
            );
            return;
        }

        idSocioSeleccionado = Integer.parseInt(
            modeloTabla.getValueAt(fila, 0).toString()
        );

        String dni = modeloTabla.getValueAt(fila, 1).toString();
        String apellido = modeloTabla.getValueAt(fila, 2).toString();
        String nombre = modeloTabla.getValueAt(fila, 3).toString();

        descripcionSocio =
            dni + " - " + apellido + ", " + nombre;

        seleccionConfirmada = true;
        dispose();
    }

    public boolean mostrarSelector() {
        setVisible(true);
        return seleccionConfirmada;
    }

    public int getIdSocioSeleccionado() {
        return idSocioSeleccionado;
    }

    public String getDescripcionSocio() {
        return descripcionSocio;
    }
}