package model;

public class Cuenta {
    private String numero;
    private String idUsuario;
    private double saldo;
    private double limiteDiario;

    public Cuenta(String numero, String idUsuario, double saldo, double limiteDiario) {
        this.numero = numero;
        this.idUsuario = idUsuario;
        this.saldo = saldo;
        this.limiteDiario = limiteDiario;
    }

    public boolean depositar(double monto) {
        if (monto <= 0) return false;
        this.saldo += monto;
        return true;
    }

    public boolean retirar(double monto) {
        if (monto <= 0 || monto > this.saldo) return false;
        this.saldo -= monto;
        return true;
    }

    // Getters
    public String getNumero() { return numero; }
    public String getIdUsuario() { return idUsuario; }
    public double getSaldo() { return saldo; }
    public double getLimiteDiario() { return limiteDiario; }

    // Setters
    public void setSaldo(double saldo) { this.saldo = saldo; }
    public void setLimiteDiario(double limiteDiario) { this.limiteDiario = limiteDiario; }

    @Override
    public String toString() {
        return numero + " | " + idUsuario + " | $" + saldo;
    }
}