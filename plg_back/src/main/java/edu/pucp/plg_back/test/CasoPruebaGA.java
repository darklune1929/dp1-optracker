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

public class CasoPruebaGA {
    private int popSize1;
    private int popSize2;

    private int nGenerations1;
    private int nGenerations2;

    private double crossoverProb1;
    private double crossoverProb2;

    private double mutationProb1;
    private double mutationProb2;

    public CombinacionGA[] combinacionGA;

    public void generarCombinaciones() {
        int popSize, nGenerations;
        double crossoverProb, mutationProb;
        this.combinacionGA = new CombinacionGA[16];
        for (int i = 0; i < 16; i++) {
            popSize = (i % 2 == 0) ? this.popSize1 : this.popSize2;
            nGenerations = (i % 4 > 1) ? this.nGenerations1 : this.nGenerations2;
            crossoverProb = (i % 8 > 3) ? this.crossoverProb1 : this.crossoverProb2;
            mutationProb = (i % 16 > 7) ? this.mutationProb1 : this.mutationProb2;
            this.combinacionGA[i] = CombinacionGA.builder()
                    .popSize(popSize)
                    .nGenerations(nGenerations)
                    .crossoverProb(crossoverProb)
                    .mutationProb(mutationProb)
                    .build();
        }
        System.out.println("Combinaciones GA generadas:");
        for (int i = 0; i < combinacionGA.length; i++) {
            CombinacionGA combinacion = combinacionGA[i];
            System.out.printf("Combinación %d: crossover=%.2f, mutation=%.2f, popSize=%d, nGenerations=%d\n",
                    i, combinacion.getCrossoverProb(), combinacion.getMutationProb(), combinacion.getPopSize(),
                    combinacion.getNGenerations());
        }
    }

}
