package model;

public class Denominacion {
    private int valor;
    private int cantidad;

    public Denominacion(int valor, int cantidad) {
        this.valor = valor;
        this.cantidad = cantidad;
    }

    public int getValor() {
        return valor;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void agregar(int cantidad) {
        this.cantidad += cantidad;
    }

    public boolean retirar(int cantidad) {
        if (cantidad <= this.cantidad) {
            this.cantidad -= cantidad;
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "$" + valor + ": " + cantidad + " billetes";
    }
}