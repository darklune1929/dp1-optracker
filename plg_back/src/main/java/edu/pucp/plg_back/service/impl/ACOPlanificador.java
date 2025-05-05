// src/main/java/edu/pucp/plg_back/service/impl/ACOPlanificador.java
package edu.pucp.plg_back.service.impl;

import edu.pucp.plg_back.model.*;
import edu.pucp.plg_back.service.Planificador;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@Primary
@RequiredArgsConstructor
public class ACOPlanificador implements Planificador {

    private final ACOParams cfg = ACOParams.builder().build();
    private final Pathfinder pathfinder; // injected
    // private final double DEADLINE_PENALTY = 1_000_000.0; // penaliza rutas que no
    // cumplen con la fecha de entrega

    // asumimos que el mapa es proveido o creado, con bloqueos
    // por simplicidad, usamos uno por defecto
    // en la realidad, el mapa debería ser parte del contexto de la planificación
    // y sera leido desde un archivo o base de datos
    private final Mapa mapa = new Mapa();

    /** Planifica rutas para cada camión con ACO-TSP + A* + deadlines */
    @Override
    public List<Ruta> planificar(List<Camion> camiones, List<Pedido> pedidos) {

        // --- Add some example blockages to the map ---
        // mapa.setBloqueado(15, 15, true);
        // mapa.setBloqueado(15, 16, true);
        // mapa.setBloqueado(16, 15, true);
        // mapa.setBloqueado(16, 16, true);
        // ---

        // 1) asignación FCFS de pedidos
        Map<Camion, List<Pedido>> asignacion = asignarPedidosFCFS(camiones, pedidos);

        // 2) generar ruta con ACO (TSP) para cada camión
        List<Ruta> resultado = new ArrayList<>();
        asignacion.forEach((camion, lista) -> {
            if (lista.isEmpty())
                return;
            Ruta r = construirRutaACO(camion, lista);
            if (r != null)
                resultado.add(r);
            else
                System.err.printf("No se pudo construir ruta para %s\n", camion.getCodigo());
        });

        // ordenar por hora de inicio
        Collections.sort(resultado);
        return resultado;
    }

    /** -------------- MÉTODO CENTRAL -------------- */
    private Ruta construirRutaACO(Camion camion, List<Pedido> pedidos) {
        // nodos = depósito + destinos unicos para cada pedido
        Nodo deposito = new Nodo(12, 8); // depósito central

        // mapear los nodos destino con la lista de pedidos
        Map<Nodo, List<Pedido>> destinoPedidosMap = new HashMap<>();
        for (Pedido p : pedidos) {
            Nodo nodo = new Nodo(p.getX(), p.getY());
            destinoPedidosMap.computeIfAbsent(nodo, k -> new ArrayList<>()).add(p);
        }
        List<Nodo> destinosUnicos = new ArrayList<>(destinoPedidosMap.keySet());

        int n = destinosUnicos.size() + 1; // incluye depósito en 0
        if (n <= 1)
            return null; // no hay ruta que construir

        // lista de todos los notos unicos
        List<Nodo> todos = new ArrayList<>();
        todos.add(deposito);
        todos.addAll(destinosUnicos);

        // calculamos las distancias usando A*
        double[][] dist = new double[n][n];
        System.out.printf("Calculando las distancias A* para los nodos %d (Camion %s)...\n", n, camion.getCodigo());
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0.0;
                    continue;
                }
                Nodo from = todos.get(i);
                Nodo to = todos.get(j);
                // resetear los nodos para A*
                from = new Nodo(from.getX(), from.getY());
                to = new Nodo(to.getX(), to.getY());

                Pathfinder.PathResult result = pathfinder.findShortestPath(from, to, mapa);
                if (result.isFound()) {
                    dist[i][j] = result.getDistance();
                    dist[j][i] = result.getDistance();
                } else {
                    System.err.printf("No se pudo calcular la distancia entre %s y %s\n", from, to);
                    dist[i][j] = Double.MAX_VALUE;
                    dist[j][i] = Double.MAX_VALUE;
                }
            }
        }
        System.out.println("Distancias calculadas con A*.");

        // inicializar la matriz de feromonas
        double[][] tau = new double[n][n];
        double initialFeromone = 1.0 / (n * n); // feromona inicial pequena
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                // evitat divisiones por cero o problemas con MAX_VALUE
                if (i != j && dist[i][j] != Double.MAX_VALUE)
                    tau[i][j] = initialFeromone;
                else
                    tau[i][j] = 0.0; // no hay camino
            }
        }

        int[] bestTour = null;
        double bestLen = Double.MAX_VALUE; // longitud del mejor tour
        boolean feasibleTourFound = false; // si se encontró un tour factible

        Random rnd = new Random();

        for (int iter = 0; iter < cfg.getNIter(); iter++) {
            List<int[]> antsTours = new ArrayList<>();
            List<Double> tourLengths = new ArrayList<>();
            List<Boolean> tourFeasibility = new ArrayList<>();

            // --- construir tours ---
            for (int k = 0; k < cfg.getNAnts(); k++) {

                int[] tour = buildAntTour(n, tau, dist, rnd);
                antsTours.add(tour);

                // evaluar tour
                RouteEvaluationResult eval = evaluateTour(tour, dist, todos, destinoPedidosMap, camion);
                tourLengths.add(eval.getLength());
                tourFeasibility.add(eval.isFeasible());

                // actualizar el mejor tour feasible encontrado
                if (eval.isFeasible() && eval.getLength() < bestLen) {
                    bestLen = eval.getLength();
                    bestTour = tour.clone();
                    feasibleTourFound = true;
                    System.out.printf("Mejor tour encontrado en iteración %d: %s (%.2f)\n",
                            iter, Arrays.toString(tour), bestLen);
                }
            }

            // --- evaporación global ---
            for (int i = 0; i < n; i++)
                for (int j = 0; j < n; j++)
                    tau[i][j] *= (1.0 - cfg.getRho());

            // --- deposito de feromonas ---
            for (int tourIdx = 0; tourIdx < antsTours.size(); tourIdx++) {
                int[] tour = antsTours.get(tourIdx);
                double l = tourLengths.get(tourIdx);
                boolean feasible = tourFeasibility.get(tourIdx);

                // Only deposit pheromone for feasible tours, or maybe penalize infeasible?
                // simple approach: only feasible tours contribute significantly
                double delta = 0;
                if (feasible && l > 0) { // avoid division by zero
                    delta = cfg.getQ() / l;
                } else if (!feasible) {
                    // optional: Small deposit even for infeasible, or negative?
                    // delta = cfg.getQ() / (l + DEADLINE_PENALTY); // Penalized deposit
                    delta = 0; // No deposit for infeasible
                }

                if (delta > 0) {
                    for (int s = 0; s < tour.length - 1; s++) {
                        int i = tour[s];
                        int j = tour[s + 1];
                        if (dist[i][j] != Double.MAX_VALUE) { // don't add pheromone to unreachable paths
                            tau[i][j] += delta;
                            tau[j][i] += delta; // symmetric
                        }
                    }
                }
            }
            // optinal: pheromone update based on the best tour of the iteration
            // other ACO variants use this
        }

        if (!feasibleTourFound || bestTour == null) {
            System.err.printf("No se encontró un tour factible para el camión %s\n", camion.getCodigo());
            return null;
        }

        // convertir el bestTour → List<Posicion>
        List<Posicion> ruta = new ArrayList<>();
        for (int idx : bestTour) {
            Nodo nodo = todos.get(idx);
            // marcar los destinos: 0 = depósito, 1 = destino
            int esDestino = idx == 0 ? 0 : 1;
            ruta.add(Posicion.builder()
                    .X(nodo.getX()).Y(nodo.getY()).destino(esDestino).build());
        }

        // calcular horas de inicio y fin
        LocalDateTime startTime = camion.getFechaInicio() != null
                ? camion.getFechaInicio().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now(); // default start time

        double minutos = (bestLen / camion.getVelocidad()) * 60; // km/h → min
        LocalDateTime endTime = startTime.plusMinutes((long) minutos);

        return new Ruta(ruta, startTime.toString(), endTime.toString()); // ruta generada
    }

    //////////////////////////////////////////////////////////////////////
    /* --- utilidades privadas --- */
    //////////////////////////////////////////////////////////////////////

    // --- ACO Tour Building Logic ---
    private int[] buildAntTour(int n, double[][] tau, double[][] dist, Random rnd) {
        boolean[] visited = new boolean[n];
        int[] tour = new int[n + 1]; // +1 para el depósito al final
        tour[0] = 0; // depósito al inicio
        visited[0] = true; // marcar depósito como visitado
        int current = 0; // nodo actual (depósito)

        for (int step = 1; step < n; step++) {
            int next = seleccionarSiguiente(current, visited, tau, dist, rnd);
            if (next == -1) {
                System.err.println("Hormiga no pudo seleccionar siguiente nodo");
                return new int[0]; // error
            }
            tour[step] = next;
            visited[next] = true; // marcar como visitado
            current = next; // mover al siguiente nodo
        }
        tour[n] = 0; // volver al depósito
        return tour; // tour completo
    }

    private int seleccionarSiguiente(int actual, boolean[] visited,
            double[][] tau, double[][] dist, Random rnd) {

        double[] prob = new double[visited.length];
        double sum = 0.0;
        for (int j = 0; j < visited.length; j++) {
            if (!visited[j] && dist[actual][j] != Double.MAX_VALUE && dist[actual][j] > 0) {
                // asegura que la distancia no sea cero o infinita
                double pheromone = Math.pow(tau[actual][j], cfg.getAlpha());
                double heuristic = Math.pow(1.0 / dist[actual][j], cfg.getBeta());
                prob[j] = pheromone * heuristic;
                sum += prob[j];
            } else {
                prob[j] = 0.0; // no se puede visitar
            }
        }

        if (sum == 0.0) {
            for (int j = 0; j < visited.length; j++)
                if (!visited[j] && dist[actual][j] != Double.MAX_VALUE)
                    return j; // no hay opciones, pero hay nodos no visitados
            return -1; // no hay opciones
        }

        // seleccion por ruleta
        double r = rnd.nextDouble() * sum;
        double acum = 0;
        for (int j = 0; j < prob.length; j++) {
            if (prob[j] > 0) {
                acum += prob[j];
                if (acum >= r)
                    return j; // nodo seleccionado
            }
        }

        // fallback (sólo debería pasar por redondeo)
        for (int j = 0; j < visited.length; j++)
            if (!visited[j] && dist[actual][j] != Double.MAX_VALUE)
                return j;
        return -1; // nunca
    }

    // --- Evaluación de rutas ---
    private static class RouteEvaluationResult {
        private final double length;
        private final boolean feasible;

        public RouteEvaluationResult(double length, boolean feasible) {
            this.length = length;
            this.feasible = feasible;
        }

        public double getLength() {
            return length;
        }

        public boolean isFeasible() {
            return feasible;
        }
    }

    private RouteEvaluationResult evaluateTour(int[] tour, double[][] dist,
            List<Nodo> todos, Map<Nodo, List<Pedido>> destinoPedidosMap, Camion camion) {

        double currentLength = 0.0;
        LocalDateTime currentTime = camion.getFechaInicio() != null
                ? camion.getFechaInicio().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now(); // default start time

        boolean deadlineMet = true; // flag para verificar si se cumplen los deadlines

        for (int i = 0; i < tour.length - 1; i++) {
            int fromIdx = tour[i];
            int toIdx = tour[i + 1];

            double segmentDist = dist[fromIdx][toIdx];
            if (segmentDist == Double.MAX_VALUE) {
                return new RouteEvaluationResult(Double.MAX_VALUE, false); // ruta no factible
            }

            currentLength += segmentDist;

            // calcular tiempo de viaje
            double travelTime = (segmentDist / camion.getVelocidad()) * 60; // km/h → min
            currentTime = currentTime.plusMinutes(Math.round(travelTime));

            // checkear si se cumplen los deadlines si toIdx es un destino
            if (toIdx != 0) { // no es el depósito
                Nodo destino = todos.get(toIdx);
                List<Pedido> pedidos = destinoPedidosMap.get(destino);
                if (pedidos != null) {
                    for (Pedido p : pedidos) {
                        if (p.getFechaLimiteEntrega() != null && currentTime.isAfter(p.getFechaLimiteEntrega())) {
                            deadlineMet = false; // deadline no cumplido
                            // se puede tambien calcular el penalty aqui en caso sea tarde
                            // double lateness = Duration.between(p.getFechaLimiteEntrega(),
                            // currentTime).toMinutes();
                            // penalty += lateness * DEADLINE_PENALTY;
                            // System.out.printf("Pedido %s no cumplido a tiempo\n", p.getId());
                            break; // un deadline no cumplido hace la ruta infeasible
                        }
                    }
                }
            }

            if (!deadlineMet) {
                break; // no necesitamos seguir evaluando
            }
        }

        // retorna la longitud total y si es factible
        // si un deadline no se cumple, la ruta es infeasible
        return new RouteEvaluationResult(currentLength, deadlineMet);
    }

    // --- naive FCFS para signacion de pedidos ---
    private Map<Camion, List<Pedido>> asignarPedidosFCFS(List<Camion> camiones,
            List<Pedido> pedidos) {
        Map<Camion, List<Pedido>> map = new HashMap<>();
        // resetar camiones
        for (Camion c : camiones) {
            c.setCargaGLP(0);
            map.put(c, new ArrayList<>());
        }

        // ordenar pedidos por fecha de pedido / deadline
        pedidos.sort(Comparator.comparing(Pedido::getFechaPedido,
                Comparator.nullsFirst(Comparator.naturalOrder()))); // FCFS

        List<Pedido> pedidosNoAsignados = new ArrayList<>();

        for (Pedido p : pedidos) {
            Optional<Camion> asignado = camiones.stream()
                    .filter(c -> c.getCapacidad() - c.getCargaGLP() >= p.getVolumen())
                    .findFirst();

            if (asignado.isPresent()) {
                Camion c = asignado.get();
                map.get(c).add(p);
                c.setCargaGLP(c.getCargaGLP() + p.getVolumen());
            } else {
                pedidosNoAsignados.add(p);
                System.err.printf("Advertencia. No se  pudo asignar el pedido %d a ningun camion.\n", p.getId(),
                        p.getVolumen());
            }
        }
        if (!pedidosNoAsignados.isEmpty()) {
            System.err.printf("Advertencia. %d pedidos no asignados.\n", pedidosNoAsignados.size());
        }
        return map;
    }

    // antes se usaba calcularLongitud, ahroa e usa evaluateTour
    // private double calcularLongitud(int[] tour, double[][] dist) {
    // double l = 0;
    // for (int i = 0; i < tour.length - 1; i++) {
    // if (dist[tour[i]][tour[i + 1]] == Double.MAX_VALUE) return Double.MAX_VALUE;
    // // Infeasible path
    // l += dist[tour[i]][tour[i + 1]];
    // }
    // return l;
    // }

    public void setParametrosACO(double alpha, double beta, double rho, int nAnts, int nIter) {
        cfg.setAlpha(alpha);
        cfg.setBeta(beta);
        cfg.setRho(rho);
        cfg.setNAnts(nAnts);
        cfg.setNIter(nIter);
    }

}
