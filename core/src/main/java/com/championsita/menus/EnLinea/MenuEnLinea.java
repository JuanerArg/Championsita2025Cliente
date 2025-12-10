package com.championsita.menus.EnLinea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.championsita.Principal;
import com.championsita.menus.herramientas.ConfigCliente;
import com.championsita.menus.menuprincipal.GestorInputMenu;
import com.championsita.menus.menuprincipal.Inicial;
import com.championsita.menus.menuprincipal.Menu;
import com.championsita.menus.menuprincipal.RenderizadorDeMenu;
import com.championsita.red.HiloCliente;
import com.championsita.red.LobbySync;

/**
 * Menú de configuración del modo ONLINE.
 * - Selección de modo (cambia sprite)
 * - Botón "listo"
 * - Sincronización con rival vía HiloCliente
 */
public class MenuEnLinea extends Menu implements LobbySync {

    // ====================================================
    // CAMPOS
    // ====================================================

    private Sprite spriteModo;
    private int indiceModo;
    private ModosEnLinea[] modos;

    private GestorInputMenu gestorInput;
    private RenderizadorDeMenu renderizador;

    private boolean estoyListo = false;
    private boolean rivalListo = false;

    private final Color colorNormal = new Color(1, 1, 1, 1);
    private final Color colorListo  = new Color(0, 1, 0, 1);

    private final HiloCliente cliente;

    private String modoActual = "1vs1";


    // ====================================================
    // CONSTRUCTOR
    // ====================================================
    public MenuEnLinea(Principal juego, HiloCliente cliente) {
        super(juego);
        this.cliente = cliente;

        if (cliente != null) {
            cliente.setLobbyPantalla(this);
        }
    }


    // ====================================================
    // CICLO DE VIDA DE PANTALLA
    // ====================================================
    @Override
    public void show() {
        super.show();

        inicializarModos();
        inicializarSpriteModo();
        inicializarInput();
        inicializarSonidos();
        sincronizarModoConRival();
    }


    @Override
    public void render(float delta) {
        super.render(delta);

        super.batch.begin();
        renderizador.renderFondo(delta);
        renderSpriteModo();
        renderBotones();
        super.batch.end();

        intentarIniciarPartida();
    }


    // ====================================================
    // INICIALIZACIÓN (helpers)
    // ====================================================
    private void inicializarModos() {
        this.modos = ModosEnLinea.values();
        this.indiceModo = 0;
    }

    private void inicializarSpriteModo() {
        this.spriteModo = new Sprite(modos[indiceModo].getTextura());
        this.spriteModo.setSize(500, 300);
        this.spriteModo.setPosition(
                (super.anchoPantalla / 2f - spriteModo.getWidth() / 2f),
                (super.altoPantalla / 2f - spriteModo.getHeight() / 2f)
        );
    }

    private void inicializarInput() {
        Gdx.input.setInputProcessor(this);
        this.gestorInput = new GestorInputMenu(this);
        this.renderizador = new RenderizadorDeMenu(this);
    }

    private void inicializarSonidos() {
        super.inicializarSonido(2);
    }


    // ====================================================
    // RENDER
    // ====================================================
    private void renderSpriteModo() {
        spriteModo.draw(super.batch);
    }

    private void renderBotones() {
        super.siguienteSprite.setColor(estoyListo ? colorListo : colorNormal);
        super.siguienteSprite.draw(super.batch);
        super.atrasSprite.draw(super.batch);
    }


    // ====================================================
    // INPUT
    // ====================================================
    @Override
    public boolean mouseMoved(int x, int y) {
        y = Gdx.graphics.getHeight() - y;
        boolean dentroAtras = gestorInput.condicionDentro(x, y, super.atrasSprite);

        gestorInput.condicionColor(dentroAtras, super.atrasSprite);
        super.reproducirSonido(1, dentroAtras);

        return dentroAtras;
    }


    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        y = Gdx.graphics.getHeight() - y;

        if (manejarClickEnModo(x, y, button)) return true;
        if (manejarClickEnListo(x, y)) return true;
        if (manejarClickEnAtras(x, y)) return true;

        return false;
    }


    // ====================================================
    // INPUT HANDLERS
    // ====================================================
    private boolean manejarClickEnModo(int x, int y, int button) {

        if (!gestorInput.condicionDentro(x, y, spriteModo)) return false;

        cambiarModo(button);
        actualizarSpriteModo();
        sincronizarModoConRival();
        enviarReady(false); // reset de ready local

        return true;
    }


    private boolean manejarClickEnListo(int x, int y) {
        if (!gestorInput.condicionDentro(x, y, super.siguienteSprite)) return false;

        estoyListo = true;
        enviarReady(true);

        return true;
    }


    private boolean manejarClickEnAtras(int x, int y) {
        if (!gestorInput.condicionDentro(x, y, super.atrasSprite)) return false;

        super.juego.actualizarPantalla(new Inicial(super.juego));
        return true;
    }


    // ====================================================
    // CAMBIO DE MODO
    // ====================================================
    private void cambiarModo(int button) {
        if (button == Input.Buttons.LEFT) {
            indiceModo = (indiceModo + 1) % modos.length;
        } else if (button == Input.Buttons.RIGHT) {
            indiceModo = (indiceModo - 1 + modos.length) % modos.length;
        }
    }

    private void actualizarSpriteModo() {
        spriteModo.setTexture(modos[indiceModo].getTextura());
        modoActual = modos[indiceModo].getCodigo();
    }


    // ====================================================
    // READY + SINCRONIZACIÓN
    // ====================================================
    private void enviarReady(boolean listo) {
        if (cliente == null) return;
        cliente.enviar("READY_MODE=" + (listo ? "1" : "0"));
    }

    private void sincronizarModoConRival() {
        if (cliente == null) return;

        ModosEnLinea modo = modos[indiceModo];
        cliente.enviar("CFG_MODO=" + modo.getCodigo());
    }


    @Override
    public void aplicarReadyRival(boolean listo) {
        rivalListo = listo;
    }


    @Override
    public void aplicarModoRival(String codigoModo) {
        ModosEnLinea modo = ModosEnLinea.fromCodigo(codigoModo);
        if (modo == null) return;

        this.indiceModo = modo.ordinal();
        this.spriteModo.setTexture(modo.getTextura());
    }

    @Override
    public void actualizarIndiceSkinRival(String substring) {}


    // ====================================================
    // TRANSICIÓN A SIGUIENTE PANTALLA
    // ====================================================
    private void intentarIniciarPartida() {
        if (!estoyListo || !rivalListo) return;

        // TEMPORAL
        System.out.println("AMBOS LISTOS → CAMBIAR A CARGA ONLINE");
        enviarReady(true);
        if(modoActual == null){
            System.err.println("EL MODO NO ESTA");
        }
        cliente.config = new ConfigCliente.Builder().modo(modoActual).build();
        juego.setScreen(new CargaOnlineSkin(juego, cliente));
    }


    // ====================================================
    // DISPOSE
    // ====================================================
    @Override
    public void dispose() {

    }
}
