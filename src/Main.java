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

        // 3. Construção do grafo (AGORA CORRETO)
        Grafo grafo = new Grafo(densidadesMedias, centroides);
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

        // Estatísticas
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasRotas = new HashMap<>();
        List<ResultadoRota> resultados = new ArrayList<>();

        // 6. Rotas por região
        System.out.println("\n=== GERANDO ROTAS OTIMIZADAS ===");
        System.out.println("Ordem das regiões: " + grupos.keySet());

        for (Map.Entry<Integer, List<String>> entry : grupos.entrySet()) {

            int idx = entry.getKey();
            List<String> nodes = entry.getValue();

            if (nodes.size() < 5) continue;

            System.out.printf("\n--- Região %d: nós=%d ---\n", idx, nodes.size());

            // 🔹 HÍBRIDO
            long inicioH = System.nanoTime();
            List<String> rotaH = opt.calcularRotaCobertura(nodes);
            OtimizadorRotas.EstatisticasRota statsH = opt.calcularEstatisticas(rotaH);
            long fimH = System.nanoTime();

            // 🔹 SIMPLES
            long inicioS = System.nanoTime();
            List<String> rotaS = opt.calcularRotaSimples(nodes);
            OtimizadorRotas.EstatisticasRota statsS = opt.calcularEstatisticas(rotaS);
            long fimS = System.nanoTime();

            // 🔹 PRINT BONITO
            System.out.println("\nComparação:");

            System.out.printf("Simples  -> Dist: %.2f | Dens: %.2f | Eff: %.6f\n",
                    statsS.distanciaTotal,
                    statsS.densidadeTotal,
                    statsS.densidadeTotal / statsS.distanciaTotal);

            System.out.printf("Híbrido  -> Dist: %.2f | Dens: %.2f | Eff: %.6f\n",
                    statsH.distanciaTotal,
                    statsH.densidadeTotal,
                    statsH.densidadeTotal / statsH.distanciaTotal);

            // 🔹 mantém estatística do híbrido pro relatório
            estatisticasRotas.put(idx, statsH);

            // 🔹 salva resultados dos dois
            resultados.add(new ResultadoRota(
                "DijkstraHibrido",
                idx,
                statsH.distanciaTotal,
                statsH.densidadeTotal,
                (fimH - inicioH) / 1_000_000.0
            ));

            resultados.add(new ResultadoRota(
                "DijkstraSimples",
                idx,
                statsS.distanciaTotal,
                statsS.densidadeTotal,
                (fimS - inicioS) / 1_000_000.0
            ));

            // salvar rota CSV
            Path out = outDir.resolve(String.format("rota_regiao_%02d.csv", idx));

            try (BufferedWriter bw = Files.newBufferedWriter(out)) {
                bw.write("ordem,celula,lat,lon,densidade\n");

                for (int i = 0; i < rotaH.size(); i++) {
                    String cel = rotaH.get(i);

                    double[] c = centroides.getOrDefault(cel, new double[]{0.0, 0.0});
                    double d = densidadesMedias.getOrDefault(cel, 0.0);

                    bw.write(String.format(Locale.US,
                            "%d,%s,%.6f,%.6f,%.6f\n",
                            i + 1, cel, c[0], c[1], d));
                }
            }

            System.out.println("Rota salva: " + out.toString());
        }

        // 7. RELATÓRIO
        System.out.println("\n=== GERANDO RELATÓRIO ANALÍTICO ===");

        GeradorRelatorio relatorio = new GeradorRelatorio(
                densidadesMedias,
                centroides,
                grupos,
                estatisticasRotas
        );

        relatorio.imprimirRelatorioCompleto();
        relatorio.exportarRelatorio("relatorio_analise_microplasticos.txt");
        relatorio.exportarDadosGraficos("graficos");

        // 8. EXPORTAÇÃO GEFFI
        exportarParaGephi(
                densidadesMedias,
                centroides,
                mapRegiao,
                grupos,
                estatisticasRotas,
                grafo
        );

        System.out.println("\n=== COMPARAÇÃO DE RESULTADOS ===");
        
        // ordenar por eficiência (maior primeiro)
        resultados.sort((a, b) -> Double.compare(b.getEficiencia(), a.getEficiencia()));

        for (ResultadoRota r : resultados) {
            r.imprimir();
        }
        System.out.println("\n=== PROCESSAMENTO FINALIZADO ===");
    }

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

                w.write(String.format(Locale.US,
                        "%s,%.4f,%.4f,%.6f,%d\n",
                        id, c[0], c[1],
                        densidades.get(id),
                        comunidades.getOrDefault(id, -1)));
            }
        }

        try (BufferedWriter w = Files.newBufferedWriter(Paths.get("grafo_edges.csv"))) {
            w.write("Source,Target,Weight\n");

            for (var entry : grafo.getAdjacencia().entrySet()) {
                for (Aresta a : entry.getValue()) {
                    w.write(String.format(Locale.US,
                            "%s,%s,%.4f\n",
                            entry.getKey(),
                            a.getDestino(),
                            a.getDistancia()));
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

                    w.write(String.format(Locale.US,
                            "%s,%.4f,%.4f,%.6f\n",
                            n, c[0], c[1], densidades.get(n)));
                }
            }

            try (BufferedWriter w = Files.newBufferedWriter(
                    Paths.get("regiao_" + id + "_edges.csv"))) {

                w.write("Source,Target,Weight\n");

                for (String s : nodes) {
                    for (Aresta a : grafo.getAdjacencia().getOrDefault(s, new ArrayList<>())) {
                        if (!set.contains(a.getDestino())) continue;

                        w.write(String.format(Locale.US,
                                "%s,%s,%.4f\n",
                                s, a.getDestino(), a.getDistancia()));
                    }
                }
            }
        }
    }
}