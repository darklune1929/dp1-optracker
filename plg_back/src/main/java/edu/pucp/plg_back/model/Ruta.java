package edu.pucp.plg_back.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class Ruta implements Comparable<Ruta> {
    private List<Posicion> ruta;
    private String startTime;
    private String endTime;

    @Override
    public int compareTo(Ruta ruta) {
        return this.getStartTime().compareTo(ruta.getStartTime());
    }
}
