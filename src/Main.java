import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {
        
        String caminhoCSV = "data/survey_data.csv";
        
        // 1. Leitura
        LeitorDeDados leitor = new LeitorDeDados();
        List<AmostraPonto> amostras = leitor.lerAmostras(caminhoCSV);
        System.out.println("Total de amostras carregadas: " + amostras.size());
        
        // 2. Pré-processamento
        PreProcessador pre = new PreProcessador();
        Map<String, List<AmostraPonto>> celulas = pre.agruparPorCoordenadas(amostras);
        Map<String, Double> densidadesMedias = pre.calcularDensidadeMedia(celulas);
        Map<String, double[]> centroides = pre.calcularCentroides(celulas);
        System.out.println("Células: " + celulas.size());
        System.out.println("Densidades calculadas: " + densidadesMedias.size());
        
        // 3. Construção do grafo
        Grafo grafo = new Grafo(densidadesMedias);
        System.out.println("Grafo construído.");
        
        // 4. Detecção de comunidades
        DetectorComunidades detector = new DetectorComunidades(grafo);
        Map<String, Integer> mapRegiao = detector.detectarComunidadesPorRegiao();
        Map<Integer, List<String>> grupos = new TreeMap<>(detector.agruparPorComunidade());
        detector.imprimirEstatisticas(centroides, densidadesMedias);
        
        // 5. Otimização de rotas
        OtimizadorRotas opt = new OtimizadorRotas(grafo, densidadesMedias, centroides);
        
        // Pasta para rotas
        Path outDir = Paths.get("routes");
        if (!Files.exists(outDir)) Files.createDirectories(outDir);
        
        // Armazenar estatísticas para relatório
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasRotas = new HashMap<>();
        
        // 6. Gerar rotas por região
        System.out.println("\n=== GERANDO ROTAS OTIMIZADAS ===");
        
        // DEBUG: ver ordem das regiões
        System.out.println("Ordem das regiões: " + grupos.keySet());
        for (Map.Entry<Integer, List<String>> entry : grupos.entrySet()) {
            int idx = entry.getKey();
            List<String> nodes = entry.getValue();
            
            if (nodes.size() < 5) continue;
            
            System.out.printf("\n--- Região %d: nós=%d ---\n", idx, nodes.size());
            
            List<String> rota = opt.calcularRotaCobertura(nodes);
            OtimizadorRotas.EstatisticasRota stats = opt.calcularEstatisticas(rota);
            stats.imprimir("Regiao_" + idx);
            System.out.println("Rota: " + rota);
            System.out.println("Distância total (custo): " + stats.distanciaTotal);
            System.out.println("Microplástico coletado: " + stats.densidadeTotal);
            
            // Armazenar estatísticas
            estatisticasRotas.put(idx, stats);
            
            // Salvar rota em CSV
            Path out = outDir.resolve(String.format("rota_regiao_%02d.csv", idx));
            try (BufferedWriter bw = Files.newBufferedWriter(out)) {
                bw.write("ordem,celula,lat,lon,densidade\n");
                for (int i = 0; i < rota.size(); i++) {
                    String cel = rota.get(i);
                    double[] c = centroides.getOrDefault(cel, new double[]{0.0,0.0});
                    double d = densidadesMedias.getOrDefault(cel, 0.0);
                    bw.write(String.format(Locale.US, "%d,%s,%.6f,%.6f,%.6f\n", 
                        i+1, cel, c[0], c[1], d));
                }
            }
            System.out.println("Rota salva: " + out.toString());
        }
        
        // 7. GERAÇÃO DO RELATÓRIO COMPLETO
        System.out.println("\n=== GERANDO RELATÓRIO ANALÍTICO ===");
        
        GeradorRelatorio relatorio = new GeradorRelatorio(
            densidadesMedias,
            centroides,
            grupos,
            estatisticasRotas
        );
        
        // Imprimir relatório no terminal
        relatorio.imprimirRelatorioCompleto();
        
        // Exportar relatório para arquivo
        relatorio.exportarRelatorio("relatorio_analise_microplasticos.txt");
        
        // Exportar dados para gráficos
        relatorio.exportarDadosGraficos("graficos");
        
        // 8. Exportar para Gephi
        exportarParaGephi(densidadesMedias, centroides, mapRegiao, 
            grupos, estatisticasRotas, grafo);
        
        System.out.println("\n=== PROCESSAMENTO FINALIZADO ===");
        System.out.println("Arquivos gerados:");
        System.out.println("  - routes/rota_regiao_XX.csv (rotas por região)");
        System.out.println("  - relatorio_analise_microplasticos.txt");
        System.out.println("  - graficos/densidade_por_regiao.csv");
        System.out.println("  - graficos/eficiencia_por_regiao.csv");
        System.out.println("  - grafo_completo_nodes.csv e edges.csv");
        System.out.println("  - regiao_X_nodes.csv e edges.csv (um par por região)");
    }
    
    /**
     * Exporta grafos para Gephi
     */
    private static void exportarParaGephi(
        Map<String, Double> densidades,
        Map<String, double[]> centroides,
        Map<String, Integer> comunidades,
        Map<Integer, List<String>> grupos,
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticas,
        Grafo grafo
    ) throws IOException {
        
        // 1. Grafo completo
        exportarGrafoCompleto(densidades, centroides, comunidades, grafo);
        
        // 2. Sub-grafos por região
        exportarSubGrafosPorRegiao(grupos, densidades, centroides, grafo);
        
        System.out.println("\n✓ Arquivos Gephi exportados com sucesso!");
    }
    
    private static void exportarGrafoCompleto(
        Map<String, Double> densidades,
        Map<String, double[]> centroides,
        Map<String, Integer> comunidades,
        Grafo grafo
    ) throws IOException {
        
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get("grafo_completo_nodes.csv"))) {
            writer.write("Id,Label,Latitude,Longitude,Densidade,Comunidade\n");
            
            for (Map.Entry<String, Double> entry : densidades.entrySet()) {
                String id = entry.getKey();
                double densidade = entry.getValue();
                double[] coords = centroides.get(id);
                int comunidade = comunidades.getOrDefault(id, -1);
                
                writer.write(String.format(Locale.US, "%s,%s,%.4f,%.4f,%.6f,%d\n",
                    id, id, coords[0], coords[1], densidade, comunidade));
            }
        }
        
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get("grafo_completo_edges.csv"))) {
            writer.write("Source,Target,Weight,Type\n");
            
            Map<String, List<Aresta>> adjacencia = grafo.getAdjacencia();
            
            for (Map.Entry<String, List<Aresta>> entry : adjacencia.entrySet()) {
                String source = entry.getKey();
                for (Aresta aresta : entry.getValue()) {
                    String target = aresta.getDestino();
                    double peso = aresta.getPeso();
                    
                    writer.write(String.format(Locale.US, "%s,%s,%.2f,Directed\n",
                        source, target, peso));
                }
            }
        }
    }
    
    private static void exportarSubGrafosPorRegiao(
        Map<Integer, List<String>> grupos,
        Map<String, Double> densidades,
        Map<String, double[]> centroides,
        Grafo grafo
    ) throws IOException {
        
        for (Map.Entry<Integer, List<String>> entry : grupos.entrySet()) {
            int regiaoId = entry.getKey();
            List<String> celulas = entry.getValue();
            
            if (celulas.size() < 5) continue;
            
            Set<String> celulasDaRegiao = new HashSet<>(celulas);
            
            // Nodes
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get("regiao_" + regiaoId + "_nodes.csv"))) {
                writer.write("Id,Label,Latitude,Longitude,Densidade\n");
                
                for (String celula : celulas) {
                    double densidade = densidades.get(celula);
                    double[] coords = centroides.get(celula);
                    
                    writer.write(String.format(Locale.US, "%s,%s,%.4f,%.4f,%.6f\n",
                        celula, celula, coords[0], coords[1], densidade));
                }
            }
            
            // Edges
            try (BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get("regiao_" + regiaoId + "_edges.csv"))) {
                writer.write("Source,Target,Weight,Type\n");
                
                Map<String, List<Aresta>> adjacencia = grafo.getAdjacencia();
                
                for (String source : celulas) {
                    for (Aresta aresta : adjacencia.getOrDefault(source, new ArrayList<>())) {
                        String target = aresta.getDestino();
                        
                        if (!celulasDaRegiao.contains(target)) continue;
                        
                        double peso = aresta.getPeso();
                        
                        writer.write(String.format(Locale.US, "%s,%s,%.2f,Directed\n",
                            source, target, peso));
                    }
                }
            }
        }
    }
}