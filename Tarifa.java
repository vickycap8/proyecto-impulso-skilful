package ar.com.skilful.modelo;

import java.math.BigDecimal;

public class Tarifa {

    private final int id;
    private final BigDecimal monto;

    public Tarifa(int id, BigDecimal monto) {
        this.id = id;
        this.monto = monto;
    }

    public int getId() { return id; }
    public BigDecimal getMonto() { return monto; }
}
