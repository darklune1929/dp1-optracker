// GAPlanificador.java
package edu.pucp.plg_back.service.impl;

import edu.pucp.plg_back.model.*;
import edu.pucp.plg_back.service.Planificador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class GAPlanificador implements Planificador {

    private final GAParams cfg = GAParams.builder().build();
    private final Random rnd = new Random();
    private final Pathfinder pathfinder = new Pathfinder();
    // private final double DEADLINE_PENALTY_FACTOR = 10.0;

    // asumimos igualmente que el mapa es proveido o creado en otra parte
    private final Mapa mapa = new Mapa();

    @Override
    public List<Ruta> planificar(List<Camion> camiones, List<Pedido> pedidos) {
        // --- Add some example blockages to the map ---
        // mapa.setBloqueado(20, 20, true);
        // mapa.setBloqueado(21, 20, true);
        // ---

        Map<Camion, List<Pedido>> buckets = asignarPedidos(camiones, pedidos);

        List<Ruta> rutas = new ArrayList<>();
        buckets.forEach((c, lista) -> {
            if (!lista.isEmpty()) {
                Ruta r = evolucionar(c, lista);
                if (r != null)
                    rutas.add(r);
                else
                    System.err.printf("No se pudo planificar la ruta para el camión %s\n", c.getCodigo());
            }
        });

        rutas.sort(Comparator.naturalOrder());
        return rutas;
    }

    /* ---------- GA-TSP para un único camión ---------- */
    private Ruta evolucionar(Camion camion, List<Pedido> pedidos) {
        Nodo deposito = new Nodo(12, 8); // depósito central

        // map destination nodes to the list of orders at that location
        Map<Nodo, List<Pedido>> pedidosPorDestino = new HashMap<>();
        for (Pedido p : pedidos) {
            Nodo destino = new Nodo(p.getX(), p.getY());
            pedidosPorDestino.computeIfAbsent(destino, k -> new ArrayList<>()).add(p);
        }
        List<Nodo> destinosUnicos = new ArrayList<>(pedidosPorDestino.keySet());

        int n = destinosUnicos.size() + 1; // 0 = depósito
        if (n <= 1)
            return null; // no hay pedidos

        List<Nodo> nodos = new ArrayList<>();
        nodos.add(deposito);
        nodos.addAll(destinosUnicos);

        double[][] dist = new double[n][n];
        System.out.printf("Calculando A* distancias para los nodos del camión %s...\n", camion.getCodigo());
        for (int i = 0; i < n; i++) {
            for (int j = i; j < n; j++) {
                if (i == j) {
                    dist[i][j] = 0;
                    continue;
                }
                Nodo from = nodos.get(i);
                Nodo to = nodos.get(j);
                // resetear los nodos
                from = new Nodo(from.getX(), from.getY());
                to = new Nodo(to.getX(), to.getY());

                Pathfinder.PathResult pathResult = pathfinder.findShortestPath(from, to, mapa);
                if (pathResult.isFound()) {
                    dist[i][j] = pathResult.getDistance();
                    dist[j][i] = pathResult.getDistance();
                } else {
                    System.err.printf("No se pudo encontrar un camino entre %s y %s\n", from, to);
                    dist[i][j] = Double.MAX_VALUE;
                    dist[j][i] = Double.MAX_VALUE;
                }
            }
        }
        System.out.printf("Distancias A* calculadas para el camión %s\n", camion.getCodigo());

        /* --- crear población inicial --- */
        Population pop = new Population(cfg.getPopSize());
        for (int i = 0; i < cfg.getPopSize(); i++)
            pop.add(randomChromosome(n));

        // evaluar la población inicial
        evaluar(pop, dist, nodos, pedidosPorDestino, camion);
        pop.sort(); // ordenar por fitness

        /* --- ciclo evolutivo --- */
        for (int gen = 0; gen < cfg.getNGenerations(); gen++) {
            Population next = new Population(cfg.getPopSize());

            if (!pop.individuals.isEmpty())
                next.add(copy(pop.best()));

            // generar el resto de la población
            while (next.individuals.size() < cfg.getPopSize()) {
                Chromosome p1 = torneo(pop);
                Chromosome p2 = torneo(pop);

                // crossover
                List<Chromosome> hijos = crossover(p1, p2);

                // mutacion
                List<Chromosome> mutableHijos = new ArrayList<>(hijos);
                mutableHijos.replaceAll(this::mutar); // mutate the copy

                // agregar hijos a la nueva población
                for (Chromosome h : mutableHijos)
                    if (next.individuals.size() < cfg.getPopSize())
                        next.add(h);
                    else
                        break;
            }

            // evaluar la nueva población
            evaluar(next, dist, nodos, pedidosPorDestino, camion);
            next.sort();
            pop = next;

            // Optional: Print best fitness per generation
            // if (gen % 10 == 0 && !pop.individuals.isEmpty()) {
            // System.out.printf("Gen %d: Best Fitness = %.6f (Dist: %.2f)\n",
            // gen, pop.best().getFitness(), 1.0/pop.best().getFitness());
            // }
        }

        if (pop.individuals.isEmpty()) {
            System.err.printf("Warning (GA): No se pudo encontrar una ruta para el camión %s\n", camion.getCodigo());
            return null;
        }

        Chromosome best = pop.best();

        // evaluar si la mejor solución es válida (fitness > 0)
        if (best.getFitness() <= 0) {
            System.err.printf("Warning (GA): Fitness <= 0 para el camión %s\n", camion.getCodigo());
            return null; // no feasible solution found
        }

        // convertir el mejor cromosoma a una ruta
        List<Posicion> ruta = best.getGenes().stream()
                .map(i -> {
                    Nodo nodo = nodos.get(i);
                    int esDestino = (i == 0) ? 0 : 1; // 0 = depósito, 1 = destino
                    return Posicion.builder()
                            .X(nodo.getX()).Y(nodo.getY())
                            .destino(esDestino).build();
                }).collect(Collectors.toList());

        // calcular tiempos de inicio y fin
        LocalDateTime startTime = camion.getFechaInicio() != null
                ? camion.getFechaInicio().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();

        // recalcular la distancia final
        double distancia = 0;
        List<Integer> genes = best.getGenes();
        for (int i = 0; i < genes.size() - 1; i++) {
            double segmentDistance = dist[genes.get(i)][genes.get(i + 1)];
            if (segmentDistance == Double.MAX_VALUE) {
                System.err.printf("Error (GA): No se pudo encontrar un camino entre %s y %s\n",
                        nodos.get(genes.get(i)), nodos.get(genes.get(i + 1)));
                return null; // no feasible solution found
            }
            distancia += segmentDistance;
        }

        double totalTime = (distancia / camion.getVelocidad()) * 60; // en minutos
        LocalDateTime endTime = startTime.plusMinutes(Math.round(totalTime));

        return new Ruta(ruta, startTime.toString(), endTime.toString());
    }

    /* ---------- GA utils ---------- */

    private Chromosome randomChromosome(int n) {
        List<Integer> g = new ArrayList<>();
        // genes = [0, perm(1..n-1), 0] pero codificamos sólo la perm, 0 implícito
        g.add(0); // depósito al inicio
        List<Integer> destinations = IntStream.range(1, n).boxed().collect(Collectors.toList());
        Collections.shuffle(destinations, rnd);
        g.addAll(destinations);
        g.add(0); // depósito al final
        return new Chromosome(g, 0.0); // fitness inicial 0
    }

    // evaluates the fitness of each chromosome in the population
    private void evaluar(Population pop, double[][] dist, List<Nodo> todos,
            Map<Nodo, List<Pedido>> pedidosDestino, Camion camion) {

        LocalDateTime startTime = camion.getFechaInicio() != null
                ? camion.getFechaInicio().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : LocalDateTime.now();

        for (Chromosome c : pop.individuals) {
            double currentLength = 0;
            LocalDateTime currentTime = startTime; // Reset time for each chromosome
            boolean deadlinesMet = true;
            boolean pathPossible = true;
            List<Integer> genes = c.getGenes();

            for (int i = 0; i < genes.size() - 1; i++) {
                int fromIdx = genes.get(i);
                int toIdx = genes.get(i + 1);

                double segmentDist = dist[fromIdx][toIdx];
                if (segmentDist == Double.MAX_VALUE) {
                    pathPossible = false;
                    break; // stop calculating if path is impossible
                }

                currentLength += segmentDist;

                // Calculate travel time
                double travelMinutes = (segmentDist / camion.getVelocidad()) * 60.0;
                currentTime = currentTime.plusMinutes(Math.round(travelMinutes));

                // Check deadlines at destination
                if (toIdx != 0) { // If it's not the depot
                    Nodo destinationNode = todos.get(toIdx);
                    List<Pedido> pedidosAtDestination = pedidosDestino.get(destinationNode);
                    if (pedidosAtDestination != null) {
                        for (Pedido p : pedidosAtDestination) {
                            if (p.getFechaLimiteEntrega() != null && currentTime.isAfter(p.getFechaLimiteEntrega())) {
                                deadlinesMet = false;
                                // Optional: Add penalty based on lateness
                                // double lateness = Duration.between(p.getFechaLimiteEntrega(),
                                // currentTime).toMinutes();
                                // currentLength += lateness * PENALTY_FACTOR; // Add penalty to distance
                                break;
                            }
                        }
                    }
                }
                if (!deadlinesMet)
                    break; // Stop checking this chromosome if deadline missed
            }

            // Assign fitness
            if (!pathPossible) {
                c.setFitness(0.0); // Impossible path = zero fitness
            } else if (!deadlinesMet) {
                // Option 1: Zero fitness for any deadline miss
                c.setFitness(0.0);
                // Option 2: Penalized fitness (inversely proportional to penalized distance)
                // double penalizedDistance = currentLength * DEADLINE_PENALTY_FACTOR; // Make
                // distance much larger
                // c.setFitness(1.0 / penalizedDistance);
            } else if (currentLength <= 0) {
                c.setFitness(0.0); // Avoid division by zero if distance is somehow zero
            } else {
                // Fitness is inversely proportional to distance for feasible solutions
                c.setFitness(1.0 / currentLength);
            }
        }
    }

    private Chromosome torneo(Population pop) {
        if (pop.individuals.isEmpty())
            return null;

        Population tournament = new Population(cfg.getTournamentK());
        for (int i = 0; i < cfg.getTournamentK(); i++) {
            Chromosome cand = pop.individuals.get(rnd.nextInt(pop.individuals.size()));
            tournament.add(cand);
        }
        tournament.sort(); // ordenar por fitness
        return tournament.best();
    }

    // Order-1 crossover
    private List<Chromosome> crossover(Chromosome p1, Chromosome p2) {
        // retorna copias de p1 y p2 si no se hace crossover
        if (rnd.nextDouble() > cfg.getCrossoverProb() || p1 == null || p2 == null)
            return List.of(copy(p1), copy(p2));

        List<Integer> g1 = p1.getGenes();
        List<Integer> g2 = p2.getGenes();
        int size = g1.size(); // Includes start/end depots

        // crossover the sequence of destinations (excluding start/end depots)
        int tourSize = size - 2;
        if (tourSize < 2) { // not enough destinations to perform crossover
            return List.of(copy(p1), copy(p2));
        }

        int a = rnd.nextInt(tourSize); // crossover points within the destination list
        int b = rnd.nextInt(tourSize);
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }
        // adjust indices to account for the starting depot at index 0
        int startIdx = a + 1;
        int endIdx = b + 1;

        // create offspring lists initialized with -1 (unassigned)
        List<Integer> h1Genes = new ArrayList<>(Collections.nCopies(size, -1));
        List<Integer> h2Genes = new ArrayList<>(Collections.nCopies(size, -1));
        h1Genes.set(0, 0); // start depot
        h2Genes.set(0, 0); // start depot
        h1Genes.set(size - 1, 0); // end depot
        h2Genes.set(size - 1, 0); // end depot

        // copy the segment from p1 to h1 and from p2 to h2
        Set<Integer> h1Segment = new HashSet<>();
        Set<Integer> h2Segment = new HashSet<>();
        for (int i = startIdx; i <= endIdx; i++) {
            h1Genes.set(i, g1.get(i));
            h2Genes.set(i, g2.get(i));
            h1Segment.add(g1.get(i));
            h2Segment.add(g2.get(i));
        }

        fillRest(h1Genes, g2, h1Segment, startIdx, endIdx);
        fillRest(h2Genes, g1, h2Segment, startIdx, endIdx);

        return List.of(new Chromosome(h1Genes, 0.0), new Chromosome(h2Genes, 0.0));
    }

    private void fillRest(List<Integer> child, List<Integer> donor, Set<Integer> childSegment, int startIdx,
            int endIdx) {
        int size = child.size();
        int donorIdx = 0;
        int childIdx = 0;

        while (child.contains(-1)) { // While there are unfilled slots
            // Find the next empty slot in the child, skipping the copied segment and depots
            while (childIdx < size - 1 && child.get(childIdx) != -1) {
                childIdx++;
            }
            if (childIdx >= size - 1)
                break; // Should be full except last depot

            // Find the next gene in the donor that's not already in the child's copied
            // segment
            int geneToAdd = -1;
            while (donorIdx < donor.size()) {
                int currentDonorGene = donor.get(donorIdx++);
                // Check if it's a destination and not already in the child's segment
                if (currentDonorGene != 0 && !childSegment.contains(currentDonorGene)
                        && !child.contains(currentDonorGene)) {
                    geneToAdd = currentDonorGene;
                    break;
                }
            }

            if (geneToAdd != -1) {
                child.set(childIdx, geneToAdd);
            } else {
                // This might happen if logic has issues or duplicates exist where they
                // shouldn't
                System.err.println("Error during crossover fillRemaining - could not find suitable gene.");
                // Fill with a placeholder or handle error
                if (child.get(childIdx) == -1)
                    child.set(childIdx, 0); // Avoid infinite loop
            }
        }
        // Ensure start/end depots are correct
        child.set(0, 0);
        child.set(size - 1, 0);
    }

    private Chromosome mutar(Chromosome c) {
        if (rnd.nextDouble() > cfg.getMutationProb() || c == null)
            return c;

        Chromosome mutated = copy(c);
        List<Integer> genes = mutated.getGenes();
        int size = genes.size();
        if (size <= 3)
            return mutated; // no mutation possible

        // Swap two random genes in the range [1, size - 2] (excluding depots)
        int i = 1 + rnd.nextInt(size - 2);
        int j = 1 + rnd.nextInt(size - 2);
        while (i == j) {
            j = 1 + rnd.nextInt(size - 2); // Ensure i and j are different
        }

        Collections.swap(genes, i, j);
        mutated.setFitness(0); // reset fitness to be recalculated
        return mutated;
    }

    private Chromosome copy(Chromosome c) {
        if (c == null)
            return null;
        return new Chromosome(new ArrayList<>(c.getGenes()), c.getFitness());
    }

    /* ---------- asignación FCFS idéntica a ACO ---------- */
    private Map<Camion, List<Pedido>> asignarPedidos(List<Camion> camiones, List<Pedido> pedidos) {
        Map<Camion, List<Pedido>> map = new HashMap<>();
        camiones.forEach(c -> {
            c.setCargaGLP(0); // resetear carga
            map.put(c, new ArrayList<>());
        });

        pedidos.stream()
                .sorted(Comparator.comparing(
                        Pedido::getFechaPedido,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(p -> camiones.stream()
                        .filter(c -> c.getCapacidad() - c.getCargaGLP() >= p.getVolumen())
                        .findFirst() // FCFS
                        .ifPresent(c -> {
                            map.get(c).add(p);
                            c.setCargaGLP(c.getCargaGLP() + p.getVolumen());
                        }));
        return map;
    }

    // ---Seteo de parametros GA para el diseño de experimentos---
    public void setParametrosGA(int popSize, int nGenerations, double crossoverProb,
            double mutationProb) {
        this.cfg.setPopSize(popSize);
        this.cfg.setNGenerations(nGenerations);
        this.cfg.setCrossoverProb(crossoverProb);
        this.cfg.setMutationProb(mutationProb);
    }
}
