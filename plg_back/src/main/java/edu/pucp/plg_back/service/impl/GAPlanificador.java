// GAPlanificador.java
package edu.pucp.plg_back.service.impl;

import edu.pucp.plg_back.model.*;
import edu.pucp.plg_back.service.Planificador;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
// import java.util.stream.Collectors;

/** GA sencillo VRP = asignación FCFS + GA-TSP por camión */
@Service
@RequiredArgsConstructor
public class GAPlanificador implements Planificador {

    private final GAParams cfg = GAParams.builder().build();
    private final Random rnd = new Random();

    @Override
    public List<Ruta> planificar(List<Camion> camiones, List<Pedido> pedidos) {

        Map<Camion, List<Pedido>> buckets = asignarPedidos(camiones, pedidos);

        List<Ruta> rutas = new ArrayList<>();
        buckets.forEach((c, lista) -> {
            if (!lista.isEmpty())
                rutas.add(evolucionar(c, lista));
        });

        rutas.sort(Comparator.naturalOrder());
        return rutas;
    }

    /* ---------- GA-TSP para un único camión ---------- */
    private Ruta evolucionar(Camion camion, List<Pedido> pedidos) {
        Nodo deposito = new Nodo(12, 8); // depósito central
                                         // :contentReference[oaicite:0]{index=0}&#8203;:contentReference[oaicite:1]{index=1}
        List<Nodo> destinos = pedidos.stream()
                .map(p -> new Nodo(p.getX(), p.getY()))
                .distinct().toList();

        int n = destinos.size() + 1; // 0 = depósito
        double[][] dist = new double[n][n];
        List<Nodo> nodos = new ArrayList<>();
        nodos.add(deposito);
        nodos.addAll(destinos);
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                dist[i][j] = nodos.get(i).getDistancia(nodos.get(j));

        /* --- crear población inicial --- */
        Population pop = new Population(cfg.getPopSize());
        for (int i = 0; i < cfg.getPopSize(); i++)
            pop.add(randomChromosome(n));
        evaluar(pop, dist);
        pop.sort();

        /* --- ciclo evolutivo --- */
        for (int gen = 0; gen < cfg.getNGenerations(); gen++) {
            Population next = new Population(cfg.getPopSize());

            // elitismo 1 individuo
            next.add(pop.best());

            while (next.individuals.size() < cfg.getPopSize()) {
                Chromosome p1 = torneo(pop);
                Chromosome p2 = torneo(pop);

                List<Chromosome> hijos = new ArrayList<>(crossover(p1, p2));
                hijos.replaceAll(this::mutar);

                next.individuals.addAll(hijos);
            }
            evaluar(next, dist);
            next.sort();
            pop = next;
        }

        Chromosome best = pop.best();
        List<Posicion> ruta = best.getGenes().stream()
                .map(i -> {
                    Nodo nodo = nodos.get(i);
                    return Posicion.builder()
                            .X(nodo.getX()).Y(nodo.getY())
                            .destino(i == 0 ? 0 : 1).build();
                }).toList();

        Calendar ini = Optional.ofNullable(camion.getFechaInicio())
                .orElse(Calendar.getInstance());
        float minutos = (float) ((1 / best.getFitness()) / camion.getVelocidad() * 60);
        Calendar fin = (Calendar) ini.clone();
        fin.add(Calendar.MINUTE, Math.round(minutos));

        return new Ruta(new ArrayList<>(ruta),
                ini.getTime().toString(),
                fin.getTime().toString());
    }

    /* ---------- GA utils ---------- */

    private Chromosome randomChromosome(int n) {
        List<Integer> g = new ArrayList<>();
        // genes = [0, perm(1..n-1), 0] pero codificamos sólo la perm, 0 implícito
        for (int i = 1; i < n; i++)
            g.add(i);
        Collections.shuffle(g, rnd);
        g.add(0, 0);
        g.add(0); // depósito al inicio y final
        return new Chromosome(g, 0.0);
    }

    private void evaluar(Population pop, double[][] dist) {
        for (Chromosome c : pop.individuals) {
            double d = 0;
            List<Integer> g = c.getGenes();
            for (int i = 0; i < g.size() - 1; i++)
                d += dist[g.get(i)][g.get(i + 1)];
            c.setFitness(1.0 / d);
        }
    }

    private Chromosome torneo(Population pop) {
        Chromosome best = null;
        for (int i = 0; i < cfg.getTournamentK(); i++) {
            Chromosome cand = pop.individuals.get(rnd.nextInt(pop.individuals.size()));
            if (best == null || cand.compareTo(best) < 0)
                best = cand;
        }
        return best;
    }

    private List<Chromosome> crossover(Chromosome p1, Chromosome p2) {
        if (rnd.nextDouble() > cfg.getCrossoverProb())
            return List.of(copy(p1), copy(p2));

        int size = p1.getGenes().size() - 2; // sin contar 0 inicial/final
        int a = 1 + rnd.nextInt(size - 1); // índices dentro de perm
        int b = 1 + rnd.nextInt(size - 1);
        if (a > b) {
            int t = a;
            a = b;
            b = t;
        }

        // Order-1 crossover
        List<Integer> h1 = new ArrayList<>(Collections.nCopies(size + 2, -1));
        List<Integer> h2 = new ArrayList<>(Collections.nCopies(size + 2, -1));

        h1.set(0, 0);
        h1.set(size + 1, 0);
        h2.set(0, 0);
        h2.set(size + 1, 0);

        for (int i = a; i <= b; i++) {
            h1.set(i, p1.getGenes().get(i));
            h2.set(i, p2.getGenes().get(i));
        }
        fillRest(h1, p2, a, b);
        fillRest(h2, p1, a, b);

        return List.of(new Chromosome(h1, 0), new Chromosome(h2, 0));
    }

    private void fillRest(List<Integer> child, Chromosome donor, int a, int b) {
        int size = donor.getGenes().size();
        int idx = (b + 1) % size;
        for (int g : donor.getGenes()) {
            if (!child.contains(g)) {
                while (child.get(idx) != -1)
                    idx = (idx + 1) % size;
                child.set(idx, g);
            }
        }
    }

    private Chromosome mutar(Chromosome c) {
        if (rnd.nextDouble() > cfg.getMutationProb())
            return c;
        int size = c.getGenes().size() - 2;
        int i = 1 + rnd.nextInt(size - 1);
        int j = 1 + rnd.nextInt(size - 1);
        Collections.swap(c.getGenes(), i, j);
        return c;
    }

    private Chromosome copy(Chromosome c) {
        return new Chromosome(new ArrayList<>(c.getGenes()), c.getFitness());
    }

    /* ---------- asignación FCFS idéntica a ACO ---------- */
    private Map<Camion, List<Pedido>> asignarPedidos(List<Camion> camiones, List<Pedido> pedidos) {
        Map<Camion, List<Pedido>> map = new HashMap<>();
        camiones.forEach(c -> map.put(c, new ArrayList<>()));

        pedidos.stream()
                .sorted(Comparator.comparing(
                        Pedido::getFechaPedido,
                        Comparator.nullsFirst(Comparator.naturalOrder())))
                .forEach(p -> camiones.stream()
                        .filter(c -> c.getCapacidad() - c.getCargaGLP() >= p.getVolumen())
                        .findFirst()
                        .ifPresent(c -> {
                            map.get(c).add(p);
                            c.setCargaGLP(c.getCargaGLP() + p.getVolumen());
                        }));
        return map;
    }
}
