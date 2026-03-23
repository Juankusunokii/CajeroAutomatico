package model;

public class Usuario {
    private String id;
    private String nombres;
    private String documento;
    private String telefono;
    private String password; // Para admin/valores

    public Usuario(String id, String nombres, String documento, String telefono, String password) {
        this.id = id;
        this.nombres = nombres;
        this.documento = documento;
        this.telefono = telefono;
        this.password = password;
    }

    // Getters
    public String getId() { return id; }
    public String getNombres() { return nombres; }
    public String getDocumento() { return documento; }
    public String getTelefono() { return telefono; }
    public String getPassword() { return password; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public void setDocumento(String documento) { this.documento = documento; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return id + " | " + nombres + " | " + documento;
    }
}