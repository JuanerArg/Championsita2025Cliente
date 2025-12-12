package com.championsita.jugabilidad.entrada;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.championsita.Principal;
import com.championsita.menus.menucarga.Carga;
import com.championsita.menus.menueleccion.Doble;
import com.championsita.red.HiloCliente;

public class EntradaJugador implements InputProcessor {

    private final int keyArriba, keyAbajo, keyIzquierda, keyDerecha, keyAccion, keySprint;

    private boolean arriba, abajo, izquierda, derecha, espacioPresionado, sprintPresionado;

    HiloCliente cliente;
    public EntradaJugador(int arriba, int abajo, int izquierda, int derecha, int accion, int sprint, HiloCliente cliente) {
        this.keyArriba = arriba;
        this.keyAbajo = abajo;
        this.keyIzquierda = izquierda;
        this.keyDerecha = derecha;
        this.keyAccion = accion;
        this.keySprint = sprint;
        this.cliente = cliente;
    }

    @Override
    public boolean keyDown(int keycode) {
        boolean handled = false;
        if (keycode == keyArriba)    { arriba = true; handled = true; }
        if (keycode == keyAbajo)     { abajo = true; handled = true; }
        if (keycode == keyIzquierda) { izquierda = true; handled = true; }
        if (keycode == keyDerecha)   { derecha = true; handled = true; }
        if (keycode == keyAccion)    { espacioPresionado = true; handled = true; }
        if (keycode == keySprint)    { sprintPresionado = true; handled = true; }
        enviarInput(this.cliente);
        return handled; // <-- SOLO true si esta instancia efectivamente manejó la tecla
    }

    @Override
    public boolean keyUp(int keycode) {
        boolean handled = false;
        if (keycode == keyArriba)    { arriba = false; handled = true; }
        if (keycode == keyAbajo)     { abajo = false; handled = true; }
        if (keycode == keyIzquierda) { izquierda = false; handled = true; }
        if (keycode == keyDerecha)   { derecha = false; handled = true; }
        if (keycode == keyAccion)    { espacioPresionado = false; handled = true; }
        if (keycode == keySprint)    { sprintPresionado = false; handled = true; }
        enviarInput(this.cliente);
        return handled; // <-- idem
    }

    public void enviarInput(HiloCliente cliente) {
        String msg = "INPUT:";
        msg += "u=" + (arriba               ? "1" : "0") + ",";
        msg += "d=" + (abajo                ? "1" : "0") + ",";
        msg += "l=" + (izquierda            ? "1" : "0") + ",";
        msg += "r=" + (derecha              ? "1" : "0") + ",";
        msg += "a=" + (espacioPresionado    ? "1" : "0") + ",";
        msg += "s=" + (sprintPresionado     ? "1" : "0");

        cliente.enviar(msg);
        System.out.println(msg);
    }

    // Métodos no usados
    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
}
