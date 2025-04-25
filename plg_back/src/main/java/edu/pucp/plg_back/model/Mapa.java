// src/main/java/edu/pucp/plg_back/model/Mapa.java
package edu.pucp.plg_back.model;

import java.util.List;

import lombok.*;

@Getter
@Setter
// @AllArgsConstructor
// @NoArgsConstructor
public class Mapa {
    private int width = 70; // 70 km
    private int height = 50; // 50 km
    private boolean[][] bloqueos; // true = nodo bloqueado

    public Mapa(int width, int height) {
        this.width = width;
        this.height = height;
        this.bloqueos = new boolean[width][height];
    }

    public Mapa() {
        this(70, 50);
    }

    public boolean estaLibre(int x, int y) { // fuera de rango = bloqueado
        return x >= 0 && x < width && y >= 0 && y < height && !bloqueos[x][y];
    }

    // para actualizar bloqueos
    public void setBloqueado(int x, int y, boolean bloqueado) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            if (this.bloqueos == null) {
                this.bloqueos = new boolean[width][height];
            }
            this.bloqueos[x][y] = bloqueado;
        }
    }

    // para adicionar multiples bloqueos
    public void addBlockages(List<Posicion> posiciones) {
        if (this.bloqueos == null) {
            this.bloqueos = new boolean[width][height];
        }
        if (posiciones != null) {
            for (Posicion p : posiciones) {
                setBloqueado(p.getX(), p.getY(), true);
            }
        }
    }
}
