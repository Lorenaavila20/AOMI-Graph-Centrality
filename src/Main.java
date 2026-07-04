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
        Grafo grafo = new Grafo(densidadesMedias, centroides);
        System.out.println("Grafo construído.");

        // 4. Detecção de comunidades
        DetectorComunidades detector = new DetectorComunidades(grafo);
        Map<String, Integer> mapRegiao = detector.detectarComunidadesPorRegiao();
        Map<Integer, List<String>> grupos = new TreeMap<>(detector.agruparPorComunidade());

        detector.imprimirEstatisticas(centroides, densidadesMedias);

        // 5. Otimização de rotas
        OtimizadorRotas opt = new OtimizadorRotas(grafo, densidadesMedias, centroides);

        Path outDir = Paths.get("routes");
        if (!Files.exists(outDir)) Files.createDirectories(outDir);

        // ✅ Três mapas, um por algoritmo
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasSimples  = new HashMap<>();
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasHibrido  = new HashMap<>();
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasAEstrela = new HashMap<>();

        List<ResultadoRota> resultados = new ArrayList<>();

        // 6. Rotas por região
        System.out.println("\n=== GERANDO ROTAS OTIMIZADAS ===");
        System.out.println("Ordem das regiões: " + grupos.keySet());

        for (Map.Entry<Integer, List<String>> entry : grupos.entrySet()) {

            int idx = entry.getKey();
            List<String> nodes = entry.getValue();

            if (nodes.size() < 5) continue;

            System.out.printf("\n--- Região %d: nós=%d ---\n", idx, nodes.size());

            // ─── SIMPLES ────────────────────────────────────────────────
            // warmup para o JIT não distorcer a primeira medição
            opt.calcularRotaSimples(nodes);

            long inicioS = System.nanoTime();
            for (int rep = 0; rep < 10; rep++) opt.calcularRotaSimples(nodes);
            long fimS = System.nanoTime();

            List<String> rotaS = opt.calcularRotaSimples(nodes);
            OtimizadorRotas.EstatisticasRota statsS = opt.calcularEstatisticas(rotaS);
            statsS.tempoMs = (fimS - inicioS) / 10_000_000.0; // média de 10 execuções

            // ─── HÍBRIDO ────────────────────────────────────────────────
            opt.calcularRotaCobertura(nodes);

            long inicioH = System.nanoTime();
            for (int rep = 0; rep < 10; rep++) opt.calcularRotaCobertura(nodes);
            long fimH = System.nanoTime();

            List<String> rotaH = opt.calcularRotaCobertura(nodes);
            OtimizadorRotas.EstatisticasRota statsH = opt.calcularEstatisticas(rotaH);
            statsH.tempoMs = (fimH - inicioH) / 10_000_000.0;

            // ─── A* ─────────────────────────────────────────────────────
            opt.calcularRotaCobertura_AEstrela(nodes, centroides);

            long inicioA = System.nanoTime();
            for (int rep = 0; rep < 10; rep++) opt.calcularRotaCobertura_AEstrela(nodes, centroides);
            long fimA = System.nanoTime();

            OtimizadorRotas.ResultadoCobertura coberturaA =
                    opt.calcularRotaCobertura_AEstrela(nodes, centroides);
            List<String> rotaA = coberturaA.rota;
            OtimizadorRotas.EstatisticasRota statsA = opt.calcularEstatisticas(rotaA);
            statsA.tempoMs      = (fimA - inicioA) / 10_000_000.0;
            statsA.nosExplorados = coberturaA.totalNosExplorados;

            // ─── PRINT ──────────────────────────────────────────────────
            System.out.println("\nComparação:");
            System.out.printf("Simples  -> Dist: %.2f | Dens: %.2f | Eff: %.6f | Tempo: %.2f ms%n",
                    statsS.distanciaTotal, statsS.densidadeTotal,
                    statsS.getEficiencia(), statsS.tempoMs);
            System.out.printf("Híbrido  -> Dist: %.2f | Dens: %.2f | Eff: %.6f | Tempo: %.2f ms%n",
                    statsH.distanciaTotal, statsH.densidadeTotal,
                    statsH.getEficiencia(), statsH.tempoMs);
            System.out.printf("A*       -> Dist: %.2f | Dens: %.2f | Eff: %.6f | Tempo: %.2f ms | Nós: %d%n",
                    statsA.distanciaTotal, statsA.densidadeTotal,
                    statsA.getEficiencia(), statsA.tempoMs, statsA.nosExplorados);

            // ─── GUARDAR ────────────────────────────────────────────────
            // ✅ ordem correta: Simples → statsS, Híbrido → statsH, A* → statsA
            estatisticasSimples .put(idx, statsS);
            estatisticasHibrido .put(idx, statsH);
            estatisticasAEstrela.put(idx, statsA);

            resultados.add(new ResultadoRota("DijkstraSimples",  idx,
                    statsS.distanciaTotal, statsS.densidadeTotal, statsS.tempoMs));
            resultados.add(new ResultadoRota("DijkstraHibrido",  idx,
                    statsH.distanciaTotal, statsH.densidadeTotal, statsH.tempoMs));
            resultados.add(new ResultadoRota("AStar",            idx,
                    statsA.distanciaTotal, statsA.densidadeTotal, statsA.tempoMs));

            // ─── SALVAR ROTA HÍBRIDO EM CSV ─────────────────────────────
            Path out = outDir.resolve(String.format("rota_regiao_%02d.csv", idx));
            try (BufferedWriter bw = Files.newBufferedWriter(out)) {
                bw.write("ordem,celula,lat,lon,densidade\n");
                for (int i = 0; i < rotaH.size(); i++) {
                    String cel = rotaH.get(i);
                    double[] c = centroides.getOrDefault(cel, new double[]{0.0, 0.0});
                    double d = densidadesMedias.getOrDefault(cel, 0.0);
                    bw.write(String.format(Locale.US,
                            "%d,%s,%.6f,%.6f,%.6f\n", i + 1, cel, c[0], c[1], d));
                }
            }
            System.out.println("Rota salva: " + out);
        }

        // 7. RELATÓRIO
        System.out.println("\n=== GERANDO RELATÓRIO ANALÍTICO ===");

        // ✅ ordem do construtor: simples, hibrido, aEstrela
        GeradorRelatorio relatorio = new GeradorRelatorio(
                densidadesMedias,
                centroides,
                grupos,
                estatisticasSimples,
                estatisticasHibrido,
                estatisticasAEstrela
        );

        relatorio.imprimirRelatorioCompleto();
        relatorio.exportarRelatorio("relatorio_analise_microplasticos.txt");
        relatorio.exportarDadosGraficos("graficos");

        // 8. EXPORTAÇÃO GEPHI
        exportarParaGephi(
                densidadesMedias,
                centroides,
                mapRegiao,
                grupos,
                estatisticasHibrido, 
                grafo
        );

        // 9. RANKING FINAL
        System.out.println("\n=== COMPARAÇÃO DE RESULTADOS ===");
        resultados.sort((a, b) -> Double.compare(b.getEficiencia(), a.getEficiencia()));
        for (ResultadoRota r : resultados) {
            r.imprimir();
        }

        System.out.println("\n=== PROCESSAMENTO FINALIZADO ===");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GEPHI
    // ─────────────────────────────────────────────────────────────────────────

    private static void exportarParaGephi(
            Map<String, Double> densidades,
            Map<String, double[]> centroides,
            Map<String, Integer> comunidades,
            Map<Integer, List<String>> grupos,
            Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticas,
            Grafo grafo
    ) throws IOException {
        exportarGrafoCompleto(densidades, centroides, comunidades, grafo);
        exportarSubGrafosPorRegiao(grupos, densidades, centroides, grafo);
        System.out.println("\n✓ Gephi exportado com sucesso!");
    }

    private static void exportarGrafoCompleto(
            Map<String, Double> densidades,
            Map<String, double[]> centroides,
            Map<String, Integer> comunidades,
            Grafo grafo
    ) throws IOException {
        try (BufferedWriter w = Files.newBufferedWriter(Paths.get("grafo_nodes.csv"))) {
            w.write("Id,Lat,Lon,Densidade,Comunidade\n");
            for (String id : densidades.keySet()) {
                double[] c = centroides.get(id);
                w.write(String.format(Locale.US, "%s,%.4f,%.4f,%.6f,%d\n",
                        id, c[0], c[1], densidades.get(id),
                        comunidades.getOrDefault(id, -1)));
            }
        }

        try (BufferedWriter w = Files.newBufferedWriter(Paths.get("grafo_edges.csv"))) {
            w.write("Source,Target,Weight\n");
            for (var entry : grafo.getAdjacencia().entrySet()) {
                for (Aresta a : entry.getValue()) {
                    w.write(String.format(Locale.US, "%s,%s,%.4f\n",
                            entry.getKey(), a.getDestino(), a.getDistancia()));
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
        for (var entry : grupos.entrySet()) {
            int id = entry.getKey();
            List<String> nodes = entry.getValue();
            if (nodes.size() < 5) continue;

            Set<String> set = new HashSet<>(nodes);

            try (BufferedWriter w = Files.newBufferedWriter(
                    Paths.get("regiao_" + id + "_nodes.csv"))) {
                w.write("Id,Lat,Lon,Densidade\n");
                for (String n : nodes) {
                    double[] c = centroides.get(n);
                    w.write(String.format(Locale.US, "%s,%.4f,%.4f,%.6f\n",
                            n, c[0], c[1], densidades.get(n)));
                }
            }

            try (BufferedWriter w = Files.newBufferedWriter(
                    Paths.get("regiao_" + id + "_edges.csv"))) {
                w.write("Source,Target,Weight\n");
                for (String s : nodes) {
                    for (Aresta a : grafo.getAdjacencia().getOrDefault(s, new ArrayList<>())) {
                        if (!set.contains(a.getDestino())) continue;
                        w.write(String.format(Locale.US, "%s,%s,%.4f\n",
                                s, a.getDestino(), a.getDistancia()));
                    }
                }
            }
        }
    }
}