package ar.com.skilful.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
import javax.swing.table.DefaultTableModel;

import ar.com.skilful.conexion.ConexionBD;

public class VentanaSocios extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField campoBusqueda;
    private JTable tablaSocios;
    private DefaultTableModel modeloTabla;

    public VentanaSocios() {
        configurarVentana();
        crearContenido();
        cargarSocios("");
    }

    private void configurarVentana() {
        setTitle("Gestión de socios - Proyecto Impulso");
        setSize(950, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    private void crearContenido() {
        setLayout(new BorderLayout(10, 10));

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(25, 25, 25));
        encabezado.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));

        JLabel titulo = new JLabel("GESTIÓN DE SOCIOS");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        encabezado.add(titulo, BorderLayout.WEST);

        JPanel panelBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBusqueda.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        panelBusqueda.add(new JLabel("Buscar por DNI, nombre o apellido:"));

        campoBusqueda = new JTextField(25);
        JButton botonBuscar = crearBoton("Buscar");
        JButton botonMostrarTodos = crearBoton("Mostrar todos");

        panelBusqueda.add(campoBusqueda);
        panelBusqueda.add(botonBuscar);
        panelBusqueda.add(botonMostrarTodos);

        modeloTabla = new DefaultTableModel(
                new Object[] {
                    "ID", "DNI", "Apellido", "Nombre",
                    "Teléfono", "Correo", "Sede"
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
        tablaSocios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaSocios.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        JScrollPane desplazamiento = new JScrollPane(tablaSocios);
        desplazamiento.setBorder(
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(5, 10, 15, 15));

        JButton botonRegistrar = crearBoton("Registrar socio");
        JButton botonActualizar = crearBoton("Actualizar socio");
        JButton botonCerrar = crearBoton("Cerrar");

        panelBotones.add(botonRegistrar);
        panelBotones.add(botonActualizar);
        panelBotones.add(botonCerrar);

        botonBuscar.addActionListener(
                e -> cargarSocios(campoBusqueda.getText().trim())
        );

        campoBusqueda.addActionListener(
                e -> cargarSocios(campoBusqueda.getText().trim())
        );

        botonMostrarTodos.addActionListener(e -> {
            campoBusqueda.setText("");
            cargarSocios("");
        });

        botonRegistrar.addActionListener(e -> {
            FormularioSocio formulario = new FormularioSocio(this);
            formulario.setVisible(true);
        });

        botonActualizar.addActionListener(e -> {
            int filaSeleccionada = tablaSocios.getSelectedRow();

            if (filaSeleccionada == -1) {
                JOptionPane.showMessageDialog(
                    this,
                    "Primero debe seleccionar un socio de la tabla."
                );
                return;
            }

            int idSocio = Integer.parseInt(
                modeloTabla.getValueAt(filaSeleccionada, 0).toString()
            );

            FormularioActualizarSocio formulario =
                new FormularioActualizarSocio(this, idSocio);

            formulario.setVisible(true);
        });

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

    private void cargarSocios(String busqueda) {
        modeloTabla.setRowCount(0);

        String sql =
            "SELECT so.id_socio, so.dni, so.apellido, so.nombre, "
          + "so.telefono, so.correo, se.nombre AS sede "
          + "FROM socio so "
          + "INNER JOIN sede se ON se.id_sede = so.id_sede_habitual "
          + "WHERE so.activo = TRUE "
          + "AND (so.dni LIKE ? OR so.nombre LIKE ? OR so.apellido LIKE ?) "
          + "ORDER BY so.apellido, so.nombre";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {

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
                        resultado.getString("telefono"),
                        resultado.getString("correo"),
                        resultado.getString("sede")
                    });
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los socios.\n" + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    public void refrescarSocios() {
        campoBusqueda.setText("");
        cargarSocios("");
    }
}
