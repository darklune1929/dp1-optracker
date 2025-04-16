package edu.pucp.plg_back.model;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Camion {
    private String codigo;

    private Integer capacidad;
    private Double tara;

}
