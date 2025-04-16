package edu.pucp.plg_back.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import edu.pucp.plg_back.model.Camion;
import edu.pucp.plg_back.model.Pedido;
import edu.pucp.plg_back.model.Ruta;
import edu.pucp.plg_back.service.Planificador;

@Service
public class AntColonyPlanificador implements Planificador {
    private Integer numIteraciones;
    private Integer numHormigas;
    private Double alfa = 1.0; // influencia feromona
    private Double beta = 2.0; // influencia heurística (1/dist)
    private Double rho = 0.5; // tasa de evaporación

    public AntColonyPlanificador(int numIteraciones, int numHormigas) {
        this.numIteraciones = numIteraciones;
        this.numHormigas = numHormigas;
    }

    public AntColonyPlanificador() {
        this(50, 20);
    }

    @Override
    public List<Ruta> planificarRutas(List<Pedido> pedidos, List<Camion> camiones) {
        if (pedidos.isEmpty())
            return List.of();

        int n = pedidos.size();
        double[][] tau = new double[n + 1][n + 1];
        for (double[] row : tau)
            Arrays.fill(row, 1.0); // inicializar

        List<Ruta> mejorSolucion = null;
        double mejorCosto = Double.MAX_VALUE;

        for (int it = 0; it < numIteraciones; it++) {
            for (int k = 0; k < numHormigas; k++) {
                List<Ruta> rutas = construirSolucion(pedidos, camiones, tau);
                double costo = rutas.stream().mapToDouble(r -> r.distancia).sum();
                if (costo < mejorCosto) {
                    mejorCosto = costo;
                    mejorSolucion = rutas;
                }
                actualizarFeromonas(tau, rutas, costo);
            }
            evaporar(tau);
        }
        return mejorSolucion == null ? List.of() : mejorSolucion;
    }

    private List<Ruta> construirSolucion(List<Pedido> pedidos, List<Camion> flota, double[][] tau) {
        List<Pedido> noAsignados = new ArrayList<>(pedidos);
        List<Ruta> resultado = new ArrayList<>();
        for (Camion c : flota) {
            if (noAsignados.isEmpty())
                break;
            List<Pedido> rutaPedidos = new ArrayList<>();
            int cargaRestante = c.getCapacidad();
            int actual = 0; // índice del depósito
            while (cargaRestante > 0 && !noAsignados.isEmpty()) {
                Pedido siguiente = seleccionarPedido(noAsignados, actual, tau, cargaRestante);
                if (siguiente == null)
                    break; // no cabe ninguno
                rutaPedidos.add(siguiente);
                cargaRestante -= siguiente.getVolumen();
                actual = pedidos.indexOf(siguiente) + 1; // +1 because depot=0
                noAsignados.remove(siguiente);
            }
            if (!rutaPedidos.isEmpty())
                resultado.add(new Ruta(c, rutaPedidos));
        }
        if (!noAsignados.isEmpty()) {
            Camion c = flota.get(0);
            List<Pedido> extra = new ArrayList<>(noAsignados);
            resultado.add(new Ruta(c, extra));
        }
        return resultado;
    }

    private Pedido seleccionarPedido(List<Pedido> candidatos, int actualIdx, double[][] tau, int cargaRestante) {
        // Filtrar los que quepan
        List<Pedido> factibles = candidatos.stream().filter(p -> p.getVolumen() <= cargaRestante)
                .collect(Collectors.toList());
        if (factibles.isEmpty())
            return null;
        double[] probs = new double[factibles.size()];
        double sum = 0;
        for (int i = 0; i < factibles.size(); i++) {
            Pedido p = factibles.get(i);
            int j = candidatos.indexOf(p) + 1;
            double eta = 1.0 / (distanceTo(p, actualIdx == 0 ? null : candidatos.get(actualIdx - 1)) + 1e-3);
            probs[i] = Math.pow(tau[actualIdx][j], alfa) * Math.pow(eta, beta);
            sum += probs[i];
        }
        double r = ThreadLocalRandom.current().nextDouble() * sum;
        for (int i = 0; i < probs.length; i++) {
            r -= probs[i];
            if (r <= 0)
                return factibles.get(i);
        }
        return factibles.get(factibles.size() - 1);
    }

    private void actualizarFeromonas(double[][] tau, List<Ruta> rutas, double costo) {
        double deposit = 1.0 / costo;
        for (Ruta r : rutas) {
            int prev = 0;
            for (Pedido p : r.pedidos) {
                int idx = (r.pedidos.indexOf(p)) + 1;
                tau[prev][idx] += deposit;
                tau[idx][prev] += deposit;
                prev = idx;
            }
        }
    }

    private void evaporar(double[][] tau) {
        for (int i = 0; i < tau.length; i++) {
            for (int j = 0; j < tau[i].length; j++) {
                tau[i][j] *= (1 - rho);
                if (tau[i][j] < 1e-4)
                    tau[i][j] = 1e-4;
            }
        }
    }

    private static double distanceTo(Pedido p, Pedido q) {
        int x1 = p.getPosX(), y1 = p.getPosY();
        int x2 = (q == null) ? 12 : q.getPosX();
        int y2 = (q == null) ? 8 : q.getPosY();
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

}
