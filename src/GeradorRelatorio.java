import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * Gera relatórios estatísticos detalhados para análise acadêmica.
 * Compara três algoritmos: Dijkstra Simples, Dijkstra Híbrido e A*.
 */
public class GeradorRelatorio {

    private Map<String, Double> densidades;
    private Map<String, double[]> centroides;
    private Map<Integer, List<String>> grupos;

    // ✅ AGORA OS TRÊS, COM NOMES CONSISTENTES (sem "estatisticasRotas" fantasma)
    private Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasSimples;
    private Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasHibrido;
    private Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasAEstrela;

    public GeradorRelatorio(
        Map<String, Double> densidades,
        Map<String, double[]> centroides,
        Map<Integer, List<String>> grupos,
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasSimples,
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasHibrido,
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasAEstrela
    ) {
        this.densidades = densidades;
        this.centroides = centroides;
        this.grupos = grupos;
        this.estatisticasSimples = estatisticasSimples;
        this.estatisticasHibrido = estatisticasHibrido;
        this.estatisticasAEstrela = estatisticasAEstrela;
    }

    public void imprimirRelatorioCompleto() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RELATÓRIO COMPLETO DE ANÁLISE DE MICROPLÁSTICOS");
        System.out.println("Comparação: Dijkstra Simples x Dijkstra Híbrido x A*");
        System.out.println("=".repeat(80));

        imprimirResumoGeral();
        imprimirEstatisticasPorRegiao();
        imprimirComparacaoAlgoritmos();
        imprimirTopRegioes();
        imprimirRecomendacoes();
    }

    private void imprimirResumoGeral() {
        System.out.println("\n### RESUMO GERAL ###\n");

        int totalCelulas = densidades.size();
        int totalRegioes = grupos.size();

        double densidadeTotal = 0;
        double densidadeMax = Double.MIN_VALUE;
        double densidadeMin = Double.MAX_VALUE;

        for (double d : densidades.values()) {
            densidadeTotal += d;
            if (d > densidadeMax) densidadeMax = d;
            if (d < densidadeMin) densidadeMin = d;
        }

        double densidadeMedia = densidadeTotal / totalCelulas;

        System.out.printf("Total de células analisadas: %d%n", totalCelulas);
        System.out.printf("Total de regiões identificadas: %d%n", totalRegioes);
        System.out.printf("Densidade média global: %.6f partículas/m³%n", densidadeMedia);
        System.out.printf("Densidade máxima: %.6f partículas/m³%n", densidadeMax);
        System.out.printf("Densidade mínima: %.6f partículas/m³%n", densidadeMin);
        System.out.printf("Desvio padrão: %.6f%n", calcularDesvioPadrao(densidades.values(), densidadeMedia));
    }

    /**
     * ✅ Tabela região-a-região com os TRÊS algoritmos lado a lado.
     * Isso é o que você precisa pro artigo: uma tabela só, comparável.
     */
    private void imprimirEstatisticasPorRegiao() {
        System.out.println("\n### ESTATÍSTICAS POR REGIÃO (Simples vs Híbrido vs A*) ###\n");

        System.out.printf("%-8s | %-8s | %-36s | %-30s | %-36s%n",
            "Região", "Células", "Dist(km)  Simples / Híbrido / A*",
            "Tempo(ms) S / H / A*", "Eff.Norm  Simples / Híbrido / A*");
        System.out.println("-".repeat(130));

        for (Integer regiaoId : grupos.keySet()) {
            List<String> celulas = grupos.get(regiaoId);
            if (celulas.size() < 5) continue;

            OtimizadorRotas.EstatisticasRota s = estatisticasSimples.get(regiaoId);
            OtimizadorRotas.EstatisticasRota h = estatisticasHibrido.get(regiaoId);
            OtimizadorRotas.EstatisticasRota a = estatisticasAEstrela.get(regiaoId);

            if (s == null || h == null || a == null) continue;

            System.out.printf(
                "%-8d | %-8d | %10.2f / %10.2f / %10.2f | %7.2f / %7.2f / %7.2f | %.5f / %.5f / %.5f%n",
                regiaoId, h.numeroCelulas,
                s.distanciaTotal, h.distanciaTotal, a.distanciaTotal,
                s.tempoMs, h.tempoMs, a.tempoMs,
                s.getEficienciaNormalizada(), h.getEficienciaNormalizada(), a.getEficienciaNormalizada()
            );
        }
    }

    /**
     * ✅ Comparação agregada dos três algoritmos (médias, vitórias por região).
     * Esse é o núcleo estatístico do seu artigo.
     */
    private void imprimirComparacaoAlgoritmos() {
        System.out.println("\n### COMPARAÇÃO AGREGADA DOS ALGORITMOS ###\n");

        double somaDistS = 0, somaDistH = 0, somaDistA = 0;
        double somaEffS = 0, somaEffH = 0, somaEffA = 0;
        int n = 0;

        int vitoriasDistS = 0, vitoriasDistH = 0, vitoriasDistA = 0;

        for (Integer regiaoId : grupos.keySet()) {
            OtimizadorRotas.EstatisticasRota s = estatisticasSimples.get(regiaoId);
            OtimizadorRotas.EstatisticasRota h = estatisticasHibrido.get(regiaoId);
            OtimizadorRotas.EstatisticasRota a = estatisticasAEstrela.get(regiaoId);

            if (s == null || h == null || a == null) continue;

            somaDistS += s.distanciaTotal;
            somaDistH += h.distanciaTotal;
            somaDistA += a.distanciaTotal;

            somaEffS += s.getEficiencia();
            somaEffH += h.getEficiencia();
            somaEffA += a.getEficiencia();

            // menor distância vence (rota mais curta = melhor para esse critério)
            double menor = Math.min(s.distanciaTotal, Math.min(h.distanciaTotal, a.distanciaTotal));
            if (menor == s.distanciaTotal) vitoriasDistS++;
            else if (menor == h.distanciaTotal) vitoriasDistH++;
            else vitoriasDistA++;

            n++;
        }

        if (n == 0) {
            System.out.println("Nenhuma região com dados suficientes (mínimo 5 células) para comparação.");
            return;
        }

        System.out.printf("Distância média (km):   Simples=%.2f | Híbrido=%.2f | A*=%.2f%n",
            somaDistS / n, somaDistH / n, somaDistA / n);
        System.out.printf("Eficiência média:        Simples=%.6f | Híbrido=%.6f | A*=%.6f%n",
            somaEffS / n, somaEffH / n, somaEffA / n);
        System.out.printf("Regiões com menor distância: Simples=%d | Híbrido=%d | A*=%d (de %d regiões)%n",
            vitoriasDistS, vitoriasDistH, vitoriasDistA, n);
    }

    private void imprimirTopRegioes() {
        System.out.println("\n### TOP 5 REGIÕES PRIORITÁRIAS PARA COLETA (base: Híbrido) ###\n");

        List<Map.Entry<Integer, OtimizadorRotas.EstatisticasRota>> ranking =
            new ArrayList<>(estatisticasHibrido.entrySet());

        ranking.sort((x, y) ->
            Double.compare(y.getValue().densidadeTotal, x.getValue().densidadeTotal));

        for (int i = 0; i < Math.min(5, ranking.size()); i++) {
            var entry = ranking.get(i);
            var stats = entry.getValue();

            System.out.printf("\n%d. REGIÃO %d%n", i + 1, entry.getKey());
            System.out.printf("   Células: %d%n", stats.numeroCelulas);
            System.out.printf("   Densidade total: %.6f partículas/m³%n", stats.densidadeTotal);
            System.out.printf("   Distância (híbrido): %.2f km%n", stats.distanciaTotal);
            System.out.printf("   Eficiência (híbrido): %.6f (dens/km)%n", stats.getEficiencia());
            System.out.printf("   Potencial de coleta: %.2f%%%n",
                (stats.densidadeTotal / calcularDensidadeGlobal()) * 100);
        }
    }

    private void imprimirRecomendacoes() {
        System.out.println("\n### RECOMENDAÇÕES OPERACIONAIS ###\n");

        List<Map.Entry<Integer, OtimizadorRotas.EstatisticasRota>> porDensidade =
            new ArrayList<>(estatisticasHibrido.entrySet());
        porDensidade.sort((x, y) ->
            Double.compare(y.getValue().densidadeTotal, x.getValue().densidadeTotal));

        System.out.println("Com base na análise dos dados, recomenda-se:");
        System.out.println();

        if (!porDensidade.isEmpty()) {
            var top1 = porDensidade.get(0);
            System.out.printf("1. Prioridade MÁXIMA: Região %d%n", top1.getKey());
            System.out.println("   - Concentra a maior densidade de microplásticos");
            System.out.printf("   - Potencial de remoção: %.6f partículas/m³%n",
                top1.getValue().densidadeTotal);
        }

        if (porDensidade.size() > 1) {
            var top2 = porDensidade.get(1);
            System.out.printf("\n2. Prioridade ALTA: Região %d%n", top2.getKey());
            System.out.println("   - Segunda maior concentração identificada");
        }

        List<Map.Entry<Integer, OtimizadorRotas.EstatisticasRota>> porEficiencia =
            new ArrayList<>(estatisticasHibrido.entrySet());
        porEficiencia.sort((x, y) ->
            Double.compare(y.getValue().getEficiencia(), x.getValue().getEficiencia()));

        if (!porEficiencia.isEmpty()) {
            var melhorEf = porEficiencia.get(0);
            System.out.printf("\n3. Operação mais ECONÔMICA: Região %d%n", melhorEf.getKey());
            System.out.printf("   - Melhor relação custo-benefício (%.6f dens/km)%n",
                melhorEf.getValue().getEficiencia());
            System.out.println("   - Ideal para navios com restrições de autonomia");
        }

        System.out.println("\n" + "=".repeat(80));
    }

    public void exportarRelatorio(String caminhoArquivo) throws IOException {
        PrintStream consoleOriginal = System.out;
        PrintStream arquivoSaida = new PrintStream(new FileOutputStream(caminhoArquivo));

        System.setOut(arquivoSaida);
        imprimirRelatorioCompleto();
        System.setOut(consoleOriginal);

        arquivoSaida.close();
        System.out.println("Relatório exportado para: " + caminhoArquivo);
    }

    /**
     * ✅ Exporta CSV com os três algoritmos — é esse arquivo que você usa
     * para gerar os gráficos de barras/comparação no artigo (Python/Excel/R).
     */
    public void exportarDadosGraficos(String pastaSaida) throws IOException {
        Path dir = Paths.get(pastaSaida);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        try (BufferedWriter writer = Files.newBufferedWriter(
                dir.resolve("comparacao_algoritmos.csv"))) {

            writer.write("regiao,num_celulas,algoritmo,distancia_km,densidade_total," +
                         "eficiencia,eff_normalizada,tempo_ms,nos_explorados\n");

            for (Integer regiaoId : grupos.keySet()) {
                OtimizadorRotas.EstatisticasRota s = estatisticasSimples.get(regiaoId);
                OtimizadorRotas.EstatisticasRota h = estatisticasHibrido.get(regiaoId);
                OtimizadorRotas.EstatisticasRota a = estatisticasAEstrela.get(regiaoId);

                if (s == null || h == null || a == null) continue;

                writer.write(String.format(Locale.US,
                    "%d,%d,Simples,%.4f,%.6f,%.6f,%.8f,%.4f,%d%n",
                    regiaoId, s.numeroCelulas, s.distanciaTotal, s.densidadeTotal,
                    s.getEficiencia(), s.getEficienciaNormalizada(), s.tempoMs, s.nosExplorados));
                writer.write(String.format(Locale.US,
                    "%d,%d,Hibrido,%.4f,%.6f,%.6f,%.8f,%.4f,%d%n",
                    regiaoId, h.numeroCelulas, h.distanciaTotal, h.densidadeTotal,
                    h.getEficiencia(), h.getEficienciaNormalizada(), h.tempoMs, h.nosExplorados));
                writer.write(String.format(Locale.US,
                    "%d,%d,AEstrela,%.4f,%.6f,%.6f,%.8f,%.4f,%d%n",
                    regiaoId, a.numeroCelulas, a.distanciaTotal, a.densidadeTotal,
                    a.getEficiencia(), a.getEficienciaNormalizada(), a.tempoMs, a.nosExplorados));
            }
        }

        System.out.println("Dados para gráficos exportados em: " + pastaSaida);
    }

    private double calcularDesvioPadrao(Collection<Double> valores, double media) {
        double soma = 0;
        for (double v : valores) {
            soma += Math.pow(v - media, 2);
        }
        return Math.sqrt(soma / valores.size());
    }

    private double calcularDensidadeGlobal() {
        double total = 0;
        for (double d : densidades.values()) {
            total += d;
        }
        return total;
    }
}