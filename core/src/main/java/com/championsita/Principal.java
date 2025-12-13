package com.championsita;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.championsita.menus.menuprincipal.Inicial;
import com.championsita.menus.menuprincipal.Menu;
import com.championsita.red.HiloCliente;

public class Principal extends Game {

    private SpriteBatch batch;
    private Menu menu;
    private float volumenMusica;
    private int indiceMusica;
    private Color accionColor;
    public HiloCliente cliente;

    @Override
    public void create() {
        this.batch = new SpriteBatch();
        this.volumenMusica = 0.09f;
        this.indiceMusica = 1;
        this.accionColor = new Color(0, 1, 0, 1);
        this.menu = new Inicial(this);
        this.actualizarPantalla(this.menu);
        Gdx.app.addLifecycleListener(new LifecycleListener() {
            @Override
            public void pause() {}

            @Override
            public void resume() {}

            @Override
            public void dispose() {
                if (cliente != null) {
                    cliente.enviar("DISCONNECT");
                    cliente.detener();
                }
            }
        });

    }

    public SpriteBatch getBatch() {
        return this.batch;
    }

    public void actualizarPantalla(Screen futuraPantalla) {
        Screen anterior = getScreen();
        if (futuraPantalla == anterior) return;

        setScreen(futuraPantalla);

        // Esperar al siguiente frame para liberar la anterior
        if (anterior != null && anterior != futuraPantalla) {
            Gdx.app.postRunnable(anterior::dispose);
        }
    }

    public void volverAlMenuPrincipal(){
        actualizarPantalla(new Inicial(this));
    }

    @Override
    public void dispose() {
        if (cliente != null) {
            cliente.enviar("DISCONNECT");
            cliente.detener();
        }
        this.batch.dispose();
        super.dispose();
    }


    public void setVolumenMusica(float volumenMusica) {
        this.volumenMusica = volumenMusica;
    }

    public float getVolumenMusica() {
        return this.volumenMusica;
    }

    public void setIndiceMusica(int indiceMusica) {
        this.indiceMusica = indiceMusica;
    }

    public int getIndiceMusica() {
        return this.indiceMusica;
    }

    public void setAccionColor(Color accionColor) {
        this.accionColor = accionColor;
    }

    public Color getAccionColor() {
        return this.accionColor;
    }
}
