package model;

import java.util.Date;

public class Movimiento {
    private String id;
    private Date fecha;
    private String tipo;
    private String cuentaOrigen;
    private String cuentaDestino;
    private double monto;
    private String descripcion;

    public Movimiento(String id, String tipo, String cuentaOrigen, String cuentaDestino, double monto, String descripcion) {
        this.id = id;
        this.fecha = new Date();
        this.tipo = tipo;
        this.cuentaOrigen = cuentaOrigen;
        this.cuentaDestino = cuentaDestino;
        this.monto = monto;
        this.descripcion = descripcion;
    }

    // Getters
    public String getId() { return id; }
    public Date getFecha() { return fecha; }
    public String getTipo() { return tipo; }
    public String getCuentaOrigen() { return cuentaOrigen; }
    public String getCuentaDestino() { return cuentaDestino; }
    public double getMonto() { return monto; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() {
        return fecha + " | " + tipo + " | $" + monto + " | " + descripcion;
    }
}