import java.util.*;

public class DijkstraHibrido {

    public static class ResultadoDijkstra {
        public final Map<String, Double> dist;
        public final Map<String, String> prev;

        public ResultadoDijkstra(Map<String, Double> dist, Map<String, String> prev) {
            this.dist = dist;
            this.prev = prev;
        }
    }

    /**
     * Dijkstra com custo híbrido:
     * custo = (alpha * distancia) / (1 + beta * densidade)
     */
    public static ResultadoDijkstra dijkstraFull(
            Grafo grafo,
            String origem,
            String destino, // pode ser null
            double alpha,
            double beta
    ) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Map<String, List<Aresta>> adj = grafo.getAdjacencia();
        Map<String, Double> dens = grafo.getNos();
        Set<String> visitados = new HashSet<>();

        // Inicialização
        for (String node : adj.keySet()) {
            dist.put(node, Double.POSITIVE_INFINITY);
            prev.put(node, null);
        }

        if (!dist.containsKey(origem)) {
            return new ResultadoDijkstra(dist, prev);
        }

        dist.put(origem, 0.0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(origem);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (visitados.contains(u)) continue;
            visitados.add(u);

            if (destino != null && u.equals(destino)) break;

            for (Aresta e : adj.getOrDefault(u, Collections.emptyList())) {
                String v = e.getDestino();

                double distancia = e.getPeso();
                double densidade = dens.getOrDefault(v, 0.0);

                double custo = (alpha * distancia) / (1.0 + beta * densidade);

                double alt = dist.get(u) + custo;

                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        return new ResultadoDijkstra(dist, prev);
    }

    /**
     * Versão simplificada (compatibilidade)
     */
    public static Map<String, String> dijkstra(
            Grafo grafo,
            String origem,
            String destino,
            double alpha,
            double beta
    ) {
        return dijkstraFull(grafo, origem, destino, alpha, beta).prev;
    }
}