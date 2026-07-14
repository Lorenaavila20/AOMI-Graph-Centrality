import java.util.*;

public class AStar {

    public static class Resultado {
        public Map<String, Double> dist = new HashMap<>();
        public Map<String, String> prev = new HashMap<>();
        public int nosExplorados = 0;
    }

    public static Resultado executar(
        Grafo grafo,
        String origem,
        String destino,
        Map<String, double[]> centroides,
        double alpha,
        double beta
    ) {

        Resultado res = new Resultado();

        Map<String, Double> gScore = new HashMap<>();
        Map<String, Double> fScore = new HashMap<>();

        PriorityQueue<String> pq = new PriorityQueue<>(
            Comparator.comparingDouble(fScore::get)
        );

        for (String n : grafo.getNos().keySet()) {
            gScore.put(n, Double.POSITIVE_INFINITY);
            fScore.put(n, Double.POSITIVE_INFINITY);
        }

        gScore.put(origem, 0.0);
        fScore.put(origem, heuristica(origem, destino, centroides));

        pq.add(origem);

        while (!pq.isEmpty()) {
            String atual = pq.poll();
            res.nosExplorados++;

            if (atual.equals(destino)) break;

            for (Aresta a : grafo.getAdjacencia().getOrDefault(atual, new ArrayList<>())) {

                String viz = a.getDestino();

                double densidade = grafo.getNos().getOrDefault(viz, 0.0);

                double custo = (alpha * a.getDistancia()) / (1 + beta * densidade);

                double tentativeG = gScore.get(atual) + custo;

                if (tentativeG < gScore.get(viz)) {

                    res.prev.put(viz, atual);
                    gScore.put(viz, tentativeG);

                    double h = heuristica(viz, destino, centroides);
                    fScore.put(viz, tentativeG + h);

                    pq.add(viz);
                }
            }
        }

        res.dist = gScore;
        return res;
    }

    private static double heuristica(
        String a,
        String b,
        Map<String, double[]> centroides
    ) {
        double[] ca = centroides.get(a);
        double[] cb = centroides.get(b);

        if (ca == null || cb == null) return 0;

        return GeoUtils.haversineKm(ca[0], ca[1], cb[0], cb[1]);
    }

    public static List<String> reconstruir(
        Map<String, String> prev,
        String origem,
        String destino
    ) {
        LinkedList<String> caminho = new LinkedList<>();

        String atual = destino;

        while (atual != null && !atual.equals(origem)) {
            caminho.addFirst(atual);
            atual = prev.get(atual);
        }

        if (atual != null) caminho.addFirst(origem);

        return caminho;
    }
}