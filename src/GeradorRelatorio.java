import java.util.*;
import java.io.*;
import java.nio.file.*;

/**
 * Gera relatórios estatísticos detalhados para análise acadêmica
 */
public class GeradorRelatorio {

    private Map<String, Double> densidades;
    private Map<String, double[]> centroides;
    private Map<Integer, List<String>> grupos;
    private Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasRotas;

    public GeradorRelatorio(
        Map<String, Double> densidades,
        Map<String, double[]> centroides,
        Map<Integer, List<String>> grupos,
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasRotas
    ) {
        this.densidades = densidades;
        this.centroides = centroides;
        this.grupos = grupos;
        this.estatisticasRotas = estatisticasRotas;
    }

    // =========================
    // COMPATIBILIDADE COM MAIN
    // =========================
    public void imprimirRelatorioCompleto() {
        imprimirRelatorioCompleto(System.out);
    }

    // =========================
    // VERSÃO PRINCIPAL
    // =========================
    public void imprimirRelatorioCompleto(PrintStream out) {

        out.println("\n" + "=".repeat(80));
        out.println("RELATÓRIO COMPLETO DE ANÁLISE DE MICROPLÁSTICOS");
        out.println("=".repeat(80));

        imprimirResumoGeral(out);
        imprimirEstatisticasPorRegiao(out);
        imprimirTopRegioes(out);
        imprimirRecomendacoes(out);

        out.println("\n" + "=".repeat(80));
    }

    // =========================
    // RESUMO
    // =========================
    private void imprimirResumoGeral(PrintStream out) {

        out.println("\n### RESUMO GERAL ###\n");

        int totalCelulas = densidades.size();
        int totalRegioes = grupos.size();

        double soma = 0;
        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;

        for (double d : densidades.values()) {
            soma += d;
            if (d > max) max = d;
            if (d < min) min = d;
        }

        double media = soma / totalCelulas;

        out.printf("Total de células analisadas: %d%n", totalCelulas);
        out.printf("Total de regiões identificadas: %d%n", totalRegioes);
        out.printf("Densidade média global: %.6f%n", media);
        out.printf("Densidade máxima: %.6f%n", max);
        out.printf("Densidade mínima: %.6f%n", min);
        out.printf("Desvio padrão: %.6f%n", calcularDesvioPadrao(densidades.values(), media));
    }

    // =========================
    // ESTATÍSTICAS POR REGIÃO (SEM ROTAS)
    // =========================
    private void imprimirEstatisticasPorRegiao(PrintStream out) {

        out.println("\n### ESTATÍSTICAS POR REGIÃO ###\n");

        out.println(String.format("%-10s | %-10s | %-15s | %-15s",
            "Região", "Células", "Dens. Total", "Dens. Média"));

        out.println("-".repeat(60));

        for (Map.Entry<Integer, List<String>> entry : grupos.entrySet()) {

            int regiaoId = entry.getKey();
            List<String> celulas = entry.getValue();

            OtimizadorRotas.EstatisticasRota stats = estatisticasRotas.get(regiaoId);
            if (stats == null) continue;

            out.printf("%-10d | %-10d | %-15.6f | %-15.6f%n",
                regiaoId,
                stats.numeroCelulas,
                stats.densidadeTotal,
                stats.densidadeMedia
            );
        }
    }

    // =========================
    // TOP REGIÕES (SEM ROTAS)
    // =========================
    private void imprimirTopRegioes(PrintStream out) {

        out.println("\n### TOP REGIÕES ###\n");

        List<Map.Entry<Integer, OtimizadorRotas.EstatisticasRota>> ranking =
            new ArrayList<>(estatisticasRotas.entrySet());

        ranking.sort((a, b) ->
            Double.compare(b.getValue().densidadeTotal, a.getValue().densidadeTotal)
        );

        for (int i = 0; i < Math.min(5, ranking.size()); i++) {

            var entry = ranking.get(i);
            var stats = entry.getValue();

            out.printf("%d. Região %d -> densidade total: %.6f%n",
                i + 1,
                entry.getKey(),
                stats.densidadeTotal
            );
        }
    }

    // =========================
    // RECOMENDAÇÕES SIMPLES
    // =========================
    private void imprimirRecomendacoes(PrintStream out) {

        out.println("\n### RECOMENDAÇÕES ###\n");

        List<Map.Entry<Integer, OtimizadorRotas.EstatisticasRota>> lista =
            new ArrayList<>(estatisticasRotas.entrySet());

        lista.sort((a, b) ->
            Double.compare(b.getValue().densidadeTotal, a.getValue().densidadeTotal)
        );

        if (!lista.isEmpty()) {
            out.printf("Prioridade máxima: Região %d%n", lista.get(0).getKey());
        }

        if (lista.size() > 1) {
            out.printf("Prioridade alta: Região %d%n", lista.get(1).getKey());
        }
    }

    // =========================
    // EXPORTS (mantidos)
    // =========================
    public void exportarRelatorio(String caminhoArquivo) throws IOException {

        PrintStream original = System.out;
        PrintStream file = new PrintStream(new FileOutputStream(caminhoArquivo));

        System.setOut(file);
        imprimirRelatorioCompleto();
        System.setOut(original);

        file.close();

        System.out.println("Relatório exportado para: " + caminhoArquivo);
    }

    public void exportarDadosGraficos(String pastaSaida) throws IOException {

        Path dir = Paths.get(pastaSaida);
        if (!Files.exists(dir)) Files.createDirectories(dir);

        try (BufferedWriter w = Files.newBufferedWriter(dir.resolve("densidade.csv"))) {
            w.write("regiao,densidade_total,densidade_media,num_celulas\n");

            for (var e : estatisticasRotas.entrySet()) {
                var s = e.getValue();

                w.write(String.format("%d,%.6f,%.6f,%d\n",
                    e.getKey(),
                    s.densidadeTotal,
                    s.densidadeMedia,
                    s.numeroCelulas
                ));
            }
        }

        System.out.println("CSV gerado em: " + pastaSaida);
    }

    // =========================
    // AUX
    // =========================
    private double calcularDesvioPadrao(Collection<Double> valores, double media) {

        double soma = 0;

        for (double v : valores) {
            soma += Math.pow(v - media, 2);
        }

        return Math.sqrt(soma / valores.size());
    }
}