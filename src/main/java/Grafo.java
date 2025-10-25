import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

public class Grafo{

    // 1. Armazenamento dos Nós e seus Pesos (Densidade Média)
    private Map<String, Double> nos;

    // 2. Armazenamento das Arestas (Lista de Adjacência)
    private Map<String, List<String>> adjacencia;

    /**
     * Construtor: Inicializa o grafo com as densidades médias calculadas
     * pelo PreProcessador e inicia a construção das arestas.
     */
    public Grafo(Map<String, Double> densidadesMedias){
        this.nos = densidadesMedias;
        this.adjacencia = new HashMap<>();
        
        // Chamada para construir as arestas assim que o grafo é inicializado
        construirArestas(); 
    }
    
    // Método principal para construir as arestas (conexões) entre os nós vizinhos
    public void construirArestas(){
        // Itera sobre todos os IDs (chaves) dos nós existentes
        for (String chaveAtual : this.nos.keySet()){
            
            // Pega as coordenadas do Nó atual para calcular a posição dos vizinhos
            double[] coords = parseChave(chaveAtual);
            double latAtual = coords[0];
            double lonAtual = coords[1];
            
            // Loop para verificar os 8 vizinhos (dLat e dLon de -1 a 1)
            for (int dLat = -1; dLat <= 1; dLat++){
                for (int dLon = -1; dLon <= 1; dLon++){
                    
                    // Pula a própria célula (dLat e dLon não podem ser zero ao mesmo tempo)
                    if (dLat == 0 && dLon == 0){
                        continue;
                    }
                    
                    // Calcula a chave (ID) do vizinho
                    double latVizinho = latAtual + dLat;
                    double lonVizinho = lonAtual + dLon;
                    String chaveVizinho = latVizinho + "_" + lonVizinho;
                    
                    // CRITÉRIO DE EXISTÊNCIA DA ARESTA: O nó vizinho deve existir no nosso mapa 'nos'
                    if (this.nos.containsKey(chaveVizinho)){
                        
                        // LÓGICA DE ADJACÊNCIA: Adiciona a conexão ao mapa 'adjacencia'
                        
                        // Passo A: Garante que o nó atual tenha uma lista de vizinhos (inicializa se não existir)
                        if (!this.adjacencia.containsKey(chaveAtual)){
                            this.adjacencia.put(chaveAtual, new ArrayList<>());
                        }
                        
                        // Passo B: Adiciona o vizinho à lista de adjacência do nó atual
                        this.adjacencia.get(chaveAtual).add(chaveVizinho);
                    }
                }
            }
        }
    }

    /**
     * Auxiliar: Converte a chave da célula ("LAT_LON") em um array de doubles.
     * @param chave A String da chave da célula.
     * @return Array [latitude, longitude].
     */
    private double[] parseChave(String chave){
        // 1. Divide a chave usando o caractere de separação "_".
        String[] partes = chave.split("_");
        
        // 2. Converte as strings resultantes em doubles.
        double lat = Double.parseDouble(partes[0]);
        double lon = Double.parseDouble(partes[1]);
        
        // 3. Retorna a Latitude e Longitude em um novo array.
        return new double[]{lat, lon};
    }
}