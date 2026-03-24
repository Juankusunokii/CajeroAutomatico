package main;

import model.*;
import service.*;
import java.io.*;
import java.util.*;

public class main {
    
    private static Scanner scanner = new Scanner(System.in);
    private static FileManager fm = new FileManager("data");
    
    // Datos en memoria
    private static List<Usuario> usuarios = new ArrayList<>();
    private static List<Cuenta> cuentas = new ArrayList<>();
    private static List<Tarjeta> tarjetas = new ArrayList<>();
    private static List<Movimiento> movimientos = new ArrayList<>();
    
    // Servicios
    private static Cajero cajero = new Cajero();
    private static AutenticacionService authService = new AutenticacionService();
    private static TransaccionService transService;
    
    // Contadores
    private static int contadorUsuarios = 1;
    private static int contadorCuentas = 1001;
    private static int contadorTarjetasCliente = 1;
    
    // Sesion actual
    private static Session sessionActual = null;
    
    public static void main(String[] args) {
        System.out.println("=== CAJERO AUTOMATICO ===\n");
        
        new File("data").mkdirs();
        cargarDatos();
        transService = new TransaccionService(cajero, movimientos);
        
        // Crear datos por defecto
        boolean necesitaGuardar = false;
        
        if (tarjetas.stream().noneMatch(t -> t.getNumero().equals("12340001"))) {
            tarjetas.add(new Tarjeta("12340001", "ADMIN", "1234", "ADMIN"));
            necesitaGuardar = true;
        }
        if (tarjetas.stream().noneMatch(t -> t.getNumero().equals("99990001"))) {
            tarjetas.add(new Tarjeta("99990001", "VALORES", "9999", "VALORES"));
            necesitaGuardar = true;
        }
        if (usuarios.stream().noneMatch(u -> u.getId().equals("ADMIN"))) {
            usuarios.add(new Usuario("ADMIN", "Administrador", "000", "000", ""));
            necesitaGuardar = true;
        }
        if (usuarios.stream().noneMatch(u -> u.getId().equals("VALORES"))) {
            usuarios.add(new Usuario("VALORES", "Empresa Valores", "000", "000", ""));
            necesitaGuardar = true;
        }
        
        if (necesitaGuardar) {
            guardarDatos();
        }
        
        while (true) {
            System.out.println("\n=== MENU PRINCIPAL ===");
            System.out.println("1. Consignar dinero (sin tarjeta)");
            System.out.println("2. Insertar tarjeta");
            System.out.println("3. Salir");
            System.out.print("Opcion: ");
            
            int opcion = leerInt();
            
            if (opcion == 3) {
                System.out.println("Gracias. Hasta luego!");
                break;
            } else if (opcion == 1) {
                consignarSinTarjeta();
            } else if (opcion == 2) {
                autenticar();
            } else {
                System.out.println("Opcion invalida.");
            }
        }
    }
    
    private static void consignarSinTarjeta() {
        System.out.println("\n--- CONSIGNACION SIN TARJETA ---");
        System.out.print("Ingrese numero de cuenta destino: ");
        String numCuenta = scanner.nextLine();
        
        System.out.println("\nIngrese los billetes (100000, 50000, 20000, 10000):");
        Map<Integer, Integer> billetes = new LinkedHashMap<>();
        int[] denoms = {100000, 50000, 20000, 10000};
        
        for (int d : denoms) {
            System.out.print("Billetes de $" + d + ": ");
            int cant = leerInt();
            if (cant < 0) cant = 0;
            billetes.put(d, cant);
        }
        
        transService.consignar(numCuenta, billetes, cuentas);
        guardarDatos();
    }
    
    private static void autenticar() {
        System.out.print("\nInserte su tarjeta (0 para cancelar): ");
        String numTarjeta = scanner.nextLine();
        if (numTarjeta.equals("0")) return;
        
        System.out.print("PIN: ");
        String pin = scanner.nextLine();
        
        sessionActual = authService.autenticar(numTarjeta, pin, tarjetas, cuentas);
        
        if (sessionActual != null) {
            System.out.println("\n--- BIENVENIDO ---");
            System.out.println("Rol: " + sessionActual.getRol());
            
            if (sessionActual.getRol().equals("ADMIN")) {
                menuAdmin();
            } else if (sessionActual.getRol().equals("VALORES")) {
                menuValores();
            } else {
                menuCliente();
            }
            
            sessionActual.cerrarSesion();
            sessionActual = null;
            System.out.println("\nSesion cerrada.");
            guardarDatos();
        }
    }
    
    private static void menuCliente() {
        while (true) {
            System.out.println("\n1.Saldo  2.Retirar  3.Transferir  4.Movimientos  5.Salir");
            System.out.print("Opcion: ");
            int op = leerInt();
            if (op == 5) break;
            
            switch (op) {
                case 1:
                    System.out.println("Saldo: $" + transService.consultarSaldo(sessionActual.getCuenta()));
                    break;
                case 2:
                    System.out.print("Monto: $");
                    int monto = leerInt();
                    transService.retirar(sessionActual, monto);
                    break;
                case 3:
                    System.out.print("Cuenta destino: ");
                    String destino = scanner.nextLine();
                    System.out.print("Monto: $");
                    double montoTrans = leerDouble();
                    transService.transferir(sessionActual, destino, montoTrans, cuentas);
                    break;
                case 4:
                    System.out.println("\n--- MOVIMIENTOS ---");
                    List<Movimiento> movs = transService.consultarMovimientos(sessionActual.getCuenta());
                    if (movs.isEmpty()) {
                        System.out.println("No hay movimientos registrados.");
                    } else {
                        for (Movimiento m : movs) System.out.println(m);
                    }
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }
    
    private static void menuValores() {
        while (true) {
            System.out.println("\n1.Estado cajero  2.Abastecer  3.Retirar excedentes  4.Historial  5.Salir");
            System.out.print("Opcion: ");
            int op = leerInt();
            if (op == 5) break;
            
            switch (op) {
                case 1:
                    System.out.println("=== ESTADO DEL CAJERO ===");
                    System.out.print(transService.mostrarEstadoCajero());
                    break;
                case 2:
                    System.out.println("\n--- ABASTECER CAJERO ---");
                    Map<Integer, Integer> billetes = new LinkedHashMap<>();
                    int[] denoms = {100000, 50000, 20000, 10000};
                    int total = 0;
                    for (int d : denoms) {
                        System.out.print("Billetes de $" + d + ": ");
                        int cnt = leerInt();
                        if (cnt < 0) cnt = 0;
                        billetes.put(d, cnt);
                        total += d * cnt;
                    }
                    if (total > 0 && transService.abastecer(billetes)) {
                        movimientos.add(new Movimiento("M" + (movimientos.size() + 1), "ABASTECIMIENTO", "", "", total, "Abastecimiento por Valores"));
                        System.out.println("Abastecimiento de $" + total + " registrado.");
                    }
                    break;
                case 3:
                    System.out.print("Monto a retirar: $");
                    int monto = leerInt();
                    if (monto <= 0 || monto % 10000 != 0) {
                        System.out.println("Monto invalido. Debe ser multiplo de 10000.");
                    } else if (transService.retirarExcedentes(monto)) {
                        movimientos.add(new Movimiento("M" + (movimientos.size() + 1), "RETIRO_EXCEDENTES", "", "", monto, "Retiro de excedentes"));
                        System.out.println("Retiro de excedentes por $" + monto + " registrado.");
                    }
                    break;
                case 4:
                    System.out.println("\n--- HISTORIAL DE ABASTECIMIENTOS ---");
                    boolean hay = false;
                    for (Movimiento m : movimientos) {
                        if (m.getTipo().equals("ABASTECIMIENTO") || m.getTipo().equals("RETIRO_EXCEDENTES")) {
                            System.out.println(m);
                            hay = true;
                        }
                    }
                    if (!hay) System.out.println("No hay registros.");
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }
    
    private static void menuAdmin() {
        while (true) {
            System.out.println("\n=== ADMINISTRACION ===");
            System.out.println("1.Crear usuario");
            System.out.println("2.Crear cuenta para usuario existente");
            System.out.println("3.Crear tarjeta para cuenta existente");
            System.out.println("4.Listar todo");
            System.out.println("5.Modificar cuenta (limite diario)");
            System.out.println("6.Activar/Desactivar tarjeta");
            System.out.println("7.Eliminar usuario (elimina todas sus cuentas y tarjetas)");
            System.out.println("8.Eliminar cuenta");
            System.out.println("9.Eliminar tarjeta");
            System.out.println("10.Salir");
            System.out.print("Opcion: ");
            int op = leerInt();
            if (op == 10) break;
            
            switch (op) {
                case 1:
                    String id = "C" + String.format("%03d", contadorUsuarios++);
                    System.out.print("Nombre: ");
                    String nombre = scanner.nextLine();
                    System.out.print("Documento: ");
                    String doc = scanner.nextLine();
                    System.out.print("Telefono: ");
                    String tel = scanner.nextLine();
                    usuarios.add(new Usuario(id, nombre, doc, tel, ""));
                    System.out.println("Usuario creado con ID: " + id);
                    break;
                    
                case 2:
                    System.out.print("ID de usuario: ");
                    String idUsuario = scanner.nextLine();
                    if (buscarUsuario(idUsuario) == null) {
                        System.out.println("Error: Usuario no existe.");
                        break;
                    }
                    String numCuenta = String.valueOf(contadorCuentas++);
                    System.out.print("Saldo inicial: $");
                    double saldo = leerDouble();
                    System.out.print("Limite diario: $");
                    double limite = leerDouble();
                    cuentas.add(new Cuenta(numCuenta, idUsuario, saldo, limite));
                    System.out.println("Cuenta creada con numero: " + numCuenta);
                    
                    System.out.print("\n¿Desea crear tarjeta? (s/n): ");
                    if (scanner.nextLine().equalsIgnoreCase("s")) {
                        String numTarjeta = String.format("%08d", contadorTarjetasCliente++);
                        System.out.print("PIN (max 4 digitos): ");
                        String pin = scanner.nextLine();
                        if (pin.length() > 4 || !pin.matches("\\d+")) {
                            System.out.println("Error: PIN invalido.");
                            contadorTarjetasCliente--;
                        } else {
                            tarjetas.add(new Tarjeta(numTarjeta, numCuenta, pin, "CLIENTE"));
                            System.out.println("Tarjeta creada: " + numTarjeta);
                        }
                    }
                    break;
                    
                case 3:
                    System.out.print("Numero cuenta: ");
                    String numCta = scanner.nextLine();
                    if (buscarCuenta(numCta) == null) {
                        System.out.println("Error: Cuenta no existe.");
                        break;
                    }
                    String numTarjeta = String.format("%08d", contadorTarjetasCliente++);
                    System.out.print("PIN (max 4 digitos): ");
                    String pin = scanner.nextLine();
                    if (pin.length() > 4 || !pin.matches("\\d+")) {
                        System.out.println("Error: PIN invalido.");
                        contadorTarjetasCliente--;
                    } else {
                        tarjetas.add(new Tarjeta(numTarjeta, numCta, pin, "CLIENTE"));
                        System.out.println("Tarjeta creada: " + numTarjeta);
                    }
                    break;
                    
                case 4:
                    System.out.println("\n=== USUARIOS ===");
                    for (Usuario u : usuarios) System.out.println(u);
                    System.out.println("\n=== CUENTAS ===");
                    for (Cuenta c : cuentas) System.out.println(c);
                    System.out.println("\n=== TARJETAS ===");
                    for (Tarjeta t : tarjetas) System.out.println(t);
                    break;
                    
                case 5:
                    System.out.print("Numero de cuenta: ");
                    Cuenta c = buscarCuenta(scanner.nextLine());
                    if (c == null) System.out.println("Cuenta no encontrada.");
                    else {
                        System.out.print("Nuevo limite diario: $");
                        c.setLimiteDiario(leerDouble());
                        System.out.println("Limite actualizado.");
                    }
                    break;
                    
                case 6:
                    System.out.print("Numero de tarjeta: ");
                    Tarjeta t = buscarTarjeta(scanner.nextLine());
                    if (t == null) System.out.println("Tarjeta no encontrada.");
                    else {
                        t.setActiva(!t.isActiva());
                        System.out.println("Tarjeta " + (t.isActiva() ? "activada" : "desactivada"));
                    }
                    break;
                    
                case 7:
                    System.out.print("ID de usuario: ");
                    String idEliminar = scanner.nextLine();
                    Usuario u = buscarUsuario(idEliminar);
                    if (u == null) System.out.println("Usuario no encontrado.");
                    else if (idEliminar.equals("ADMIN") || idEliminar.equals("VALORES")) {
                        System.out.println("No se puede eliminar.");
                    } else {
                        List<Cuenta> cuentasUsuario = new ArrayList<>();
                        for (Cuenta cta : cuentas) {
                            if (cta.getIdUsuario().equals(idEliminar)) cuentasUsuario.add(cta);
                        }
                        if (cuentasUsuario.isEmpty()) {
                            usuarios.remove(u);
                            System.out.println("Usuario eliminado.");
                        } else {
                            System.out.println("El usuario tiene " + cuentasUsuario.size() + " cuenta(s).");
                            System.out.print("¿Eliminar todo? (s/n): ");
                            if (scanner.nextLine().equalsIgnoreCase("s")) {
                                for (Cuenta cta : cuentasUsuario) {
                                    tarjetas.removeIf(tar -> tar.getNumCuenta().equals(cta.getNumero()));
                                }
                                cuentas.removeAll(cuentasUsuario);
                                usuarios.remove(u);
                                System.out.println("Usuario y cuentas eliminados.");
                            }
                        }
                    }
                    break;
                    
                case 8:
                    System.out.print("Numero de cuenta: ");
                    Cuenta cuentaEliminar = buscarCuenta(scanner.nextLine());
                    if (cuentaEliminar == null) System.out.println("Cuenta no encontrada.");
                    else {
                        if (tarjetas.stream().anyMatch(tar -> tar.getNumCuenta().equals(cuentaEliminar.getNumero()))) {
                            System.out.println("Error: Cuenta tiene tarjeta asociada.");
                        } else {
                            cuentas.remove(cuentaEliminar);
                            System.out.println("Cuenta eliminada.");
                        }
                    }
                    break;
                    
                case 9:
                    System.out.print("Numero de tarjeta: ");
                    Tarjeta tarjetaEliminar = buscarTarjeta(scanner.nextLine());
                    if (tarjetaEliminar == null) System.out.println("Tarjeta no encontrada.");
                    else if (tarjetaEliminar.getNumero().equals("12340001") || tarjetaEliminar.getNumero().equals("99990001")) {
                        System.out.println("No se puede eliminar tarjeta maestra.");
                    } else {
                        tarjetas.remove(tarjetaEliminar);
                        System.out.println("Tarjeta eliminada.");
                    }
                    break;
                    
                default:
                    System.out.println("Opcion invalida.");
            }
            guardarDatos();
        }
    }
    
    // ==================== UTILIDADES ====================
    
    private static Tarjeta buscarTarjeta(String num) {
        for (Tarjeta t : tarjetas) if (t.getNumero().equals(num)) return t;
        return null;
    }
    
    private static Cuenta buscarCuenta(String num) {
        for (Cuenta c : cuentas) if (c.getNumero().equals(num)) return c;
        return null;
    }
    
    private static Usuario buscarUsuario(String id) {
        for (Usuario u : usuarios) if (u.getId().equals(id)) return u;
        return null;
    }
    
    private static int leerInt() {
        try { return Integer.parseInt(scanner.nextLine()); }
        catch (Exception e) { return -1; }
    }
    
    private static double leerDouble() {
        try { return Double.parseDouble(scanner.nextLine()); }
        catch (Exception e) { return 0; }
    }
    
    private static void cargarDatos() {
        usuarios = fm.cargarUsuarios();
        cuentas = fm.cargarCuentas();
        tarjetas = fm.cargarTarjetas();
        movimientos = fm.cargarMovimientos();
        
        for (Usuario u : usuarios) {
            if (u.getId().matches("C\\d+")) {
                int num = Integer.parseInt(u.getId().substring(1));
                if (num >= contadorUsuarios) contadorUsuarios = num + 1;
            }
        }
        for (Cuenta c : cuentas) {
            int num = Integer.parseInt(c.getNumero());
            if (num >= contadorCuentas) contadorCuentas = num + 1;
        }
        for (Tarjeta t : tarjetas) {
            if (t.getNumero().matches("0000\\d+") && !t.getRol().equals("ADMIN") && !t.getRol().equals("VALORES")) {
                int num = Integer.parseInt(t.getNumero());
                if (num >= contadorTarjetasCliente) contadorTarjetasCliente = num + 1;
            }
        }
    }
    
    private static void guardarDatos() {
        fm.guardarUsuarios(usuarios);
        fm.guardarCuentas(cuentas);
        fm.guardarTarjetas(tarjetas);
        fm.guardarMovimientos(movimientos);
    }
}