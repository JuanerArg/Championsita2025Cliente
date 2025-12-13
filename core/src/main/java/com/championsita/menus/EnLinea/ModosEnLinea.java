package com.championsita.menus.EnLinea;

import com.badlogic.gdx.graphics.Texture;
import com.championsita.menus.compartido.Assets;

public enum ModosEnLinea {

    FUTBOL("1v1", "menuInicial/2jugadoresBoton.png"),
    ESPECIAL("especial", "Especial.png"),
    FUTSAL("futsal", "menuInicial/futsal.png");


    private final String codigo;
    private final String textura;

    ModosEnLinea(String codigo, String textura) {
        this.codigo = codigo;
        this.textura = textura;
    }

    public String getCodigo() {
        return codigo;
    }

    public Texture getTextura() {
        return Assets.tex(textura);
    }

    public static ModosEnLinea fromCodigo(String codigo) {
        if (codigo == null) return null;
        for (ModosEnLinea m : values()) {
            if (m.codigo.equalsIgnoreCase(codigo.trim())) return m;
        }
        return null;
    }
}

