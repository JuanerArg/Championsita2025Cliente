package com.championsita.jugabilidad.visuales;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.championsita.red.EstadoPelota;

public class DibujadorPelota {

    private final Animation<TextureRegion> animacion;

    // datos que llegan del servidor
    private float x, y;
    private float width, height;
    private float stateTime;
    private boolean animar;

    public DibujadorPelota(Animation<TextureRegion> animacion) {
        this.animacion = animacion;
    }

    // NUEVO — recibe directamente el objeto del estado
    public void actualizar(EstadoPelota ep) {
        this.x = ep.x;
        this.y = ep.y;
        this.width = ep.width;
        this.height = ep.height;
        this.stateTime = ep.stateTime;
        this.animar = ep.animar;
    }

    private TextureRegion obtenerFrame() {
        if (animar) {
            return animacion.getKeyFrame(stateTime, true);
        } else {
            return animacion.getKeyFrame(0f, true);
        }
    }

    public void dibujarPelota(SpriteBatch batch) {
        TextureRegion frame = obtenerFrame();
        batch.draw(frame, x, y, width, height);
    }
}
