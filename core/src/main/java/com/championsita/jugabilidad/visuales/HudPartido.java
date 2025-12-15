package com.championsita.jugabilidad.visuales;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.championsita.jugabilidad.constantes.Constantes;
import com.championsita.menus.herramientas.ConfigCliente;
import com.championsita.red.EstadoPartidaCliente;
import com.championsita.jugabilidad.herramientas.Texto;

import java.util.Objects;

public class HudPartido {

    private final Texto marcadorRojo;
    private final Texto marcadorAzul;
    private final Texto tiempo;
    private final Texto ganador;

    public HudPartido() {
        marcadorRojo = new Texto(Constantes.fuente1, 40, com.badlogic.gdx.graphics.Color.RED);
        marcadorAzul = new Texto(Constantes.fuente1, 40, com.badlogic.gdx.graphics.Color.BLUE);
        tiempo       = new Texto(Constantes.fuente1, 32, com.badlogic.gdx.graphics.Color.WHITE);
        ganador      = new Texto(Constantes.fuente1, 50, Color.BLACK);

        marcadorRojo.setPosition(50, 680);
        marcadorAzul.setPosition(1180, 680);
        tiempo.setPosition(600, 680);
        ganador.setPosition(300, 450);
    }

    public void dibujarHud(SpriteBatch batch, EstadoPartidaCliente est, ConfigCliente config) {
        marcadorRojo.setTexto("" + est.golesAzul);
        marcadorAzul.setTexto("" + est.golesRojo);

        tiempo.setTexto(String.format("%.1f", est.tiempo));

        if(!Objects.equals(est.ganador, "")){
            ganador.setPosition(500, 450);
            ganador.setTexto(est.ganador);
            if (!Objects.equals(est.ganador, "EMPATE")) {
                ganador.setPosition(300, 450);
                ganador.setTexto("El equipo " + est.ganador + " a ganado");
            }
            ganador.dibujar(batch);
        }

        marcadorRojo.dibujar(batch);
        marcadorAzul.dibujar(batch);
        tiempo.dibujar(batch);
    }
}
