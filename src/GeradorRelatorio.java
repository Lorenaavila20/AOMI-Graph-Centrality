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
    
    /**
     * Imprime relatório completo no terminal
     */
    public void imprimirRelatorioCompleto() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("RELATÓRIO COMPLETO DE ANÁLISE DE MICROPLÁSTICOS");
        System.out.println("=".repeat(80));
        
        imprimirResumoGeral();
        imprimirEstatisticasPorRegiao();
        imprimirComparacaoRegioes();
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
    
    private void imprimirEstatisticasPorRegiao() {
        System.out.println("\n### ESTATÍSTICAS POR REGIÃO ###\n");
        
        System.out.println(String.format("%-8s | %-10s | %-15s | %-15s | %-15s | %-15s",
            "Região", "Células", "Dist. Total(km)", "Dens. Total", "Dens. Média", "Eficiência"));
        System.out.println("-".repeat(100));
        
        for (Map.Entry<Integer, List<String>> entry : grupos.entrySet()) {
            int regiaoId = entry.getKey();
            List<String> celulas = entry.getValue();
            
            if (celulas.size() < 5) continue;
            
            OtimizadorRotas.EstatisticasRota stats = estatisticasRotas.get(regiaoId);
            if (stats == null) continue;
            
            double eficiencia = stats.distanciaTotal > 0 ? 
                stats.densidadeTotal / stats.distanciaTotal : 0.0;
            
            System.out.printf("%-8d | %-10d | %15.2f | %15.6f | %15.6f | %15.6f%n",
                regiaoId,
                stats.numeroCelulas,
                stats.distanciaTotal,
                stats.densidadeTotal,
                stats.densidadeMedia,
                eficiencia
            );
        }
    }
    
    private void imprimirComparacaoRegioes() {
        System.out.println("\n### COMPARAÇÃO ENTRE REGIÕES ###\n");
        
        List<Map.Entry<Integer, OtimizadorRotas.EstatisticasRota>> listaStats = 
            new ArrayList<>(estatisticasRotas.entrySet());
        
        // Região com maior densidade total
        listaStats.sort((a, b) -> 
            Double.compare(b.getValue().densidadeTotal, a.getValue().densidadeTotal));
        
        if (!listaStats.isEmpty()) {
            var melhorDens = listaStats.get(0);
            System.out.printf("Região com MAIOR densidade coletável: Região %d (%.6f)%n",
                melhorDens.getKey(), melhorDens.getValue().densidadeTotal);
        }
        
        // Região com melhor eficiência
        listaStats.sort((a, b) -> {
            double efA = a.getValue().distanciaTotal > 0 ? 
                a.getValue().densidadeTotal / a.getValue().distanciaTotal : 0;
            double efB = b.getValue().distanciaTotal > 0 ? 
                b.getValue().densidadeTotal / b.getValue().distanciaTotal : 0;
            return Double.compare(efB, efA);
        });
        
        if (!listaStats.isEmpty()) {
            var melhorEf = listaStats.get(0);
            double ef = melhorEf.getValue().distanciaTotal > 0 ?
                melhorEf.getValue().densidadeTotal / melhorEf.getValue().distanciaTotal : 0;
            System.out.printf("Região com MELHOR eficiência: Região %d (%.6f dens/km)%n",
                melhorEf.getKey(), ef);
        }
        
        // Região mais extensa
        listaStats.sort((a, b) -> 
            Double.compare(b.getValue().distanciaTotal, a.getValue().distanciaTotal));
        
        if (!listaStats.isEmpty()) {
            var maisExtensa = listaStats.get(0);
            System.out.printf("Região mais EXTENSA: Região %d (%.2f km)%n",
                maisExtensa.getKey(), maisExtensa.getValue().distanciaTotal);
        }
    }
    
    private void imprimirTopRegioes() {
        System.out.println("\n### TOP 5 REGIÕES PRIORITÁRIAS PARA COLETA ###\n");
        
        List<Map.Entry<Integer, OtimizadorRotas.EstatisticasRota>> ranking = 
            new ArrayList<>(estatisticasRotas.entrySet());
        
        // Critério: densidade total (pode ser ajustado)
        ranking.sort((a, b) -> 
            Double.compare(b.getValue().densidadeTotal, a.getValue().densidadeTotal));
        
        for (int i = 0; i < Math.min(5, ranking.size()); i++) {
            var entry = ranking.get(i);
            var stats = entry.getValue();
            double ef = stats.distanciaTotal > 0 ? 
                stats.densidadeTotal / stats.distanciaTotal : 0;
            
            System.out.printf("\n%d. REGIÃO %d%n", i+1, entry.getKey());
            System.out.printf("   Células: %d%n", stats.numeroCelulas);
            System.out.printf("   Densidade total: %.6f partículas/m³%n", stats.densidadeTotal);
            System.out.printf("   Distância: %.2f km%n", stats.distanciaTotal);
            System.out.printf("   Eficiência: %.6f (dens/km)%n", ef);
            System.out.printf("   Potencial de coleta: %.2f%%%n", 
                (stats.densidadeTotal / calcularDensidadeGlobal()) * 100);
        }
    }
    
    private void imprimirRecomendacoes() {
        System.out.println("\n### RECOMENDAÇÕES OPERACIONAIS ###\n");
        
        List<Map.Entry<Integer, OtimizadorRotas.EstatisticasRota>> listaStats = 
            new ArrayList<>(estatisticasRotas.entrySet());
        
        listaStats.sort((a, b) -> 
            Double.compare(b.getValue().densidadeTotal, a.getValue().densidadeTotal));
        
        System.out.println("Com base na análise dos dados, recomenda-se:");
        System.out.println();
        
        if (!listaStats.isEmpty()) {
            var top1 = listaStats.get(0);
            System.out.printf("1. Prioridade MÁXIMA: Região %d%n", top1.getKey());
            System.out.println("   - Concentra a maior densidade de microplásticos");
            System.out.printf("   - Potencial de remoção: %.6f partículas/m³%n", 
                top1.getValue().densidadeTotal);
        }
        
        if (listaStats.size() > 1) {
            var top2 = listaStats.get(1);
            System.out.printf("\n2. Prioridade ALTA: Região %d%n", top2.getKey());
            System.out.println("   - Segunda maior concentração identificada");
        }
        
        // Região mais eficiente
        listaStats.sort((a, b) -> {
            double efA = a.getValue().distanciaTotal > 0 ? 
                a.getValue().densidadeTotal / a.getValue().distanciaTotal : 0;
            double efB = b.getValue().distanciaTotal > 0 ? 
                b.getValue().densidadeTotal / b.getValue().distanciaTotal : 0;
            return Double.compare(efB, efA);
        });
        
        if (!listaStats.isEmpty()) {
            var melhorEf = listaStats.get(0);
            double ef = melhorEf.getValue().distanciaTotal > 0 ?
                melhorEf.getValue().densidadeTotal / melhorEf.getValue().distanciaTotal : 0;
            System.out.printf("\n3. Operação mais ECONÔMICA: Região %d%n", melhorEf.getKey());
            System.out.printf("   - Melhor relação custo-benefício (%.6f dens/km)%n", ef);
            System.out.println("   - Ideal para navios com restrições de autonomia");
        }
        
        System.out.println("\n" + "=".repeat(80));
    }
    
    /**
     * Exporta relatório para arquivo de texto
     */
    public void exportarRelatorio(String caminhoArquivo) throws IOException {
        // Redirecionar System.out para arquivo temporariamente
        PrintStream consolaOriginal = System.out;
        PrintStream arquivoSaida = new PrintStream(new FileOutputStream(caminhoArquivo));
        
        System.setOut(arquivoSaida);
        imprimirRelatorioCompleto();
        System.setOut(consolaOriginal);
        
        arquivoSaida.close();
        System.out.println("Relatório exportado para: " + caminhoArquivo);
    }
    
    /**
     * Exporta dados para gráficos (CSV)
     */
    public void exportarDadosGraficos(String pastaSaida) throws IOException {
        Path dir = Paths.get(pastaSaida);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        
        // 1. Densidade por região
        try (BufferedWriter writer = Files.newBufferedWriter(
                dir.resolve("densidade_por_regiao.csv"))) {
            writer.write("regiao,densidade_total,densidade_media,num_celulas\n");
            
            for (Map.Entry<Integer, OtimizadorRotas.EstatisticasRota> entry : 
                    estatisticasRotas.entrySet()) {
                var stats = entry.getValue();
                writer.write(String.format("%d,%.6f,%.6f,%d\n",
                    entry.getKey(),
                    stats.densidadeTotal,
                    stats.densidadeMedia,
                    stats.numeroCelulas
                ));
            }
        }
        
        // 2. Eficiência por região
        try (BufferedWriter writer = Files.newBufferedWriter(
                dir.resolve("eficiencia_por_regiao.csv"))) {
            writer.write("regiao,distancia_km,densidade_total,eficiencia\n");
            
            for (Map.Entry<Integer, OtimizadorRotas.EstatisticasRota> entry : 
                    estatisticasRotas.entrySet()) {
                var stats = entry.getValue();
                double ef = stats.distanciaTotal > 0 ? 
                    stats.densidadeTotal / stats.distanciaTotal : 0;
                writer.write(String.format("%d,%.2f,%.6f,%.6f\n",
                    entry.getKey(),
                    stats.distanciaTotal,
                    stats.densidadeTotal,
                    ef
                ));
            }
        }
        
        System.out.println("Dados para gráficos exportados em: " + pastaSaida);
    }
    
    // Métodos auxiliares
    
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