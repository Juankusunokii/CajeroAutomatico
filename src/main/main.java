package main;

import model.Usuario;
import model.Cuenta;
import model.Tarjeta;
import model.Movimiento;
import service.FileManager;
import java.util.*;

public class Main {
    
    private static Scanner scanner = new Scanner(System.in);
    private static FileManager fm = new FileManager("data");
    
    // Datos en memoria
    private static List<Usuario> usuarios = new ArrayList<>();
    private static List<Cuenta> cuentas = new ArrayList<>();
    private static List<Tarjeta> tarjetas = new ArrayList<>();
    private static List<Movimiento> movimientos = new ArrayList<>();
    
    // Sesion actual
    private static Tarjeta tarjetaActual = null;
    private static Cuenta cuentaActual = null;
    private static int contadorMovimientos = 1;
    
    public static void main(String[] args) {
        System.out.println("=== CAJERO AUTOMATICO ===\n");
        cargarDatos();
        
        // Crear admin por defecto si no existe
        if (usuarios.stream().noneMatch(u -> u.getId().equals("ADMIN"))) {
            usuarios.add(new Usuario("ADMIN", "Administrador", "000", "000", "admin123"));
            tarjetas.add(new Tarjeta("12340001", "ADMIN", "1234", "ADMIN"));
            guardarDatos();
        }
        
        // Crear valores por defecto si no existe
        if (usuarios.stream().noneMatch(u -> u.getId().equals("VALORES"))) {
            usuarios.add(new Usuario("VALORES", "Empresa Valores", "000", "000", "valores123"));
            tarjetas.add(new Tarjeta("99990001", "VALORES", "9999", "VALORES"));
            guardarDatos();
        }
        
        while (true) {
            System.out.print("\nInserte su tarjeta (0 para salir): ");
            String numTarjeta = scanner.nextLine();
            
            if (numTarjeta.equals("0")) {
                System.out.println("Gracias. Hasta luego!");
                break;
            }
            
            Tarjeta tarjeta = buscarTarjeta(numTarjeta);
            if (tarjeta == null) {
                System.out.println("Tarjeta no reconocida.");
                continue;
            }
            
            if (!tarjeta.isActiva()) {
                System.out.println("Tarjeta bloqueada.");
                continue;
            }
            
            System.out.print("PIN: ");
            String pin = scanner.nextLine();
            
            if (tarjeta.validarPin(pin)) {
                tarjetaActual = tarjeta;
                cuentaActual = buscarCuenta(tarjeta.getNumCuenta());
                
                System.out.println("\n--- BIENVENIDO ---");
                System.out.println("Rol: " + tarjeta.getRol());
                
                if (tarjeta.getRol().equals("ADMIN")) {
                    menuAdmin();
                } else if (tarjeta.getRol().equals("VALORES")) {
                    menuValores();
                } else {
                    menuCliente();
                }
                
                System.out.println("\nSesion cerrada.");
                guardarDatos();
                tarjetaActual = null;
                cuentaActual = null;
            } else {
                System.out.println("PIN incorrecto.");
                if (!tarjeta.isActiva()) {
                    System.out.println("Tarjeta bloqueada por 3 intentos.");
                    guardarDatos();
                }
            }
        }
    }
    
    // ==================== MENUS ====================
    
    private static void menuCliente() {
        while (true) {
            System.out.println("\n1.Saldo  2.Retirar  3.Consignar  4.Transferir  5.Movimientos  6.Salir");
            System.out.print("Opcion: ");
            int op = leerInt();
            
            if (op == 6) break;
            
            switch (op) {
                case 1:
                    System.out.println("Saldo: $" + cuentaActual.getSaldo());
                    break;
                case 2:
                    System.out.print("Monto: $");
                    double monto = leerDouble();
                    if (monto <= 0) {
                        System.out.println("Monto invalido.");
                    } else if (monto > cuentaActual.getSaldo()) {
                        System.out.println("Saldo insuficiente.");
                    } else if (monto > cuentaActual.getLimiteDiario()) {
                        System.out.println("Excede limite diario de retiro.");
                    } else {
                        cuentaActual.retirar(monto);
                        movimientos.add(new Movimiento("M" + contadorMovimientos++, "RETIRO", cuentaActual.getNumero(), "", monto, "Retiro en cajero"));
                        System.out.println("Retiro exitoso. Nuevo saldo: $" + cuentaActual.getSaldo());
                    }
                    break;
                case 3:
                    System.out.print("Monto: $");
                    double consignar = leerDouble();
                    if (consignar <= 0) {
                        System.out.println("Monto invalido.");
                    } else {
                        cuentaActual.depositar(consignar);
                        movimientos.add(new Movimiento("M" + contadorMovimientos++, "CONSIGNACION", "", cuentaActual.getNumero(), consignar, "Consignacion en cajero"));
                        System.out.println("Consignacion exitosa. Nuevo saldo: $" + cuentaActual.getSaldo());
                    }
                    break;
                case 4:
                    System.out.print("Cuenta destino: ");
                    String destino = scanner.nextLine();
                    System.out.print("Monto: $");
                    double transferir = leerDouble();
                    Cuenta cuentaDestino = buscarCuenta(destino);
                    if (cuentaDestino == null) {
                        System.out.println("Cuenta destino no existe.");
                    } else if (transferir > cuentaActual.getSaldo()) {
                        System.out.println("Saldo insuficiente.");
                    } else {
                        cuentaActual.retirar(transferir);
                        cuentaDestino.depositar(transferir);
                        movimientos.add(new Movimiento("M" + contadorMovimientos++, "TRANSFERENCIA", cuentaActual.getNumero(), destino, transferir, "Transferencia a " + destino));
                        System.out.println("Transferencia exitosa.");
                    }
                    break;
                case 5:
                    System.out.println("\n--- MOVIMIENTOS ---");
                    for (Movimiento m : movimientos) {
                        if (m.getCuentaOrigen().equals(cuentaActual.getNumero()) || m.getCuentaDestino().equals(cuentaActual.getNumero())) {
                            System.out.println(m);
                        }
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
                    System.out.println("Efectivo disponible: $10,000,000");
                    System.out.println("Capacidad maxima: $50,000,000");
                    break;
                case 2:
                    System.out.print("Monto a abastecer: $");
                    double abasto = leerDouble();
                    movimientos.add(new Movimiento("M" + contadorMovimientos++, "ABASTECIMIENTO", "", "", abasto, "Abastecimiento por Valores"));
                    System.out.println("Abastecimiento de $" + abasto + " registrado.");
                    break;
                case 3:
                    System.out.print("Monto a retirar: $");
                    double retiro = leerDouble();
                    movimientos.add(new Movimiento("M" + contadorMovimientos++, "RETIRO_EXCEDENTES", "", "", retiro, "Retiro de excedentes"));
                    System.out.println("Retiro de excedentes por $" + retiro + " registrado.");
                    break;
                case 4:
                    System.out.println("\n--- HISTORIAL DE ABASTECIMIENTOS ---");
                    for (Movimiento m : movimientos) {
                        if (m.getTipo().equals("ABASTECIMIENTO") || m.getTipo().equals("RETIRO_EXCEDENTES")) {
                            System.out.println(m);
                        }
                    }
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }
    
    private static void menuAdmin() {
        while (true) {
            System.out.println("\n=== ADMINISTRACION ===");
            System.out.println("1.Crear usuario  2.Crear cuenta  3.Crear tarjeta  4.Listar todo  5.Salir");
            System.out.print("Opcion: ");
            int op = leerInt();
            if (op == 5) break;
            
            switch (op) {
                case 1:
                    System.out.print("ID: ");
                    String id = scanner.nextLine();
                    System.out.print("Nombre: ");
                    String nombre = scanner.nextLine();
                    System.out.print("Documento: ");
                    String doc = scanner.nextLine();
                    System.out.print("Telefono: ");
                    String tel = scanner.nextLine();
                    System.out.print("Password: ");
                    String pass = scanner.nextLine();
                    usuarios.add(new Usuario(id, nombre, doc, tel, pass));
                    System.out.println("Usuario creado.");
                    break;
                case 2:
                    System.out.print("Numero cuenta: ");
                    String numCuenta = scanner.nextLine();
                    System.out.print("ID usuario: ");
                    String idUsr = scanner.nextLine();
                    System.out.print("Saldo inicial: $");
                    double saldo = leerDouble();
                    System.out.print("Limite diario: $");
                    double limite = leerDouble();
                    cuentas.add(new Cuenta(numCuenta, idUsr, saldo, limite));
                    System.out.println("Cuenta creada.");
                    break;
                case 3:
                    System.out.print("Numero tarjeta (0000XXXX, 1234XXXX, 9999XXXX): ");
                    String numTarjeta = scanner.nextLine();
                    System.out.print("Numero cuenta: ");
                    String numCta = scanner.nextLine();
                    System.out.print("PIN: ");
                    String pin = scanner.nextLine();
                    String rol = numTarjeta.startsWith("1234") ? "ADMIN" : (numTarjeta.startsWith("9999") ? "VALORES" : "CLIENTE");
                    tarjetas.add(new Tarjeta(numTarjeta, numCta, pin, rol));
                    System.out.println("Tarjeta creada con rol: " + rol);
                    break;
                case 4:
                    System.out.println("\n=== USUARIOS ===");
                    for (Usuario u : usuarios) System.out.println(u);
                    System.out.println("\n=== CUENTAS ===");
                    for (Cuenta c : cuentas) System.out.println(c);
                    System.out.println("\n=== TARJETAS ===");
                    for (Tarjeta t : tarjetas) System.out.println(t);
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }
    
    // ==================== UTILIDADES ====================
    
    private static Tarjeta buscarTarjeta(String num) {
        for (Tarjeta t : tarjetas) {
            if (t.getNumero().equals(num)) return t;
        }
        return null;
    }
    
    private static Cuenta buscarCuenta(String num) {
        for (Cuenta c : cuentas) {
            if (c.getNumero().equals(num)) return c;
        }
        return null;
    }
    
    private static int leerInt() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            return -1;
        }
    }
    
    private static double leerDouble() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (Exception e) {
            return 0;
        }
    }
    
    private static void cargarDatos() {
        usuarios = fm.cargarUsuarios();
        cuentas = fm.cargarCuentas();
        tarjetas = fm.cargarTarjetas();
        movimientos = fm.cargarMovimientos();
        contadorMovimientos = movimientos.size() + 1;
    }
    
    private static void guardarDatos() {
        fm.guardarUsuarios(usuarios);
        fm.guardarCuentas(cuentas);
        fm.guardarTarjetas(tarjetas);
        fm.guardarMovimientos(movimientos);
    }
}