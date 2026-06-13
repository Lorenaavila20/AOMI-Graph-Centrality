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
        Set<String> visitados = new HashSet<>();

        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        for (String n : nodes) {
            dist.put(n, Double.POSITIVE_INFINITY);
            prev.put(n, null);
        }

        dist.put(origem, 0.0);
        pq.add(origem);

        while (!pq.isEmpty()) {
            String atual = pq.poll();

            if (visitados.contains(atual)) continue;
            visitados.add(atual);

            for (Aresta a : grafo.getAdjacencia().getOrDefault(atual, new ArrayList<>())) {

                String vizinho = a.getDestino();

                if (!nodes.contains(vizinho)) continue;

                double novaDist = dist.get(atual) + a.getPeso();

                if (novaDist < dist.get(vizinho)) {
                    dist.put(vizinho, novaDist);
                    prev.put(vizinho, atual);
                    pq.add(vizinho);
                }
            }
        }

        // 🔥 agora sim: reconstruir rota
        return reconstruirCaminho(prev, origem);
    }

    private List<String> reconstruirCaminho(Map<String, String> prev, String origem) {

        List<String> rota = new ArrayList<>();

        for (String node : prev.keySet()) {
            List<String> caminho = new ArrayList<>();
            String atual = node;

            while (atual != null) {
                caminho.add(atual);
                atual = prev.get(atual);
            }

            Collections.reverse(caminho);

            if (!caminho.isEmpty() && caminho.get(0).equals(origem)) {
                rota.addAll(caminho);
            }
        }

        return rota;
    }
}