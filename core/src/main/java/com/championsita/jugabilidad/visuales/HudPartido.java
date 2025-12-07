package com.championsita.jugabilidad.visuales;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.championsita.jugabilidad.constantes.Constantes;
import com.championsita.red.EstadoPartidaCliente;
import com.championsita.jugabilidad.herramientas.Texto;

public class HudPartido {

    private final Texto marcadorRojo;
    private final Texto marcadorAzul;
    private final Texto tiempo;

    public HudPartido() {
        marcadorRojo = new Texto(Constantes.fuente1, 40, com.badlogic.gdx.graphics.Color.RED);
        marcadorAzul = new Texto(Constantes.fuente1, 40, com.badlogic.gdx.graphics.Color.BLUE);
        tiempo       = new Texto(Constantes.fuente1, 32, com.badlogic.gdx.graphics.Color.WHITE);

        marcadorRojo.setPosition(50, 680);
        marcadorAzul.setPosition(1180, 680);
        tiempo.setPosition(600, 680);
    }

    public void dibujarHud(SpriteBatch batch, EstadoPartidaCliente est) {
        marcadorRojo.setTexto("" + est.golesRojo);
        marcadorAzul.setTexto("" + est.golesAzul);

        tiempo.setTexto(String.format("%.1f", est.tiempo));

        marcadorRojo.dibujar(batch);
        marcadorAzul.dibujar(batch);
        tiempo.dibujar(batch);
    }
}
