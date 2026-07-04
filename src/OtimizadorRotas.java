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

    // ─────────────────────────────────────────────────────────────────────────
    // DIJKSTRA HÍBRIDO — cobertura completa da região
    // custo = (alpha * distancia) / (1 + beta * densidade)
    // Expande para todos os destinos de uma vez (single-source),
    // depois escolhe o melhor candidato não visitado.
    // ─────────────────────────────────────────────────────────────────────────
    public List<String> calcularRotaCobertura(Collection<String> subgrafoNodes) {
        Set<String> nodesSet = new HashSet<>(subgrafoNodes);
        List<String> rotaFinal = new ArrayList<>();

        if (nodesSet.isEmpty()) return rotaFinal;

        String atual = escolherNoSemente(nodesSet);

        Set<String> visitados = new HashSet<>();
        visitados.add(atual);
        rotaFinal.add(atual);

        double alpha = 1.0;
        double beta  = 0.5;

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
            double melhorCusto   = Double.POSITIVE_INFINITY;

            for (String cand : candidatos) {
                double d = dist.getOrDefault(cand, Double.POSITIVE_INFINITY);
                if (d < melhorCusto) {
                    melhorCusto   = d;
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

    // ─────────────────────────────────────────────────────────────────────────
    // DIJKSTRA SIMPLES — cobertura sem peso de densidade (beta = 0)
    // Minimiza apenas distância geográfica.
    // ─────────────────────────────────────────────────────────────────────────
    public List<String> calcularRotaSimples(List<String> nodes) {
        if (nodes.isEmpty()) return new ArrayList<>();

        List<String> rota      = new ArrayList<>();
        Set<String> visitados  = new HashSet<>();

        String atual = escolherNoSemente(new HashSet<>(nodes));
        rota.add(atual);
        visitados.add(atual);

        while (visitados.size() < nodes.size()) {

            String melhorProximo = null;
            double melhorDist    = Double.POSITIVE_INFINITY;

            // beta = 0 → sem influência de densidade → Dijkstra puro por distância
            Map<String, String> prev = DijkstraHibrido.dijkstra(grafo, atual, null, 1.0, 0.0);

            for (String candidato : nodes) {
                if (visitados.contains(candidato)) continue;

                List<String> caminho = DijkstraHibrido.reconstruirCaminho(prev, atual, candidato);
                if (caminho.isEmpty() || caminho.size() < 2) continue;

                double dist = calcularDistanciaCaminho(caminho);
                if (dist == 0) continue;

                if (dist < melhorDist) {
                    melhorDist    = dist;
                    melhorProximo = candidato;
                }
            }

            if (melhorProximo == null) break;

            atual = melhorProximo;
            rota.add(atual);
            visitados.add(atual);
        }

        return rota;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // A* — cobertura ponto-a-ponto com heurística haversine
    // Diferença fundamental: recebe um destino fixo a cada chamada e usa
    // a heurística para guiar a busca até ele, podando ramos desnecessários.
    // ─────────────────────────────────────────────────────────────────────────
    public ResultadoCobertura calcularRotaCobertura_AEstrela(Collection<String> subgrafoNodes,
                                                              Map<String, double[]> centroidesGlobais) {
        Set<String> nodesSet       = new HashSet<>(subgrafoNodes);
        List<String> rotaFinal     = new ArrayList<>();
        int totalNosExplorados     = 0;

        if (nodesSet.isEmpty()) return new ResultadoCobertura(new ArrayList<>(), 0);

        String atual = escolherNoSemente(nodesSet);

        Set<String> visitados = new HashSet<>();
        visitados.add(atual);
        rotaFinal.add(atual);

        double alpha = 1.0;
        double beta  = 0.5;

        while (visitados.size() < nodesSet.size()) {

            List<String> candidatos = new ArrayList<>();
            for (String n : nodesSet) {
                if (!visitados.contains(n)) candidatos.add(n);
            }
            if (candidatos.isEmpty()) break;

            // A* exige destino fixo: escolhemos o candidato geograficamente
            // mais próximo do nó atual como alvo desta chamada.
            String alvo = escolherCandidatoMaisProximo(atual, candidatos, centroidesGlobais);
            if (alvo == null) break;

            AStar.Resultado resultado = AStar.executar(
                    grafo, atual, alvo, centroidesGlobais, alpha, beta);

            totalNosExplorados += resultado.nosExplorados;

            List<String> caminho = AStar.reconstruir(resultado.prev, atual, alvo);

            if (caminho.isEmpty() || caminho.size() < 2) {
                // sem caminho válido: avança diretamente para o alvo
                visitados.add(alvo);
                rotaFinal.add(alvo);
                atual = alvo;
                continue;
            }

            for (int i = 1; i < caminho.size(); i++) {
                String v = caminho.get(i);
                visitados.add(v);
                rotaFinal.add(v);
            }

            atual = caminho.get(caminho.size() - 1);
        }

        return new ResultadoCobertura(rotaFinal, totalNosExplorados);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UTILITÁRIOS
    // ─────────────────────────────────────────────────────────────────────────

    private String escolherCandidatoMaisProximo(String atual, List<String> candidatos,
                                                 Map<String, double[]> centroidesGlobais) {
        double[] a = centroidesGlobais.get(atual);
        if (a == null) return candidatos.isEmpty() ? null : candidatos.get(0);

        String melhor    = null;
        double menorDist = Double.POSITIVE_INFINITY;

        for (String c : candidatos) {
            double[] b = centroidesGlobais.get(c);
            if (b == null) continue;
            double d = GeoUtils.haversineKm(a[0], a[1], b[0], b[1]);
            if (d < menorDist) {
                menorDist = d;
                melhor    = c;
            }
        }

        return melhor != null ? melhor : candidatos.get(0);
    }

    private String escolherNoSemente(Set<String> nodes) {
        String melhor   = null;
        int melhorGrau  = -1;

        Map<String, List<Aresta>> adj = grafo.getAdjacencia();

        for (String n : nodes) {
            int grau = 0;
            for (Aresta a : adj.getOrDefault(n, Collections.emptyList())) {
                if (nodes.contains(a.getDestino())) grau++;
            }
            if (grau > melhorGrau) {
                melhorGrau = grau;
                melhor     = n;
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

    private double calcularDistanciaCaminho(List<String> caminho) {
        double total = 0.0;
        for (int i = 0; i < caminho.size() - 1; i++) {
            String u = caminho.get(i);
            String v = caminho.get(i + 1);
            for (Aresta a : grafo.getAdjacencia().getOrDefault(u, new ArrayList<>())) {
                if (a.getDestino().equals(v)) {
                    total += a.getDistancia();
                    break;
                }
            }
        }
        return total;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ESTATÍSTICAS
    // ─────────────────────────────────────────────────────────────────────────

    public EstatisticasRota calcularEstatisticas(List<String> rota) {
        if (rota == null || rota.isEmpty())
            return new EstatisticasRota(0, 0, 0, 0);

        double distanciaTotal = 0;
        double densidadeTotal = 0;

        for (int i = 0; i < rota.size() - 1; i++) {
            distanciaTotal += distanciaKm(rota.get(i), rota.get(i + 1));
        }

        Set<String> jaContados = new HashSet<>(rota);
        for (String n : jaContados) {
            densidadeTotal += densidades.getOrDefault(n, 0.0);
}

        double media = densidadeTotal / rota.size();
        return new EstatisticasRota(rota.size(), distanciaTotal, densidadeTotal, media);
    }

    public static class EstatisticasRota {
        public int    numeroCelulas;
        public double distanciaTotal;
        public double densidadeTotal;
        public double densidadeMedia;
        public double tempoMs       = 0.0; // ✅ tempo médio de execução (média de 10 runs)
        public int    nosExplorados = 0;   // ✅ total de nós expandidos (A* e Híbrido)

        public EstatisticasRota(int n, double dTotal, double densTotal, double densMedia) {
            this.numeroCelulas  = n;
            this.distanciaTotal = dTotal;
            this.densidadeTotal = densTotal;
            this.densidadeMedia = densMedia;
        }

        public double getEficiencia() {
            if (distanciaTotal == 0) return 0;
            return densidadeTotal / distanciaTotal;
        }

        // ✅ eficiência normalizada pelo número de células visitadas
        // torna os três algoritmos comparáveis mesmo visitando quantidades diferentes
        public double getEficienciaNormalizada() {
            if (distanciaTotal == 0 || numeroCelulas == 0) return 0;
            return (densidadeTotal / numeroCelulas) / distanciaTotal;
        }

        public void imprimir(String nome) {
            System.out.printf("\n=== %s ===%n", nome);
            System.out.printf("Células:          %d%n",       numeroCelulas);
            System.out.printf("Distância:        %.2f km%n",  distanciaTotal);
            System.out.printf("Densidade total:  %.6f%n",     densidadeTotal);
            System.out.printf("Eficiência:       %.6f%n",     getEficiencia());
            System.out.printf("Eff. normalizada: %.6f%n",     getEficienciaNormalizada());
            System.out.printf("Tempo médio:      %.2f ms%n",  tempoMs);
            System.out.printf("Nós explorados:   %d%n",       nosExplorados);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CLASSES DE RESULTADO
    // ─────────────────────────────────────────────────────────────────────────

    public static class ResultadoCobertura {
        public List<String> rota;
        public int totalNosExplorados;

        public ResultadoCobertura(List<String> rota, int totalNosExplorados) {
            this.rota               = rota;
            this.totalNosExplorados = totalNosExplorados;
        }
    }

    // mantido por compatibilidade com código existente
    public ResultadoRota executarAlgoritmo(String nomeAlgoritmo, Collection<String> nodes) {
        long inicio = System.nanoTime();
        List<String> rota       = calcularRotaCobertura(nodes);
        EstatisticasRota stats  = calcularEstatisticas(rota);
        long fim = System.nanoTime();

        return new ResultadoRota(nomeAlgoritmo, -1,
                stats.distanciaTotal, stats.densidadeTotal,
                (fim - inicio) / 1_000_000.0);
    }
}