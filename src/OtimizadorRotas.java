import java.util.*;

/**
 * Otimizador para gerar uma ROTA DE COBERTURA dentro de uma região/subgrafo.
 * Estratégia: Start em nó-semente -> enquanto existirem nós não visitados, escolhe o nó não visitado
 * que tem menor custo (Dijkstra) a partir do nó atual; concatena o caminho retornado.
 *
 * Observações:
 * - usamos k pequeno (ex: 0.5) para que densidade influencie pouco na busca do próximo nó
 *   (priorizamos visitar nós, não maximizar densidade estritamente).
 * - nós intermediários do caminho são marcados como visitados (isso aumenta cobertura).
 */
public class OtimizadorRotas {

    private final Grafo grafo;
    private final Map<String, Double> densidades;
    private final Map<String, double[]> centroides;

    public OtimizadorRotas(Grafo grafo, Map<String, Double> densidades, Map<String, double[]> centroides) {
        this.grafo = grafo;
        this.densidades = densidades;
        this.centroides = centroides;
    }

    /**
     * Gera rota de cobertura para o conjunto de nós 'subgrafoNodes'.
     * Retorna lista ordenada de nós representando a rota concatenada.
     */
    public List<String> calcularRotaCobertura(Collection<String> subgrafoNodes) {
        Set<String> nodesSet = new HashSet<>(subgrafoNodes);
        List<String> rotaFinal = new ArrayList<>();
        if (nodesSet.isEmpty()) return rotaFinal;

        // 1) escolher nó inicial: nó de maior grau dentro do subgrafo (ou densidade máxima se preferir)
        String seed = escolherNoSemente(nodesSet);
        String atual = seed;
        // marcar visitados
        Set<String> visitados = new HashSet<>();
        visitados.add(atual);
        rotaFinal.add(atual);

        // Precompute neighbors for speed if needed
        Map<String, List<Aresta>> adj = grafo.getAdjacencia();

        // Parâmetro k pequeno (priorizar cobertura)
        final double K = 0.5;

        // Enquanto houver nós não visitados dentro do subgrafo
        while (visitados.size() < nodesSet.size()) {

            // construir lista de candidatos não visitados
            List<String> candidatos = new ArrayList<>();
            for (String n : nodesSet) if (!visitados.contains(n)) candidatos.add(n);
            if (candidatos.isEmpty()) break;

            String melhorDestino = null;
            double melhorCusto = Double.POSITIVE_INFINITY;
            Map<String, String> melhorPrev = null;

            // Para cada candidato, roda Dijkstra até o candidato para obter custo do caminho
            // (poderia rodar uma só vez dijkstraFull(origem,null) e ler dist para todos; faremos isso para eficiência)
            DijkstraHibrido.ResultadoDijkstra resultado = DijkstraHibrido.dijkstraFull(grafo, atual, null, K);
            Map<String, Double> distFromAtual = resultado.dist;
            Map<String, String> prevFromAtual = resultado.prev;

            // escolher o candidato com menor distância do atual (usando distFromAtual)
            for (String cand : candidatos) {
                double d = distFromAtual.getOrDefault(cand, Double.POSITIVE_INFINITY);
                if (d < melhorCusto) {
                    melhorCusto = d;
                    melhorDestino = cand;
                    melhorPrev = prevFromAtual; // prevFromAtual já é o mapa que chega ao cand
                }
            }

            if (melhorDestino == null || melhorCusto == Double.POSITIVE_INFINITY) {
                // candidato inacessível (desconexo) -> parar
                break;
            }

            // reconstruir o caminho atual -> melhorDestino usando melhorPrev
            List<String> caminhoSegmento = reconstruirCaminho(melhorPrev, atual, melhorDestino);

            if (caminhoSegmento.isEmpty()) {
                // não encontrou caminho válido
                break;
            }

            // concatenar segmento: marcar todos nodes do segmento como visitados e adicioná-los à rota
            // (se segmento inicia com 'atual', remove duplicação)
            if (caminhoSegmento.get(0).equals(atual)) {
                // comece pelo segundo elemento para evitar duplicação, afinal 'atual' já está na rotaFinal
                for (int i = 1; i < caminhoSegmento.size(); i++) {
                    String v = caminhoSegmento.get(i);
                    if (!visitados.contains(v)) {
                        visitados.add(v);
                    }
                    rotaFinal.add(v);
                }
            } else {
                // caso estranho: segmento não inicia no atual -> adiciona tudo
                for (String v : caminhoSegmento) {
                    if (!visitados.contains(v)) visitados.add(v);
                    rotaFinal.add(v);
                }
            }

            // avançar o 'atual' para a última posição do segmento
            atual = caminhoSegmento.get(caminhoSegmento.size() - 1);
        }

        return rotaFinal;
    }

    private String escolherNoSemente(Set<String> nodes) {
        // heurística: nó com maior grau (número de arestas) dentro do subgrafo
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
        // fallback
        if (melhor == null) {
            Iterator<String> it = nodes.iterator();
            melhor = it.hasNext() ? it.next() : null;
        }
        return melhor;
    }

    private List<String> reconstruirCaminho(Map<String, String> prev, String origem, String destino) {
        LinkedList<String> caminho = new LinkedList<>();
        String atual = destino;
        while (atual != null && !atual.equals(origem)) {
            caminho.addFirst(atual);
            atual = prev.get(atual);
        }
        if (atual != null && atual.equals(origem)) {
            caminho.addFirst(origem);
        }
        return caminho;
    }

    /**
     * Estatísticas simples sobre a rota
     */
    private double distanciaEuclidianaKm(String aKey, String bKey) {
        double[] a = centroides.get(aKey);
        double[] b = centroides.get(bKey);
        if (a == null || b == null) return 0.0;
        double dx = a[0] - b[0];
        double dy = a[1] - b[1];
        double graus = Math.sqrt(dx*dx + dy*dy);
        return graus * 110.0; // aproximação
    }

    public EstatisticasRota calcularEstatisticas(List<String> rota) {
        if (rota == null || rota.isEmpty()) return new EstatisticasRota(0,0,0,0);
        double distanciaTotal = 0;
        double densidadeTotal = 0;
        for (int i = 0; i < rota.size() - 1; i++) {
            distanciaTotal += distanciaEuclidianaKm(rota.get(i), rota.get(i+1));
        }
        for (String n : rota) densidadeTotal += densidades.getOrDefault(n, 0.0);
        double densMedia = rota.size() > 0 ? densidadeTotal / rota.size() : 0.0;
        return new EstatisticasRota(rota.size(), distanciaTotal, densidadeTotal, densMedia);
    }

    public static class EstatisticasRota {
        public int numeroCelulas;
        public double distanciaTotal;
        public double densidadeTotal;
        public double densidadeMedia;
        public EstatisticasRota(int numeroCelulas, double distanciaTotal, double densidadeTotal, double densidadeMedia) {
            this.numeroCelulas = numeroCelulas;
            this.distanciaTotal = distanciaTotal;
            this.densidadeTotal = densidadeTotal;
            this.densidadeMedia = densidadeMedia;
        }
        public void imprimir(String nomeRegiao) {
            System.out.printf("\n=== ESTATÍSTICAS DA ROTA - %s ===%n", nomeRegiao);
            System.out.printf("Células na rota: %d%n", numeroCelulas);
            System.out.printf("Distância total: %.2f km%n", distanciaTotal);
            System.out.printf("Densidade total coletada: %.6f%n", densidadeTotal);
            System.out.printf("Densidade média: %.6f%n", densidadeMedia);
            System.out.printf("Eficiência (densidade/km): %.6f%n", distanciaTotal > 0 ? densidadeTotal / distanciaTotal : 0.0);
        }
    }
}
