package com.championsita.menus.EnLinea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.championsita.Principal;
import com.championsita.menus.menuprincipal.GestorInputMenu;
import com.championsita.menus.menuprincipal.Inicial;
import com.championsita.menus.menuprincipal.Menu;
import com.championsita.menus.menuprincipal.RenderizadorDeMenu;
import com.championsita.red.HiloCliente;
import com.championsita.red.LobbySync;

/**
 * Menú de configuración del modo de juego ONLINE.
 * Visualmente se comporta como "Opcion":
 * - Un solo sprite grande que cambia según el índice.
 * - Click izquierdo / derecho para avanzar / retroceder.
 *
 * Cada cambio de modo se notifica al servidor con:
 *   CFG_MODO=<codigo>
 *
 * El otro cliente recibe el mensaje y actualiza su índice.
 */
public class MenuEnLinea extends Menu implements LobbySync {

    private Sprite spriteModo;              // sprite principal (como opciones en Opcion)
    private int indiceModo;                 // índice actual del enum ModosEnLinea
    private ModosEnLinea[] modos;            // cache de valores

    private GestorInputMenu gestorMenu;
    private RenderizadorDeMenu renderizador;

    private boolean estoyListo = false;
    private boolean rivalListo = false;

    private Color colorNormal = new Color(1, 1, 1, 1);
    private Color colorListo  = new Color(0, 1, 0, 1); // verde


    private final HiloCliente cliente;      // hilo de red ya conectado

    public MenuEnLinea(Principal juego, HiloCliente cliente) {
        super(juego);
        this.cliente = cliente;

        // asociamos esta pantalla al cliente para recibir updates remotos
        if (this.cliente != null) {
            this.cliente.setPantallaActual(this);
        }
    }

    @Override
    public void show() {
        super.show();

        this.modos = ModosEnLinea.values();
        this.indiceModo = 0;

        // Sprite inicial
        this.spriteModo = new Sprite(modos[indiceModo].getTextura());
        this.spriteModo.setSize(500, 300);
        this.spriteModo.setPosition(
                (super.anchoPantalla / 2f - this.spriteModo.getWidth() / 2f),
                (super.altoPantalla / 2f - this.spriteModo.getHeight() / 2f)
        );

        Gdx.input.setInputProcessor(this);

        this.gestorMenu = new GestorInputMenu(this);
        this.renderizador = new RenderizadorDeMenu(this);

        // Sonidos: 1 slot para "clic" + 1 para atrás (si querés)
        super.inicializarSonido(2);

        // Al entrar, mandamos el modo inicial para que ambos se sincronicen
        enviarModoSeleccionado();
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        super.batch.begin();
        renderizador.renderFondo(delta);
        spriteModo.draw(super.batch);

        // ==== COLOREAR BOTÓN DE LISTO ====
        super.siguienteSprite.setColor(estoyListo ? colorListo : colorNormal);
        super.siguienteSprite.draw(super.batch);

        super.atrasSprite.draw(super.batch);
        super.batch.end();
    }


    @Override
    public boolean mouseMoved(int x, int y) {
        y = Gdx.graphics.getHeight() - y;

        boolean dentroAtras = this.gestorMenu.condicionDentro(x, y, super.atrasSprite);
        this.gestorMenu.condicionColor(dentroAtras, super.atrasSprite);
        super.reproducirSonido(1, dentroAtras);

        // si quisieras hover sobre el spriteModo, podrías hacerlo acá también

        return dentroAtras;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        y = Gdx.graphics.getHeight() - y;

        // ------------------------------
        // CAMBIO DE MODO
        // ------------------------------
        boolean dentroModo = gestorMenu.condicionDentro(x, y, spriteModo);
        if (dentroModo) {

            // cambiar el modo normalmente
            if (button == Input.Buttons.LEFT)
                indiceModo = (indiceModo + 1) % modos.length;
            else if (button == Input.Buttons.RIGHT)
                indiceModo = (indiceModo - 1 + modos.length) % modos.length;

            spriteModo.setTexture(modos[indiceModo].getTextura());

            // sincronizar modo
            enviarModoSeleccionado();

            // -------- DESCONECTAR “LISTO” DE AMBOS --------
            estoyListo = false;
            rivalListo = false;
            enviarReady(false); // notifico que NO estoy listo

            return true;
        }

        // ------------------------------
        // BOTÓN LISTO
        // ------------------------------
        if (gestorMenu.condicionDentro(x, y, super.siguienteSprite)) {

            estoyListo = !estoyListo;     // toggle
            enviarReady(estoyListo);

            // si ambos están listos → avanzar
            if (estoyListo && rivalListo) {
                iniciarCargaOnline();
            }
            return true;
        }

        // ------------------------------
        // ATRÁS
        // ------------------------------
        if (gestorMenu.condicionDentro(x, y, super.atrasSprite)) {
            super.juego.actualizarPantalla(new Inicial(super.juego));
            return true;
        }

        return false;
    }

    private void enviarReady(boolean listo) {
        if (cliente == null) return;
        cliente.enviar("READY=" + (listo ? "1" : "0"));
    }

    private void iniciarCargaOnline() {
        // TEMPORAL – después hacemos la pantalla real
        System.out.println("AMBOS LISTOS → CAMBIAR A PANTALLA DE CARGA ONLINE");
        juego.setScreen(new CargaOnlineSkin(juego, cliente));
    }

    @Override
    public void aplicarModoRival(String codigoModo) {
        ModosEnLinea modo = ModosEnLinea.fromCodigo(codigoModo);
        if (modo == null) return;

        this.indiceModo = modo.ordinal();
        this.spriteModo.setTexture(modo.getTextura());
    }

    /**
     * Envía por red el modo actualmente seleccionado.
     * En el servidor deberías reenviarlo al otro cliente.
     */
    private void enviarModoSeleccionado() {
        if (cliente == null) return;
        ModosEnLinea modo = this.modos[this.indiceModo];
        String msg = "CFG_MODO=" + modo.getCodigo();
        cliente.enviar(msg);
    }

    /**
     * Llamado DESDE el hilo de red (via Gdx.app.postRunnable) cuando llega un mensaje remoto.
     * Actualiza el índice en función del código recibido.
     */
    public void aplicarModoRemoto(String codigoModo) {
        ModosEnLinea modo = ModosEnLinea.fromCodigo(codigoModo);
        if (modo == null) return;

        this.indiceModo = modo.ordinal();
        this.spriteModo.setTexture(modo.getTextura());
    }

    @Override
    public void dispose() {

        // opcional: si no querés que el cliente guarde referencia
        //if (this.cliente != null) {
        //    this.cliente.setPantallaLobby(null);
        //}
    }

    
}
