import java.util.*;

/**
 * Detecta comunidades (clusters) no grafo usando algoritmo guloso de modularidade.
 * Baseado no algoritmo de Louvain simplificado.
 */
public class DetectorComunidades {
    
    private Map<String, Integer> comunidades;
    private Grafo grafo;
    
    public DetectorComunidades(Grafo grafo) {
        this.grafo = grafo;
        this.comunidades = new HashMap<>();
    }
    
    /**
     * Detecta comunidades usando clustering espacial simples.
     * Agrupa células próximas geograficamente.
     */
    public Map<String, Integer> detectarComunidadesEspaciais() {
        Map<String, Integer> resultado = new HashMap<>();
        Map<String, List<Aresta>> adj = grafo.getAdjacencia();
        Set<String> visitados = new HashSet<>();
        int comunidadeAtual = 0;
        
        // Para cada nó não visitado, cria uma nova comunidade
        for (String celula : adj.keySet()) {
            if (!visitados.contains(celula)) {
                // BFS para encontrar componente conectado
                Queue<String> fila = new LinkedList<>();
                fila.add(celula);
                visitados.add(celula);
                
                while (!fila.isEmpty()) {
                    String atual = fila.poll();
                    resultado.put(atual, comunidadeAtual);
                    
                    // Adiciona vizinhos não visitados
                    for (Aresta a : adj.getOrDefault(atual, new ArrayList<>())) {
                        String viz = a.getDestino();
                        if (!visitados.contains(viz)) {
                            visitados.add(viz);
                            fila.add(viz);
                        }
                    }
                }
                comunidadeAtual++;
            }
        }
        
        this.comunidades = resultado;
        System.out.println("\n=== DETECÇÃO DE COMUNIDADES ===");
        System.out.println("Total de comunidades encontradas: " + comunidadeAtual);
        
        return resultado;
    }
    
    /**
     * Detecta comunidades por proximidade geográfica (latitude/longitude)
     * Agrupa células que estão na mesma região oceânica
     */
    public Map<String, Integer> detectarComunidadesPorRegiao() {
        Map<String, Integer> resultado = new HashMap<>();
        Map<String, Double> densidades = grafo.getNos();
        
        // Definir regiões oceânicas principais
        Map<String, String> regioesNomes = new HashMap<>();
        int comunidadeId = 0;
        
        for (String celula : densidades.keySet()) {
            double[] coords = parseChave(celula);
            double lat = coords[0];
            double lon = coords[1];
            
            // Identificar região baseada em lat/lon
            String regiao = identificarRegiao(lat, lon);
            
            if (!regioesNomes.containsKey(regiao)) {
                regioesNomes.put(regiao, regiao);
                System.out.println("Região " + comunidadeId + ": " + regiao);
                comunidadeId++;
            }
            
            // Atribui comunidade baseada na região
            resultado.put(celula, getIndicePorRegiao(regiao, regioesNomes));
        }
        
        this.comunidades = resultado;
        System.out.println("\n=== COMUNIDADES POR REGIÃO ===");
        System.out.println("Total de regiões identificadas: " + regioesNomes.size());
        
        return resultado;
    }
    
    /**
     * Identifica região oceânica baseada em coordenadas
     */
    private String identificarRegiao(double lat, double lon) {
        // Atlântico Norte
        if (lat >= 30 && lat <= 60 && lon >= -80 && lon <= 0) {
            return "Atlantico_Norte";
        }
        // Atlântico Sul
        if (lat >= -60 && lat < 0 && lon >= -60 && lon <= 20) {
            return "Atlantico_Sul";
        }
        // Pacífico Norte
        if (lat >= 20 && lat <= 60 && lon >= -180 && lon <= -100) {
            return "Pacifico_Norte";
        }
        // Pacífico Sul
        if (lat >= -60 && lat < 0 && lon >= -180 && lon <= -70) {
            return "Pacifico_Sul";
        }
        // Índico
        if (lat >= -60 && lat <= 30 && lon >= 20 && lon <= 120) {
            return "Indico";
        }
        // Mediterrâneo
        if (lat >= 30 && lat <= 45 && lon >= -6 && lon <= 37) {
            return "Mediterraneo";
        }
        // Pacífico Equatorial
        if (lat >= -20 && lat <= 20 && lon >= -180 && lon <= -70) {
            return "Pacifico_Equatorial";
        }
        // Mar da China
        if (lat >= 0 && lat <= 45 && lon >= 100 && lon <= 150) {
            return "Mar_China";
        }
        
        return "Outras_Regioes";
    }
    
    private int getIndicePorRegiao(String regiao, Map<String, String> mapa) {
        List<String> regioes = new ArrayList<>(mapa.keySet());
        return regioes.indexOf(regiao);
    }
    
    private double[] parseChave(String chave) {
        String[] p = chave.split("_");
        return new double[]{
            Double.parseDouble(p[0].replace(",", ".")),
            Double.parseDouble(p[1].replace(",", "."))
        };
    }
    
    /**
     * Agrupa células por comunidade
     */
    public Map<Integer, List<String>> agruparPorComunidade() {
        Map<Integer, List<String>> grupos = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : comunidades.entrySet()) {
            int comunidade = entry.getValue();
            grupos.computeIfAbsent(comunidade, k -> new ArrayList<>()).add(entry.getKey());
        }
        
        return grupos;
    }
    
    /**
     * Estatísticas por comunidade
     */
    public void imprimirEstatisticas(Map<String, double[]> centroides, Map<String, Double> densidades) {
        Map<Integer, List<String>> grupos = agruparPorComunidade();
        
        System.out.println("\n=== ESTATÍSTICAS POR COMUNIDADE ===");
        
        for (Map.Entry<Integer, List<String>> entry : grupos.entrySet()) {
            int comunidadeId = entry.getKey();
            List<String> celulas = entry.getValue();
            
            double densidadeTotal = 0;
            double latMedia = 0;
            double lonMedia = 0;
            
            for (String celula : celulas) {
                densidadeTotal += densidades.getOrDefault(celula, 0.0);
                double[] coords = centroides.get(celula);
                latMedia += coords[0];
                lonMedia += coords[1];
            }
            
            int n = celulas.size();
            double densidadeMedia = densidadeTotal / n;
            latMedia /= n;
            lonMedia /= n;
            
            System.out.printf("\nComunidade %d:%n", comunidadeId);
            System.out.printf("  Células: %d%n", n);
            System.out.printf("  Densidade média: %.6f%n", densidadeMedia);
            System.out.printf("  Centro geográfico: (%.2f, %.2f)%n", latMedia, lonMedia);
            System.out.printf("  Densidade total: %.6f%n", densidadeTotal);
        }
    }
    
    public Map<String, Integer> getComunidades() {
        return comunidades;
    }
}