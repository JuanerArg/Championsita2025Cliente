package com.championsita.red;

public class EstadoPelota {

    public float x, y;
    public float width, height;

    public boolean animar;
    public float stateTime;

    public EstadoPelota(float x, float y, float width, float height,
                        float stateTime, boolean animar) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.animar = animar;
        this.stateTime = stateTime;
    }

    public EstadoPelota() {}

}
