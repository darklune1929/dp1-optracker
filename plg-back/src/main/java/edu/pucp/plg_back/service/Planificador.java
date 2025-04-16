package edu.pucp.plg_back.service;

import java.util.List;

import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;

public interface Planificador {
    List<Ruta> planificarRutas(List<Pedido> pedidos, List<Camion> camiones);

}
