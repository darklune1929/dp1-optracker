package edu.pucp.plg_back.model;

import lombok.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Nodo implements Comparable<Nodo>, Serializable {

    private int idNodo;

    private int X;
    private int Y;

    @Builder.Default
    private double distanciaMinima = Double.MAX_VALUE;

    @Builder.Default
    private double f = Double.MAX_VALUE;

    @Builder.Default
    private double g = 1;

    private double h;

    @Builder.Default
    private boolean estaBloqueado = false;

    private Date inicioBloqueo;
    private Date finBloqueo;

    @Builder.Default
    private Nodo nodoprevio = null;

    public Nodo(int X, int Y) {
        this.X = X;
        this.Y = Y;
    }

    public float getDistancia(Nodo destino) {
        int a, b, c, d;
        float r, r1;
        a = this.X;
        b = destino.getX();
        c = this.Y;
        d = destino.getY();
        r = Math.abs((float) b - a);
        r1 = Math.abs((float) d - c);

        return r + r1;
    }

    @Override
    public int compareTo(Nodo v) {
        return Double.compare(this.f, v.getF());
    }

    @Override
    public boolean equals(Object obj) {
        Nodo v = (Nodo) obj;
        return this.X == v.getX() && this.Y == v.getY();
    }

    public Nodo(Nodo n) {
        this.setX(n.getX());
        this.setY(n.getY());
    }

    @Override
    public String toString() {
        return String.format("[%d,%d]", X, Y);
    }

}
