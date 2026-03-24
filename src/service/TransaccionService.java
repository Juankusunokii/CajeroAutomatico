package service;

import model.Cuenta;
import model.Movimiento;
import model.Cajero;
import model.Session;
import java.util.*;

public class TransaccionService {
    
    private Cajero cajero;
    private List<Movimiento> movimientos;
    private int contadorMovimientos;
    
    public TransaccionService(Cajero cajero, List<Movimiento> movimientos) {
        this.cajero = cajero;
        this.movimientos = movimientos;
        this.contadorMovimientos = movimientos.size() + 1;
    }
    
    public double consultarSaldo(Cuenta cuenta) {
        return cuenta.getSaldo();
    }
    
    public boolean retirar(Session session, int monto) {
        if (session == null || !session.isActiva()) {
            System.out.println("Sesion no activa.");
            return false;
        }
        
        Cuenta cuenta = session.getCuenta();
        
        if (monto <= 0 || monto % 10000 != 0) {
            System.out.println("Monto invalido. Debe ser multiplo de 10000.");
            return false;
        }
        
        if (monto > cuenta.getSaldo()) {
            System.out.println("Saldo insuficiente.");
            return false;
        }
        
        if (monto > cuenta.getLimiteDiario()) {
            System.out.println("Excede limite diario de retiro.");
            return false;
        }
        
        if (monto > cajero.getTotalEfectivo()) {
            System.out.println("Cajero no tiene suficiente efectivo disponible.");
            return false;
        }
        
        if (cajero.calcularRetiro(monto) == null) {
            System.out.println("No es posible dispensar ese monto con las denominaciones disponibles.");
            return false;
        }
        
        if (!cajero.retirar(monto)) {
            System.out.println("Error en el retiro del cajero.");
            return false;
        }
        
        cuenta.retirar(monto);
        movimientos.add(new Movimiento("M" + contadorMovimientos++, "RETIRO", 
                        cuenta.getNumero(), "", monto, "Retiro en cajero"));
        System.out.println("Retiro exitoso. Nuevo saldo: $" + cuenta.getSaldo());
        return true;
    }
    
    public boolean consignar(String numCuenta, Map<Integer, Integer> billetes, List<Cuenta> cuentas) {
        Cuenta cuenta = buscarCuenta(numCuenta, cuentas);
        
        if (cuenta == null) {
            System.out.println("Error: Cuenta no existe.");
            return false;
        }
        
        int total = billetes.entrySet().stream().mapToInt(e -> e.getKey() * e.getValue()).sum();
        if (total <= 0) {
            System.out.println("Error: No ingreso ningun billete valido.");
            return false;
        }
        
        if (!cajero.consignar(billetes)) {
            System.out.println("Error: El cajero se encuentra a su capacidad maxima. Vuelva despues.");
            return false;
        }
        
        cuenta.depositar(total);
        movimientos.add(new Movimiento("M" + contadorMovimientos++, "CONSIGNACION", 
                        "", numCuenta, total, "Consignacion en cajero"));
        
        System.out.println("\n=== CONSIGNACION EXITOSA ===");
        System.out.println("Cuenta destino: " + numCuenta);
        System.out.println("Monto consignado: $" + total);
        return true;
    }
    
    public boolean transferir(Session session, String cuentaDestino, double monto, List<Cuenta> cuentas) {
        if (session == null || !session.isActiva()) {
            System.out.println("Sesion no activa.");
            return false;
        }
        
        Cuenta cuentaOrigen = session.getCuenta();
        Cuenta destino = buscarCuenta(cuentaDestino, cuentas);
        
        if (destino == null) {
            System.out.println("Cuenta destino no existe.");
            return false;
        }
        
        if (monto > cuentaOrigen.getSaldo()) {
            System.out.println("Saldo insuficiente.");
            return false;
        }
        
        cuentaOrigen.retirar(monto);
        destino.depositar(monto);
        movimientos.add(new Movimiento("M" + contadorMovimientos++, "TRANSFERENCIA", 
                        cuentaOrigen.getNumero(), cuentaDestino, monto, "Transferencia a " + cuentaDestino));
        System.out.println("Transferencia exitosa.");
        return true;
    }
    
    public List<Movimiento> consultarMovimientos(Cuenta cuenta) {
        List<Movimiento> resultado = new ArrayList<>();
        for (Movimiento m : movimientos) {
            if (m.getCuentaOrigen().equals(cuenta.getNumero()) || m.getCuentaDestino().equals(cuenta.getNumero())) {
                resultado.add(m);
            }
        }
        return resultado;
    }
    
    private Cuenta buscarCuenta(String num, List<Cuenta> cuentas) {
        for (Cuenta c : cuentas) {
            if (c.getNumero().equals(num)) return c;
        }
        return null;
    }
    
    public String mostrarEstadoCajero() {
        return cajero.mostrarEstado();
    }
    
    public boolean abastecer(Map<Integer, Integer> billetes) {
        return cajero.consignar(billetes);
    }
    
    public boolean retirarExcedentes(int monto) {
        return cajero.retirar(monto);
    }
}