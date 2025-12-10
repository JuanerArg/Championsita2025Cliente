package com.championsita.red;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.championsita.Principal;
import com.championsita.menus.EnLinea.MenuEnLinea;
import com.championsita.menus.herramientas.ConfigCliente;
import com.championsita.partida.herramientas.PantallaEsperandoServidor;
import com.championsita.partida.herramientas.PantallaPartida;

import java.net.*;
import java.util.ArrayList;
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
    public List<EstadoPersonaje> jugadores;
    public EstadoPelota pelota;
    public EstadoArco estadoArcoIzquierdo, estadoArcoDerecho;
    public int golesAzul, golesRojo;


    private boolean fin = false;
    private Principal juego;
    private LobbySync pantallaLobby = null;
    public ConfigCliente config = new ConfigCliente();


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
            if(msg.substring(11).startsWith("jugador")){
                actualizarUI(() -> pantallaLobby.aplicarSkinRival(msg.substring(11)));
            }else{
                actualizarUI(() -> pantallaLobby.actualizarIndiceSkinRival(msg.substring(11)));
            }
            return true;
        }

        if (msg.startsWith("READY_MODE")){
            boolean ready = msg.endsWith("1");
            actualizarUI(() -> pantallaLobby.aplicarReadyRival(ready));
            return true;
        }

        if(msg.startsWith("READY_CAMPO")){
            boolean ready = msg.endsWith("1");
            actualizarUI(() -> pantallaLobby.aplicarReadyRival(ready));
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

        if (msg.equals("PARTIDA_INICIADA")) {
            actualizarUI(() -> juego.actualizarPantalla(new PantallaPartida(juego, this, config)));
            return true;
        }

        if (msg.startsWith("STATE;")) {
            estadoActual = procesarEstadoPartidaInterno(msg.substring(6));
            System.out.println(msg);
            return true;
        }

        if (msg.startsWith("")) return false;

        return false;
    }

    private EstadoPartidaCliente procesarEstadoPartidaInterno(String contenido) {

        ArrayList<EstadoPersonaje> jugadores = new ArrayList<>();
        EstadoPelota pelota = new EstadoPelota();
        EstadoArco arcoIzquierdo = new EstadoArco();
        EstadoArco arcoDerecho = new EstadoArco();
        String mensaje;

        int golesRojo = 0, golesAzul = 0;

        // Quitar "STATE;"
        if(contenido.startsWith("STATE")){
            mensaje = contenido.substring("STATE".length());
        }else {
            mensaje = contenido;
        }

        String[] partes = mensaje.split(";");

        for (String p : partes) {

            // 1) Divido por comas
            String[] tokens = p.split(",");

            if (tokens.length == 0) continue;

            // 2) tokens[0] es J1 / J2 / PEL / ARC_I / ARC_D / HUD
            String prefix = tokens[0];

            // 3) Parsear todos los key=value a solo value
            //    Ej: "x=100" → "100"
            ArrayList<String> values = new ArrayList<>();

            for (int i = 1; i < tokens.length; i++) {
                String token = tokens[i];

                String[] kv = token.split("="); // kv[0]=clave, kv[1]=valor
                if (kv.length == 2) {
                    values.add(kv[1]);
                } else {
                    values.add(""); // por si llegara algo raro
                }
            }

            // 4) Ahora "values" ya tiene solo los valores puros
            switch (prefix) {

                case "J0":
                case "J1": {
                    EstadoPersonaje pj = new EstadoPersonaje(
                            Float.parseFloat(values.get(0)), // x
                            Float.parseFloat(values.get(1)), // y
                            Float.parseFloat(values.get(2)), // w
                            Float.parseFloat(values.get(3)), // h
                            Boolean.parseBoolean(values.get(4)), // mov
                            values.get(5), // direccion
                            Float.parseFloat(values.get(6)), // tiempo anim
                            Float.parseFloat(values.get(7)), // stamina actual
                            Float.parseFloat(values.get(8))  // stamina max
                    );
                    jugadores.add(pj);
                    break;
                }

                case "PEL": {
                    pelota = new EstadoPelota(
                            Float.parseFloat(values.get(0)),
                            Float.parseFloat(values.get(1)),
                            Float.parseFloat(values.get(2)),
                            Float.parseFloat(values.get(3)),
                            Float.parseFloat(values.get(4)), // stateTime
                            Boolean.parseBoolean(values.get(5)) // animar
                    );
                    break;
                }

                case "ARC_I": {
                    arcoIzquierdo = new EstadoArco(
                            Float.parseFloat(values.get(0)),
                            Float.parseFloat(values.get(1)),
                            Float.parseFloat(values.get(2)),
                            Float.parseFloat(values.get(3))
                    );
                    break;
                }

                case "ARC_D": {
                    arcoDerecho = new EstadoArco(
                            Float.parseFloat(values.get(0)),
                            Float.parseFloat(values.get(1)),
                            Float.parseFloat(values.get(2)),
                            Float.parseFloat(values.get(3))
                    );
                    break;
                }

                case "HUD": {
                    golesRojo = Integer.parseInt(values.get(0));
                    golesAzul = Integer.parseInt(values.get(1));
                    break;
                }
            }
        }

        return new EstadoPartidaCliente(
                jugadores,
                pelota,
                arcoIzquierdo,
                arcoDerecho,
                golesRojo,
                golesAzul
        );
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

    public void enviarConfig(ConfigCliente config) {
        String mensaje = new String();

        mensaje += "CFG_FINAL=";
        mensaje += "campo:" + config.campo + ";";
        mensaje += "goles:" + config.goles + ";";
        mensaje += "tiempo:" + config.tiempo + ";";
        mensaje += "modo:" + config.modo + ";";
        mensaje += "skin:" + config.skinsJugadores.get(0) + ";";
        if(config.habilidadesEspeciales.toArray().length != 0) mensaje += "habilidad" + config.habilidadesEspeciales.get(0) + ";";

        enviar(mensaje);
    }
}
