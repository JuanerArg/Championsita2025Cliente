package com.championsita.menus.menuprincipal;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.championsita.Principal;
import com.championsita.menus.EnLinea.MenuEnLinea;
import com.championsita.menus.local.Local;
import com.championsita.menus.menuopcion.Opcion;
import com.championsita.partida.herramientas.PantallaEsperandoServidor;
import com.championsita.red.EstadoCliente;

public class Inicial extends Menu {

    private Sprite[] botones;
    private float anchoBotones;
    private float altoBotones;
    private GestorInputMenu gestorMenu;
    private RenderizadorDeMenu renderizador;


    public Inicial(Principal juego) {
        super(juego);
    }

    @Override
    public void show() {
        super.show();

        this.botones = new Sprite[] {
                new Sprite(new Texture("menuInicial/onlineBoton.png")),
                // Sprite(new Texture("menuInicial/localBoton.png")),
                new Sprite(new Texture("menuInicial/opcionesBoton.png")),
                new Sprite(new Texture("menuInicial/salirBoton.png"))
        };


        this.anchoBotones = botones[0].getWidth() - 100;
        this.altoBotones = botones[0].getHeight() - 20;

        int apilar = 0;
        for(int i = 0; i < botones.length; i++) {
            botones[i].setSize(anchoBotones, altoBotones);
            float xBoton = super.anchoPantalla / 2f - anchoBotones / 2f;
            float yBoton = super.altoPantalla / 1.6f - altoBotones / 2f - apilar;
            this.botones[i].setPosition(xBoton, yBoton);
            apilar += 80;
        }

        int cantBotones = 6;
        super.inicializarSonido(cantBotones);
        Gdx.input.setInputProcessor(this);


        //Inicializar Gestores-Herramientas
        gestorMenu = new GestorInputMenu(this);
        renderizador = new RenderizadorDeMenu(this);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewportMenus.apply();
        batch.setProjectionMatrix(viewportMenus.getCamera().combined);

        batch.begin();
        renderizador.renderFondo(delta);
        for (Sprite b : botones) b.draw(batch);
        batch.end();
    }



    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        viewportMenus.apply();
        Vector2 coords = viewportMenus.unproject(new Vector2(x, y));
        Gdx.app.log("TOUCH", "Coords desproyectadas: " + coords);
        for (int i = 0; i < botones.length; i++) {
            Sprite boton = botones[i];
            float bx = boton.getX();
            float by = boton.getY();
            Gdx.app.log("BOTON", "Boton " + i + ": (" + bx + "," + by + ") → (" + (bx + anchoBotones) + "," + (by + altoBotones) + ")");

            if (coords.x >= bx && coords.x <= bx + anchoBotones &&
                    coords.y >= by && coords.y <= by + altoBotones) {
                Gdx.app.log("HIT", "Click sobre botón " + i);
                cambiarMenu(i);
                return true;
            }
        }

        return false;
    }



    @Override
    public boolean mouseMoved(int x, int y) {
        viewportMenus.apply();
        Vector2 coords = viewportMenus.unproject(new Vector2(x, y));
        boolean hitAlgo = false;

        for (int i = 0; i < botones.length; i++) {
            Sprite boton = botones[i];
            boolean dentro = gestorMenu.condicionDentro((int) coords.x, (int) coords.y, boton);
            gestorMenu.condicionColor(dentro, boton);
            super.reproducirSonido(i, dentro);
            hitAlgo |= dentro;
        }

        return hitAlgo;
    }


    private void cambiarMenu(int i) {
        switch (i) {
            case 0: {
                super.juego.setScreen(new PantallaEsperandoServidor(super.juego));
                break;
            }
//            case 1: {
                // LOCAL => submenú propio con 2 Jugadores y Práctica
//                super.juego.actualizarPantalla(new Local(super.juego));
//                break;
//            }
            case 1: {
                super.juego.setScreen(new Opcion(super.juego));
                break;
            }
            case 2: {
                // SALIR
                Gdx.app.exit();
                break;
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
