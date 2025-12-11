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
     * Versão que retorna distancias e predecessors (útil para comparar custos).
     * Custo usado: distanciaKm / (1 + k * densidadeDestino)
     */
    public static ResultadoDijkstra dijkstraFull(
            Grafo grafo,
            String origem,
            String destino, // pode ser null -> roda completo
            double k
    ) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Map<String, List<Aresta>> adj = grafo.getAdjacencia();
        Map<String, Double> dens = grafo.getNos();
        Set<String> visitados = new HashSet<>();

        for (String node : adj.keySet()) {
            dist.put(node, Double.POSITIVE_INFINITY);
            prev.put(node, null);
        }
        if (!dist.containsKey(origem)) {
            // origem inválida — retorna estruturas vazias para evitar NPE
            return new ResultadoDijkstra(dist, prev);
        }
        dist.put(origem, 0.0);

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));
        pq.add(origem);

        final double EPS = 1e-9;

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (visitados.contains(u)) continue;
            visitados.add(u);

            double du = dist.get(u);
            if (du == Double.POSITIVE_INFINITY) break;

            if (destino != null && u.equals(destino)) {
                // Parar cedo se chegamos ao destino pedido
                break;
            }

            for (Aresta e : adj.getOrDefault(u, Collections.emptyList())) {
                String v = e.getDestino();
                double distanciaKm = e.getPeso();
                double densV = dens.getOrDefault(v, 0.0);

                double fatorDensidade = 1.0 + (k * densV);
                double custo = distanciaKm / fatorDensidade;
                if (custo < EPS) custo = EPS;

                double alt = du + custo;
                if (alt + 1e-12 < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        return new ResultadoDijkstra(dist, prev);
    }

    /**
     * Versão simples compatível com seu código existente: retorna apenas prev (mantida por compatibilidade).
     */
    public static Map<String, String> dijkstra(
            Grafo grafo,
            String origem,
            String destino,
            double k
    ) {
        ResultadoDijkstra r = dijkstraFull(grafo, origem, destino, k);
        return r.prev;
    }
}
