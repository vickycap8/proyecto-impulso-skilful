package ar.com.skilful.vista;

import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;

import javax.swing.*;

import ar.com.skilful.dao.BeneficioDAO;
import ar.com.skilful.dao.BeneficioDAO.Opcion;

public class FormularioBeneficio extends JDialog {

    private static final long serialVersionUID = 1L;

    private final VentanaBeneficios ventanaBeneficios;
    private final BeneficioDAO beneficioDAO = new BeneficioDAO();

    private JTextField campoSocio;
    private int idSocioSeleccionado = -1;
    private JComboBox<Opcion> comboTipo;
    private JComboBox<Opcion> comboEmpresa;
    private JTextField campoPresentacion;
    private JTextField campoVencimiento;
    private JTextField campoObservacion;

    public FormularioBeneficio(VentanaBeneficios ventanaBeneficios) {
        super(ventanaBeneficios, "Asignar beneficio", true);
        this.ventanaBeneficios = ventanaBeneficios;

        configurarVentana();
        crearContenido();
        cargarTipos();
        cargarEmpresas();
        actualizarFormulario();
    }

    private void configurarVentana() {
        setSize(620, 470);
        setLocationRelativeTo(ventanaBeneficios);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(false);
    }

    private void crearContenido() {
        setLayout(new BorderLayout());

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(25, 25, 25));
        encabezado.setBorder(
            BorderFactory.createEmptyBorder(18, 20, 18, 20)
        );

        JLabel titulo = new JLabel("ASIGNAR BENEFICIO");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        encabezado.add(titulo);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(
            BorderFactory.createEmptyBorder(25, 30, 15, 30)
        );

        campoSocio = new JTextField(25);
        campoSocio.setEditable(false);

        comboTipo = new JComboBox<>();
        comboEmpresa = new JComboBox<>();

        JButton botonBuscarSocio = crearBoton("Buscar socio");

        JPanel panelSocio = new JPanel(new BorderLayout(5, 0));
        panelSocio.add(campoSocio, BorderLayout.CENTER);
        panelSocio.add(botonBuscarSocio, BorderLayout.EAST);

        botonBuscarSocio.addActionListener(e -> seleccionarSocio());

        campoPresentacion = new JTextField(
            LocalDate.now().toString(), 20
        );
        campoVencimiento = new JTextField(20);
        campoVencimiento.setEditable(false);

        campoObservacion = new JTextField(20);

        agregarCampo(formulario, "Socio:", panelSocio, 0);
        agregarCampo(formulario, "Tipo de beneficio:", comboTipo, 1);
        agregarCampo(formulario, "Empresa:", comboEmpresa, 2);

        agregarCampo(
            formulario,
            "Fecha de presentación:",
            campoPresentacion,
            3
        );

        agregarCampo(
            formulario,
            "Fecha de vencimiento:",
            campoVencimiento,
            4
        );

        agregarCampo(
            formulario,
            "Observación:",
            campoObservacion,
            5
        );

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        panelBotones.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 15, 20)
        );

        JButton botonGuardar = crearBoton("Guardar");
        JButton botonCancelar = crearBoton("Cancelar");

        panelBotones.add(botonGuardar);
        panelBotones.add(botonCancelar);

        comboTipo.addActionListener(e -> actualizarFormulario());

        campoPresentacion.addActionListener(
            e -> actualizarFormulario()
        );

        botonGuardar.addActionListener(e -> guardarBeneficio());
        botonCancelar.addActionListener(e -> dispose());

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
        restricciones.insets = new Insets(8, 5, 8, 5);
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
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setOpaque(true);

        return boton;
    }

    private void cargarTipos() {
        try {
            for (Opcion tipo : beneficioDAO.tiposActivos()) comboTipo.addItem(tipo);
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar los tipos de beneficio.", e);
        }
    }

    private void cargarEmpresas() {
        try {
            for (Opcion empresa : beneficioDAO.empresasActivas()) comboEmpresa.addItem(empresa);
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las empresas.", e);
        }
    }

    private void actualizarFormulario() {
        Opcion tipo = (Opcion) comboTipo.getSelectedItem();

        if (tipo == null) {
            return;
        }

        boolean esConvenio =
            "CONVENIO".equalsIgnoreCase(tipo.getNombre());

        comboEmpresa.setEnabled(esConvenio);

        try {
            LocalDate presentacion = LocalDate.parse(
                campoPresentacion.getText().trim()
            );

            if ("ESTUDIANTE".equalsIgnoreCase(tipo.getNombre())) {
                campoVencimiento.setText(
                    presentacion.plusMonths(6).toString()
                );
            } else {
                campoVencimiento.setText("Sin vencimiento");
            }

        } catch (Exception e) {
            campoVencimiento.setText("");
        }
    }
    
    private void seleccionarSocio() {
        SelectorSocio selector = new SelectorSocio(this);

        if (selector.mostrarSelector()) {
            idSocioSeleccionado =
                selector.getIdSocioSeleccionado();

            campoSocio.setText(
                selector.getDescripcionSocio()
            );
        }
    }

    private void guardarBeneficio() {
    	Opcion tipo = (Opcion) comboTipo.getSelectedItem();

    	if (idSocioSeleccionado == -1 || tipo == null) {
            JOptionPane.showMessageDialog(
                this,
                "Debe seleccionar un socio y un beneficio."
            );
            return;
        }

        LocalDate fechaPresentacion;

        try {
            fechaPresentacion = LocalDate.parse(
                campoPresentacion.getText().trim()
            );
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "La fecha debe tener el formato AAAA-MM-DD.",
                "Fecha incorrecta",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        boolean esEstudiante =
            "ESTUDIANTE".equalsIgnoreCase(tipo.getNombre());

        Opcion empresa =
            (Opcion) comboEmpresa.getSelectedItem();

        if (!esEstudiante && empresa == null) {
            JOptionPane.showMessageDialog(
                this,
                "Debe seleccionar la empresa del convenio."
            );
            return;
        }

        try {
            beneficioDAO.asignar(idSocioSeleccionado, tipo.getId(),
                esEstudiante ? null : empresa.getId(), fechaPresentacion,
                esEstudiante, campoObservacion.getText().trim());
            JOptionPane.showMessageDialog(this, "El beneficio fue asignado correctamente.");
            ventanaBeneficios.cargarBeneficios("");
            dispose();
        } catch (SQLException e) {
            mostrarError("No se pudo asignar el beneficio.", e);
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
