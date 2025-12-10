package com.championsita.jugabilidad.herramientas;

public enum Direccion {

    DERECHA("derecha"),
    IZQUIERDA("izquierda"),
    ARRIBA_DERECHA("arriba_derecha"),
    ARRIBA_IZQUIERDA("arriba_izquierda"),
    ABAJO_DERECHA("abajo_derecha"),
    ABAJO_IZQUIERDA("abajo_izquierda"),
    ARRIBA("arriba"),
    ABAJO("abajo");

    private final String direccion;

    Direccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDireccion() {
        return direccion;
    }

    @Override
    public String toString() {
        return direccion;
    }

    public static Direccion fromString(String s) {
        for (Direccion d : values()) {
            if (d.direccion.equalsIgnoreCase(s)) {
                return d;
            }
        }
        throw new IllegalArgumentException("Direccion inválida: " + s);
    }

}