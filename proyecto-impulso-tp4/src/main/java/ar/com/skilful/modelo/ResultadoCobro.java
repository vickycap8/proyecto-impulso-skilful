package ar.com.skilful.modelo;

import java.math.BigDecimal;

public class ResultadoCobro {

    private final BigDecimal descuento;
    private final BigDecimal recargo;
    private final BigDecimal importeFinal;
    private final BigDecimal saldoAplicado;
    private final BigDecimal totalAbonar;

    public ResultadoCobro(
            BigDecimal descuento,
            BigDecimal recargo,
            BigDecimal importeFinal,
            BigDecimal saldoAplicado,
            BigDecimal totalAbonar) {

        this.descuento = descuento;
        this.recargo = recargo;
        this.importeFinal = importeFinal;
        this.saldoAplicado = saldoAplicado;
        this.totalAbonar = totalAbonar;
    }

    public BigDecimal getDescuento() { return descuento; }
    public BigDecimal getRecargo() { return recargo; }
    public BigDecimal getImporteFinal() { return importeFinal; }
    public BigDecimal getSaldoAplicado() { return saldoAplicado; }
    public BigDecimal getTotalAbonar() { return totalAbonar; }
}
