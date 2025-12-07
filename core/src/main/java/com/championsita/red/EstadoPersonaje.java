package com.championsita.red;

public class EstadoPersonaje {

    public float x, y;
    public float ancho, alto;

    public boolean estaMoviendo;
    public String direccion;   // "ARRIBA", "DERECHA", "ABAJO_IZQUIERDA", etc.
    public float tiempoAnimacion;

    public float staminaActual;
    public float staminaMaxima;
}

