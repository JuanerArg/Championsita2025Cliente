package com.championsita.red;

import java.util.ArrayList;
import java.util.List;

public class EstadoPartidaCliente {

    public ArrayList<EstadoPersonaje> jugadores;
    public EstadoPelota pelota;

    public EstadoArco arcoIzq;
    public EstadoArco arcoDer;

    public int golesRojo;
    public int golesAzul;

    public float tiempo;

    public EstadoPartidaCliente(ArrayList<EstadoPersonaje> jugadores, EstadoPelota pelota, EstadoArco arcoIzq, EstadoArco arcoDer, int golesAzul, int golesRojo){
        this.jugadores = jugadores;
        this.pelota = pelota;
        this.arcoIzq = arcoIzq;
        this.arcoDer = arcoDer;
        this.golesAzul = golesAzul;
        this.golesRojo = golesRojo;
    }

    public synchronized void actualizar(
            List<EstadoPersonaje> jugadoresNuevos,
            EstadoPelota pelotaNueva,
            EstadoArco izq,
            EstadoArco der,
            int golesR, int golesA
    ) {
        jugadores.clear();
        jugadores.addAll(jugadoresNuevos);

        pelota = pelotaNueva;

        arcoIzq = izq;
        arcoDer = der;

        golesRojo = golesR;
        golesAzul = golesA;
    }
}
