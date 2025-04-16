package edu.pucp.plg_back.service.impl;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;

import edu.pucp.plg_back.service.Planificador;

@Service
public class AlgoGenPlanificador implements Planificador {

    private Integer poblacionSize;
    private Integer generaciones;
    private Double crossoverRate;
    private Double mutationRate;

    public AlgoGenPlanificador(Integer poblacionSize, Integer generaciones,
            Double crossoverRate,
            Double mutationRate) {
        this.poblacionSize = poblacionSize;
        this.generaciones = generaciones;
        this.crossoverRate = crossoverRate;
        this.mutationRate = mutationRate;
    }

    public AlgoGenPlanificador() {
        this(40, 100, 0.8, 0.2);
    }

    @Override
    public List<Ruta> planificarRutas(List<Pedido> pedidos, List<Camion> camiones) {
        // cada cromosoma es una permutación de índices de pedidos
        // punto de corte por capacidad se calcula al construir las rutas
        List<int[]> poblacion = inicializarPoblacion(pedidos.size());
        int[] mejor = null;
        double mejorFitness = Double.MAX_VALUE;
        for (int gen = 0; gen < generaciones; gen++) {
            // evaluar fitness (distancia total)
            List<Double> fitnessVals = new ArrayList<>();
            for (int[] crom : poblacion) {
                double fitness = fitness(crom, pedidos, camiones);
                fitnessVals.add(fitness);
                if (fitness < mejorFitness) {
                    mejorFitness = fitness;
                    mejor = crom.clone();
                }
            }

            // nueva población
            List<int[]> nuevaPoblacion = new ArrayList<>();
            while (nuevaPoblacion.size() < poblacionSize) {
                int[] p1 = seleccionar(poblacion, fitnessVals);
                int[] p2 = seleccionar(poblacion, fitnessVals);
                if (ThreadLocalRandom.current().nextDouble() < crossoverRate) {
                    int[][] hijos = crossoverOX(p1, p2);
                    nuevaPoblacion.add(hijos[0]);
                    if (nuevaPoblacion.size() < poblacionSize)
                        nuevaPoblacion.add(hijos[1]);
                } else {
                    nuevaPoblacion.add(p1.clone());
                    if (nuevaPoblacion.size() < poblacionSize)
                        nuevaPoblacion.add(p2.clone());
                }
            }
            // mutación
            nuevaPoblacion.forEach(ch -> {
                if (ThreadLocalRandom.current().nextDouble() < mutationRate)
                    mutarSwap(ch);
            });
            poblacion = nuevaPoblacion;
        }
        return cromosomaARutas(mejor, pedidos, camiones);
    }

    private List<int[]> inicializarPoblacion(int n) {
        List<int[]> pob = new ArrayList<>();
        int[] base = new int[n];
        for (int i = 0; i < n; i++)
            base[i] = i;
        for (int i = 0; i < poblacionSize; i++) {
            int[] chrom = base.clone();
            shuffleArray(chrom);
            pob.add(chrom);
        }
        return pob;
    }

    private static void shuffleArray(int[] arr) {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        for (int i = arr.length - 1; i > 0; i--) {
            int idx = rnd.nextInt(i + 1);
            int tmp = arr[idx];
            arr[idx] = arr[i];
            arr[i] = tmp;
        }
    }

    private double fitness(int[] chrom, List<Pedido> pedidos, List<Camion> flota) {
        List<Ruta> rutas = cromosomaARutas(chrom, pedidos, flota);
        return rutas.stream().mapToDouble(r -> r.distancia).sum();
    }

    private int[] seleccionar(List<int[]> pob, List<Double> fitness) {
        // tournament selection size=3
        int bestIdx = ThreadLocalRandom.current().nextInt(pob.size());
        for (int i = 0; i < 2; i++) {
            int idx = ThreadLocalRandom.current().nextInt(pob.size());
            if (fitness.get(idx) < fitness.get(bestIdx))
                bestIdx = idx;
        }
        return pob.get(bestIdx);
    }

    private int[][] crossoverOX(int[] p1, int[] p2) {
        int n = p1.length;
        int a = ThreadLocalRandom.current().nextInt(n);
        int b = ThreadLocalRandom.current().nextInt(a, n);
        int[] h1 = new int[n];
        int[] h2 = new int[n];
        Arrays.fill(h1, -1);
        Arrays.fill(h2, -1);
        System.arraycopy(p1, a, h1, a, b - a);
        System.arraycopy(p2, a, h2, a, b - a);
        fillOX(h1, p2, b, n);
        fillOX(h2, p1, b, n);
        return new int[][] { h1, h2 };
    }

    private void fillOX(int[] hijo, int[] padre, int start, int n) {
        int idx = start % n;
        for (int i = 0; i < n; i++) {
            int gene = padre[(start + i) % n];
            if (!contains(hijo, gene)) {
                hijo[idx] = gene;
                idx = (idx + 1) % n;
            }
        }
    }

    private static boolean contains(int[] arr, int val) {
        for (int j : arr)
            if (j == val)
                return true;
        return false;
    }

    private void mutarSwap(int[] chrom) {
        int i = ThreadLocalRandom.current().nextInt(chrom.length);
        int j = ThreadLocalRandom.current().nextInt(chrom.length);
        int tmp = chrom[i];
        chrom[i] = chrom[j];
        chrom[j] = tmp;
    }

    /* Conversión de permutación → rutas respetando capacidad. */
    private List<Ruta> cromosomaARutas(int[] chrom, List<Pedido> pedidos, List<Camion> flota) {
        List<Ruta> rutas = new ArrayList<>();
        int camIdx = 0;
        int cargaActual = 0;
        List<Pedido> actuales = new ArrayList<>();
        for (int idx : chrom) {
            Pedido p = pedidos.get(idx);
            if (camIdx >= flota.size())
                camIdx = 0; // overflow simple
            Camion c = flota.get(camIdx);
            if (cargaActual + p.getVolumen() > c.getCapacidad()) { // cerrar ruta actual
                rutas.add(new Ruta(c, actuales));
                camIdx = (camIdx + 1) % flota.size();
                c = flota.get(camIdx);
                actuales = new ArrayList<>();
                cargaActual = 0;
            }
            actuales.add(p);
            cargaActual += p.getVolumen();
        }
        if (!actuales.isEmpty())
            rutas.add(new Ruta(flota.get(camIdx), actuales));
        return rutas;
    }

}
