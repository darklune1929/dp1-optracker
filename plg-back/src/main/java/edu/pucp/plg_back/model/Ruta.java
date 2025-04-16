package edu.pucp.plg_back.model;

import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Ruta {
    private Camion camion;
    public List<Pedido> pedidos;
    public double distancia;

    public Ruta(Camion camion, List<Pedido> pedidos) {
        this.camion = camion;
        this.pedidos = List.copyOf(pedidos);
        this.distancia = calcularDistancia();
    }

    private double calcularDistancia() {
        int cx = 12, cy = 8;
        double d = 0;
        int prevX = cx, prevY = cy;
        for (Pedido p : pedidos) {
            d += manhattan(prevX, prevY, p.getPosX(), p.getPosY());
            prevX = p.getPosX();
            prevY = p.getPosY();
        }
        d += manhattan(prevX, prevY, cx, cy);
        return d;
    }

    private static int manhattan(int x1, int y1, int x2, int y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    @Override
    public String toString() {
        return "Ruta{" + camion.getCodigo() +
                ", pedidos=" + pedidos.stream()
                        .map(p -> p.getId() + "@" + p.getClienteId())
                        .collect(Collectors.toList())
                +
                ", dist=" + distancia + "}";
    }
}
