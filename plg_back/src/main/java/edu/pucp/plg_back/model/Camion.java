package edu.pucp.plg_back.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Camion {
    private String codigo;

    private int cargaGLP;
    private double pesoTara;
    private double pesoCargaGLP;
    private double pesoTotal;

    private Calendar fechaInicio;

    @Builder.Default
    private List<Pedido> listaPedidos = new ArrayList<>();

    @Builder.Default
    List<Nodo> rutaVehiculo = new ArrayList<>();

    @Builder.Default
    Nodo nodoActual = new Nodo();

    @Builder.Default
    private double cantidadPedidos = 0;

    @Builder.Default
    private double velocidad = 50;

    private double combustible;
    private double capacidad;

    public void clearCamion() {
        cantidadPedidos = 0;
        listaPedidos.clear();
    }

    public float calculateTimeToDispatch() {
        return ((float) this.rutaVehiculo.size() / (float) this.velocidad) * 60;
    }

    public List<Posicion> getRutaVehiculoPositions(List<Pedido> pedidos) {
        List<Posicion> ruta = new ArrayList<>();
        this.getRutaVehiculo().forEach(nodo -> {
            int esDestino = 0;
            for (Pedido pedido : pedidos) {
                if (nodo.getX() == pedido.getX() && nodo.getY() == pedido.getY()) {
                    esDestino = 1;
                    break;
                }
            }
            ruta.add(Posicion.builder().X(nodo.getX()).Y(nodo.getY()).destino(Integer.valueOf(esDestino)).build());
        });
        return ruta;
    }

}
