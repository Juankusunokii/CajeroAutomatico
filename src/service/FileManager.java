package service;

import model.Usuario;
import model.Cuenta;
import model.Tarjeta;
import model.Movimiento;
import java.io.*;
import java.util.*;

public class FileManager {
    private String rutaBase;

    public FileManager(String rutaBase) {
        this.rutaBase = rutaBase;
        crearCarpetas();
    }

    private void crearCarpetas() {
        new File(rutaBase).mkdirs();
    }

    // Usuarios
    public List<Usuario> cargarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        File file = new File(rutaBase + "/usuarios.txt");
        if (!file.exists()) return lista;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] parts = linea.split("\\|");
                if (parts.length >= 5) {
                    lista.add(new Usuario(parts[0], parts[1], parts[2], parts[3], parts[4]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void guardarUsuarios(List<Usuario> usuarios) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaBase + "/usuarios.txt"))) {
            for (Usuario u : usuarios) {
                pw.println(u.getId() + "|" + u.getNombres() + "|" + u.getDocumento() + "|" + u.getTelefono() + "|" + u.getPassword());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Cuentas
    public List<Cuenta> cargarCuentas() {
        List<Cuenta> lista = new ArrayList<>();
        File file = new File(rutaBase + "/cuentas.txt");
        if (!file.exists()) return lista;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] parts = linea.split("\\|");
                if (parts.length >= 4) {
                    lista.add(new Cuenta(parts[0], parts[1], Double.parseDouble(parts[2]), Double.parseDouble(parts[3])));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void guardarCuentas(List<Cuenta> cuentas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaBase + "/cuentas.txt"))) {
            for (Cuenta c : cuentas) {
                pw.println(c.getNumero() + "|" + c.getIdUsuario() + "|" + c.getSaldo() + "|" + c.getLimiteDiario());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Tarjetas
    public List<Tarjeta> cargarTarjetas() {
        List<Tarjeta> lista = new ArrayList<>();
        File file = new File(rutaBase + "/tarjetas.txt");
        if (!file.exists()) return lista;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] parts = linea.split("\\|");
                if (parts.length >= 5) {
                    Tarjeta t = new Tarjeta(parts[0], parts[1], parts[2], parts[3]);
                    if (!Boolean.parseBoolean(parts[4])) t.bloquear();
                    lista.add(t);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void guardarTarjetas(List<Tarjeta> tarjetas) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaBase + "/tarjetas.txt"))) {
            for (Tarjeta t : tarjetas) {
                pw.println(t.getNumero() + "|" + t.getNumCuenta() + "|" + "****" + "|" + t.getRol() + "|" + t.isActiva());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Movimientos
    public List<Movimiento> cargarMovimientos() {
        List<Movimiento> lista = new ArrayList<>();
        File file = new File(rutaBase + "/movimientos.txt");
        if (!file.exists()) return lista;
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] parts = linea.split("\\|");
                if (parts.length >= 6) {
                    lista.add(new Movimiento(parts[0], parts[1], parts[2], parts[3], Double.parseDouble(parts[4]), parts[5]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void guardarMovimientos(List<Movimiento> movimientos) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(rutaBase + "/movimientos.txt"))) {
            for (Movimiento m : movimientos) {
                pw.println(m.getId() + "|" + m.getTipo() + "|" + m.getCuentaOrigen() + "|" + m.getCuentaDestino() + "|" + m.getMonto() + "|" + m.getDescripcion());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}