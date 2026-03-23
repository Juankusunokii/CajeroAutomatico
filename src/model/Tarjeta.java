package model;

public class Tarjeta {
    private String numero;
    private String numCuenta;
    private String pin;
    private boolean activa;
    private int intentosFallidos;
    private String rol; // CLIENTE, VALORES, ADMIN

    public Tarjeta(String numero, String numCuenta, String pin, String rol) {
        this.numero = numero;
        this.numCuenta = numCuenta;
        this.pin = pin;
        this.activa = true;
        this.intentosFallidos = 0;
        this.rol = rol;
    }

    public boolean validarPin(String pinIngresado) {
        if (!this.activa) return false;
        
        if (this.pin.equals(pinIngresado)) {
            this.intentosFallidos = 0;
            return true;
        } else {
            this.intentosFallidos++;
            if (this.intentosFallidos >= 3) {
                this.activa = false;
            }
            return false;
        }
    }

    public void reiniciarIntentos() {
        this.intentosFallidos = 0;
    }

    public void bloquear() {
        this.activa = false;
    }

    // Getters
    public String getNumero() { return numero; }
    public String getNumCuenta() { return numCuenta; }
    public boolean isActiva() { return activa; }
    public int getIntentosFallidos() { return intentosFallidos; }
    public String getRol() { return rol; }

    // Setters
    public void setActiva(boolean activa) { this.activa = activa; }

    @Override
    public String toString() {
        return numero + " | " + numCuenta + " | " + rol + " | " + (activa ? "Activa" : "Bloqueada");
    }
}