package ar.com.skilful.modelo;

import java.math.BigDecimal;

public class Pago {

    private int idSocio;
    private int idCuota;
    private int idMedioPago;
    private int idTarifaAplicada;
    private int idUsuario;
    private int idSede;
    private BigDecimal precioOriginal;
    private BigDecimal descuento;
    private BigDecimal recargo;
    private BigDecimal saldoAplicado;
    private BigDecimal importeFinal;
    private BigDecimal importeAbonado;
    private BigDecimal saldoGenerado;

    public int getIdSocio() { return idSocio; }
    public void setIdSocio(int idSocio) { this.idSocio = idSocio; }
    public int getIdCuota() { return idCuota; }
    public void setIdCuota(int idCuota) { this.idCuota = idCuota; }
    public int getIdMedioPago() { return idMedioPago; }
    public void setIdMedioPago(int idMedioPago) { this.idMedioPago = idMedioPago; }
    public int getIdTarifaAplicada() { return idTarifaAplicada; }
    public void setIdTarifaAplicada(int idTarifaAplicada) { this.idTarifaAplicada = idTarifaAplicada; }
    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public int getIdSede() { return idSede; }
    public void setIdSede(int idSede) { this.idSede = idSede; }
    public BigDecimal getPrecioOriginal() { return precioOriginal; }
    public void setPrecioOriginal(BigDecimal precioOriginal) { this.precioOriginal = precioOriginal; }
    public BigDecimal getDescuento() { return descuento; }
    public void setDescuento(BigDecimal descuento) { this.descuento = descuento; }
    public BigDecimal getRecargo() { return recargo; }
    public void setRecargo(BigDecimal recargo) { this.recargo = recargo; }
    public BigDecimal getSaldoAplicado() { return saldoAplicado; }
    public void setSaldoAplicado(BigDecimal saldoAplicado) { this.saldoAplicado = saldoAplicado; }
    public BigDecimal getImporteFinal() { return importeFinal; }
    public void setImporteFinal(BigDecimal importeFinal) { this.importeFinal = importeFinal; }
    public BigDecimal getImporteAbonado() { return importeAbonado; }
    public void setImporteAbonado(BigDecimal importeAbonado) { this.importeAbonado = importeAbonado; }
    public BigDecimal getSaldoGenerado() { return saldoGenerado; }
    public void setSaldoGenerado(BigDecimal saldoGenerado) { this.saldoGenerado = saldoGenerado; }
}
