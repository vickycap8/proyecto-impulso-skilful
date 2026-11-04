package ar.com.skilful.vista;

import java.awt.*;
import java.sql.SQLException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import ar.com.skilful.sesion.SesionUsuario;
import ar.com.skilful.servicio.SuscripcionServicio;
import ar.com.skilful.dao.SuscripcionDAO;

public class VentanaSuscripciones extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final int COL_ID = 0;
    private static final int COL_SOCIO = 2;
    private static final int COL_PERIODO = 3;
    private static final int COL_ESTADO_CUOTA = 4;
    private static final int COL_PERMANENCIA = 9;
    private static final int COL_ID_CUOTA = 14;
    private static final int COL_ID_INTENTO = 15;

    private JTextField campoBusqueda;
    private JTable tablaSuscripciones;
    private DefaultTableModel modeloTabla;
    private final SuscripcionServicio suscripcionServicio;
    private final SuscripcionDAO suscripcionDAO = new SuscripcionDAO();

    public VentanaSuscripciones() {
        suscripcionServicio = new SuscripcionServicio();
        configurarVentana();
        crearContenido();
        cargarSuscripciones("");
    }

    private void configurarVentana() {
        setTitle("Control de suscripciones - Proyecto Impulso");
        setSize(1180, 600);
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

        JLabel titulo = new JLabel("CONTROL DE SUSCRIPCIONES");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 22));

        JLabel informacion = new JLabel(
            "Seleccione la fila del período que desea gestionar"
        );
        informacion.setForeground(Color.LIGHT_GRAY);

        encabezado.add(titulo, BorderLayout.WEST);
        encabezado.add(informacion, BorderLayout.EAST);

        JPanel panelBusqueda = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );

        panelBusqueda.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 0, 10)
        );

        panelBusqueda.add(
            new JLabel("Buscar por DNI, nombre o apellido:")
        );

        campoBusqueda = new JTextField(25);

        JButton botonBuscar = crearBoton("Buscar");
        JButton botonMostrarTodos = crearBoton("Mostrar todos");

        panelBusqueda.add(campoBusqueda);
        panelBusqueda.add(botonBuscar);
        panelBusqueda.add(botonMostrarTodos);

        modeloTabla = new DefaultTableModel(
            new Object[] {
                "ID", "DNI", "Socio", "Período", "Estado cuota",
                "Teléfono", "Sede", "Plan", "Día", "Permanencia",
                "Intentos", "Último resultado", "Notificación", "Estado",
                "ID cuota", "ID intento"
            }, 0
        ) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };

        tablaSuscripciones = new JTable(modeloTabla);
        tablaSuscripciones.setRowHeight(25);
        tablaSuscripciones.setAutoResizeMode(
            JTable.AUTO_RESIZE_OFF
        );
        tablaSuscripciones.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        // Identificadores del registro seleccionado: no dependen del orden visible.
        tablaSuscripciones.removeColumn(tablaSuscripciones.getColumnModel().getColumn(15));
        tablaSuscripciones.removeColumn(tablaSuscripciones.getColumnModel().getColumn(14));
        establecerAnchos();

        JScrollPane desplazamiento =
            new JScrollPane(tablaSuscripciones);

        desplazamiento.setBorder(
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        );

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        panelBotones.setBorder(
            BorderFactory.createEmptyBorder(5, 10, 15, 15)
        );
        
        JButton botonNuevaSuscripcion = crearBoton("Nueva suscripción");

        JButton botonNuevoIntento = crearBoton("Registrar resultado");

        JButton botonNotificar = crearBoton("Marcar como notificado");
        
        JButton botonFinalizar = crearBoton("Finalizar suscripción");
        
        JButton botonCerrar = crearBoton("Cerrar");

        panelBotones.add(botonNuevaSuscripcion);
        panelBotones.add(botonNuevoIntento);
        panelBotones.add(botonNotificar);
        panelBotones.add(botonFinalizar);
        panelBotones.add(botonCerrar);

        botonBuscar.addActionListener(
            e -> cargarSuscripciones(
                campoBusqueda.getText().trim()
            )
        );

        campoBusqueda.addActionListener(
            e -> cargarSuscripciones(
                campoBusqueda.getText().trim()
            )
        );

        botonMostrarTodos.addActionListener(e -> {
            campoBusqueda.setText("");
            cargarSuscripciones("");
        });
        
        botonNuevaSuscripcion.addActionListener(e -> {
            FormularioSuscripcion formulario =
                new FormularioSuscripcion(
                    this,
                    () -> cargarSuscripciones("")
                );

            formulario.setVisible(true);
        });

        botonNuevoIntento.addActionListener(e -> {

            Object[] opciones = {
                "APROBADO",
                "RECHAZADO",
                "Cancelar"
            };

            int resultado = JOptionPane.showOptionDialog(
                this,
                "Seleccione el resultado del intento de cobro:",
                "Registrar resultado",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]
            );

            if (resultado == 0) {
                registrarIntentoAprobado();

            } else if (resultado == 1) {
                registrarIntentoRechazado();
            }
        });

        botonNotificar.addActionListener(
        	    e -> registrarNotificacion()
        );
        
        botonFinalizar.addActionListener(
        	    e -> finalizarSuscripcion()
        	);

        botonCerrar.addActionListener(e -> dispose());

        JPanel superior = new JPanel(new BorderLayout());
        superior.add(encabezado, BorderLayout.NORTH);
        superior.add(panelBusqueda, BorderLayout.SOUTH);

        add(superior, BorderLayout.NORTH);
        add(desplazamiento, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void establecerAnchos() {
        int[] anchos = {
            45, 90, 160, 90, 100, 110, 120, 70,
            45, 100, 65, 125, 105, 80
        };

        for (int i = 0; i < anchos.length; i++) {
            tablaSuscripciones.getColumnModel()
                .getColumn(i)
                .setPreferredWidth(anchos[i]);
        }
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

    private int filaSeleccionada() {
        int fila = tablaSuscripciones.getSelectedRow();
        return fila < 0 ? -1 : tablaSuscripciones.convertRowIndexToModel(fila);
    }

    private int entero(int fila, int columna) {
        return ((Number) modeloTabla.getValueAt(fila, columna)).intValue();
    }

    private void registrarIntentoRechazado() {
        registrarResultadoDesdePantalla(false);
    }

    private void registrarIntentoAprobado() {
        registrarResultadoDesdePantalla(true);
    }

    private void registrarResultadoDesdePantalla(boolean aprobado) {
        int fila = filaSeleccionada();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione la suscripción y el período.");
            return;
        }
        int idCuota = entero(fila, COL_ID_CUOTA);
        String estado = String.valueOf(modeloTabla.getValueAt(fila, COL_ESTADO_CUOTA));
        if (idCuota == 0 || !("PENDIENTE".equals(estado) || "VENCIDA".equals(estado))) {
            JOptionPane.showMessageDialog(this,
                "Seleccione una cuota PENDIENTE o VENCIDA.");
            return;
        }
        String resultado = aprobado ? "APROBADO" : "RECHAZADO";
        String periodo = String.valueOf(modeloTabla.getValueAt(fila, COL_PERIODO));
        if (JOptionPane.showConfirmDialog(this,
                "Socio: " + modeloTabla.getValueAt(fila, COL_SOCIO)
                + "\nPeríodo: " + periodo + "\nResultado: " + resultado
                + (aprobado ? "\nTambién se registrará el pago de esta cuota." : "")
                + "\n¿Confirma el resultado consultado en Mercado Pago?",
                "Registrar resultado", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        String observacion = JOptionPane.showInputDialog(this,
            "Ingrese una observación o referencia:", "Cobro " + resultado + " por Mercado Pago");
        if (observacion == null) {
            return;
        }
        try {
            int numero = suscripcionDAO.guardarResultado(entero(fila, COL_ID), idCuota,
                SesionUsuario.getIdUsuario(), SesionUsuario.getIdSede(),
                aprobado, observacion.trim());
            JOptionPane.showMessageDialog(this, "Intento " + numero + " registrado como "
                + resultado + " para el período " + periodo + "."
                + (aprobado ? "\nLa cuota quedó PAGADA." : ""));
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                "No se pudo registrar el resultado", JOptionPane.ERROR_MESSAGE);
        }
        cargarSuscripciones(campoBusqueda.getText().trim());
    }

    private void finalizarSuscripcion() {

        int fila = filaSeleccionada();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Primero debe seleccionar una suscripción."
            );
            return;
        }

        int idSuscripcion = Integer.parseInt(
            modeloTabla.getValueAt(fila, COL_ID).toString()
        );

        String socio =
            modeloTabla.getValueAt(fila, COL_SOCIO).toString();

        String permanencia =
            modeloTabla.getValueAt(fila, COL_PERMANENCIA).toString();

        int confirmacion = JOptionPane.showConfirmDialog(
            this,
            "Se intentará finalizar la suscripción de:\n"
                + socio + "\n\n"
                + "Permanencia mínima hasta: "
                + permanencia + "\n\n"
                + "¿Desea continuar?",
            "Finalizar suscripción",
            JOptionPane.YES_NO_OPTION
        );

        if (confirmacion != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            if (!suscripcionServicio.finalizar(idSuscripcion)) {
                JOptionPane.showMessageDialog(
                    this,
                    "La suscripción no puede finalizarse todavía.\n"
                        + "La permanencia mínima se extiende hasta "
                        + permanencia + ".",
                    "Permanencia vigente",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            JOptionPane.showMessageDialog(
                this,
                "La suscripción fue finalizada correctamente."
            );

            cargarSuscripciones("");

        } catch (SQLException error) {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo finalizar la suscripción.\n"
                    + error.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void registrarNotificacion() {
        int fila = filaSeleccionada();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione la suscripción y el período.");
            return;
        }
        long idIntento = ((Number) modeloTabla.getValueAt(fila, COL_ID_INTENTO)).longValue();
        if (idIntento == 0) {
            JOptionPane.showMessageDialog(this,
                "El último intento de este período no tiene una notificación pendiente.");
            return;
        }
        String observacion = JOptionPane.showInputDialog(this,
            "Período: " + modeloTabla.getValueAt(fila, COL_PERIODO)
            + "\nRegistre la notificación después de contactar al socio por WhatsApp."
            + "\nObservación:", "Socio notificado por WhatsApp");
        if (observacion == null) {
            return;
        }
        try {
            suscripcionDAO.guardarNotificacion(idIntento, entero(fila, COL_ID),
                entero(fila, COL_ID_CUOTA), SesionUsuario.getIdUsuario(),
                observacion.trim());
            JOptionPane.showMessageDialog(this, "La notificación quedó registrada.");
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(),
                "No se pudo registrar la notificación", JOptionPane.ERROR_MESSAGE);
        }
        cargarSuscripciones(campoBusqueda.getText().trim());
    }

    private void cargarSuscripciones(String busqueda) {
        modeloTabla.setRowCount(0);
        try {
            for (Object[] fila : suscripcionDAO.buscarParaPantalla(busqueda)) {
                modeloTabla.addRow(fila);
            }
        } catch (SQLException error) {
            JOptionPane.showMessageDialog(this,
                "No se pudieron cargar las suscripciones.\n" + error.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
