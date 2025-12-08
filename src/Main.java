import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Locale;

public class Main {

    public static void main(String[] args) {

        String caminhoCSV = "data/survey_data.csv";

        LeitorDeDados leitor = new LeitorDeDados();
        List<AmostraPonto> amostras = leitor.lerAmostras(caminhoCSV);
        System.out.println("Total de amostras carregadas: " + amostras.size());

        PreProcessador pre = new PreProcessador();
        Map<String, List<AmostraPonto>> celulas = pre.agruparPorCoordenadas(amostras);
        System.out.println("Total de células criadas: " + celulas.size());

        Map<String, Double> densidadesMedias = pre.calcularDensidadeMedia(celulas);
        Map<String, double[]> centroides = pre.calcularCentroides(celulas); 
        System.out.println("Células com densidade média calculada: " + densidadesMedias.size());

        Grafo grafo = new Grafo(densidadesMedias);
        System.out.println("Grafo construído com sucesso!");

        List<Map.Entry<String, Double>> topDensidades = new ArrayList<>(densidadesMedias.entrySet());
        topDensidades.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        
        System.out.println("\n=== TOP 10 CÉLULAS COM MAIOR DENSIDADE ===");
        for (int i = 0; i < Math.min(10, topDensidades.size()); i++) {
            Map.Entry<String, Double> entry = topDensidades.get(i);
            double[] coords = centroides.get(entry.getKey());
            System.out.printf(Locale.US, "%d. Célula %s - Densidade: %.6f - Coords: (%.2f, %.2f)%n", 
                i+1, entry.getKey(), entry.getValue(), coords[0], coords[1]);
        }

        String origem = topDensidades.get(0).getKey();
        String destino = null;
        Map<String, List<Aresta>> adjacencia = grafo.getAdjacencia();
        
        for (int i = 1; i < Math.min(20, topDensidades.size()); i++) {
            String candidato = topDensidades.get(i).getKey();
            if (!candidato.equals(origem)) {
                destino = candidato;
                break;
            }
        }

        if (destino == null) {
            System.out.println("Não foi possível encontrar destino adequado.");
            return;
        }

        System.out.println("\n=== ROTA DE COLETA SELECIONADA ===");
        System.out.println("Origem (maior densidade): " + origem);
        double[] coordsOrigem = centroides.get(origem);
        System.out.printf(Locale.US, "  Coordenadas: (%.2f, %.2f)%n", coordsOrigem[0], coordsOrigem[1]);
        System.out.printf(Locale.US, "  Densidade: %.6f%n", densidadesMedias.get(origem));
        
        System.out.println("Destino (alta densidade): " + destino);
        double[] coordsDestino = centroides.get(destino);
        System.out.printf(Locale.US, "  Coordenadas: (%.2f, %.2f)%n", coordsDestino[0], coordsDestino[1]);
        System.out.printf(Locale.US, "  Densidade: %.6f%n", densidadesMedias.get(destino));

        double k = 5.0;
        System.out.println("\nFator de priorização (k): " + k);
        
        Map<String, String> prev = DijkstraHibrido.dijkstra(
            grafo, origem, destino, k
        );

        if (prev.get(destino) == null && !origem.equals(destino)) {
            System.out.println("\n⚠️  AVISO: Grafo desconexo! Não existe caminho entre origem e destino.");
            System.out.println("Isso pode acontecer se as regiões estão em oceanos diferentes.");
            System.out.println("Tentando encontrar rota dentro da mesma região...");
            
            destino = encontrarDestinoConectado(grafo, origem, densidadesMedias);
            if (destino == null) {
                System.out.println("Não foi possível encontrar rota conectada.");
                return;
            }
            
            System.out.println("Novo destino: " + destino);
            prev = DijkstraHibrido.dijkstra(grafo, origem, destino, k);
        }

        System.out.println("\n=== CAMINHO DE COLETA OTIMIZADO ===");
        Set<String> rotaOtimizada = reconstruirEColetarCaminho(prev, origem, destino); 
        imprimirCaminho(prev, origem, destino, densidadesMedias, centroides); 

        exportarNodes(densidadesMedias, centroides, origem, destino);
        exportarEdges(grafo, rotaOtimizada);
        System.out.println("\n✓ Arquivos nodes.csv e edges.csv exportados com sucesso!");
        System.out.println("  Importe no Gephi para visualizar a rede de coleta!");
    }

    private static String encontrarDestinoConectado(Grafo grafo, String origem, Map<String, Double> densidades) {
        Map<String, List<Aresta>> adj = grafo.getAdjacencia();
        Set<String> visitados = new HashSet<>();
        Stack<String> pilha = new Stack<>();
        pilha.push(origem);
        visitados.add(origem);
        
        String melhorDestino = null;
        double melhorDensidade = 0;
        
        while (!pilha.isEmpty() && visitados.size() < 100) {
            String atual = pilha.pop();
            double densAtual = densidades.get(atual);
            
            if (!atual.equals(origem) && densAtual > melhorDensidade) {
                melhorDestino = atual;
                melhorDensidade = densAtual;
            }
            
            for (Aresta a : adj.getOrDefault(atual, new ArrayList<>())) {
                String viz = a.getDestino();
                if (!visitados.contains(viz)) {
                    visitados.add(viz);
                    pilha.push(viz);
                }
            }
        }
        
        return melhorDestino;
    }

    private static void imprimirCaminho(Map<String, String> prev, String origem, String destino, 
                                       Map<String, Double> densidades, Map<String, double[]> centroides) {
        Stack<String> caminho = new Stack<>();
        String atual = destino;
        double densidadeTotal = 0;
        int passos = 0;
        
        while (atual != null) {
            caminho.push(atual);
            densidadeTotal += densidades.get(atual);
            passos++;
            if (atual.equals(origem)) break;
            atual = prev.get(atual);
        }

        System.out.println("Número de células no caminho: " + passos);
        System.out.printf(Locale.US, "Densidade total coletada: %.6f%n", densidadeTotal);
        System.out.println("\nCaminho detalhado:");
        
        int step = 1;
        while (!caminho.isEmpty()) {
            String celula = caminho.pop();
            double[] coords = centroides.get(celula);
            double dens = densidades.get(celula);
            System.out.printf(Locale.US, "  %d. %s - Coords: (%.2f, %.2f) - Densidade: %.6f%n", 
                step++, celula, coords[0], coords[1], dens);
        }
    }
    
    private static Set<String> reconstruirEColetarCaminho(Map<String, String> prev, String origem, String destino) {
        Set<String> rotaArestas = new HashSet<>();
        String atual = destino;
        
        while (atual != null && !atual.equals(origem)) {
            String predecessor = prev.get(atual);
            if (predecessor != null) {
                rotaArestas.add(predecessor + "_" + atual); 
            }
            atual = predecessor;
        }
        return rotaArestas;
    }

    private static void exportarNodes(Map<String, Double> densidades, Map<String, double[]> centroides, String origem, String destino) {
        try (FileWriter writer = new FileWriter("nodes.csv")) {
            writer.append("Id,Label,Latitude,Longitude,Densidade,Tipo\n");

            for (Map.Entry<String, Double> entry : densidades.entrySet()) {
                String id = entry.getKey();
                double densidade = entry.getValue();
                double[] coords = centroides.get(id);

                String tipo = "Intermediario";
                if (id.equals(origem)) tipo = "Origem";
                else if (id.equals(destino)) tipo = "Destino";

                writer.append(String.format(Locale.US, "\"%s\",\"%s\",%.4f,%.4f,%.6f,\"%s\"\n",
                                            id, id, coords[0], coords[1], densidade, tipo));
            }
        } catch (IOException e) {
            System.err.println("Erro ao exportar nodes.csv: " + e.getMessage());
        }
    }

    private static void exportarEdges(Grafo grafo, Set<String> rotaOtimizada) {
        try (FileWriter writer = new FileWriter("edges.csv")) {
            writer.append("Source,Target,Weight,Type,isRoute\n");
            
            Map<String, List<Aresta>> adjacencia = grafo.getAdjacencia();

            for (Map.Entry<String, List<Aresta>> entry : adjacencia.entrySet()) {
                String source = entry.getKey();
                for (Aresta aresta : entry.getValue()) {
                    String target = aresta.getDestino();
                    double peso = aresta.getPeso(); 
                    
                    String arestaKey = source + "_" + target;
                    int isRoute = rotaOtimizada.contains(arestaKey) ? 1 : 0;
                    
                    writer.append(String.format(Locale.US, "\"%s\",\"%s\",%.2f,\"%s\",%d\n",
                                                source, target, peso, "Directed", isRoute));
                }
            }
        } catch (IOException e) {
            System.err.println("Erro ao exportar edges.csv: " + e.getMessage());
        }
    }
}