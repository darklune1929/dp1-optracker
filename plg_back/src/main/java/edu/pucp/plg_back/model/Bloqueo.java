package edu.pucp.plg_back.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class Bloqueo {
    // conjunto de nodos bloqueados
    // estos forman un conjunto de segmentos lo cual sera considerado como un tramo
    // este tramo será polígono abierto
    List<Nodo> tramo;
    private String periodo;
    private String motivo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

}
