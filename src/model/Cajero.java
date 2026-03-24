package model;

import java.util.*;

public class Cajero {
    
    private static final int[] DENOMINACIONES = {100000, 50000, 20000, 10000};
    private static final int CAPACIDAD_MAXIMA = 50000000;
    private Map<Integer, Integer> denominaciones;
    
    public Cajero() {
        denominaciones = new LinkedHashMap<>();
        denominaciones.put(100000, 50);
        denominaciones.put(50000, 100);
        denominaciones.put(20000, 200);
        denominaciones.put(10000, 500);
    }
    
    public int getTotalEfectivo() {
        return denominaciones.entrySet().stream()
                .mapToInt(e -> e.getKey() * e.getValue())
                .sum();
    }
    
    public int getCapacidadMaxima() {
        return CAPACIDAD_MAXIMA;
    }
    
    public Map<Integer, Integer> getDenominaciones() {
        return new LinkedHashMap<>(denominaciones);
    }
    
    public Map<Integer, Integer> calcularRetiro(int monto) {
        if (monto <= 0 || monto % 10000 != 0 || monto > getTotalEfectivo()) return null;
        
        int restante = monto;
        Map<Integer, Integer> resultado = new LinkedHashMap<>();
        
        for (int d : DENOMINACIONES) {
            int disponibles = denominaciones.getOrDefault(d, 0);
            int usar = Math.min(disponibles, restante / d);
            if (usar > 0) {
                resultado.put(d, usar);
                restante -= usar * d;
            }
        }
        
        return restante == 0 ? resultado : null;
    }
    
    public boolean retirar(int monto) {
        Map<Integer, Integer> retiro = calcularRetiro(monto);
        if (retiro == null) return false;
        
        for (Map.Entry<Integer, Integer> entry : retiro.entrySet()) {
            int den = entry.getKey();
            int cant = entry.getValue();
            denominaciones.put(den, denominaciones.get(den) - cant);
        }
        return true;
    }
    
    public boolean consignar(Map<Integer, Integer> ingresos) {
        int total = ingresos.entrySet().stream().mapToInt(e -> e.getKey() * e.getValue()).sum();
        if (total <= 0) return false;
        
        int totalActual = getTotalEfectivo();
        if (totalActual + total > CAPACIDAD_MAXIMA) return false;
        
        for (Map.Entry<Integer, Integer> entry : ingresos.entrySet()) {
            int den = entry.getKey();
            int cant = entry.getValue();
            if (!denominaciones.containsKey(den) || cant < 0) return false;
            denominaciones.put(den, denominaciones.get(den) + cant);
        }
        return true;
    }
    
    public String mostrarEstado() {
        StringBuilder sb = new StringBuilder();
        sb.append("Disponible en cajero: $").append(getTotalEfectivo());
        sb.append(" / Capacidad maxima: $").append(CAPACIDAD_MAXIMA).append("\n");
        for (int d : DENOMINACIONES) {
            sb.append("  ").append(d).append(": ").append(denominaciones.getOrDefault(d, 0)).append(" billetes\n");
        }
        return sb.toString();
    }
}