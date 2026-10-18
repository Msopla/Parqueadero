package parqueadero;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;


public class VentanaParqueadero extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FECHA = DateTimeFormatter
            .ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());

    private JPanel contentPane;
    private JTextField txtPlaca;
    private JTextField txtPropietario;
    private JTable tablaVehiculos;
    private JTable tablaCaja;
    private JLabel lblResumen;
    private JLabel lblEstado;
    private JTabbedPane pestanas;
    private Parqueadero parqueadero;

    /** Launch the application. */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    
                }
                VentanaParqueadero ventana = new VentanaParqueadero();
                try {
                    ventana.parqueadero = new Parqueadero(Paths.get("datos"));
                    ventana.actualizarTablas();
                    ventana.setVisible(true);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(ventana,
                            "No se pudo abrir el parqueadero.\n" + ex.getMessage(),
                            "Error de archivos", JOptionPane.ERROR_MESSAGE);
                    ventana.dispose();
                }
            }
        });
    }

    
    public VentanaParqueadero() {
        setTitle("Parqueadero");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1040, 640);
        setMinimumSize(new Dimension(920, 560));
        setLocationRelativeTo(null);

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        contentPane.setLayout(new BorderLayout(10, 12));
        setContentPane(contentPane);

        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(new BorderLayout(0, 12));
        contentPane.add(panelSuperior, BorderLayout.NORTH);

        JPanel panelTitulo = new JPanel();
        panelTitulo.setLayout(new GridLayout(2, 1, 0, 5));
        panelSuperior.add(panelTitulo, BorderLayout.NORTH);

        JLabel lblTitulo = new JLabel("PARQUEADERO");
        lblTitulo.setFont(new Font("Times New Roman", Font.BOLD, 25));
        lblTitulo.setForeground(new Color(27, 65, 104));
        panelTitulo.add(lblTitulo);

        JLabel lblTarifa = new JLabel("Tarifa: $0,50 por hora o fracción  |  Cobro mínimo: $0,50");
        lblTarifa.setFont(new Font("Times New Roman", Font.PLAIN, 14));
        panelTitulo.add(lblTarifa);

        JPanel panelIngreso = new JPanel();
        panelIngreso.setBorder(new TitledBorder("Registrar ingreso"));
        panelIngreso.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 12));
        panelSuperior.add(panelIngreso, BorderLayout.CENTER);

        JLabel lblPlaca = new JLabel("Placa:");
        lblPlaca.setFont(new Font("Times New Roman", Font.PLAIN, 9));
        panelIngreso.add(lblPlaca);

        txtPlaca = new JTextField();
        txtPlaca.setColumns(12);
        txtPlaca.setToolTipText("Ejemplo: ABC-1234");
        lblPlaca.setLabelFor(txtPlaca);
        panelIngreso.add(txtPlaca);

        JLabel lblPropietario = new JLabel("Propietario (opcional):");
        lblPropietario.setFont(new Font("Times New Roman", Font.PLAIN, 9));
        panelIngreso.add(lblPropietario);

        txtPropietario = new JTextField();
        txtPropietario.setFont(new Font("Times New Roman", Font.PLAIN, 9));
        txtPropietario.setColumns(21);
        lblPropietario.setLabelFor(txtPropietario);
        panelIngreso.add(txtPropietario);

        JButton btnIngresar = new JButton("Registrar ingreso");
        btnIngresar.setFont(new Font("Times New Roman", Font.PLAIN, 9));
        btnIngresar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registrarIngreso();
            }
        });
        panelIngreso.add(btnIngresar);
        getRootPane().setDefaultButton(btnIngresar);

        pestanas = new JTabbedPane(JTabbedPane.TOP);
        contentPane.add(pestanas, BorderLayout.CENTER);

        JPanel panelVehiculos = new JPanel(new BorderLayout(0, 8));
        pestanas.addTab("Vehículos dentro", null, panelVehiculos, "Ingresos sin salida registrada");

        JLabel lblInstruccion = new JLabel("Seleccione un vehículo para consultar el valor o registrar su salida.");
        lblInstruccion.setFont(new Font("Times New Roman", Font.PLAIN, 9));
        panelVehiculos.add(lblInstruccion, BorderLayout.NORTH);

        JScrollPane scrollVehiculos = new JScrollPane();
        panelVehiculos.add(scrollVehiculos, BorderLayout.CENTER);

        tablaVehiculos = new JTable();
        tablaVehiculos.setModel(new DefaultTableModel(
                new Object[][] {}, new String[] {"Placa", "Propietario", "Entrada"}) {
            private static final long serialVersionUID = 1L;
            public boolean isCellEditable(int row, int column) { return false; }
        });
        tablaVehiculos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaVehiculos.setRowHeight(27);
        tablaVehiculos.setAutoCreateRowSorter(true);
        tablaVehiculos.setFillsViewportHeight(true);
        scrollVehiculos.setViewportView(tablaVehiculos);
        scrollVehiculos.setColumnHeaderView(tablaVehiculos.getTableHeader());

        JPanel panelSalida = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        panelVehiculos.add(panelSalida, BorderLayout.SOUTH);

        JButton btnCalcular = new JButton("Calcular valor");
        btnCalcular.setFont(new Font("Times New Roman", Font.PLAIN, 9));
        btnCalcular.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                consultarValor();
            }
        });
        panelSalida.add(btnCalcular);

        JButton btnSalida = new JButton("Cobrar y registrar salida");
        btnSalida.setFont(new Font("Times New Roman", Font.PLAIN, 9));
        btnSalida.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registrarSalida();
            }
        });
        panelSalida.add(btnSalida);

        JScrollPane scrollCaja = new JScrollPane();
        pestanas.addTab("Caja / historial de salidas", null, scrollCaja, "Cobros registrados");

        tablaCaja = new JTable();
        tablaCaja.setModel(new DefaultTableModel(new Object[][] {},
                new String[] {"Placa", "Entrada", "Salida", "Horas cobradas", "Valor"}) {
            private static final long serialVersionUID = 1L;
            public boolean isCellEditable(int row, int column) { return false; }
        });
        tablaCaja.setRowHeight(27);
        tablaCaja.setFillsViewportHeight(true);
        scrollCaja.setViewportView(tablaCaja);
        scrollCaja.setColumnHeaderView(tablaCaja.getTableHeader());

        JPanel panelInferior = new JPanel(new GridLayout(2, 1, 0, 7));
        contentPane.add(panelInferior, BorderLayout.SOUTH);

        lblResumen = new JLabel("Vehículos dentro: 0    |    Salidas: 0    |    Total en caja: $0,00");
        lblResumen.setFont(new Font("SansSerif", Font.BOLD, 14));
        panelInferior.add(lblResumen);

        lblEstado = new JLabel("Listo. Los datos se guardan automáticamente en la carpeta datos.");
        lblEstado.setForeground(new Color(75, 75, 75));
        panelInferior.add(lblEstado);
    }

    private void registrarIngreso() {
        try {
            Vehiculo vehiculo = parqueadero.ingresar(txtPlaca.getText(), txtPropietario.getText(), Instant.now());
            actualizarTablas();
            pestanas.setSelectedIndex(0);
            txtPlaca.setText("");
            txtPropietario.setText("");
            txtPlaca.requestFocusInWindow();
            lblEstado.setText("Ingreso guardado: " + vehiculo.getPlaca() + " - " + FECHA.format(vehiculo.getEntrada()));
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        } catch (IOException ex) {
            mostrarError("No se guardó el ingreso.\n" + ex.getMessage());
        }
    }

    private String placaSeleccionada() {
        int fila = tablaVehiculos.getSelectedRow();
        if (fila == -1) throw new IllegalArgumentException("Primero seleccione un vehículo en la tabla.");
        int filaModelo = tablaVehiculos.convertRowIndexToModel(fila);
        return tablaVehiculos.getModel().getValueAt(filaModelo, 0).toString();
    }

    private void consultarValor() {
        try {
            MovimientoCaja consulta = parqueadero.calcular(placaSeleccionada(), Instant.now());
            JOptionPane.showMessageDialog(this, detalle(consulta)
                    + "\n\nValor calculado para este momento. La consulta no registra la salida.",
                    "Valor a cancelar", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        }
    }

    private void registrarSalida() {
        try {
            MovimientoCaja consulta = parqueadero.calcular(placaSeleccionada(), Instant.now());
            int respuesta = JOptionPane.showConfirmDialog(this,
                    detalle(consulta) + "\n\n¿Confirma el cobro y la salida a la hora indicada?",
                    "Confirmar salida", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (respuesta != JOptionPane.YES_OPTION) return;
            
            MovimientoCaja cobro = parqueadero.retirar(consulta.getVehiculo().getPlaca(), consulta.getSalida());
            actualizarTablas();
            lblEstado.setText("Salida guardada: " + cobro.getVehiculo().getPlaca()
                    + " | Cobrado: " + dinero(cobro.getTotalCentavos()));
            JOptionPane.showMessageDialog(this, "Salida registrada correctamente.\n\n" + detalle(cobro),
                    "Comprobante", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            mostrarError(ex.getMessage());
        } catch (IOException ex) {
            mostrarError("No se guardó la salida; el vehículo continúa registrado dentro.\n" + ex.getMessage());
        }
    }

    private void actualizarTablas() {
        DefaultTableModel modeloVehiculos = (DefaultTableModel) tablaVehiculos.getModel();
        modeloVehiculos.setRowCount(0);
        for (Vehiculo vehiculo : parqueadero.getActivos()) {
            modeloVehiculos.addRow(new Object[] {vehiculo.getPlaca(), vehiculo.getPropietario(),
                    FECHA.format(vehiculo.getEntrada())});
        }
        DefaultTableModel modeloCaja = (DefaultTableModel) tablaCaja.getModel();
        modeloCaja.setRowCount(0);
        for (MovimientoCaja movimiento : parqueadero.getCaja()) {
            modeloCaja.addRow(new Object[] {movimiento.getVehiculo().getPlaca(),
                    FECHA.format(movimiento.getVehiculo().getEntrada()), FECHA.format(movimiento.getSalida()),
                    movimiento.getHorasCobradas(), dinero(movimiento.getTotalCentavos())});
        }
        lblResumen.setText("Vehículos dentro: " + modeloVehiculos.getRowCount()
                + "    |    Salidas: " + modeloCaja.getRowCount()
                + "    |    Total en caja: " + dinero(parqueadero.getTotalCajaCentavos()));
    }

    private String detalle(MovimientoCaja movimiento) {
        long segundos = Duration.between(movimiento.getVehiculo().getEntrada(), movimiento.getSalida()).getSeconds();
        return "Placa: " + movimiento.getVehiculo().getPlaca()
                + "\nEntrada: " + FECHA.format(movimiento.getVehiculo().getEntrada())
                + "\nSalida / consulta: " + FECHA.format(movimiento.getSalida())
                + "\nTiempo transcurrido: " + (segundos / 3600) + " h " + ((segundos % 3600) / 60)
                + " min " + (segundos % 60) + " s"
                + "\nHoras cobradas: " + movimiento.getHorasCobradas()
                + "\nTotal a cancelar: " + dinero(movimiento.getTotalCentavos());
    }

    private static String dinero(long centavos) {
        return "$" + (centavos / 100) + "," + String.format(Locale.ROOT, "%02d", centavos % 100);
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Revise los datos", JOptionPane.ERROR_MESSAGE);
    }
}
