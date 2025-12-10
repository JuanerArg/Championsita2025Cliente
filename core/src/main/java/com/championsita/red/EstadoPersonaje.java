package com.championsita.red;

public class    EstadoPersonaje {

    public float x, y;
    public float ancho, alto;

    public boolean estaMoviendo;
    public String direccion;   // "ARRIBA", "DERECHA", "ABAJO_IZQUIERDA", etc.
    public float tiempoAnimacion;

    public float staminaActual;
    public float staminaMaxima;

    public EstadoPersonaje(float x,
                           float y,
                           float ancho,
                           float alto,
                           boolean estaMoviendo,
                           String direccion,
                           float tiempoAnimacion,
                           float staminaActual,
                           float staminaMaxima
                           ){
        this.x = x;
        this.y = y;
        this.alto = alto;
        this.ancho = ancho;
        this.estaMoviendo = estaMoviendo;
        this.direccion = direccion;
        this.tiempoAnimacion = tiempoAnimacion;
        this.staminaActual = staminaActual;
        this.staminaMaxima = staminaMaxima;
    }
}

