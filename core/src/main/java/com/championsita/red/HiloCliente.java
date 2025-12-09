package com.championsita.red;

import com.badlogic.gdx.Gdx;
import com.championsita.Principal;
import com.championsita.menus.EnLinea.MenuEnLinea;

import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

public class HiloCliente extends Thread {

    // ====================================================
    // CAMPOS PRINCIPALES
    // ====================================================
    private DatagramSocket socket;
    private InetSocketAddress servidor;
    private InetAddress broadcast;

    private final AtomicLong ultimoPong = new AtomicLong(0);

    public volatile EstadoCliente estado = EstadoCliente.DESCONECTADO;
    public EstadoPartidaCliente estadoActual;

    private boolean fin = false;
    private Principal juego;
    private LobbySync pantallaLobby = null;

    private static final long TIMEOUT_MS = 3000;


    // ====================================================
    // CONSTRUCTOR
    // ====================================================
    public HiloCliente(Principal juego) {
        this.juego = juego;
        try {
            socket = new DatagramSocket();
            socket.setBroadcast(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // ====================================================
    // API LOBBY
    // ====================================================
    public void setLobbyPantalla(LobbySync pantalla) {
        this.pantallaLobby = pantalla;
    }


    // ====================================================
    // LOOP PRINCIPAL
    // ====================================================
    @Override
    public void run() {

        intentarEncontrarServidor();
        if (estado == EstadoCliente.DESCONECTADO) return;

        intentarConectarAlServidor();

        loopRecepcionYKeepAlive();
    }


    // ====================================================
    // ENVÍO GENERAL
    // ====================================================
    public void enviar(String msg) {
        try {
            byte[] data = msg.getBytes();
            DatagramPacket p = new DatagramPacket(
                    data, data.length,
                    servidor.getAddress(),
                    servidor.getPort()
            );
            socket.send(p);
        } catch (Exception e) { e.printStackTrace(); }
    }


    // ====================================================
    // PROCESAMIENTO DE MENSAJES
    // ====================================================
    private void procesar(DatagramPacket dp) {
        String msg = new String(dp.getData(), 0, dp.getLength()).trim();

        if (procesarHandshake(msg)) return;
        if (procesarLobbySync(msg)) return;
        if (procesarEstadoPartida(msg)) return;
        if (procesarKeepAlive(msg)) return;
        if (procesarDesconexion(msg)) return;
    }

    // ------------------------------
    // HANDSHAKE
    // ------------------------------
    private boolean procesarHandshake(String msg) {

        switch (msg) {
            case "Conectado":
                estado = EstadoCliente.CONECTADO;
                return true;

            case "conexion_establecida":
                abrirLobby();
                return true;
        }

        return false;
    }

    private void abrirLobby() {
        Gdx.app.postRunnable(() -> {
            MenuEnLinea lobby = new MenuEnLinea(juego, this);
            juego.actualizarPantalla(lobby);
            setLobbyPantalla(lobby);
        });
    }

    // ------------------------------
    // SINCRONIZACIÓN DE LOBBY
    // ------------------------------
    private boolean procesarLobbySync(String msg) {
        if (pantallaLobby == null) return false;

        if (msg.startsWith("SKIN_RIVAL=")) {
            actualizarUI(() -> pantallaLobby.aplicarSkinRival(msg.substring(11)));
            return true;
        }

        if (msg.startsWith("READY_MODE")){
            boolean ready = msg.endsWith("1");
            actualizarUI(() -> pantallaLobby.aplicarReadyRival(ready));
            return true;
        }

        if (msg.startsWith("READY_SKIN=")) {
            boolean ready = msg.endsWith("1");
            actualizarUI(() -> pantallaLobby.aplicarReadyRival(ready));
            System.out.println(msg);
            return true;
        }

        if (msg.startsWith("CFG_CAMPO=")) {
            actualizarUI(() -> pantallaLobby.aplicarCampoRival(msg.substring(10)));
            return true;
        }

        if (msg.startsWith("CFG_GOLES=")) {
            int goles = parseEntero(msg, 10);
            actualizarUI(() -> pantallaLobby.aplicarGolesRival(goles));
            return true;
        }

        if (msg.startsWith("CFG_TIEMPO=")) {
            int tiempo = parseEntero(msg, 11);
            actualizarUI(() -> pantallaLobby.aplicarTiempoRival(tiempo));
            return true;
        }

        if (msg.startsWith("CFG_MODO=")) {
            actualizarUI(() -> pantallaLobby.aplicarModoRival(msg.substring(9)));
            return true;
        }

        return false;
    }


    // ------------------------------
    // ESTADO DE PARTIDA
    // ------------------------------
    private boolean procesarEstadoPartida(String msg) {

        if (msg.equals("PARTIDA_INICIADA")) return true;

        if (msg.startsWith("STATE;")) {
            procesarEstadoPartidaInterno(msg.substring(6));
            return true;
        }

        return false;
    }

    private void procesarEstadoPartidaInterno(String contenido) {
        //
    }


    // ------------------------------
    // KEEP ALIVE
    // ------------------------------
    private boolean procesarKeepAlive(String msg) {
        if (!msg.equals("PONG")) return false;

        ultimoPong.set(System.currentTimeMillis());
        return true;
    }


    // ------------------------------
    // DESCONEXIÓN
    // ------------------------------
    private boolean procesarDesconexion(String msg) {
        if (!msg.equals("PLAYER_DISCONNECTED")) return false;

        actualizarUI(() -> juego.volverAlMenuPrincipal());
        return true;
    }


    // ====================================================
    // BUSCAR SERVIDOR
    // ====================================================
    private void intentarEncontrarServidor() {
        estado = EstadoCliente.BUSCANDO_SERVIDOR;

        try {
            broadcast = obtenerBroadcast();
        } catch (Exception e) {
            estado = EstadoCliente.DESCONECTADO;
            return;
        }

        for (int i = 0; i < 5; i++) {
            try {
                enviarBroadcast("Hello_There");
                socket.setSoTimeout(800);

                DatagramPacket respuesta = recibirPaquete(256);
                if (respuesta == null) continue;

                if (new String(respuesta.getData(), 0, respuesta.getLength()).trim().equals("General_Kenobi")) {
                    servidor = new InetSocketAddress(respuesta.getAddress(), respuesta.getPort());
                    estado = EstadoCliente.CONECTANDO;
                    return;
                }

            } catch (Exception ignored) {}
        }

        estado = EstadoCliente.DESCONECTADO;
    }


    private void enviarBroadcast(String msg) throws Exception {
        byte[] data = msg.getBytes();
        DatagramPacket p = new DatagramPacket(data, data.length, broadcast, 4321);
        socket.send(p);
    }


    // ====================================================
    // CONEXIÓN FORMAL
    // ====================================================
    private void intentarConectarAlServidor() {

        if (estado != EstadoCliente.CONECTANDO) return;

        for (int i = 0; i < 5; i++) {
            enviar("Conectar");

            try {
                socket.setSoTimeout(1000);

                DatagramPacket p = recibirPaquete(256);
                if (p == null) continue;

                if (new String(p.getData(), 0, p.getLength()).trim().equals("Conectado")) {
                    estado = EstadoCliente.CONECTADO;
                    ultimoPong.set(System.currentTimeMillis());
                    return;
                }

            } catch (Exception ignored) {}
        }

        estado = EstadoCliente.DESCONECTADO;
    }


    // ====================================================
    // LOOP RECEPCIÓN + PING
    // ====================================================
    private void loopRecepcionYKeepAlive() {

        while (!fin) {

            verificarTimeout();

            try {
                socket.setSoTimeout(200);

                DatagramPacket dp = recibirPaquete(1024);
                if (dp != null) procesar(dp);

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (estado == EstadoCliente.CONECTADO ||
                    estado == EstadoCliente.CONEXION_ESTABLECIDA) {
                enviar("PING");
            }
        }
    }

    private void verificarTimeout() {
        if (estado == EstadoCliente.CONECTADO &&
                (System.currentTimeMillis() - ultimoPong.get()) > TIMEOUT_MS) {
            estado = EstadoCliente.PERDIDA_CONEXION;
        }
    }


    // ====================================================
    // HELPERS
    // ====================================================
    private DatagramPacket recibirPaquete(int size) {
        try {
            byte[] buffer = new byte[size];
            DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            socket.receive(dp);
            return dp;
        } catch (Exception e) {
            return null;
        }
    }

    private void actualizarUI(Runnable r) {
        Gdx.app.postRunnable(r);
    }

    private int parseEntero(String msg, int offset) {
        return Integer.parseInt(msg.substring(offset));
    }

    private InetAddress obtenerBroadcast() throws SocketException {
        for (NetworkInterface ni : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
            if (!ni.isUp() || ni.isLoopback()) continue;

            for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                InetAddress b = ia.getBroadcast();
                if (b != null) return b;
            }
        }
        throw new RuntimeException("No se encontró broadcast");
    }
}
