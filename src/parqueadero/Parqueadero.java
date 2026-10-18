package parqueadero;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class Parqueadero {
    private static final String CABECERA_VEHICULOS = "id\tplaca\tpropietario\tentrada";
    private static final String CABECERA_CAJA = "idIngreso\tsalida\thorasCobradas\ttotalCentavos";
    private final ArrayList<Vehiculo> vehiculos = new ArrayList<Vehiculo>();
    private final ArrayList<MovimientoCaja> caja = new ArrayList<MovimientoCaja>();
    private final Path archivoVehiculos;
    private final Path archivoCaja;

    public Parqueadero(Path carpeta) throws IOException {
        Files.createDirectories(carpeta);
        archivoVehiculos = carpeta.resolve("vehiculos.txt");
        archivoCaja = carpeta.resolve("caja.txt");
        cargar();
        if (!Files.exists(archivoVehiculos)) guardarVehiculos(vehiculos);
        if (!Files.exists(archivoCaja)) guardarCaja(caja);
    }

    public Vehiculo ingresar(String placa, String propietario, Instant entrada) throws IOException {
        if (buscarActivo(placa) != null) {
            throw new IllegalArgumentException("Esta placa ya está dentro del parqueadero.");
        }
        Vehiculo vehiculo = new Vehiculo(UUID.randomUUID().toString(), placa, propietario, entrada);
        ArrayList<Vehiculo> nuevos = new ArrayList<Vehiculo>(vehiculos);
        nuevos.add(vehiculo);
        guardarVehiculos(nuevos);
        vehiculos.add(vehiculo); 
        return vehiculo;
    }

    public MovimientoCaja calcular(String placa, Instant salida) {
        Vehiculo vehiculo = buscarActivo(placa);
        if (vehiculo == null) {
            throw new IllegalArgumentException("Seleccione un vehículo que esté dentro del parqueadero.");
        }
        return new MovimientoCaja(vehiculo, salida);
    }

    public MovimientoCaja retirar(String placa, Instant salida) throws IOException {
        MovimientoCaja movimiento = calcular(placa, salida);
        ArrayList<MovimientoCaja> nuevos = new ArrayList<MovimientoCaja>(caja);
        nuevos.add(movimiento);
        guardarCaja(nuevos);
        caja.add(movimiento);
        return movimiento;
    }

    public Vehiculo buscarActivo(String placa) {
        String normalizada = Vehiculo.normalizarPlaca(placa);
        for (Vehiculo vehiculo : getActivos()) {
            if (vehiculo.getPlaca().equals(normalizada)) return vehiculo;
        }
        return null;
    }

    public ArrayList<Vehiculo> getActivos() {
        ArrayList<Vehiculo> activos = new ArrayList<Vehiculo>();
        for (Vehiculo vehiculo : vehiculos) {
            if (!tieneSalida(vehiculo.getId())) activos.add(vehiculo);
        }
        return activos;
    }

    public ArrayList<MovimientoCaja> getCaja() {
        return new ArrayList<MovimientoCaja>(caja);
    }

    public long getTotalCajaCentavos() {
        long total = 0;
        for (MovimientoCaja movimiento : caja) total += movimiento.getTotalCentavos();
        return total;
    }

    private boolean tieneSalida(String id) {
        for (MovimientoCaja movimiento : caja) {
            if (movimiento.getVehiculo().getId().equals(id)) return true;
        }
        return false;
    }

    private Vehiculo buscarPorId(String id) {
        for (Vehiculo vehiculo : vehiculos) {
            if (vehiculo.getId().equals(id)) return vehiculo;
        }
        return null;
    }

    private void cargar() throws IOException {
        List<String> lineas = leer(archivoVehiculos, CABECERA_VEHICULOS);
        for (int i = 1; i < lineas.size(); i++) {
            try {
                String[] datos = lineas.get(i).split("\t", -1);
                if (datos.length != 4 || buscarPorId(datos[0]) != null) {
                    throw new IllegalArgumentException("Ingreso inválido o repetido.");
                }
                vehiculos.add(new Vehiculo(datos[0], datos[1], datos[2], Instant.parse(datos[3])));
            } catch (RuntimeException ex) {
                throw new IOException("Error en vehiculos.txt, línea " + (i + 1) + ": " + ex.getMessage(), ex);
            }
        }
        lineas = leer(archivoCaja, CABECERA_CAJA);
        for (int i = 1; i < lineas.size(); i++) {
            try {
                String[] datos = lineas.get(i).split("\t", -1);
                if (datos.length != 4 || tieneSalida(datos[0])) {
                    throw new IllegalArgumentException("Salida inválida o repetida.");
                }
                Vehiculo vehiculo = buscarPorId(datos[0]);
                MovimientoCaja movimiento = new MovimientoCaja(vehiculo, Instant.parse(datos[1]));
                if (movimiento.getHorasCobradas() != Long.parseLong(datos[2])
                        || movimiento.getTotalCentavos() != Long.parseLong(datos[3])) {
                    throw new IllegalArgumentException("El cobro no coincide con la tarifa.");
                }
                caja.add(movimiento);
            } catch (RuntimeException ex) {
                throw new IOException("Error en caja.txt, línea " + (i + 1) + ": " + ex.getMessage(), ex);
            }
        }
        ArrayList<String> placas = new ArrayList<String>();
        for (Vehiculo vehiculo : getActivos()) {
            if (placas.contains(vehiculo.getPlaca())) {
                throw new IOException("Hay dos ingresos activos con la placa " + vehiculo.getPlaca());
            }
            placas.add(vehiculo.getPlaca());
        }
    }

    private List<String> leer(Path archivo, String cabecera) throws IOException {
        if (!Files.exists(archivo)) return new ArrayList<String>();
        List<String> lineas = Files.readAllLines(archivo, StandardCharsets.UTF_8);
        if (lineas.isEmpty() || !lineas.get(0).equals(cabecera)) {
            throw new IOException("Formato incorrecto en " + archivo.getFileName()
                    + ". No se modificaron sus datos.");
        }
        return lineas;
    }

    private void guardarVehiculos(List<Vehiculo> lista) throws IOException {
        List<String> lineas = new ArrayList<String>();
        lineas.add(CABECERA_VEHICULOS);
        for (Vehiculo v : lista) {
            lineas.add(v.getId() + "\t" + v.getPlaca() + "\t" + v.getPropietario() + "\t" + v.getEntrada());
        }
        escribir(archivoVehiculos, lineas);
    }

    private void guardarCaja(List<MovimientoCaja> lista) throws IOException {
        List<String> lineas = new ArrayList<String>();
        lineas.add(CABECERA_CAJA);
        for (MovimientoCaja m : lista) {
            lineas.add(m.getVehiculo().getId() + "\t" + m.getSalida() + "\t"
                    + m.getHorasCobradas() + "\t" + m.getTotalCentavos());
        }
        escribir(archivoCaja, lineas);
    }

    private void escribir(Path archivo, List<String> lineas) throws IOException {
        Path temporal = Files.createTempFile(archivo.toAbsolutePath().getParent(), "parqueadero-", ".tmp");
        try {
            Files.write(temporal, lineas, StandardCharsets.UTF_8);
            try {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
