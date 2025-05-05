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

public class CasoPruebaACO {

    private int nant1;
    private int nant2;

    private int niter1;
    private int niter2;

    private double alpha1;
    private double alpha2;

    private double beta1;
    private double beta2;

    private double rho1;
    private double rho2;

    public CombinacionACO[] combinacionACO;

    public void generarCombinaciones() {
        int ant, iter;
        double a, b, r;
        this.combinacionACO = new CombinacionACO[32];
        for (int i = 0; i < 32; i++) {
            ant = (i % 2 == 0) ? this.nant1 : this.nant2;
            iter = (i % 4 > 1) ? this.niter1 : this.niter2;
            a = (i % 8 > 3) ? this.alpha1 : this.alpha2;
            b = (i % 16 > 7) ? this.beta1 : this.beta2;
            r = (i % 32 > 15) ? this.rho1 : this.rho2;
            this.combinacionACO[i] = CombinacionACO.builder()
                    .nAnts(ant)
                    .nIter(iter)
                    .alpha(a)
                    .beta(b)
                    .rho(r)
                    .build();
        }
        System.out.println("Combinaciones generadas:");
        for (int i = 0; i < combinacionACO.length; i++) {
            CombinacionACO combinacion = combinacionACO[i];
            System.out.printf("Combinación %d: alpha=%.2f, beta=%.2f, rho=%.2f, nAnts=%d, nIter=%d\n",
                    i, combinacion.getAlpha(), combinacion.getBeta(), combinacion.getRho(),
                    combinacion.getNAnts(), combinacion.getNIter());
        }
    }
}
