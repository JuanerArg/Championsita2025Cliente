package com.championsita.menus.EnLinea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.championsita.Principal;
import com.championsita.menus.compartido.Assets;
import com.championsita.menus.herramientas.ConfigCliente;
import com.championsita.menus.menucarga.Campo;
import com.championsita.menus.menuprincipal.GestorInputMenu;
import com.championsita.menus.menuprincipal.Menu;
import com.championsita.menus.menuprincipal.RenderizadorDeMenu;
import com.championsita.partida.herramientas.PantallaEsperandoServidor;
import com.championsita.red.HiloCliente;
import com.championsita.red.LobbySync;

import java.util.HashMap;
import java.util.Map;

/**
 * Pantalla ONLINE donde ambos jugadores eligen:
 *  - Campo
 *  - Goles
 *  - Tiempo
 *
 * Los cambios se sincronizan entre los dos clientes.
 * El OK se sincroniza. Cuando ambos están listos, se arma config final
 * y se envía al servidor.
 */
public class CargaOnlineCampo extends Menu implements LobbySync {

    private final HiloCliente cliente;

    // Campo
    private Campo[] listaCampos;
    private int indiceCampo;
    private Sprite cartelCampo;
    private Sprite campoSprite;
    private final Map<String, Texture> cacheCampos = new HashMap<>();

    // Flechas
    private Texture flechaNormalTex;
    private Texture flechaHoverTex;
    private Sprite flechaIzq;
    private Sprite flechaDer;

    // Goles
    private final int[] opcionesGoles = {1,3,5};
    private int indiceGoles;
    private Sprite cartelGoles;

    // Tiempo
    private final int[] opcionesTiempo = {1,2,3};
    private int indiceTiempo;
    private Sprite cartelTiempo;

    // Ready
    private boolean estoyListo = false;
    private boolean rivalListo = false;

    private Color normal = new Color(1,1,1,1);
    private Color listo = new Color(0,1,0,1);

    private GestorInputMenu gestor;
    private RenderizadorDeMenu renderizador;

    private final String skinLocal;

    public CargaOnlineCampo(Principal juego, HiloCliente cliente, String skinLocal) {
        super(juego);
        this.cliente = cliente;
        this.cliente.setPantallaActual(this);// ← ESTA LÍNEA FALTA
        this.skinLocal = skinLocal;
    }

    @Override
    public void show() {
        super.show();
        Gdx.input.setInputProcessor(this);

        gestor = new GestorInputMenu(this);
        renderizador = new RenderizadorDeMenu(this);

        // Fondo
        super.fondoSprite.setTexture(Assets.tex("menuCreacion/menuDosJug.png"));

        // Campo
        listaCampos = Campo.values();
        indiceCampo = 0;

        cartelCampo = new Sprite(Assets.tex("menuCreacion/campoCartel.png"));
        cartelCampo.setPosition(
                Gdx.graphics.getWidth()/2f - cartelCampo.getWidth()/2f,
                215
        );

        campoSprite = new Sprite(cargarCampoTexture(listaCampos[indiceCampo].getNombre()));
        campoSprite.setSize(cartelCampo.getWidth(), 200);
        campoSprite.setPosition(cartelCampo.getX(), cartelCampo.getY() - 200);

        // Flechas
        flechaNormalTex = Assets.tex("menuDosJugadores/flechaNormal.png");
        flechaHoverTex  = Assets.tex("menuDosJugadores/flechaInvertida.png");

        flechaIzq = new Sprite(flechaNormalTex);
        flechaDer = new Sprite(flechaNormalTex);

        float yF = campoSprite.getY() + campoSprite.getHeight()/3.5f - 20f;

        flechaIzq.setPosition(campoSprite.getX() - flechaIzq.getWidth() - 5, yF);
        flechaDer.setPosition(campoSprite.getX() + campoSprite.getWidth() + 5, yF);
        flechaDer.setRotation(180);

        // Goles
        indiceGoles = 0;
        cartelGoles = new Sprite(Assets.tex("menuCreacion/golesCartel1.png"));
        cartelGoles.setPosition(30, 70);

        // Tiempo
        indiceTiempo = 0;
        cartelTiempo = new Sprite(Assets.tex("menuCreacion/tiempoCartel1.png"));
        cartelTiempo.setPosition(
                Gdx.graphics.getWidth() - 30 - cartelTiempo.getWidth(),
                70
        );

        super.inicializarSonido(4);

        // Enviar estado inicial al rival
        enviarCampo();
        enviarGoles();
        enviarTiempo();
    }

    private Texture cargarCampoTexture(String nombre) {
        return cacheCampos.computeIfAbsent(nombre,
                n -> Assets.tex("campos/campo" + n + ".png")
        );
    }

    @Override
    public void render(float delta) {
        super.batch.begin();

        renderizador.renderFondo(delta);
        renderizador.cargarAtrasSiguiente();

        cartelCampo.draw(super.batch);
        campoSprite.draw(super.batch);

        flechaIzq.draw(super.batch);
        flechaDer.draw(super.batch);

        cartelGoles.draw(super.batch);
        cartelTiempo.draw(super.batch);

        super.siguienteSprite.setColor(estoyListo ? listo : normal);
        super.siguienteSprite.draw(super.batch);

        super.batch.end();
    }


    // ======================
    //   INPUT LOCAL
    // ======================

    @Override
    public boolean mouseMoved(int x, int y) {
        y = Gdx.graphics.getHeight() - y;

        boolean dAtras = gestor.condicionDentro(x, y, super.atrasSprite);
        super.reproducirSonido(0, dAtras);

        boolean dOk = gestor.condicionDentro(x, y, super.siguienteSprite);
        super.reproducirSonido(1, dOk);

        boolean dG = hit(cartelGoles, x, y);
        cartelGoles.setColor(dG ? listo : normal);

        boolean dT = hit(cartelTiempo, x, y);
        cartelTiempo.setColor(dT ? listo : normal);

        updateFlechaHover(flechaIzq, x, y, true);
        updateFlechaHover(flechaDer, x, y, false);

        return true;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        y = Gdx.graphics.getHeight() - y;

        // Campo
        if (hit(flechaIzq, x, y)) {
            cambiarCampo(-1);
            return true;
        }
        if (hit(flechaDer, x, y)) {
            cambiarCampo(+1);
            return true;
        }

        // Goles
        if (hit(cartelGoles, x, y)) {
            indiceGoles = (indiceGoles + 1) % opcionesGoles.length;
            cartelGoles.setTexture(Assets.tex("menuCreacion/golesCartel" +
                    opcionesGoles[indiceGoles] + ".png"));
            enviarGoles();
            resetReady();
            return true;
        }

        // Tiempo
        if (hit(cartelTiempo, x, y)) {
            indiceTiempo = (indiceTiempo + 1) % opcionesTiempo.length;
            cartelTiempo.setTexture(Assets.tex("menuCreacion/tiempoCartel" +
                    opcionesTiempo[indiceTiempo] + ".png"));
            enviarTiempo();
            resetReady();
            return true;
        }

        // Atrás
        if (gestor.condicionDentro(x,y, super.atrasSprite)) {
            super.juego.setScreen(new MenuEnLinea(super.juego, super.juego.cliente));
            return true;
        }

        // READY
        if (gestor.condicionDentro(x, y, super.siguienteSprite)) {
            estoyListo = !estoyListo;
            enviarReady();
            if (estoyListo && rivalListo) avanzar();
            return true;
        }

        return false;
    }


    private void cambiarCampo(int delta) {
        indiceCampo = (indiceCampo + delta + listaCampos.length) % listaCampos.length;
        String nombre = listaCampos[indiceCampo].getNombre();

        campoSprite.setTexture(cargarCampoTexture(nombre));
        campoSprite.setSize(cartelCampo.getWidth(), 200);
        campoSprite.setPosition(cartelCampo.getX(), cartelCampo.getY() - 200);

        enviarCampo();
        resetReady();
    }


    private void resetReady() {
        estoyListo = false;
        enviarReady();
        rivalListo = false;
    }


    // ======================
    //     SINCRONIZACIÓN
    // ======================

    private void enviarCampo() {
        if (cliente != null)
            cliente.enviar("CFG_CAMPO=" + listaCampos[indiceCampo].getNombre());
    }

    private void enviarGoles() {
        if (cliente != null)
            cliente.enviar("CFG_GOLES=" + opcionesGoles[indiceGoles]);
    }

    private void enviarTiempo() {
        if (cliente != null)
            cliente.enviar("CFG_TIEMPO=" + opcionesTiempo[indiceTiempo]);
    }

    private void enviarReady() {
        if (cliente != null)
            cliente.enviar("READY_CAMPO=" + (estoyListo ? "1" : "0"));
    }


    // ====== MÉTODOS QUE EL HILOCLIENTE LLAMA AL RECIBIR MENSAJES =======

    public void aplicarCampoRemoto(String nombre) {
        for (int i = 0; i < listaCampos.length; i++) {
            if (listaCampos[i].getNombre().equals(nombre)) {
                indiceCampo = i;
                campoSprite.setTexture(cargarCampoTexture(nombre));
                return;
            }
        }
    }

    public void aplicarGolesRemoto(int g) {
        for (int i = 0; i < opcionesGoles.length; i++) {
            if (opcionesGoles[i] == g) {
                indiceGoles = i;
                cartelGoles.setTexture(Assets.tex("menuCreacion/golesCartel" + g + ".png"));
                return;
            }
        }
    }

    public void aplicarTiempoRemoto(int t) {
        for (int i = 0; i < opcionesTiempo.length; i++) {
            if (opcionesTiempo[i] == t) {
                indiceTiempo = i;
                cartelTiempo.setTexture(Assets.tex("menuCreacion/tiempoCartel" + t + ".png"));
                return;
            }
        }
    }

    public void aplicarReadyRemoto(boolean listo) {
        rivalListo = listo;
        if (estoyListo && rivalListo) avanzar();
    }


    // ======================
    //       AVANZAR
    // ======================
    private void avanzar() {

        ConfigCliente config = new ConfigCliente.Builder()
                .agregarSkin(skinLocal)           // skin local
                .campo(listaCampos[indiceCampo].getNombre())
                .goles(opcionesGoles[indiceGoles])
                .tiempo(opcionesTiempo[indiceTiempo])
                .modo("1v1")
                .build();

        //cliente.enviarConfig(config);

        juego.actualizarPantalla(new PantallaEsperandoServidor(juego));
    }


    // ======================
    //    UTILS
    // ======================

    private boolean hit(Sprite s, int x, int y) {
        return x >= s.getX() && x <= s.getX()+s.getWidth() &&
                y >= s.getY() && y <= s.getY()+s.getHeight();
    }

    private void updateFlechaHover(Sprite f, int x, int y, boolean izq) {
        boolean d = hit(f, x, y);
        f.setTexture(d ? flechaHoverTex : flechaNormalTex);
        if (!izq) f.setRotation(180);
    }
}
