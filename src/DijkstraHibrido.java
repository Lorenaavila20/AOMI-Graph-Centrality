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

    public static ResultadoDijkstra dijkstraFull(
            Grafo grafo,
            String origem,
            String destino,
            double alpha,
            double beta
    ) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Map<String, List<Aresta>> adj = grafo.getAdjacencia();
        Map<String, Double> dens = grafo.getNos();
        Set<String> visitados = new HashSet<>();

        // 🔹 NOVO: pegar min/max para normalização
        double densMin = Collections.min(dens.values());
        double densMax = Collections.max(dens.values());

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

            double du = dist.get(u);
            if (du == Double.POSITIVE_INFINITY) break;

            if (destino != null && u.equals(destino)) break;

            for (Aresta e : adj.getOrDefault(u, Collections.emptyList())) {
                String v = e.getDestino();
                double distanciaKm = e.getPeso();
                double densV = dens.getOrDefault(v, 0.0);

                // 🔹 NORMALIZAÇÃO
                double densNorm = (densV - densMin) / (densMax - densMin + 1e-9);

                // 🔹 NOVA FUNÇÃO DE CUSTO
                double custo = alpha * distanciaKm - beta * densNorm;

                // 🔹 evitar custo negativo (necessário pro Dijkstra)
                if (custo < 1e-6) custo = 1e-6;

                double alt = du + custo;

                if (alt < dist.getOrDefault(v, Double.POSITIVE_INFINITY)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        return new ResultadoDijkstra(dist, prev);
    }
}