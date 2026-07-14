import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Main {
    public static void main(String[] args) throws Exception {

        String caminhoCSV = "data/survey_data.csv";

        LeitorDeDados leitor = new LeitorDeDados();
        List<AmostraPonto> amostras = leitor.lerAmostras(caminhoCSV);
        System.out.println("Total de amostras carregadas: " + amostras.size());

        PreProcessador pre = new PreProcessador();
        Map<String, List<AmostraPonto>> celulas = pre.agruparPorCoordenadas(amostras);
        Map<String, Double> densidadesMedias = pre.calcularDensidadeMedia(celulas);
        Map<String, double[]> centroides = pre.calcularCentroides(celulas);

        System.out.println("Células: " + celulas.size());

        Grafo grafo = new Grafo(densidadesMedias, centroides);
        System.out.println("Grafo construído.");

        DetectorComunidades detector = new DetectorComunidades(grafo);
        detector.detectarComunidadesPorRegiao();
        Map<Integer, List<String>> grupos = new TreeMap<>(detector.agruparPorComunidade());

        detector.imprimirEstatisticas(centroides, densidadesMedias);

        OtimizadorRotas opt = new OtimizadorRotas(grafo, densidadesMedias, centroides);

        Path outDir = Paths.get("routes");
        if (!Files.exists(outDir)) Files.createDirectories(outDir);

        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasSimples  = new HashMap<>();
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasHibrido  = new HashMap<>();
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasAEstrela = new HashMap<>();

        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasSimples2opt  = new HashMap<>();
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasHibrido2opt  = new HashMap<>();
        Map<Integer, OtimizadorRotas.EstatisticasRota> estatisticasAEstrela2opt = new HashMap<>();

        List<ResultadoRota> resultados = new ArrayList<>();

        System.out.println("\n=== GERANDO ROTAS OTIMIZADAS ===");
        System.out.println("Ordem das regiões: " + grupos.keySet());

        Path graficosDir = Paths.get("graficos");
        if (!Files.exists(graficosDir)) Files.createDirectories(graficosDir);

        try (BufferedWriter bw2opt = Files.newBufferedWriter(graficosDir.resolve("comparacao_2opt.csv"))) {
            bw2opt.write("regiao,algoritmo,dist_antes,dist_depois,eff_antes,eff_depois,melhoria_pct,tempo_2opt_ms\n");

            for (Map.Entry<Integer, List<String>> entry : grupos.entrySet()) {

                int idx = entry.getKey();
                List<String> nodes = entry.getValue();

                if (nodes.size() < 5) continue;

                System.out.printf("\n--- Região %d: nós=%d ---%n", idx, nodes.size());

                opt.calcularRotaSimples(nodes);
                long inicioS = System.nanoTime();
                for (int rep = 0; rep < 10; rep++) opt.calcularRotaSimples(nodes);
                long fimS = System.nanoTime();

                List<String> rotaS = opt.calcularRotaSimples(nodes);
                OtimizadorRotas.EstatisticasRota statsS = opt.calcularEstatisticas(rotaS);
                statsS.tempoMs = (fimS - inicioS) / 10_000_000.0;

                opt.calcularRotaCobertura(nodes);
                long inicioH = System.nanoTime();
                for (int rep = 0; rep < 10; rep++) opt.calcularRotaCobertura(nodes);
                long fimH = System.nanoTime();

                List<String> rotaH = opt.calcularRotaCobertura(nodes);
                OtimizadorRotas.EstatisticasRota statsH = opt.calcularEstatisticas(rotaH);
                statsH.tempoMs = (fimH - inicioH) / 10_000_000.0;

                opt.calcularRotaCobertura_AEstrela(nodes, centroides);
                long inicioA = System.nanoTime();
                for (int rep = 0; rep < 10; rep++) opt.calcularRotaCobertura_AEstrela(nodes, centroides);
                long fimA = System.nanoTime();

                OtimizadorRotas.ResultadoCobertura coberturaA =
                        opt.calcularRotaCobertura_AEstrela(nodes, centroides);
                List<String> rotaA = coberturaA.rota;
                OtimizadorRotas.EstatisticasRota statsA = opt.calcularEstatisticas(rotaA);
                statsA.tempoMs       = (fimA - inicioA) / 10_000_000.0;
                statsA.nosExplorados = coberturaA.totalNosExplorados;

                System.out.println("\nComparação (antes do 2-opt):");
                System.out.printf("Simples  -> Dist: %.2f | Eff: %.6f | Tempo: %.2f ms%n",
                        statsS.distanciaTotal, statsS.getEficiencia(), statsS.tempoMs);
                System.out.printf("Híbrido  -> Dist: %.2f | Eff: %.6f | Tempo: %.2f ms%n",
                        statsH.distanciaTotal, statsH.getEficiencia(), statsH.tempoMs);
                System.out.printf("A*       -> Dist: %.2f | Eff: %.6f | Tempo: %.2f ms | Nós: %d%n",
                        statsA.distanciaTotal, statsA.getEficiencia(), statsA.tempoMs, statsA.nosExplorados);

                long t0;

                t0 = System.nanoTime();
                List<String> rotaS2opt = opt.aplicar2Opt(rotaS);
                double tempoS2opt = (System.nanoTime() - t0) / 1_000_000.0;
                OtimizadorRotas.EstatisticasRota statsS2opt = opt.calcularEstatisticas(rotaS2opt);
                statsS2opt.tempoMs = tempoS2opt;

                t0 = System.nanoTime();
                List<String> rotaH2opt = opt.aplicar2Opt(rotaH);
                double tempoH2opt = (System.nanoTime() - t0) / 1_000_000.0;
                OtimizadorRotas.EstatisticasRota statsH2opt = opt.calcularEstatisticas(rotaH2opt);
                statsH2opt.tempoMs = tempoH2opt;

                t0 = System.nanoTime();
                List<String> rotaA2opt = opt.aplicar2Opt(rotaA);
                double tempoA2opt = (System.nanoTime() - t0) / 1_000_000.0;
                OtimizadorRotas.EstatisticasRota statsA2opt = opt.calcularEstatisticas(rotaA2opt);
                statsA2opt.tempoMs = tempoA2opt;

                System.out.println("\nApós refinamento 2-opt:");
                System.out.printf("Simples+2opt -> Dist: %.2f (%.1f%% menor) | Eff: %.6f | 2opt: %.2f ms%n",
                        statsS2opt.distanciaTotal,
                        100.0 * (statsS.distanciaTotal - statsS2opt.distanciaTotal) / statsS.distanciaTotal,
                        statsS2opt.getEficiencia(), tempoS2opt);
                System.out.printf("Híbrido+2opt -> Dist: %.2f (%.1f%% menor) | Eff: %.6f | 2opt: %.2f ms%n",
                        statsH2opt.distanciaTotal,
                        100.0 * (statsH.distanciaTotal - statsH2opt.distanciaTotal) / statsH.distanciaTotal,
                        statsH2opt.getEficiencia(), tempoH2opt);
                System.out.printf("A*+2opt      -> Dist: %.2f (%.1f%% menor) | Eff: %.6f | 2opt: %.2f ms%n",
                        statsA2opt.distanciaTotal,
                        100.0 * (statsA.distanciaTotal - statsA2opt.distanciaTotal) / statsA.distanciaTotal,
                        statsA2opt.getEficiencia(), tempoA2opt);

                bw2opt.write(String.format(Locale.US, "%d,Simples,%.4f,%.4f,%.6f,%.6f,%.2f,%.4f%n",
                        idx, statsS.distanciaTotal, statsS2opt.distanciaTotal,
                        statsS.getEficiencia(), statsS2opt.getEficiencia(),
                        100.0 * (statsS.distanciaTotal - statsS2opt.distanciaTotal) / statsS.distanciaTotal,
                        tempoS2opt));
                bw2opt.write(String.format(Locale.US, "%d,Hibrido,%.4f,%.4f,%.6f,%.6f,%.2f,%.4f%n",
                        idx, statsH.distanciaTotal, statsH2opt.distanciaTotal,
                        statsH.getEficiencia(), statsH2opt.getEficiencia(),
                        100.0 * (statsH.distanciaTotal - statsH2opt.distanciaTotal) / statsH.distanciaTotal,
                        tempoH2opt));
                bw2opt.write(String.format(Locale.US, "%d,AEstrela,%.4f,%.4f,%.6f,%.6f,%.2f,%.4f%n",
                        idx, statsA.distanciaTotal, statsA2opt.distanciaTotal,
                        statsA.getEficiencia(), statsA2opt.getEficiencia(),
                        100.0 * (statsA.distanciaTotal - statsA2opt.distanciaTotal) / statsA.distanciaTotal,
                        tempoA2opt));

                estatisticasSimples.put(idx, statsS);
                estatisticasHibrido.put(idx, statsH);
                estatisticasAEstrela.put(idx, statsA);

                estatisticasSimples2opt.put(idx, statsS2opt);
                estatisticasHibrido2opt.put(idx, statsH2opt);
                estatisticasAEstrela2opt.put(idx, statsA2opt);

                resultados.add(new ResultadoRota("DijkstraSimples",  idx,
                        statsS.distanciaTotal, statsS.densidadeTotal, statsS.tempoMs));
                resultados.add(new ResultadoRota("DijkstraHibrido",  idx,
                        statsH.distanciaTotal, statsH.densidadeTotal, statsH.tempoMs));
                resultados.add(new ResultadoRota("AStar",            idx,
                        statsA.distanciaTotal, statsA.densidadeTotal, statsA.tempoMs));
                resultados.add(new ResultadoRota("Simples+2opt",     idx,
                        statsS2opt.distanciaTotal, statsS2opt.densidadeTotal, tempoS2opt));
                resultados.add(new ResultadoRota("Hibrido+2opt",     idx,
                        statsH2opt.distanciaTotal, statsH2opt.densidadeTotal, tempoH2opt));
                resultados.add(new ResultadoRota("AStar+2opt",       idx,
                        statsA2opt.distanciaTotal, statsA2opt.densidadeTotal, tempoA2opt));

                Path out = outDir.resolve(String.format("rota_regiao_%02d.csv", idx));
                try (BufferedWriter bw = Files.newBufferedWriter(out)) {
                    bw.write("ordem,celula,lat,lon,densidade\n");
                    for (int i = 0; i < rotaH2opt.size(); i++) {
                        String cel = rotaH2opt.get(i);
                        double[] c = centroides.getOrDefault(cel, new double[]{0.0, 0.0});
                        double d = densidadesMedias.getOrDefault(cel, 0.0);
                        bw.write(String.format(Locale.US,
                                "%d,%s,%.6f,%.6f,%.6f\n", i + 1, cel, c[0], c[1], d));
                    }
                }
                System.out.println("Rota (híbrido+2opt) salva: " + out);
            }
        }

        System.out.println("\n=== GERANDO RELATÓRIO ANALÍTICO ===");

        GeradorRelatorio relatorio = new GeradorRelatorio(
                densidadesMedias, centroides, grupos,
                estatisticasSimples, estatisticasHibrido, estatisticasAEstrela
        );

        relatorio.imprimirRelatorioCompleto();
        relatorio.exportarRelatorio("relatorio_analise_microplasticos.txt");
        relatorio.exportarDadosGraficos("graficos");

        System.out.println("\n=== RESUMO DO GANHO COM 2-OPT (média das regiões) ===");
        imprimirGanhoMedio("Simples", estatisticasSimples, estatisticasSimples2opt);
        imprimirGanhoMedio("Híbrido", estatisticasHibrido, estatisticasHibrido2opt);
        imprimirGanhoMedio("A*",      estatisticasAEstrela, estatisticasAEstrela2opt);

        System.out.println("\n=== COMPARAÇÃO DE RESULTADOS ===");
        resultados.sort((a, b) -> Double.compare(b.getEficiencia(), a.getEficiencia()));
        for (ResultadoRota r : resultados) {
            r.imprimir();
        }

        System.out.println("\n=== PROCESSAMENTO FINALIZADO ===");
    }

    private static void imprimirGanhoMedio(String nome,
            Map<Integer, OtimizadorRotas.EstatisticasRota> antes,
            Map<Integer, OtimizadorRotas.EstatisticasRota> depois) {

        double somaAntes = 0, somaDepois = 0;
        int n = 0;

        for (Integer k : antes.keySet()) {
            if (!depois.containsKey(k)) continue;
            somaAntes  += antes.get(k).distanciaTotal;
            somaDepois += depois.get(k).distanciaTotal;
            n++;
        }

        if (n == 0) return;

        double reducaoPct = 100.0 * (somaAntes - somaDepois) / somaAntes;
        System.out.printf("%-10s -> Dist. média antes: %10.2f km | depois: %10.2f km | redução: %.2f%%%n",
                nome, somaAntes / n, somaDepois / n, reducaoPct);
    }
}