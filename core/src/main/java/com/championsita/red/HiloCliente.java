package com.championsita.red;

import com.badlogic.gdx.Gdx;
import com.championsita.Principal;
import com.championsita.menus.EnLinea.CargaOnlineCampo;
import com.championsita.menus.EnLinea.CargaOnlineSkin;
import com.championsita.menus.EnLinea.MenuEnLinea;
import com.championsita.menus.herramientas.ConfigCliente;
import java.net.*;
import java.util.concurrent.atomic.AtomicLong;

public class HiloCliente extends Thread {

    private DatagramSocket socket;
    private InetSocketAddress servidor;
    private InetAddress broadcast;

    private final AtomicLong ultimoPong = new AtomicLong(0);
    public volatile EstadoCliente estado = EstadoCliente.DESCONECTADO;
    public EstadoPartidaCliente estadoActual;
    private boolean fin = false;

    private Principal juego;

    // Pantalla del lobby actualmente visible
    private LobbySync pantallaActual = null;

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

    // ====================================================
    // Permite a cada pantalla registrarse como pantalla actual
    // ====================================================
    public void setPantallaActual(LobbySync pantalla) {
        this.pantallaActual = pantalla;
    }

    @Override
    public void run() {

        buscarServidor();
        if (estado == EstadoCliente.DESCONECTADO) return;

        conectar();

        loopRecepcion();
    }

    // ====================================================
    // ENVIAR MENSAJES
    // ====================================================
    public void enviar(String msg) {
        try {
            byte[] data = msg.getBytes();
            DatagramPacket p = new DatagramPacket(
                    data, data.length,
                    servidor.getAddress(), servidor.getPort()
            );
            socket.send(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ====================================================
    // PROCESAR RESPUESTAS DEL SERVIDOR
    // ====================================================
    private void procesar(DatagramPacket dp) {

        String msg = new String(dp.getData(), 0, dp.getLength()).trim();
        System.out.println("[CLIENTE] " + msg);

        // ---------------------------
        // Handshake
        // ---------------------------
        if (msg.equals("Conectado")) {
            estado = EstadoCliente.CONECTADO;
            return;
        }

        if (msg.equals("conexion_establecida")) {

            Gdx.app.postRunnable(() -> {
                MenuEnLinea lobby = new MenuEnLinea(juego, this);
                juego.setScreen(lobby);
                setPantallaActual(lobby);
            });

            return;
        }


        // ---------------------------
        // Sincronización de lobby
        // ---------------------------
        if (msg.startsWith("SKIN_RIVAL=")) {
            if (pantallaActual != null)
                Gdx.app.postRunnable(() ->pantallaActual.aplicarSkinRival(msg.substring(11)));
            return;
        }

        if (msg.startsWith("READY_SKIN=")) {
            if (pantallaActual != null)
                Gdx.app.postRunnable(() ->pantallaActual.aplicarReadyRival(msg.endsWith("1")));
            return;
        }

        if (msg.startsWith("CFG_CAMPO=")) {
            if (pantallaActual != null)
                Gdx.app.postRunnable(() ->pantallaActual.aplicarCampoRival(msg.substring(10)));
            return;
        }

        if (msg.startsWith("CFG_GOLES=")) {
            if (pantallaActual != null)
                Gdx.app.postRunnable(() ->pantallaActual.aplicarGolesRival(Integer.parseInt(msg.substring(10))));
            return;
        }

        if (msg.startsWith("CFG_TIEMPO=")) {
            if (pantallaActual != null)
                Gdx.app.postRunnable(() ->pantallaActual.aplicarTiempoRival(Integer.parseInt(msg.substring(11))));
            return;
        }

        if (msg.startsWith("CFG_MODO=")) {
            if (pantallaActual != null)
                Gdx.app.postRunnable(() ->pantallaActual.aplicarModoRival(msg.substring(9)));
            return;
        }

        // ---------------------------
        // Comienzo de la partida
        // ---------------------------
        if (msg.equals("PARTIDA_INICIADA")) {
            //aca pondriamos para recibir lo que tenemos que mostrar
            return;
        }

        // ---------------------------
        // ACTUALIZACIÓN DE PARTIDA
        // ---------------------------
        if (msg.startsWith("STATE;")) {
            procesarEstadoPartida(msg.substring(6));
            return;
        }

        // ---------------------------
        // Rival desconectado
        // ---------------------------
        if (msg.startsWith("PLAYER_DISCONNECTED")) {
            Gdx.app.postRunnable(() -> juego.volverAlMenuPrincipal());
            return;
        }

        // ---------------------------
        // Pong
        // ---------------------------
        if (msg.equals("PONG")) {
            ultimoPong.set(System.currentTimeMillis());
            return;
        }
    }

    // ====================================================
    // ESTADO DE PARTIDA
    // ====================================================
    private void procesarEstadoPartida(String msg) {
        // NO LO TOCO — ya lo hiciste vos
        // Solo lo llamo acá
    }

    // ====================================================
    // BÚSQUEDA DEL SERVIDOR
    // ====================================================
    private void buscarServidor() {
        estado = EstadoCliente.BUSCANDO_SERVIDOR;

        try { broadcast = obtenerBroadcast(); }
        catch (Exception e) { estado = EstadoCliente.DESCONECTADO; return; }

        for (int i = 0; i < 5; i++) {
            try {
                enviarBroadcast("Hello_There");

                socket.setSoTimeout(800);
                byte[] buf = new byte[256];
                DatagramPacket resp = new DatagramPacket(buf, buf.length);
                socket.receive(resp);

                if (new String(resp.getData(), 0, resp.getLength()).trim().equals("General_Kenobi")) {
                    servidor = new InetSocketAddress(resp.getAddress(), resp.getPort());
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
    // CONECTAR
    // ====================================================
    private void conectar() {
        if (estado != EstadoCliente.CONECTANDO) return;

        for (int i = 0; i < 5; i++) {
            enviar("Conectar");

            try {
                socket.setSoTimeout(1000);
                byte[] buffer = new byte[256];
                DatagramPacket p = new DatagramPacket(buffer, buffer.length);
                socket.receive(p);

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
    // LOOP
    // ====================================================
    private void loopRecepcion() {

        while (!fin) {

            if (estado == EstadoCliente.CONECTADO &&
                    (System.currentTimeMillis() - ultimoPong.get()) > TIMEOUT_MS) {
                estado = EstadoCliente.PERDIDA_CONEXION;
            }

            try {
                socket.setSoTimeout(200);
                byte[] buffer = new byte[1024];
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                socket.receive(dp);

                procesar(dp);

            } catch (SocketTimeoutException ignored) {}
            catch (Exception e) { e.printStackTrace(); }

            if (estado == EstadoCliente.CONECTADO ||
                    estado == EstadoCliente.CONEXION_ESTABLECIDA) {
                enviar("PING");
            }
        }
    }

    // ====================================================
    // UTILS
    // ====================================================
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
