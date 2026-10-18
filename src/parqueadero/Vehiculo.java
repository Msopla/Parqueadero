package parqueadero;

import java.time.Instant;
import java.util.Locale;


public class Vehiculo {
    private final String id;
    private final String placa;
    private final String propietario;
    private final Instant entrada;

    public Vehiculo(String id, String placa, String propietario, Instant entrada) {
        if (id == null || id.trim().isEmpty() || id.matches(".*[\\t\\r\\n].*")) {
            throw new IllegalArgumentException("Identificador inválido.");
        }
        String normalizada = normalizarPlaca(placa);
        if (!normalizada.matches("[A-Z0-9]+(?:-[A-Z0-9]+)*") || normalizada.length() > 12) {
            throw new IllegalArgumentException("Ingrese una placa de hasta 12 letras, números o guiones.");
        }
        if (propietario == null || propietario.trim().length() > 80
                || propietario.matches("(?s).*[\\t\\r\\n].*")) {
            throw new IllegalArgumentException("El propietario debe tener hasta 80 caracteres.");
        }
        if (entrada == null) {
            throw new IllegalArgumentException("Falta la fecha de entrada.");
        }
        this.id = id;
        this.placa = normalizada;
        this.propietario = propietario.trim();
        this.entrada = entrada;
    }

    public static String normalizarPlaca(String placa) {
        return placa == null ? "" : placa.trim().toUpperCase(Locale.ROOT);
    }

    public String getId() { return id; }
    public String getPlaca() { return placa; }
    public String getPropietario() { return propietario; }
    public Instant getEntrada() { return entrada; }
}
