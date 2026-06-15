import java.util.*;

public class DijkstraSimples {

    private Grafo grafo;

    public DijkstraSimples(Grafo grafo) {
        this.grafo = grafo;
    }

    public List<String> calcularRota(Collection<String> nodes) {

        if (nodes.isEmpty()) return new ArrayList<>();

        String origem = nodes.iterator().next();

        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();

        PriorityQueue<Map.Entry<String, Double>> pq =
            new PriorityQueue<>(Map.Entry.comparingByValue());

        // inicialização
        for (String n : nodes) {
            dist.put(n, Double.POSITIVE_INFINITY);
            prev.put(n, null);
        }

        dist.put(origem, 0.0);
        pq.add(new AbstractMap.SimpleEntry<>(origem, 0.0));

        while (!pq.isEmpty()) {
            
            Map.Entry<String, Double> entry = pq.poll();
            String atual = entry.getKey();

            for (Aresta a : grafo.getAdjacencia()
                    .getOrDefault(atual, Collections.emptyList())) {

                String vizinho = a.getDestino();

                if (!nodes.contains(vizinho)) continue;

                double novaDist = dist.get(atual) + a.getDistancia();

                if (novaDist < dist.get(vizinho)) {
                    dist.put(vizinho, novaDist);
                    prev.put(vizinho, atual);
                    pq.add(new AbstractMap.SimpleEntry<>(vizinho, novaDist));
                }
            }
        }

        // ✔ reconstruir caminho até o mais distante
        String destino = null;
        double maior = -1;

        for (String n : nodes) {
            double d = dist.getOrDefault(n, Double.POSITIVE_INFINITY);
            if (d != Double.POSITIVE_INFINITY && d > maior) {
                maior = d;
                destino = n;
            }
        }

        List<String> rota = new ArrayList<>();

        while (destino != null) {
            rota.add(destino);
            destino = prev.get(destino);
        }

        Collections.reverse(rota);
        return rota;
    }
}