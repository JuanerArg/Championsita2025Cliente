package com.championsita.partida.herramientas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;

import com.championsita.Principal;
import com.championsita.jugabilidad.constantes.Constantes;
import com.championsita.jugabilidad.herramientas.Texto;
import com.championsita.menus.EnLinea.MenuEnLinea;
import com.championsita.red.HiloCliente;

import static com.championsita.red.EstadoCliente.CONEXION_ESTABLECIDA;

public class PantallaEsperandoServidor implements Screen {

    private SpriteBatch batch;
    private FitViewport viewport;

    private Texto texto;
    private HiloCliente cliente;

    private float timerReintento = 0;
    private boolean mostrarBotonReintentar = false;

    private Principal juego;

    public PantallaEsperandoServidor(Principal juego) {
        this.batch = new SpriteBatch();
        this.viewport = new FitViewport(1280, 720);
        this.juego = juego;

        this.texto = new Texto(Constantes.fuente1, 32, Color.WHITE, 2f, Color.BLACK);

        // Crear y arrancar cliente en thread separado
        cliente = new HiloCliente(juego);
        cliente.start();


        this.texto.setTexto("Buscando servidor...");
        texto.setPosition(
                Gdx.graphics.getWidth()/4 - (int)(texto.getAncho()),
                Gdx.graphics.getHeight()/4 - (int)(texto.getAlto())
        );


    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        actualizarEstado(delta);
        texto.dibujar(batch);

        batch.end();
    }


    private void actualizarEstado(float delta) {

        switch (cliente.estado) {

            case BUSCANDO_SERVIDOR:
                texto.setPosition(
                        Gdx.graphics.getWidth()/4 - (int)(texto.getAncho()),
                        Gdx.graphics.getHeight()/4 - (int)(texto.getAlto())
                );
                texto.setTexto("Buscando servidor...");
                break;

            case CONECTANDO:
                texto.setPosition(
                        Gdx.graphics.getWidth()/4 - (int)(texto.getAncho()),
                        Gdx.graphics.getHeight()/4 - (int)(texto.getAlto())
                );
                texto.setTexto("Conectando...");
                break;

            case CONECTADO:
                texto.setPosition(
                        Gdx.graphics.getWidth()/4 - (int)(texto.getAncho()),
                        Gdx.graphics.getHeight()/4 - (int)(texto.getAlto())
                );
                texto.setTexto("Conectado!\nEsperando Contrincante...");
                break;

            case CONEXION_ESTABLECIDA:
                this.juego.setScreen(new MenuEnLinea(this.juego, this.cliente));
                break;

            case PERDIDA_CONEXION:
                Gdx.gl.glClearColor(0, 0, 0, 1);
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
                texto.setPosition(
                        Gdx.graphics.getWidth()/4 - (int)(texto.getAncho()),
                        Gdx.graphics.getHeight()/4 - (int)(texto.getAlto())
                );
                texto.setTexto("Se perdió la conexión con el servidor.");
                break;

            case DESCONECTADO:
                texto.setPosition(
                        Gdx.graphics.getWidth()/4 - (int)(texto.getAncho()),
                        Gdx.graphics.getHeight()/4 - (int)(texto.getAlto())
                );
                texto.setTexto("No se encontró servidor.\nPresioná ENTER para reintentar.");
                mostrarBotonReintentar = true;
                break;
        }

        if (mostrarBotonReintentar && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            reintentarConexion();
        }
    }


    private void reintentarConexion() {
        mostrarBotonReintentar = false;

        texto.setTexto("Reintentando...");
        texto.setPosition(200, 400);

        // Crear nuevo cliente y volver a arrancar
        cliente = new HiloCliente(juego);
        cliente.start();
    }



    @Override public void resize(int w, int h) { viewport.update(w, h); }
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() { batch.dispose(); texto.dispose(); }

}
