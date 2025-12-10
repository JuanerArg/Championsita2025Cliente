package com.championsita.partida.herramientas;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.championsita.jugabilidad.visuales.DibujadorCancha;
import com.championsita.jugabilidad.visuales.DibujadorJugador;
import com.championsita.jugabilidad.visuales.DibujadorPelota;
import com.championsita.jugabilidad.visuales.HudPartido;
import com.championsita.red.EstadoPartidaCliente;

import java.util.ArrayList;

public class RenderizadorPartida {

    private EstadoPartidaCliente estadoPartida;

    public void setEstado(EstadoPartidaCliente estado) {
        this.estadoPartida = estado;
    }

    // ======================
    //  FONDO / CANCHA
    // ======================
    public void renderFondo(SpriteBatch batch,
                            FitViewport viewport,
                            DibujadorCancha cancha) {

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        cancha.dibujarCancha(batch, viewport);
        batch.end();
    }

    // ======================
    //  ENTIDADES
    // ======================
    public void renderEntidades(SpriteBatch batch,
                                FitViewport viewport,
                                ArrayList<DibujadorJugador> jugadores,
                                DibujadorPelota dibPelota,
                                String modo) {

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Jugadores

        for (DibujadorJugador dj : jugadores) {
            dj.dibujarJugador(batch);
        }


        // Pelota
        dibPelota.dibujarPelota(batch);

        batch.end();
    }

    // ======================
    //  HUD DEL PARTIDO
    // ======================
    public void renderHudPartido(SpriteBatch batch,
                                 HudPartido hud,
                                 int ancho,
                                 int alto) {

        // HUD en coordenadas de pantalla
        batch.setProjectionMatrix(new Matrix4().setToOrtho2D(0, 0, ancho, alto));

        batch.begin();
        if (estadoPartida != null)
            hud.dibujarHud(batch, estadoPartida);
        batch.end();
    }

    // ======================
    //  ARCOS
    // ======================
    public void renderArcos(ShapeRenderer renderer,
                            FitViewport viewport,
                            DibujadorCancha cancha) {

        viewport.apply();
        renderer.setProjectionMatrix(viewport.getCamera().combined);

        renderer.begin(ShapeRenderer.ShapeType.Line);
        cancha.dibujarArcos(renderer);
        renderer.end();
    }
}
