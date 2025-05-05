package edu.pucp.plg_back.test;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CombinacionACO {
    private int nAnts;
    private int nIter;
    private double alpha;
    private double beta;
    private double rho;
}
