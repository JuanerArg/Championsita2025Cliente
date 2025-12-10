package com.championsita.jugabilidad.visuales;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.Map;

import com.championsita.jugabilidad.herramientas.Direccion;

public class AnimacionesFactory {

    // -------------------------
    //     PELOTA
    // -------------------------
    public static Animation<TextureRegion> obtenerAnimacionPelota() {
        Texture sheet = new Texture("pelota/pelotaAnimada.png");

        int columnas = 6;
        int filas = 1;

        TextureRegion[][] tmp = TextureRegion.split(
                sheet,
                sheet.getWidth() / columnas,
                sheet.getHeight() / filas
        );

        TextureRegion[] frames = new TextureRegion[columnas];
        for (int i = 0; i < columnas; i++) {
            frames[i] = tmp[0][i];
        }

        return new Animation<>(0.08f, frames);
    }

    // -------------------------
    //     JUGADOR
    // -------------------------
    public static Map<Direccion, Animation<TextureRegion>> cargarAnimacionesPersonaje(String skin) {

        EnumMap<Direccion, Animation<TextureRegion>> mapa = new EnumMap<>(Direccion.class);

        mapa.put(Direccion.DERECHA,          cargar(sheet(skin, "jugadorCorriendoDerecha.png"), 7, 1));
        mapa.put(Direccion.IZQUIERDA,        cargar(sheet(skin, "jugadorCorriendoIzquierda.png"), 7, 1));
        mapa.put(Direccion.ARRIBA,           cargar(sheet(skin, "jugadorCorriendoArriba.png"), 6, 1));
        mapa.put(Direccion.ABAJO,            cargar(sheet(skin, "jugadorCorriendoAbajo.png"), 6, 1));
        mapa.put(Direccion.ARRIBA_DERECHA,   cargar(sheet(skin, "jugadorCorriendoArribaDerecha.png"), 6, 1));
        mapa.put(Direccion.ARRIBA_IZQUIERDA, cargar(sheet(skin, "jugadorCorriendoArribaIzquierda.png"), 6, 1));
        mapa.put(Direccion.ABAJO_DERECHA,    cargar(sheet(skin, "jugadorCorriendoAbajoDerecha.png"), 6, 1));
        mapa.put(Direccion.ABAJO_IZQUIERDA,  cargar(sheet(skin, "jugadorCorriendoAbajoIzquierda.png"), 6, 1));

        return mapa;
    }

    public static TextureRegion cargarFrameQuieto(String skin) {
        Texture quieto = new Texture("jugador/" + skin + "/Jugador.png");
        return new TextureRegion(quieto);
    }

    private static Texture sheet(String skin, String archivo) {
        return new Texture("jugador/" + skin + "/" + archivo);
    }

    private static Animation<TextureRegion> cargar(Texture sheet, int columnas, int filas) {
        TextureRegion[][] tmp = TextureRegion.split(sheet, sheet.getWidth() / columnas, sheet.getHeight() / filas);
        TextureRegion[] frames = new TextureRegion[columnas * filas];

        int idx = 0;
        for (int i = 0; i < filas; i++)
            for (int j = 0; j < columnas; j++)
                frames[idx++] = tmp[i][j];

        return new Animation<>(0.1f, frames);
    }
}
