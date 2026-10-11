package ar.com.skilful.modelo;

import java.math.BigDecimal;

public class IntentoCobro {

    private int idSuscripcion;
    private int idCuota;
    private int numeroIntento;
    private BigDecimal importe;
    private String estado;
    private String observacion;

    public int getIdSuscripcion() { return idSuscripcion; }
    public void setIdSuscripcion(int idSuscripcion) { this.idSuscripcion = idSuscripcion; }
    public int getIdCuota() { return idCuota; }
    public void setIdCuota(int idCuota) { this.idCuota = idCuota; }
    public int getNumeroIntento() { return numeroIntento; }
    public void setNumeroIntento(int numeroIntento) { this.numeroIntento = numeroIntento; }
    public BigDecimal getImporte() { return importe; }
    public void setImporte(BigDecimal importe) { this.importe = importe; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
}
