// src/main/java/edu/pucp/plg_back/service/impl/AntColonyPlanificador.java
package edu.pucp.plg_back.service.impl;

import edu.pucp.plg_back.model.*;
import edu.pucp.plg_back.service.Planificador;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class AntColonyPlanificador implements Planificador {

    private final ACOParams cfg = ACOParams.builder().build();

    /** Planifica rutas para cada camión con ACO-TSP sencillo sobre sus destinos */
    @Override
    public List<Ruta> planificar(List<Camion> camiones, List<Pedido> pedidos) {

        // 1) asignación naïve de pedidos → primer camión con capacidad suficiente
        Map<Camion, List<Pedido>> asignacion = new HashMap<>();
        for (Camion c : camiones)
            asignacion.put(c, new ArrayList<>());

        pedidos.sort(
                Comparator.comparing(
                        Pedido::getFechaPedido,
                        Comparator.nullsFirst(Comparator.naturalOrder()))); // FCFS
        for (Pedido p : pedidos) {
            Optional<Camion> opt = camiones.stream()
                    .filter(c -> c.getCapacidad() - c.getCargaGLP() >= p.getVolumen())
                    .findFirst();
            opt.ifPresent(c -> {
                asignacion.get(c).add(p);
                c.setCargaGLP(c.getCargaGLP() + p.getVolumen());
            });
        }

        // 2) generar ruta con ACO (TSP) para cada camión
        List<Ruta> resultado = new ArrayList<>();
        asignacion.forEach((camion, lista) -> {
            if (lista.isEmpty())
                return;
            Ruta r = construirRutaACO(camion, lista);
            resultado.add(r);
        });

        // ordenar por hora de inicio
        Collections.sort(resultado);
        return resultado;
    }

    /** -------------- MÉTODO CENTRAL -------------- */
    private Ruta construirRutaACO(Camion camion, List<Pedido> pedidos) {
        // nodos = depósito + destinos
        Nodo deposito = new Nodo(12, 8); // depósito central
                                         // :contentReference[oaicite:4]{index=4}&#8203;:contentReference[oaicite:5]{index=5}
        List<Nodo> destinos = pedidos.stream()
                .map(p -> new Nodo(p.getX(), p.getY()))
                .distinct()
                .collect(Collectors.toList());

        int n = destinos.size() + 1; // incluye depósito en 0
        double[][] dist = new double[n][n];
        List<Nodo> todos = new ArrayList<>();
        todos.add(deposito);
        todos.addAll(destinos);

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                dist[i][j] = todos.get(i).getDistancia(todos.get(j));

        double[][] tau = new double[n][n];
        for (double[] row : tau)
            Arrays.fill(row, 1.0); // feromona inicial

        int[] bestTour = null;
        double bestLen = Double.MAX_VALUE;

        Random rnd = new Random();

        for (int iter = 0; iter < cfg.getNIter(); iter++) {
            List<int[]> antsTours = new ArrayList<>();

            // --- construir tours ---
            for (int k = 0; k < cfg.getNAnts(); k++) {
                boolean[] visited = new boolean[n];
                int[] tour = new int[n + 1]; // circuito
                tour[0] = 0; // parte del depósito
                visited[0] = true;

                for (int step = 1; step < n; step++) {
                    int current = tour[step - 1];
                    int next = seleccionarSiguiente(current, visited, tau, dist, rnd);
                    tour[step] = next;
                    visited[next] = true;
                }
                tour[n] = 0; // regreso al depósito
                antsTours.add(tour);

                double l = calcularLongitud(tour, dist);
                if (l < bestLen) {
                    bestLen = l;
                    bestTour = tour.clone();
                }
            }

            // --- evaporación global ---
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    tau[i][j] *= (1 - cfg.getRho());

            // --- depósito de feromona ---
            for (int[] tour : antsTours) {
                double l = calcularLongitud(tour, dist);
                double delta = cfg.getQ() / l;
                for (int s = 0; s < tour.length - 1; s++) {
                    int i = tour[s], j = tour[s + 1];
                    tau[i][j] += delta;
                    tau[j][i] += delta;
                }
            }
        }

        // convertir el bestTour → List<Posicion>
        List<Posicion> ruta = new ArrayList<>();
        for (int idx : bestTour) {
            Nodo nodo = todos.get(idx);
            int esDestino = idx == 0 ? 0 : 1;
            ruta.add(Posicion.builder()
                    .X(nodo.getX()).Y(nodo.getY()).destino(esDestino).build());
        }

        // hora de inicio = now, fin = now + t (horas) → para demo
        Calendar inicio = Optional.ofNullable(camion.getFechaInicio())
                .orElse(Calendar.getInstance());
        float minutos = ((float) bestLen / (float) camion.getVelocidad()) * 60f;
        Calendar fin = (Calendar) inicio.clone();
        fin.add(Calendar.MINUTE, Math.round(minutos));

        return new Ruta(ruta, inicio.getTime().toString(), fin.getTime().toString());
    }

    /* --- utilidades privadas --- */

    private int seleccionarSiguiente(int actual, boolean[] visited,
            double[][] tau, double[][] dist, Random rnd) {
        double[] prob = new double[visited.length];
        double sum = 0.0;
        for (int j = 0; j < visited.length; j++) {
            if (!visited[j]) {
                prob[j] = Math.pow(tau[actual][j], cfg.getAlpha()) *
                        Math.pow(1.0 / dist[actual][j], cfg.getBeta());
                sum += prob[j];
            }
        }
        double r = rnd.nextDouble() * sum;
        double acum = 0;
        for (int j = 0; j < prob.length; j++) {
            if (!visited[j]) {
                acum += prob[j];
                if (acum >= r)
                    return j;
            }
        }
        // fallback (sólo debería pasar por redondeo)
        for (int j = 0; j < visited.length; j++)
            if (!visited[j])
                return j;
        return 0; // nunca
    }

    private double calcularLongitud(int[] tour, double[][] dist) {
        double l = 0;
        for (int i = 0; i < tour.length - 1; i++)
            l += dist[tour[i]][tour[i + 1]];
        return l;
    }
}
