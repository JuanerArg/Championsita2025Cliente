package com.championsita.jugabilidad.visuales;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

import com.championsita.jugabilidad.herramientas.Direccion;
import com.championsita.red.EstadoPersonaje;

public class DibujadorJugador {


    private final Map<Direccion, Animation<TextureRegion>> animaciones;
    private final TextureRegion frameQuieto;

    // Estado recibido por red
    private float x, y;
    private float ancho, alto;
    private Direccion direccionActual;
    private boolean estaMoviendo;
    private float tiempoAnimacion;

    // HUD
    private final HudPersonaje hud;
    private float staminaActual, staminaMaxima;

    public DibujadorJugador(
            Map<Direccion, Animation<TextureRegion>> animaciones,
            TextureRegion frameQuieto
    ) {
        this.animaciones = animaciones;
        this.frameQuieto = frameQuieto;

        this.hud = new HudPersonaje(); // lo adaptamos para funcionar sin Personaje
    }

    public void actualizar(EstadoPersonaje estadoPersonaje) {
        this.x = estadoPersonaje.x;
        this.y = estadoPersonaje.y;
        this.ancho = estadoPersonaje.ancho;
        this.alto = estadoPersonaje.alto;

        this.direccionActual = Direccion.fromString(estadoPersonaje.direccion);
        this.estaMoviendo = estadoPersonaje.estaMoviendo;
        this.tiempoAnimacion = estadoPersonaje.tiempoAnimacion;

        this.staminaActual = estadoPersonaje.staminaActual;
        this.staminaMaxima = estadoPersonaje.staminaMaxima;
    }

    public void dibujarJugador(SpriteBatch batch) {
        TextureRegion frame;
        //System.out.println(estaMoviendo);
        if (estaMoviendo) {
            Animation<TextureRegion> anim = animaciones.get(direccionActual);
            frame = anim.getKeyFrame(tiempoAnimacion, true);
        } else {
            frame = frameQuieto;
        }

        batch.draw(frame, x, y, ancho, alto);

        // Dibujar HUD (posible solo si lo adaptás)
        if (hud != null) {
            hud.dibujarBarraStamina(batch, x, y, this.alto, this.ancho, staminaActual, staminaMaxima);
        }
    }
}
