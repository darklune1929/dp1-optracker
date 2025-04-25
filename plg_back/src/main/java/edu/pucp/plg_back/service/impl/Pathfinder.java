package edu.pucp.plg_back.service.impl;

import edu.pucp.plg_back.model.Mapa;
import edu.pucp.plg_back.model.Nodo;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class Pathfinder {

    // resultado del intento de encontrar el camino
    public static class PathResult {
        private final double distance;
        private final List<Nodo> path; // reconstruct path if needed
        private final boolean found;

        public PathResult(double distance, List<Nodo> path, boolean found) {
            this.distance = distance;
            this.path = path;
            this.found = found;
        }

        public double getDistance() {
            return distance;
        }

        public List<Nodo> getPath() {
            return path;
        }

        public boolean isFound() {
            return found;
        }
    }

    // A* Algorithm Implementation
    public PathResult findShortestPath(Nodo startNode, Nodo endNode, Mapa map) {
        PriorityQueue<Nodo> openSet = new PriorityQueue<>();
        Set<Nodo> closedSet = new HashSet<>();

        // initialize start node
        startNode.setG(0);
        startNode.calculateHeuristic(endNode);
        startNode.calculateF();
        startNode.setNodoprevio(null);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Nodo current = openSet.poll();

            if (current.equals(endNode)) {
                // path found - reconstruct and return distance
                return new PathResult(current.getG(), reconstructPath(current), true);
            }

            closedSet.add(current);

            for (Nodo neighbor : getNeighbors(current, map)) {
                if (closedSet.contains(neighbor)) {
                    continue; // ignorar los vecinos ya evaluados
                }

                // distancia desde el nodo actual al vecino es siempre 1
                double tentativeGScore = current.getG() + 1;

                boolean inOpenSet = openSet.stream().anyMatch(n -> n.equals(neighbor));

                if (!inOpenSet || tentativeGScore < neighbor.getG()) {
                    neighbor.setNodoprevio(current);
                    neighbor.setG(tentativeGScore);
                    neighbor.calculateHeuristic(endNode);
                    neighbor.calculateF();

                    if (!inOpenSet) {
                        openSet.add(neighbor);
                    } else {
                        // actualizar la pq si g ha mejorado
                        openSet.remove(neighbor); // se necesita eliminar para actualizar
                        openSet.add(neighbor);
                    }
                }
            }
        }

        // camino no encontrado
        return new PathResult(Double.MAX_VALUE, Collections.emptyList(), false);
    }

    // helper para validad neighbors (up, down, left, right)
    private List<Nodo> getNeighbors(Nodo node, Mapa map) {
        List<Nodo> neighbors = new ArrayList<>();
        int[] dx = { 0, 0, 1, -1 };
        int[] dy = { 1, -1, 0, 0 };

        for (int i = 0; i < 4; i++) {
            int newX = node.getX() + dx[i];
            int newY = node.getY() + dy[i];

            if (map.estaLibre(newX, newY)) {
                // Create a new Nodo instance for the neighbor to avoid modifying
                // existing nodes in the open/closed sets incorrectly.
                // We only need coordinates for equality checks and heuristic.
                // G, H, F scores will be calculated if it's a better path.
                Nodo neighborNode = new Nodo(newX, newY);
                neighbors.add(neighborNode);
            }
        }
        return neighbors;
    }

    // Helper to reconstruct the path (optional)
    private List<Nodo> reconstructPath(Nodo target) {
        List<Nodo> path = new LinkedList<>();
        Nodo current = target;
        while (current != null) {
            path.add(0, current); // Add to the beginning
            current = current.getNodoprevio();
        }
        return path;
    }
}
