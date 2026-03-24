package model;

public class Session {
    
    private String id;
    private Tarjeta tarjeta;
    private Cuenta cuenta;
    private String rol;
    private boolean activa;
    private static int contadorId = 1;
    
    public Session(Tarjeta tarjeta, Cuenta cuenta, String rol) {
        this.id = "SES" + (contadorId++);
        this.tarjeta = tarjeta;
        this.cuenta = cuenta;
        this.rol = rol;
        this.activa = true;
    }
    
    public void cerrarSesion() {
        this.activa = false;
    }
    
    // Getters
    public String getId() { return id; }
    public Tarjeta getTarjeta() { return tarjeta; }
    public Cuenta getCuenta() { return cuenta; }
    public String getRol() { return rol; }
    public boolean isActiva() { return activa; }
    
    @Override
    public String toString() {
        return "Session{" +
                "id='" + id + '\'' +
                ", tarjeta=" + (tarjeta != null ? tarjeta.getNumero() : "null") +
                ", cuenta=" + (cuenta != null ? cuenta.getNumero() : "null") +
                ", rol='" + rol + '\'' +
                ", activa=" + activa +
                '}';
    }
}