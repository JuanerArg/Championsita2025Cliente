package com.championsita.partida.herramientas;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.championsita.Principal;
import com.championsita.jugabilidad.entrada.EntradaJugador;
import com.championsita.menus.pausa.Pausa;
import com.championsita.red.HiloCliente;
import com.championsita.red.EstadoPartidaCliente;
import com.championsita.menus.herramientas.ConfigCliente;

import com.championsita.jugabilidad.visuales.*;

import java.util.ArrayList;

public class PantallaPartida extends InputAdapter implements Screen {

    private final Principal juego;
    private final HiloCliente cliente;
    private final ConfigCliente config;

    private final RenderizadorPartida renderizador = new RenderizadorPartida();

    private SpriteBatch batch;
    private ShapeRenderer shape;
    private FitViewport viewportJuego;

    // Dibujadores
    private DibujadorCancha dibCancha;
    private DibujadorPelota dibPelota;
    private ArrayList<DibujadorJugador> dibJugadores = new ArrayList<>();
    private EntradaJugador inputJugador;
    private HudPartido hud;

    private String modoDeJuego;

    private Pausa pausa;

    public PantallaPartida(Principal juego, HiloCliente cliente, ConfigCliente config) {
        this.juego = juego;
        this.cliente = cliente;
        this.config = config;

        this.batch = juego.getBatch();
        this.shape = new ShapeRenderer();

        this.viewportJuego = new FitViewport(8f, 5f);
        resize(8,5);
        this.modoDeJuego = config.modo;

        inputJugador = new EntradaJugador(Input.Keys.W,
                Input.Keys.S,
                Input.Keys.A,
                Input.Keys.D,
                Input.Keys.SPACE,
                Input.Keys.SHIFT_LEFT,
                this.cliente);
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        multiplexer.addProcessor(inputJugador);
        Gdx.input.setInputProcessor(multiplexer);
        this.pausa = new Pausa(juego, this.cliente);
        inicializarDibujadores();
    }

    // ===========================================
    //   INICIALIZAR DIBUJADORES SEGÚN CONFIG
    // ===========================================
    private void inicializarDibujadores() {

        // ------------------------
        // CANCHA + ARCOS
        // ------------------------
        this.dibCancha = new DibujadorCancha(
                "campos/campo" + config.campo + ".png"
        );

        // ------------------------
        // PELOTA
        // ------------------------
        this.dibPelota = new DibujadorPelota(
                AnimacionesFactory.obtenerAnimacionPelota()
        );

        // ------------------------
        // JUGADORES
        // ------------------------
        for (String skin : config.skinsJugadores) {
            DibujadorJugador dib =
                    new DibujadorJugador(
                            AnimacionesFactory.cargarAnimacionesPersonaje(skin),
                            AnimacionesFactory.cargarFrameQuieto(skin)
                    );
            dibJugadores.add(dib);
        }

        // ------------------------
        // HUD PARTIDO
        // ------------------------
        this.hud = new HudPartido();
    }

    @Override
    public void show() {
        this.pausa.show();
    }

    // ===========================================
    //               RENDER
    // ===========================================
    @Override
    public void render(float delta) {
        if (batch.isDrawing()) System.out.println("ERROR: batch quedó abierto");
        EstadoPartidaCliente estado = cliente.estadoActual;
        inputJugador.enviarInput(cliente);

        if (estado != null) {

            // Jugadores
            if (estado.jugadores.size() == dibJugadores.size()) {
                for (int i = 0; i < dibJugadores.size(); i++) {
                    try{
                        dibJugadores.get(i).actualizar(estado.jugadores.get(i));
                    }catch (IndexOutOfBoundsException e){
                        System.err.println("Tamaño de jugadores no coincide: estado=" + estado.jugadores.size() + " dibujadores=" + dibJugadores.size());
                    }
                }
            } else {
                System.out.println("Tamaño de jugadores no coincide: estado=" + estado.jugadores.size() + " dibujadores=" + dibJugadores.size());
            }


            // Pelota
            dibPelota.actualizar(estado.pelota);

            // Arcos de la cancha
            dibCancha.actualizarArcos(
                    estado.arcoIzq.x, estado.arcoIzq.y,
                    estado.arcoIzq.w, estado.arcoIzq.h,
                    estado.arcoDer.x, estado.arcoDer.y,
                    estado.arcoDer.w, estado.arcoDer.h
            );

            renderizador.setEstado(estado);
        }

        // ---------------------
        // LIMPIAR PANTALLA
        // ---------------------
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ---------------------
        // DIBUJAR CANCHA
        // ---------------------
        renderizador.renderFondo(batch, viewportJuego, dibCancha);

        // ---------------------
        // DIBUJAR ENTIDADES
        // ---------------------
        renderizador.renderEntidades(
                batch,
                viewportJuego,
                dibJugadores,
                dibPelota,
                modoDeJuego
        );

        // ---------------------
        // DIBUJAR ARCOS
        //----------------------
        try {
            renderizador.renderArcos(shape, viewportJuego, dibCancha);
        } catch (Exception e) {
            System.out.println("EXCEPCIÓN OCULTA EN ARCOS: " + e.getMessage());
        }

        // ---------------------
        // HUD
        // ---------------------
        renderizador.renderHudPartido(batch, hud, 1280, 720);
        this.pausa.renderizar(batch);
    }

    @Override
    public boolean keyDown(int key) {
        return this.pausa.keyDown(key);
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        y = Gdx.graphics.getHeight() - y;
        return this.pausa.mouseMoved(x, y);
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        y = Gdx.graphics.getHeight() - y;
        return this.pausa.touchUp(x, y, pointer, button);
    }

    @Override public void resize(int w, int h) {
        viewportJuego.update(w, h, true);
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override
    public void dispose() {
        if (shape != null) shape.dispose();
        Gdx.input.setInputProcessor(null);
    }

}
