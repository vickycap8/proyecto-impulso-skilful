package ar.com.skilful.modelo;

import java.math.BigDecimal;

public class MedioPago {

    private final int id;
    private final String nombre;
    private final BigDecimal recargoPorcentaje;
    private final boolean admiteBeneficio;

    public MedioPago(
            int id,
            String nombre,
            BigDecimal recargoPorcentaje,
            boolean admiteBeneficio) {

        this.id = id;
        this.nombre = nombre;
        this.recargoPorcentaje = recargoPorcentaje;
        this.admiteBeneficio = admiteBeneficio;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public BigDecimal getRecargoPorcentaje() {
        return recargoPorcentaje;
    }

    public boolean isAdmiteBeneficio() {
        return admiteBeneficio;
    }

    @Override
    public String toString() {
        return nombre.replace("_", " ");
    }
}
