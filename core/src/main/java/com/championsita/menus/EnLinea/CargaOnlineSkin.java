package com.championsita.menus.EnLinea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.championsita.Principal;
import com.championsita.menus.menueleccion.Jugador;
import com.championsita.menus.menueleccion.JugadorDos;
import com.championsita.menus.menueleccion.JugadorUno;
import com.championsita.menus.menuprincipal.GestorInputMenu;
import com.championsita.menus.menuprincipal.Menu;
import com.championsita.menus.menuprincipal.RenderizadorDeMenu;
import com.championsita.menus.compartido.Assets;
import com.championsita.red.HiloCliente;
import com.championsita.red.LobbySync;

/**
 * Pantalla online donde cada jugador elige SOLO su skin.
 * El rival solo se muestra (sprite chiquito).
 *
 * Sincronización mínima:
 *  - READY_SKIN=1/0 para avanzar a la siguiente pantalla
 *
 * Cuando ambos están listos -> pasa a CargaOnlineCampo
 */
public class CargaOnlineSkin extends Menu implements LobbySync {

    private JugadorUno[] skinsJugador1;
    private JugadorDos[] skinsJugador2;
    private Jugador[] skinsLocales;
    private int idxSkinLocal;
    private Sprite spriteLocal;

    private Sprite spriteRival; // Se actualiza desde red

    private boolean estoyListo = false;
    private boolean rivalListo = false;

    private Color normal = new Color(1, 1, 1, 1);
    private Color listo = new Color(0, 1, 0, 1);

    private GestorInputMenu gestor;
    private RenderizadorDeMenu renderizador;

    private final HiloCliente cliente;

    public CargaOnlineSkin(Principal juego, HiloCliente cliente) {
        super(juego);
        this.cliente = cliente;
        this.cliente.setLobbyPantalla(this);
    }

    @Override
    public void show() {
        super.show();
        Gdx.input.setInputProcessor(this);

        skinsLocales = Jugador.values();
        idxSkinLocal = 0;

        spriteLocal = crearSpriteJugador(
                skinsLocales[idxSkinLocal].getNombre(),
                300, 120, 420, 420
        );

        // sprite del rival (más pequeño)
        spriteRival = crearSpriteJugador(
                skinsLocales[0].getNombre(),
                50, 380, 150, 150
        );

        renderizador = new RenderizadorDeMenu(this);
        gestor = new GestorInputMenu(this);

        super.inicializarSonido(4);
        renderizador.crearFlechas(2);

        int y = 160;
        super.flechas[0].setPosition(240, y);
        super.flechas[1].setPosition(640, y);
    }

    private Sprite crearSpriteJugador(String skin, float x, float y, float w, float h) {
        Sprite s = new Sprite(Assets.tex("jugador/" + skin.toLowerCase() + "/Jugador.png"));
        s.setBounds(x, y, w, h);
        return s;
    }

    private void cambiarSkin(boolean derecha) {
        idxSkinLocal = (idxSkinLocal + (derecha ? 1 : -1) + skinsLocales.length) % skinsLocales.length;

        String nombre = skinsLocales[idxSkinLocal].getNombre();
        spriteLocal.setTexture(Assets.tex("jugador/" + nombre.toLowerCase() + "/Jugador.png"));
        if (cliente != null) cliente.enviar("SKIN_RIVAL=jugador/" + nombre.toLowerCase() + "/Jugador.png");
    }

    @Override
    public void render(float delta) {
        super.batch.begin();

        renderizador.renderFondo(delta);
        renderizador.cargarAtrasSiguiente();

        for (Sprite f : super.flechas) f.draw(super.batch);

        spriteLocal.draw(super.batch);
        spriteRival.draw(super.batch);

        super.siguienteSprite.setColor(estoyListo ? listo : normal);
        super.siguienteSprite.draw(super.batch);

        super.batch.end();
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        y = Gdx.graphics.getHeight() - y;

        boolean hit = false;
        for (int i = 0; i < super.flechas.length; i++) {
            boolean dentro = gestor.condicionFlechas(super.flechas[i], x, y);
            super.reproducirSonido(i, dentro);
            hit |= dentro;
        }

        boolean dentroOk = gestor.condicionDentro(x, y, super.siguienteSprite);
        super.reproducirSonido(2, dentroOk);

        boolean dentroAtras = gestor.condicionDentro(x, y, super.atrasSprite);
        super.reproducirSonido(3, dentroAtras);

        return hit || dentroOk || dentroAtras;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        y = Gdx.graphics.getHeight() - y;

        // Cambiar skin
        if (gestor.condicionFlechas(super.flechas[0], x, y)) {
            cambiarSkin(false);
            estoyListo = false;
            enviarReady(false);
            return true;
        }
        if (gestor.condicionFlechas(super.flechas[1], x, y)) {
            cambiarSkin(true);
            estoyListo = false;
            enviarReady(false);
            return true;
        }

        // OK → toggle ready
        if (gestor.condicionDentro(x, y, super.siguienteSprite)) {
            estoyListo = !estoyListo;
            enviarReady(estoyListo);

            if (estoyListo && rivalListo) avanzar();
            return true;
        }

        // Atrás
        if (gestor.condicionDentro(x, y, super.atrasSprite)) {
            super.juego.setScreen(new MenuEnLinea(super.juego, super.juego.cliente));
            return true;
        }

        return false;
    }

    private void enviarReady(boolean v) {
        if (cliente != null)
            cliente.enviar("READY_SKIN=" + (v ? "1" : "0"));
    }


    @Override
    public void aplicarReadyRival(boolean listo) {
        this.rivalListo = listo;
        if (estoyListo && rivalListo) avanzar();
    }

    public void aplicarSkinRival(String skin) {
        spriteRival.setTexture(Assets.tex(skin.toLowerCase()));
    }

    private void avanzar() {
        juego.actualizarPantalla(new CargaOnlineCampo(juego, cliente,
                skinsLocales[idxSkinLocal].getNombre()
        ));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
