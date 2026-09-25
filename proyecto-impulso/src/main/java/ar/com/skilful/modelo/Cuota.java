package ar.com.skilful.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Cuota {

    private int id;
    private int idPlan;
    private String nombrePlan;
    private LocalDate periodo;
    private LocalDate fechaVencimiento;
    private String estado;
    private BigDecimal importeOriginal;
    private String beneficioActivo;
    private boolean suscripcionActiva;
    private int intentosSuscripcion;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPlan() {
        return idPlan;
    }

    public void setIdPlan(int idPlan) {
        this.idPlan = idPlan;
    }

    public String getNombrePlan() {
        return nombrePlan;
    }

    public void setNombrePlan(String nombrePlan) {
        this.nombrePlan = nombrePlan;
    }

    public LocalDate getPeriodo() {
        return periodo;
    }

    public void setPeriodo(LocalDate periodo) {
        this.periodo = periodo;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getImporteOriginal() {
        return importeOriginal;
    }

    public void setImporteOriginal(BigDecimal importeOriginal) {
        this.importeOriginal = importeOriginal;
    }

    public String getBeneficioActivo() {
        return beneficioActivo;
    }

    public void setBeneficioActivo(String beneficioActivo) {
        this.beneficioActivo = beneficioActivo;
    }

    public boolean isSuscripcionActiva() {
        return suscripcionActiva;
    }

    public void setSuscripcionActiva(boolean suscripcionActiva) {
        this.suscripcionActiva = suscripcionActiva;
    }

    public int getIntentosSuscripcion() {
        return intentosSuscripcion;
    }

    public void setIntentosSuscripcion(int intentosSuscripcion) {
        this.intentosSuscripcion = intentosSuscripcion;
    }
}
