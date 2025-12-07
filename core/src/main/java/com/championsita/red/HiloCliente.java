package com.championsita.red;

import com.championsita.Principal;
import com.championsita.menus.herramientas.ConfigCliente;
import com.championsita.partida.herramientas.PantallaPartida;
import com.championsita.partida.herramientas.RenderizadorPartida;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

public class HiloCliente extends Thread {

    public EstadoPartidaCliente estadoActual;
    private DatagramSocket socket;

    private InetSocketAddress servidor;
    private InetAddress broadcast;

    private final AtomicLong ultimoPong = new AtomicLong(0);

    public volatile EstadoCliente estado = EstadoCliente.DESCONECTADO;
    private ConfigCliente configCliente;

    private boolean fin = false;

    private Principal juego;

    // Tiempo máximo sin recibir PONG antes de considerar caída
    private static final long TIMEOUT_MS = 3000;

    public HiloCliente(Principal juego) {
        this.juego = juego;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

        // Paso 1: buscar servidor
        buscarServidor();

        // Si no se encontró:
        if (estado == EstadoCliente.DESCONECTADO) {
            System.err.println("No se encontró servidor.");
            return;
        }

        // Paso 2: intentar conectar
        conectar();

        // Paso 3: loop principal de red
        loopRecepcion();
    }


    // ========================
    //  BÚSQUEDA DE SERVIDOR
    // ========================
    private void buscarServidor() {
        estado = EstadoCliente.BUSCANDO_SERVIDOR;

        try {
            broadcast = obtenerBroadcast();
        } catch (Exception e) {
            estado = EstadoCliente.DESCONECTADO;
            return;
        }

        for (int intento = 0; intento < 5; intento++) {
            try {
                // Enviar broadcast
                byte[] data = "Hello_There".getBytes();
                DatagramPacket p = new DatagramPacket(data, data.length, broadcast, 4321);
                socket.send(p);

                // Esperar respuesta
                socket.setSoTimeout(800);
                byte[] buffer = new byte[256];
                DatagramPacket resp = new DatagramPacket(buffer, buffer.length);

                socket.receive(resp);

                String msg = new String(resp.getData(), 0, resp.getLength()).trim();

                if(msg.equals("General_Kenobi")) {
                    servidor = new InetSocketAddress(resp.getAddress(), resp.getPort());
                    estado = EstadoCliente.CONECTANDO;
                    return;
                }

            } catch (Exception ignored) {}
        }

        estado = EstadoCliente.DESCONECTADO;
    }


    // ========================
    //      CONEXIÓN
    // ========================
    private void conectar() {

        if (estado != EstadoCliente.CONECTANDO) return;

        for (int i = 0; i < 5; i++) {
            enviar("Conectar");

            try {
                socket.setSoTimeout(1000);
                byte[] buffer = new byte[256];
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                socket.receive(p);

                String msg = new String(p.getData(), 0, p.getLength()).trim();

                if (msg.equals("Conectado")) {
                    estado = EstadoCliente.CONECTADO;
                    ultimoPong.set(System.currentTimeMillis());
                    return;
                }

            } catch (Exception ignored) {}
        }

        estado = EstadoCliente.DESCONECTADO;
    }


    // ========================
    //   LOOP PRINCIPAL UDP
    // ========================
    private void loopRecepcion() {

        while (!fin) {

            // Detectar caída por timeout
            if (estado == EstadoCliente.CONECTADO &&
                    (System.currentTimeMillis() - ultimoPong.get()) > TIMEOUT_MS) {
                estado = EstadoCliente.PERDIDA_CONEXION;
                System.err.println("SERVIDOR CAÍDO");
            }

            try {
                socket.setSoTimeout(200);
                byte[] buffer = new byte[1024];
                DatagramPacket datagrama = new DatagramPacket(buffer, buffer.length);
                socket.receive(datagrama);

                procesar(datagrama);

            } catch (SocketTimeoutException ignored) {
                // No llega nada → seguimos
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Enviar heartbeat cada 1 segundo
            if (estado == EstadoCliente.CONECTADO) {
                enviar("PING");
            }
        }
    }


    // ========================
    //   PROCESAR MENSAJES
    // ========================
    private void procesar(DatagramPacket p) {
        String msg = new String(p.getData(), 0, p.getLength()).trim();

        if (msg.startsWith("STATE")) {
            procesarEstadoPartida(msg);
            return;
        }

        switch (msg) {
            case "iniciar_partida":
                juego.setScreen(new PantallaPartida(juego, this, configCliente));  // o como lo manejes
                break;
            case "PONG":
                ultimoPong.set(System.currentTimeMillis());
                break;
            case "PLAYER_DISCONNECTED":
                System.out.println("El otro jugador se desconectó.");
                break;
            default:
                System.out.println("Mensaje: " + msg);
        }
    }



    // ========================
    //    MÉTODOS ÚTILES
    // ========================
    private void procesarEstadoPartida(String msg) {

        EstadoPartidaCliente nuevo = new EstadoPartidaCliente();

        String[] lineas = msg.split(";");

        for (String linea : lineas) {
            linea = linea.trim();

            if (linea.startsWith("J")) {
                // ----------------------------
                // JUGADOR
                // ----------------------------
                EstadoPersonaje pj = new EstadoPersonaje();

                String datos = linea.substring(linea.indexOf(":") + 1);
                String[] campos = datos.split(",");

                for (String c : campos) {
                    String[] kv = c.split("=");
                    if (kv.length != 2) continue;

                    String k = kv[0].trim();
                    String v = kv[1].trim();

                    switch (k) {
                        case "x":  pj.x = Float.parseFloat(v); break;
                        case "y":  pj.y = Float.parseFloat(v); break;
                        case "w":  pj.ancho = Float.parseFloat(v); break;
                        case "h":  pj.alto  = Float.parseFloat(v); break;
                        case "mov": pj.estaMoviendo = v.equals("1"); break;
                        case "dir": pj.direccion = v; break;
                        case "ta":  pj.tiempoAnimacion = Float.parseFloat(v); break;
                        case "st":  pj.staminaActual = Float.parseFloat(v); break;
                        case "stm": pj.staminaMaxima = Float.parseFloat(v); break;
                    }
                }

                nuevo.jugadores.add(pj);
            }

            else if (linea.startsWith("PEL")) {
                // ----------------------------
                // PELOTA
                // ----------------------------
                EstadoPelota p = new EstadoPelota();

                String datos = linea.substring(linea.indexOf(":") + 1);
                String[] campos = datos.split(",");

                for (String c : campos) {
                    String[] kv = c.split("=");
                    if (kv.length != 2) continue;

                    String k = kv[0].trim();
                    String v = kv[1].trim();

                    switch (k) {
                        case "x":  p.x = Float.parseFloat(v); break;
                        case "y":  p.y = Float.parseFloat(v); break;
                        case "w":  p.width  = Float.parseFloat(v); break;
                        case "h":  p.height = Float.parseFloat(v); break;
                        case "st": p.stateTime = Float.parseFloat(v); break;
                        case "a":  p.animar     = v.equals("1"); break;
                    }
                }

                nuevo.pelota = p;
            }

            else if (linea.startsWith("ARC_I")) {
                // ARCO IZQUIERDO
                EstadoArco a = new EstadoArco();
                String datos = linea.substring(linea.indexOf(":") + 1);
                cargarArco(a, datos);
                nuevo.arcoIzq = a;
            }

            else if (linea.startsWith("ARC_D")) {
                // ARCO DERECHO
                EstadoArco a = new EstadoArco();
                String datos = linea.substring(linea.indexOf(":") + 1);
                cargarArco(a, datos);
                nuevo.arcoDer = a;
            }

            else if (linea.startsWith("HUD")) {
                // ----------------------------
                // HUD (goles y tiempo)
                // ----------------------------
                String datos = linea.substring(linea.indexOf(":") + 1);
                String[] campos = datos.split(",");

                for (String c : campos) {
                    String[] kv = c.split("=");
                    if (kv.length != 2) continue;

                    String k = kv[0].trim();
                    String v = kv[1].trim();

                    switch (k) {
                        case "gr": nuevo.golesRojo = Integer.parseInt(v); break;
                        case "ga": nuevo.golesAzul = Integer.parseInt(v); break;
                        case "t":  nuevo.tiempo    = Float.parseFloat(v); break;
                    }
                }
            }
        }

        // Finalmente actualizar el estado global del cliente
        this.estadoActual = nuevo;
    }

    private void cargarArco(EstadoArco arco, String datos) {
        String[] campos = datos.split(",");

        for (String c : campos) {
            String[] kv = c.split("=");
            if (kv.length != 2) continue;

            String k = kv[0].trim();
            String v = kv[1].trim();

            switch (k) {
                case "x": arco.x = Float.parseFloat(v); break;
                case "y": arco.y = Float.parseFloat(v); break;
                case "w": arco.w = Float.parseFloat(v); break;
                case "h": arco.h = Float.parseFloat(v); break;
            }
        }
    }


    public void enviar(String msg) {
        try {
            byte[] data = msg.getBytes();
            DatagramPacket p = new DatagramPacket(
                    data, data.length,
                    servidor.getAddress(),
                    servidor.getPort()
            );
            socket.send(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private InetAddress obtenerBroadcast() throws SocketException {

        for (NetworkInterface ni :
                java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {

            if (!ni.isUp() || ni.isLoopback()) continue;

            for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                InetAddress b = ia.getBroadcast();
                if (b != null) return b;
            }
        }

        throw new RuntimeException("No se encontró broadcast");
    }


    public void enviarConfig(ConfigCliente config) {
        configCliente = config;
    }
}
