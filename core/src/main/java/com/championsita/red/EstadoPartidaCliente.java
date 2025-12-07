package com.championsita.red;

import java.util.ArrayList;
import java.util.List;

public class EstadoPartidaCliente {

    public List<EstadoPersonaje> jugadores = new ArrayList<>();
    public EstadoPelota pelota = new EstadoPelota();

    public EstadoArco arcoIzq = new EstadoArco();
    public EstadoArco arcoDer = new EstadoArco();

    public int golesRojo;
    public int golesAzul;

    public float tiempo;

    public synchronized void actualizar(
            List<EstadoPersonaje> jugadoresNuevos,
            EstadoPelota pelotaNueva,
            EstadoArco izq,
            EstadoArco der,
            int golesR, int golesA,
            float tiempoPartido
    ) {
        jugadores.clear();
        jugadores.addAll(jugadoresNuevos);

        pelota = pelotaNueva;

        arcoIzq = izq;
        arcoDer = der;

        golesRojo = golesR;
        golesAzul = golesA;

        tiempo = tiempoPartido;
    }
}
