package edu.pucp.plg_back.service;

import java.util.List;

import edu.pucp.plg_back.model.Bloqueo;
import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;

public interface Planificador {
    List<Ruta> planificar(List<Camion> camiones, List<Pedido> pedidos, List<Bloqueo> bloqueos);

}
