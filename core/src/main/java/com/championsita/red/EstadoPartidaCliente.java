package com.championsita.red;

import java.util.ArrayList;
import java.util.List;

public class EstadoPartidaCliente {

    public String ganador;
    public ArrayList<EstadoPersonaje> jugadores;
    public EstadoPelota pelota;

    public EstadoArco arcoIzq;
    public EstadoArco arcoDer;

    public int golesRojo;
    public int golesAzul;

    public float tiempo;

    public EstadoPartidaCliente(ArrayList<EstadoPersonaje> jugadores, EstadoPelota pelota, EstadoArco arcoIzq, EstadoArco arcoDer, int golesAzul, int golesRojo, int tiempo, String ganador){
        this.jugadores = jugadores;
        this.pelota = pelota;
        this.arcoIzq = arcoIzq;
        this.arcoDer = arcoDer;
        this.golesAzul = golesAzul;
        this.golesRojo = golesRojo;
        this.tiempo = tiempo;
        this.ganador = ganador;
    }

    public synchronized void actualizar(EstadoPartidaCliente estadoPartidaActualizado
    ) {
        jugadores.clear();
        jugadores.addAll(estadoPartidaActualizado.jugadores);

        pelota = estadoPartidaActualizado.pelota;

        arcoIzq = estadoPartidaActualizado.arcoIzq;
        arcoDer = estadoPartidaActualizado.arcoDer;

        golesRojo = estadoPartidaActualizado.golesRojo;
        golesAzul = estadoPartidaActualizado.golesAzul;

        tiempo = estadoPartidaActualizado.tiempo;

        ganador = estadoPartidaActualizado.ganador;
    }
}
