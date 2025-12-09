package com.championsita.menus.menueleccion;

public enum Jugador {
    ROJO("Rojo"),
    VERDE("Verde"),
    AMARILLO("Amarillo"),
    CELESTE("Celeste");

    private String nombre;

    private Jugador(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return this.nombre;
    }
}
