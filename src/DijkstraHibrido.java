import java.util.*;

public class DijkstraHibrido {

    // Retorna predecessores para reconstruir caminho
    public static Map<String, String> dijkstra(
            Grafo grafo,
            String origem,
            String destino,
            double k
    ) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Map<String, List<Aresta>> adj = grafo.getAdjacencia();
        Map<String, Double> dens = grafo.getNos();

        for (String node : adj.keySet()) {
            dist.put(node, Double.POSITIVE_INFINITY);
            prev.put(node, null);
        }

        dist.put(origem, 0.0);

        PriorityQueue<String> pq =
                new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        pq.add(origem);

        final double EPS = 1e-6;

        while (!pq.isEmpty()) {

            String u = pq.poll();

            if (u.equals(destino))
                break;

            double du = dist.get(u);
            if (du == Double.POSITIVE_INFINITY)
                break;

            for (Aresta e : adj.getOrDefault(u, Collections.emptyList())) {

                String v = e.getDestino();
                double distanciaKm = e.getPeso();
                double densV = dens.getOrDefault(v, 0.0);

                double custo = distanciaKm - k * densV;
                if (custo < EPS) custo = EPS;

                double alt = du + custo;

                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);

                    pq.remove(v);
                    pq.add(v);
                }
            }
        }

        return prev;
    }
}
