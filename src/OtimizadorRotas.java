import java.util.*;

public class OtimizadorRotas {

    private final Grafo grafo;
    private final Map<String, Double> densidades;
    private final Map<String, double[]> centroides;

    public OtimizadorRotas(Grafo grafo, Map<String, Double> densidades, Map<String, double[]> centroides) {
        this.grafo = grafo;
        this.densidades = densidades;
        this.centroides = centroides;
    }

    public List<String> calcularRotaCobertura(Collection<String> subgrafoNodes) {
        Set<String> nodesSet = new HashSet<>(subgrafoNodes);
        List<String> rotaFinal = new ArrayList<>();

        if (nodesSet.isEmpty()) return rotaFinal;

        String atual = escolherNoSemente(nodesSet);

        Set<String> visitados = new HashSet<>();
        visitados.add(atual);
        rotaFinal.add(atual);

        
        double alpha = 1.0;  // peso da distância
        double beta = 0.5;   // peso da densidade

        while (visitados.size() < nodesSet.size()) {

            List<String> candidatos = new ArrayList<>();
            for (String n : nodesSet) {
                if (!visitados.contains(n)) candidatos.add(n);
            }

            if (candidatos.isEmpty()) break;

            DijkstraHibrido.ResultadoDijkstra resultado =
                    DijkstraHibrido.dijkstraFull(grafo, atual, null, alpha, beta);

            Map<String, Double> dist = resultado.dist;
            Map<String, String> prev = resultado.prev;

            String melhorDestino = null;
            double melhorCusto = Double.POSITIVE_INFINITY;

            for (String cand : candidatos) {
                double d = dist.getOrDefault(cand, Double.POSITIVE_INFINITY);
                if (d < melhorCusto) {
                    melhorCusto = d;
                    melhorDestino = cand;
                }
            }

            if (melhorDestino == null) break;

            List<String> caminho = reconstruirCaminho(prev, atual, melhorDestino);
            if (caminho.isEmpty()) break;

            for (int i = 1; i < caminho.size(); i++) {
                String v = caminho.get(i);
                visitados.add(v);
                rotaFinal.add(v);
            }

            atual = caminho.get(caminho.size() - 1);
        }

        return rotaFinal;
    }

    private String escolherNoSemente(Set<String> nodes) {
        String melhor = null;
        int melhorGrau = -1;

        Map<String, List<Aresta>> adj = grafo.getAdjacencia();

        for (String n : nodes) {
            int grau = 0;
            for (Aresta a : adj.getOrDefault(n, Collections.emptyList())) {
                if (nodes.contains(a.getDestino())) grau++;
            }

            if (grau > melhorGrau) {
                melhorGrau = grau;
                melhor = n;
            }
        }

        return melhor != null ? melhor : nodes.iterator().next();
    }

    private List<String> reconstruirCaminho(Map<String, String> prev, String origem, String destino) {
        LinkedList<String> caminho = new LinkedList<>();
        String atual = destino;

        while (atual != null && !atual.equals(origem)) {
            caminho.addFirst(atual);
            atual = prev.get(atual);
        }

        if (atual != null) caminho.addFirst(origem);

        return caminho;
    }

    private double distanciaKm(String aKey, String bKey) {
        double[] a = centroides.get(aKey);
        double[] b = centroides.get(bKey);
    
        if (a == null || b == null) return 0.0;
    
        return GeoUtils.haversineKm(a[0], a[1], b[0], b[1]);
    }

    public EstatisticasRota calcularEstatisticas(List<String> rota) {
        if (rota == null || rota.isEmpty())
            return new EstatisticasRota(0, 0, 0, 0);

        double distanciaTotal = 0;
        double densidadeTotal = 0;

        for (int i = 0; i < rota.size() - 1; i++) {
            distanciaTotal += distanciaKm(rota.get(i), rota.get(i + 1));
        }

        for (String n : rota) {
            densidadeTotal += densidades.getOrDefault(n, 0.0);
        }

        double media = densidadeTotal / rota.size();

        return new EstatisticasRota(rota.size(), distanciaTotal, densidadeTotal, media);
    }

    public static class EstatisticasRota {
        public int numeroCelulas;
        public double distanciaTotal;
        public double densidadeTotal;
        public double densidadeMedia;

        public EstatisticasRota(int n, double dTotal, double densTotal, double densMedia) {
            this.numeroCelulas = n;
            this.distanciaTotal = dTotal;
            this.densidadeTotal = densTotal;
            this.densidadeMedia = densMedia;
        }

        public void imprimir(String nome) {
            System.out.printf("\n=== %s ===\n", nome);
            System.out.printf("Células: %d\n", numeroCelulas);
            System.out.printf("Distância: %.2f km\n", distanciaTotal);
            System.out.printf("Densidade total: %.6f\n", densidadeTotal);
            System.out.printf("Eficiência: %.6f\n",
                    distanciaTotal > 0 ? densidadeTotal / distanciaTotal : 0);
        }
    }

    public ResultadoRota executarAlgoritmo(
        String nomeAlgoritmo,
        Collection<String> nodes
    ) {
        long inicio = System.nanoTime();
    
        List<String> rota = calcularRotaCobertura(nodes);
        EstatisticasRota stats = calcularEstatisticas(rota);
    
        long fim = System.nanoTime();
    
        return new ResultadoRota(
            nomeAlgoritmo,
            -1, // região você coloca fora
            stats.distanciaTotal,
            stats.densidadeTotal,
            (fim - inicio) / 1_000_000.0
        );
    }
}