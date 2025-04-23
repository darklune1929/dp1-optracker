// src/main/java/edu/pucp/plg_back/model/Mapa.java
package edu.pucp.plg_back.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Mapa {
    private int width; // 70 km
                       // :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}
    private int height; // 50 km
                        // :contentReference[oaicite:2]{index=2}&#8203;:contentReference[oaicite:3]{index=3}
    private boolean[][] bloqueos; // true = nodo bloqueado

    public boolean estaLibre(int x, int y) { // fuera de rango = bloqueado
        return x >= 0 && x < width && y >= 0 && y < height && !bloqueos[x][y];
    }
}
