package com.championsita.menus.menucarga;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.audio.Sound;
import com.championsita.Principal;
import com.championsita.jugabilidad.herramientas.HabilidadesEspeciales;
import com.championsita.menus.herramientas.ConfigCliente;
import com.championsita.menus.menuprincipal.GestorInputMenu;
import com.championsita.menus.menuprincipal.Menu;
import com.championsita.menus.menueleccion.Doble;
import com.championsita.menus.compartido.Assets;
import com.championsita.menus.compartido.OpcionDeGoles;
import com.championsita.menus.compartido.OpcionDeTiempo;
import com.championsita.menus.menuprincipal.RenderizadorDeMenu;
import com.championsita.partida.herramientas.PantallaEsperandoServidor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Carga extends Menu {

    private String equipoJ1;
    private String equipoJ2;

    private SpriteBatch batch;
    private final String pielJugador1;
    private final String pielJugador2;

    private ArrayList<HabilidadesEspeciales> habilidades = new ArrayList<>();
    private final String modo; // "1v1", "practica", "especial"

    private Texture fondoTex;
    private Texture botonJugarTex;

    private Texture controlJugador1Tex;
    private Texture controlJugador2Tex;
    private Sprite controlJugador1;
    private Sprite controlJugador2;

    private Texture cartelCampoTex;
    private Sprite cartelCampo;

    private Texture campoTexActual;
    private Sprite campoSprite;

    private Texture[] golesTex;
    private Texture[] tiempoTex;
    private Sprite cartelGoles;
    private Sprite cartelTiempo;

    private Texture flechaNormalTex;
    private Texture flechaHoverTex;
    private Sprite flechaIzq;
    private Sprite flechaDer;

    private Campo[] listaCampos;
    private int indiceCampo;

    private final int[] opcionesGoles = {1, 3, 5};
    private final int[] opcionesTiempo = {1, 2, 3};
    private int indiceGoles;
    private int indiceTiempo;

    private float[] golesXY;
    private float[] tiempoXY;

    private final Map<String, Texture> cacheCampos = new HashMap<>();

    private Sound hoverSound;
    private Color colorNormal;
    private Color colorHover;

    private static final float ALTURA_CONTROLES = 335f;
    private static final float CAMPOS_PANEL_Y   = 215f;
    private static final float CAMPOS_ALTURA    = 200f;
    private static final float FLECHA_OFFSET    = 5f;

    private GestorInputMenu gestorMenu;
    private RenderizadorDeMenu renderizador;

    // Constructor normal
    public Carga(Principal juego, String pielUno, String pielDos, String modo) {
        super(juego);
        this.pielJugador1 = pielUno;
        this.pielJugador2 = pielDos;
        this.modo = (modo == null ? "1v1" : modo);
        this.equipoJ1 = null;
        this.equipoJ2 = null;
    }

    // Constructor con equipos
    public Carga(Principal juego,
                 String skinJ1,
                 String skinJ2,
                 String modoDestino,
                 String equipoJ1,
                 String equipoJ2) {

        this(juego, skinJ1, skinJ2, modoDestino);
        this.equipoJ1 = equipoJ1;
        this.equipoJ2 = equipoJ2;
    }

    public Carga(Principal juego,
                 String skinJ1,
                 String skinJ2,
                 String modoDestino,
                 String equipoJ1,
                 String equipoJ2,
                 ArrayList<HabilidadesEspeciales> habilidades) {

        this(juego, skinJ1, skinJ2, modoDestino);
        this.equipoJ1 = equipoJ1;
        this.equipoJ2 = equipoJ2;
        this.habilidades.addAll(habilidades);
    }

    @Override
    public void show() {
        super.show();

        this.batch = this.juego.getBatch();
        this.colorNormal = new Color(super.atrasSprite.getColor());
        this.colorHover  = new Color(0, 1, 0, 1);

        this.fondoTex = Assets.tex("menuCreacion/menuDosJug.png");
        super.fondoSprite.setTexture(this.fondoTex);

        this.botonJugarTex = Assets.tex("menuDosJugadores/jugarBoton.png");
        super.siguienteSprite.setTexture(this.botonJugarTex);

        this.controlJugador1Tex = Assets.tex("menuCreacion/primerJugador.png");
        this.controlJugador2Tex = Assets.tex("menuCreacion/segundoJugador.png");
        this.controlJugador1 = new Sprite(this.controlJugador1Tex);
        this.controlJugador2 = new Sprite(this.controlJugador2Tex);
        colocarControles(ALTURA_CONTROLES);

        this.listaCampos = Campo.values();
        this.indiceCampo = 0;

        this.cartelCampoTex = Assets.tex("menuCreacion/campoCartel.png");
        this.cartelCampo = new Sprite(this.cartelCampoTex);
        this.cartelCampo.setPosition(
                Gdx.graphics.getWidth() / 2f - this.cartelCampo.getWidth() / 2f,
                CAMPOS_PANEL_Y
        );

        this.campoTexActual = getCampoTexture(listaCampos[indiceCampo].getNombre());
        this.campoSprite = new Sprite(this.campoTexActual);
        float anchoCampo = this.cartelCampo.getWidth();
        this.campoSprite.setSize(anchoCampo, CAMPOS_ALTURA);
        this.campoSprite.setPosition(this.cartelCampo.getX(), this.cartelCampo.getY() - CAMPOS_ALTURA);

        this.flechaNormalTex = Assets.tex("menuDosJugadores/flechaNormal.png");
        this.flechaHoverTex  = Assets.tex("menuDosJugadores/flechaInvertida.png");
        this.flechaIzq = new Sprite(this.flechaNormalTex);
        this.flechaDer = new Sprite(this.flechaNormalTex);
        float yFlechas = campoSprite.getY() + campoSprite.getHeight() / 3.5f - 20f;
        this.flechaIzq.setPosition(this.campoSprite.getX() - this.flechaIzq.getWidth() - FLECHA_OFFSET, yFlechas);
        this.flechaDer.setPosition(this.campoSprite.getX() + campoSprite.getWidth() + FLECHA_OFFSET, yFlechas);
        this.flechaDer.setRotation(180f);

        this.golesTex  = new Texture[] {
                Assets.tex("menuCreacion/golesCartel1.png"),
                Assets.tex("menuCreacion/golesCartel3.png"),
                Assets.tex("menuCreacion/golesCartel5.png")
        };
        this.tiempoTex = new Texture[] {
                Assets.tex("menuCreacion/tiempoCartel1.png"),
                Assets.tex("menuCreacion/tiempoCartel2.png"),
                Assets.tex("menuCreacion/tiempoCartel3.png")
        };
        this.indiceGoles  = 0;
        this.indiceTiempo = 0;
        this.cartelGoles  = new Sprite(golesTex[indiceGoles]);
        this.cartelTiempo = new Sprite(tiempoTex[indiceTiempo]);

        int ubiX = 30, ubiY = 70;
        this.cartelGoles.setPosition(ubiX, ubiY);
        this.cartelTiempo.setPosition(Gdx.graphics.getWidth() - ubiX - this.cartelTiempo.getWidth(), ubiY);
        this.golesXY  = new float[]{ this.cartelGoles.getX(), this.cartelGoles.getY() };
        this.tiempoXY = new float[]{ this.cartelTiempo.getX(), this.cartelTiempo.getY() };

        this.hoverSound = super.sonido;
        super.inicializarSonido(2);

        Gdx.input.setInputProcessor(this);

        gestorMenu = new GestorInputMenu(this);
        renderizador = new RenderizadorDeMenu(this);
    }

    @Override
    public void render(float delta) {
        batch.begin();
        renderizador.renderFondo(delta);
        renderizador.cargarAtrasSiguiente();

        controlJugador1.draw(batch);
        controlJugador2.draw(batch);
        cartelCampo.draw(batch);
        campoSprite.draw(batch);
        flechaIzq.draw(batch);
        flechaDer.draw(batch);
        cartelGoles.draw(batch);
        cartelTiempo.draw(batch);

        batch.end();
    }

    @Override
    public boolean mouseMoved(int x, int y) {
        y = Gdx.graphics.getHeight() - y;

        boolean dentroG = hit(cartelGoles, x, y);
        cartelGoles.setColor(dentroG ? colorHover : colorNormal);

        boolean dentroT = hit(cartelTiempo, x, y);
        cartelTiempo.setColor(dentroT ? colorHover : colorNormal);

        boolean dentroAtras = hit(super.atrasSprite, x, y);
        gestorMenu.condicionColor(dentroAtras, super.atrasSprite);
        super.reproducirSonido(0, dentroAtras);

        boolean dentroJugar = hit(super.siguienteSprite, x, y);
        gestorMenu.condicionColor(dentroJugar, super.siguienteSprite);
        super.reproducirSonido(1, dentroJugar);

        updateFlechaHover(flechaIzq, x, y, true);
        updateFlechaHover(flechaDer, x, y, false);

        return dentroAtras || dentroJugar || dentroG || dentroT;
    }

    @Override
    public boolean touchUp(int x, int y, int pointer, int button) {
        y = Gdx.graphics.getHeight() - y;
        boolean clic = false;

        if (hit(cartelGoles, x, y)) {
            indiceGoles = (indiceGoles + 1) % opcionesGoles.length;
            cartelGoles.setTexture(golesTex[indiceGoles]);
            cartelGoles.setPosition(golesXY[0], golesXY[1]);
            clic = true;
        }

        if (hit(cartelTiempo, x, y)) {
            indiceTiempo = (indiceTiempo + 1) % opcionesTiempo.length;
            cartelTiempo.setTexture(tiempoTex[indiceTiempo]);
            cartelTiempo.setPosition(tiempoXY[0], tiempoXY[1]);
            clic = true;
        }

        if (hit(flechaIzq, x, y)) { cambiarCampo(-1); clic = true; }
        if (hit(flechaDer, x, y)) { cambiarCampo(+1); clic = true; }

        if (hit(super.atrasSprite, x, y)) {
            super.cambiarMenu(true, new Doble(super.juego, this.modo));
            return true;
        }

        // ============================
        // BOTÓN JUGAR → ARMAMOS CONFIG
        // ============================
        if (hit(super.siguienteSprite, x, y)) {

            // Convertimos habilidades a String
            List<String> habilidadesStr = habilidades.stream()
                    .map(Enum::name)
                    .toList();

            ConfigCliente.Builder builder =
                    new ConfigCliente.Builder()
                            .agregarSkin(pielJugador1)
                            .agregarSkin(pielJugador2)
                            .campo(listaCampos[indiceCampo].getNombre())     // STRING
                            .goles(opcionesGoles[indiceGoles])                // INT
                            .tiempo(opcionesTiempo[indiceTiempo])            // INT
                            .modo(this.modo);

            if (this.modo.equals("especial")) {
                if (equipoJ1 != null) builder.agregarEquipo(equipoJ1);
                if (equipoJ2 != null) builder.agregarEquipo(equipoJ2);
                builder.agregarHabilidades(habilidadesStr);
            }

            ConfigCliente config = builder.build();

            //juego.cliente.enviarConfig(config);
            //juego.setScreen(new PantallaEsperandoServidor(super.juego));

            return true;
        }

        return clic;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private void cambiarCampo(int delta) {
        int len = listaCampos.length;
        indiceCampo = (indiceCampo + delta + len) % len;
        String nombre = listaCampos[indiceCampo].getNombre();
        Texture tex = getCampoTexture(nombre);
        campoSprite.setTexture(tex);
        campoSprite.setSize(cartelCampo.getWidth(), CAMPOS_ALTURA);
        campoSprite.setPosition(cartelCampo.getX(), cartelCampo.getY() - CAMPOS_ALTURA);
    }

    private Texture getCampoTexture(String nombre) {
        return cacheCampos.computeIfAbsent(nombre, n ->
                Assets.tex("campos/campo" + n + ".png"));
    }

    private boolean hit(Sprite s, int x, int y) {
        return x >= s.getX() &&
                x <= s.getX() + s.getWidth() &&
                y >= s.getY() &&
                y <= s.getY() + s.getHeight();
    }

    private void updateFlechaHover(Sprite flecha, int x, int y, boolean izquierda) {
        boolean dentro = hit(flecha, x, y);
        flecha.setTexture(dentro ? flechaHoverTex : flechaNormalTex);
        if (!izquierda) flecha.setRotation(180f);
    }

    private void colocarControles(float y) {
        float xUno = Gdx.graphics.getWidth() / 2f - 184f - controlJugador1Tex.getWidth() / 2f;
        float xDos = Gdx.graphics.getWidth() / 2f + 200f - controlJugador2Tex.getWidth() / 2f;
        this.controlJugador1 = new Sprite(controlJugador1Tex);
        this.controlJugador2 = new Sprite(controlJugador2Tex);
        this.controlJugador1.setPosition(xUno, y);
        this.controlJugador2.setPosition(xDos, y);
    }

    // OJO: estas dos referencias las tenés que resolver en tu proyecto:
    // - cliente: instancia de tu HiloCliente o lo que uses para la red
    // - mostrarPantallaDeEspera(): cambiar a la pantalla que corresponda
}
