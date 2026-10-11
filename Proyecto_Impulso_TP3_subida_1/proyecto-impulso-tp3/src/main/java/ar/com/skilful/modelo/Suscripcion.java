package ar.com.skilful.modelo;

import java.time.LocalDate;

public class Suscripcion {

    private int id;
    private int idMembresia;
    private LocalDate fechaInicio;
    private LocalDate permanenciaHasta;
    private int diaCobro;
    private String estado;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getIdMembresia() { return idMembresia; }
    public void setIdMembresia(int idMembresia) { this.idMembresia = idMembresia; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDate getPermanenciaHasta() { return permanenciaHasta; }
    public void setPermanenciaHasta(LocalDate permanenciaHasta) { this.permanenciaHasta = permanenciaHasta; }
    public int getDiaCobro() { return diaCobro; }
    public void setDiaCobro(int diaCobro) { this.diaCobro = diaCobro; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
