package parqueadero;

import java.time.Duration;
import java.time.Instant;


public class MovimientoCaja {
    public static final long TARIFA_CENTAVOS = 50;
    private final Vehiculo vehiculo;
    private final Instant salida;
    private final long horasCobradas;
    private final long totalCentavos;

    public MovimientoCaja(Vehiculo vehiculo, Instant salida) {
        if (vehiculo == null || salida == null || salida.isBefore(vehiculo.getEntrada())) {
            throw new IllegalArgumentException("La salida no puede ser anterior a la entrada.");
        }
        this.vehiculo = vehiculo;
        this.salida = salida;
        Duration tiempo = Duration.between(vehiculo.getEntrada(), salida);
        long horas = tiempo.getSeconds() / 3600;
        if (tiempo.getSeconds() % 3600 != 0 || tiempo.getNano() != 0) {
            horas++;
        }
        
        horasCobradas = Math.max(1, horas);
        totalCentavos = Math.multiplyExact(horasCobradas, TARIFA_CENTAVOS);
    }

    public Vehiculo getVehiculo() { return vehiculo; }
    public Instant getSalida() { return salida; }
    public long getHorasCobradas() { return horasCobradas; }
    public long getTotalCentavos() { return totalCentavos; }
}
