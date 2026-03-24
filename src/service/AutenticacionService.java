package service;

import model.Tarjeta;
import model.Cuenta;
import model.Session;
import java.util.List;

public class AutenticacionService {
    
    private static final int INTENTOS_MAX = 3;
    
    public Session autenticar(String numTarjeta, String pin, List<Tarjeta> tarjetas, List<Cuenta> cuentas) {
        Tarjeta tarjeta = buscarTarjeta(numTarjeta, tarjetas);
        
        if (tarjeta == null) {
            System.out.println("Tarjeta no reconocida.");
            return null;
        }
        
        if (!tarjeta.isActiva()) {
            System.out.println("Tarjeta bloqueada.");
            return null;
        }
        
        if (!pin.matches("\\d+")) {
            System.out.println("PIN invalido. Solo se permiten numeros.");
            return null;
        }
        
        if (tarjeta.validarPin(pin)) {
            Cuenta cuenta = buscarCuenta(tarjeta.getNumCuenta(), cuentas);
            return new Session(tarjeta, cuenta, tarjeta.getRol());
        } else {
            System.out.println("PIN incorrecto.");
            if (!tarjeta.isActiva()) {
                System.out.println("Tarjeta bloqueada por " + INTENTOS_MAX + " intentos.");
            }
            return null;
        }
    }
    
    private Tarjeta buscarTarjeta(String num, List<Tarjeta> tarjetas) {
        for (Tarjeta t : tarjetas) {
            if (t.getNumero().equals(num)) return t;
        }
        return null;
    }
    
    private Cuenta buscarCuenta(String num, List<Cuenta> cuentas) {
        for (Cuenta c : cuentas) {
            if (c.getNumero().equals(num)) return c;
        }
        return null;
    }
}