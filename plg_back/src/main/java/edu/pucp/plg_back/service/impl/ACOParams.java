// src/main/java/edu/pucp/plg_back/service/impl/ACOParams.java
package edu.pucp.plg_back.service.impl;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ACOParams {
    @Builder.Default
    private int nAnts = 30;
    @Builder.Default
    private int nIter = 150;
    @Builder.Default
    private double alpha = 1.0; // influencia de la feromona
    @Builder.Default
    private double beta = 3.0; // influencia de la heurística (1/dist)
    @Builder.Default
    private double rho = 0.5; // factor de evaporación
    @Builder.Default
    private double q = 100.0; // cantidad de feromona depositada
}
