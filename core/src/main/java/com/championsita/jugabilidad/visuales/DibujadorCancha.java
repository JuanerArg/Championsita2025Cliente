package com.championsita.jugabilidad.visuales;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class DibujadorCancha {

    private final Texture textura;
    private float arcoIzqX, arcoIzqY, arcoIzqW, arcoIzqH;
    private float arcoDerX, arcoDerY, arcoDerW, arcoDerH;

    public DibujadorCancha(String nombreCampo) {
        this.textura = new Texture(nombreCampo);
    }

    public void dibujarCancha(SpriteBatch batch, FitViewport viewport) {
        batch.draw(
                textura,
                0, 0,
                viewport.getWorldWidth(),
                viewport.getWorldHeight()
        );
    }

    public void actualizarArcos(
            float ix, float iy, float iw, float ih,
            float dx, float dy, float dw, float dh)
    {
        this.arcoIzqX = ix;
        this.arcoIzqY = iy;
        this.arcoIzqW = iw;
        this.arcoIzqH = ih;

        this.arcoDerX = dx;
        this.arcoDerY = dy;
        this.arcoDerW = dw;
        this.arcoDerH = dh;
    }

    public void dibujarArcos(ShapeRenderer sh) {
        // Izquierdo
        sh.rect(arcoIzqX, arcoIzqY, arcoIzqW, arcoIzqH);

        // Derecho
        sh.rect(arcoDerX, arcoDerY, arcoDerW, arcoDerH);
    }
}
