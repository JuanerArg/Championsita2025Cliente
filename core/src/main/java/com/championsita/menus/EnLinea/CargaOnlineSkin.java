package com.championsita.menus.EnLinea;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.championsita.Principal;
import com.championsita.jugabilidad.constantes.Constantes;
import com.championsita.jugabilidad.herramientas.HabilidadesEspeciales;
import com.championsita.jugabilidad.herramientas.Texto;
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

    private Jugador[] skinsLocales;
    private int idxSkinLocal;
    private int idxSkinRival;
    private Sprite spriteLocal;
    private String nombreSkinElegida;
    private String nombreSkinRival;

    private Sprite spriteRival; // Se actualiza desde red

    private boolean estoyListo = false;
    private boolean rivalListo = false;

    private Color normal = new Color(1, 1, 1, 1);
    private Color listo = new Color(0, 1, 0, 1);

    private GestorInputMenu gestor;
    private RenderizadorDeMenu renderizador;

    private final HiloCliente cliente;
    private HabilidadesEspeciales[] habilidadesLocales;
    private Texto habilidades = new Texto(Constantes.fuente1, 30,Color.BLACK);
    private int idxHabLocal;
    boolean esEspecial;

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
                300, 180, 420, 420
        );

        // sprite del rival (más pequeño)
        spriteRival = crearSpriteJugador(
                skinsLocales[0].getNombre(),
                50, 380, 150, 150
        );

        renderizador = new RenderizadorDeMenu(this);
        gestor = new GestorInputMenu(this);

        super.inicializarSonido(4);

        esEspecial = cliente.config.modo.equals("especial");

        int totalFlechas = esEspecial ? 4 : 2;
        renderizador.crearFlechas(totalFlechas);

        int y = 220;
        super.flechas[0].setPosition(270, y);
        super.flechas[1].setPosition(670, y);

        if (esEspecial) {
            habilidadesLocales = HabilidadesEspeciales.values();
            idxHabLocal = 0;
        }

        if (esEspecial) {
            int yHab = 40;   // o donde quieras ponerlas debajo del sprite grande

            flechas[2].setPosition(270, yHab); // izquierda habilidad
            flechas[3].setPosition(670, yHab); // derecha habilidad
        }

    }

    private Sprite crearSpriteJugador(String skin, float x, float y, float w, float h) {
        Sprite s = new Sprite(Assets.tex("jugador/" + skin.toLowerCase() + "/Jugador.png"));
        s.setBounds(x, y, w, h);
        return s;
    }

    private void cambiarSkin(boolean derecha) {
        idxSkinLocal = (idxSkinLocal + (derecha ? 1 : -1) + skinsLocales.length) % skinsLocales.length;

        nombreSkinElegida = skinsLocales[idxSkinLocal].getNombre();
        spriteLocal.setTexture(Assets.tex("jugador/" + nombreSkinElegida.toLowerCase() + "/Jugador.png"));
        if (cliente != null) {
            cliente.enviar("SKIN_RIVAL=jugador/" + nombreSkinElegida.toLowerCase() + "/Jugador.png");
            cliente.enviar("SKIN_RIVAL=" + idxSkinLocal);
        }
    }

    @Override
    public void render(float delta) {
        super.batch.begin();

        renderizador.renderFondo(delta);
        renderizador.cargarAtrasSiguiente();

        for (Sprite f : super.flechas) f.draw(super.batch);

        spriteLocal.draw(super.batch);
        spriteRival.draw(super.batch);

        if (esEspecial) {
            habilidades.setTexto(String.valueOf(habilidadesLocales[idxHabLocal]));
            habilidades.setPosition(400, 120);
            habilidades.dibujar(super.batch);
        }

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

        if (esEspecial){
            //cambiar habilidad
            if(gestor.condicionFlechas(super.flechas[2], x, y)){
                cambiarHabilidad(false);
                return true;
            }
            if(gestor.condicionFlechas(super.flechas[3], x, y)){
                cambiarHabilidad(true);
                return true;
            }
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

    private void cambiarHabilidad(boolean derecha) {
        if (esEspecial) {
            if (!derecha) {
                idxHabLocal--;
                if (idxHabLocal < 0) idxHabLocal = habilidadesLocales.length - 1;

            }

            if (derecha) {
                idxHabLocal++;
                if (idxHabLocal >= habilidadesLocales.length) idxHabLocal = 0;
            }
        }
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

    @Override
    public void actualizarIndiceSkinRival(String indiceRival){
        this.idxSkinRival = Integer.parseInt(String.valueOf(indiceRival));
    }

    private void avanzar() {
        String miSkin = skinsLocales[idxSkinLocal].getNombre().toLowerCase();
        String rivalSkin = skinsLocales[idxSkinRival].getNombre().toLowerCase();

        if (cliente.getIdJugador() == 1) {
            // Soy jugador ROJO → mi skin va primero
            cliente.config.skinsJugadores.add(miSkin);
            cliente.config.skinsJugadores.add(rivalSkin);
        } else {
            // Soy jugador AZUL → mi skin va segundo
            cliente.config.skinsJugadores.add(rivalSkin);
            cliente.config.skinsJugadores.add(miSkin);
        }
        if(esEspecial){
            cliente.config.habilidadesEspeciales.add(String.valueOf(habilidadesLocales[idxHabLocal]));
        }
        juego.actualizarPantalla(new CargaOnlineCampo(juego, cliente));
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
