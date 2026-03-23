package main;

import model.Usuario;
import model.Cuenta;
import model.Tarjeta;
import model.Movimiento;
import service.FileManager;
import java.util.*;
import java.io.*;

public class main {
    
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
        
        new File("data").mkdirs();
        cargarDatos();
        
        // Crear tarjetas por defecto
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
            usuarios.add(new Usuario("ADMIN", "Administrador", "000", "000", "admin123"));
            necesitaGuardar = true;
        }
        if (usuarios.stream().noneMatch(u -> u.getId().equals("VALORES"))) {
            usuarios.add(new Usuario("VALORES", "Empresa Valores", "000", "000", "valores123"));
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
                autenticarConTarjeta();
            } else {
                System.out.println("Opcion invalida.");
            }
        }
    }
    
    // ==================== CONSIGNACION SIN TARJETA ====================
    
    private static void consignarSinTarjeta() {
        System.out.println("\n--- CONSIGNACION SIN TARJETA ---");
        
        System.out.print("Ingrese numero de cuenta destino: ");
        String numCuenta = scanner.nextLine();
        
        Cuenta cuentaDestino = buscarCuenta(numCuenta);
        if (cuentaDestino == null) {
            System.out.println("Error: Cuenta no existe.");
            return;
        }
        
        System.out.println("\nIngrese los billetes (solo denominaciones validas: 100000, 50000, 20000, 10000)");
        
        int[] valores = {100000, 50000, 20000, 10000};
        int[] cantidades = new int[4];
        double total = 0;
        
        for (int i = 0; i < valores.length; i++) {
            System.out.print("Billetes de $" + valores[i] + ": ");
            cantidades[i] = leerInt();
            if (cantidades[i] < 0) cantidades[i] = 0;
            total += valores[i] * cantidades[i];
        }
        
        if (total <= 0) {
            System.out.println("Error: No ingreso ningun billete valido.");
            return;
        }
        
        System.out.println("\nMonto total a consignar: $" + total);
        System.out.print("¿Confirmar consignacion? (s/n): ");
        String confirmar = scanner.nextLine();
        
        if (!confirmar.equalsIgnoreCase("s")) {
            System.out.println("Consignacion cancelada.");
            return;
        }
        
        // Registrar la consignacion
        cuentaDestino.depositar(total);
        movimientos.add(new Movimiento("M" + contadorMovimientos++, "CONSIGNACION", "", numCuenta, total, 
                        "Consignacion sin tarjeta en cajero"));
        guardarDatos();
        
        System.out.println("\n=== CONSIGNACION EXITOSA ===");
        System.out.println("Cuenta destino: " + numCuenta);
        System.out.println("Monto consignado: $" + total);
        System.out.println("Transaccion completada. Retire su comprobante.");
    }
    
    // ==================== AUTENTICACION CON TARJETA ====================
    
    private static void autenticarConTarjeta() {
        System.out.print("\nInserte su tarjeta (0 para cancelar): ");
        String numTarjeta = scanner.nextLine();
        
        if (numTarjeta.equals("0")) return;
        
        Tarjeta tarjeta = buscarTarjeta(numTarjeta);
        if (tarjeta == null) {
            System.out.println("Tarjeta no reconocida.");
            return;
        }
        
        if (!tarjeta.isActiva()) {
            System.out.println("Tarjeta bloqueada.");
            return;
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
    
    // ==================== MENU CLIENTE ====================
    
    private static void menuCliente() {
        while (true) {
            System.out.println("\n1.Saldo  2.Retirar  3.Transferir  4.Movimientos  5.Salir");
            System.out.print("Opcion: ");
            int op = leerInt();
            
            if (op == 5) break;
            
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
                case 4:
                    System.out.println("\n--- MOVIMIENTOS ---");
                    boolean hayMovimientos = false;
                    for (Movimiento m : movimientos) {
                        if (m.getCuentaOrigen().equals(cuentaActual.getNumero()) || m.getCuentaDestino().equals(cuentaActual.getNumero())) {
                            System.out.println(m);
                            hayMovimientos = true;
                        }
                    }
                    if (!hayMovimientos) System.out.println("No hay movimientos registrados.");
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }
    
    // ==================== MENU VALORES ====================
    
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
                    boolean hayAbastos = false;
                    for (Movimiento m : movimientos) {
                        if (m.getTipo().equals("ABASTECIMIENTO") || m.getTipo().equals("RETIRO_EXCEDENTES")) {
                            System.out.println(m);
                            hayAbastos = true;
                        }
                    }
                    if (!hayAbastos) System.out.println("No hay registros de abastecimientos.");
                    break;
                default:
                    System.out.println("Opcion invalida.");
            }
        }
    }
    
    // ==================== MENU ADMIN ====================
    
    private static void menuAdmin() {
        while (true) {
            System.out.println("\n=== ADMINISTRACION ===");
            System.out.println("1.Crear usuario");
            System.out.println("2.Crear cuenta");
            System.out.println("3.Crear tarjeta");
            System.out.println("4.Listar todo");
            System.out.println("5.Modificar cuenta (limite diario)");
            System.out.println("6.Activar/Desactivar tarjeta");
            System.out.println("7.Eliminar usuario");
            System.out.println("8.Eliminar cuenta");
            System.out.println("9.Eliminar tarjeta");
            System.out.println("10.Salir");
            System.out.print("Opcion: ");
            int op = leerInt();
            
            if (op == 10) break;
            
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
                    if (buscarUsuario(idUsr) == null) {
                        System.out.println("Error: Usuario no existe.");
                        break;
                    }
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
                    if (buscarCuenta(numCta) == null && !numCta.equals("ADMIN") && !numCta.equals("VALORES")) {
                        System.out.println("Error: Cuenta no existe.");
                        break;
                    }
                    System.out.print("PIN: ");
                    String pin = scanner.nextLine();
                    String rol = numTarjeta.startsWith("1234") ? "ADMIN" : (numTarjeta.startsWith("9999") ? "VALORES" : "CLIENTE");
                    tarjetas.add(new Tarjeta(numTarjeta, numCta, pin, rol));
                    System.out.println("Tarjeta creada con rol: " + rol);
                    break;
                    
                case 4:
                    System.out.println("\n=== USUARIOS ===");
                    if (usuarios.isEmpty()) System.out.println("(No hay usuarios)");
                    for (Usuario u : usuarios) System.out.println(u);
                    
                    System.out.println("\n=== CUENTAS ===");
                    if (cuentas.isEmpty()) System.out.println("(No hay cuentas)");
                    for (Cuenta c : cuentas) System.out.println(c);
                    
                    System.out.println("\n=== TARJETAS ===");
                    if (tarjetas.isEmpty()) System.out.println("(No hay tarjetas)");
                    for (Tarjeta t : tarjetas) System.out.println(t);
                    break;
                    
                case 5:
                    System.out.print("Numero de cuenta: ");
                    String numMod = scanner.nextLine();
                    Cuenta cuentaMod = buscarCuenta(numMod);
                    if (cuentaMod == null) {
                        System.out.println("Cuenta no encontrada.");
                    } else {
                        System.out.print("Nuevo limite diario: $");
                        double nuevoLimite = leerDouble();
                        cuentaMod.setLimiteDiario(nuevoLimite);
                        System.out.println("Limite diario actualizado a $" + nuevoLimite);
                    }
                    break;
                    
                case 6:
                    System.out.print("Numero de tarjeta: ");
                    String numTarjetaMod = scanner.nextLine();
                    Tarjeta tarjetaMod = buscarTarjeta(numTarjetaMod);
                    if (tarjetaMod == null) {
                        System.out.println("Tarjeta no encontrada.");
                    } else {
                        tarjetaMod.setActiva(!tarjetaMod.isActiva());
                        System.out.println("Tarjeta " + (tarjetaMod.isActiva() ? "activada" : "desactivada"));
                    }
                    break;
                    
                case 7:
                    System.out.print("ID de usuario a eliminar: ");
                    String idEliminar = scanner.nextLine();
                    Usuario usuarioEliminar = buscarUsuario(idEliminar);
                    if (usuarioEliminar == null) {
                        System.out.println("Usuario no encontrado.");
                    } else if (idEliminar.equals("ADMIN") || idEliminar.equals("VALORES")) {
                        System.out.println("No se puede eliminar el usuario administrador o de valores.");
                    } else {
                        boolean tieneCuentas = cuentas.stream().anyMatch(c -> c.getIdUsuario().equals(idEliminar));
                        if (tieneCuentas) {
                            System.out.println("Error: El usuario tiene cuentas asociadas. Elimine las cuentas primero.");
                        } else {
                            usuarios.remove(usuarioEliminar);
                            System.out.println("Usuario eliminado.");
                        }
                    }
                    break;
                    
                case 8:
                    System.out.print("Numero de cuenta a eliminar: ");
                    String numEliminar = scanner.nextLine();
                    Cuenta cuentaEliminar = buscarCuenta(numEliminar);
                    if (cuentaEliminar == null) {
                        System.out.println("Cuenta no encontrada.");
                    } else {
                        boolean tieneTarjeta = tarjetas.stream().anyMatch(t -> t.getNumCuenta().equals(numEliminar));
                        if (tieneTarjeta) {
                            System.out.println("Error: La cuenta tiene tarjeta asociada. Elimine la tarjeta primero.");
                        } else {
                            cuentas.remove(cuentaEliminar);
                            System.out.println("Cuenta eliminada.");
                        }
                    }
                    break;
                    
                case 9:
                    System.out.print("Numero de tarjeta a eliminar: ");
                    String numTarjetaEliminar = scanner.nextLine();
                    Tarjeta tarjetaEliminar = buscarTarjeta(numTarjetaEliminar);
                    if (tarjetaEliminar == null) {
                        System.out.println("Tarjeta no encontrada.");
                    } else if (numTarjetaEliminar.equals("12340001") || numTarjetaEliminar.equals("99990001")) {
                        System.out.println("No se puede eliminar la tarjeta maestra de administrador o valores.");
                    } else {
                        tarjetas.remove(tarjetaEliminar);
                        System.out.println("Tarjeta eliminada.");
                    }
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
    
    private static Usuario buscarUsuario(String id) {
        for (Usuario u : usuarios) {
            if (u.getId().equals(id)) return u;
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