package com.championsita.jugabilidad.visuales;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class HudPersonaje {

    private Texture texturaBarra;

    // Tamaño de la barra
    private float anchoBarra = 0.5f;
    private float altoBarra  = 0.5f;

    // Offset visual sobre la cabeza del jugador
    private float offsetY = -0.3f;

    public HudPersonaje() {
        texturaBarra = new Texture("BarraStamina.png");
    }

    /**
     * Dibuja la barra de stamina usando datos crudos enviados por el servidor.
     */
    public void dibujarBarraStamina(
            SpriteBatch batch,
            float x, float y,
            float anchoJugador, float altoJugador,
            float staminaActual, float staminaMax
    ) {

        // Porcentaje de stamina
        float porcentaje = staminaActual / staminaMax;

        // Centrar barra sobre el sprite
        float posX = x + (anchoJugador - anchoBarra) / 2f;
        float posY = y + altoJugador + offsetY;

        // Dibujar barra proporcional
        batch.draw(
                texturaBarra,
                posX, posY,
                anchoBarra * porcentaje,
                altoBarra
        );
    }

    public void dispose() {
        texturaBarra.dispose();
    }
}
