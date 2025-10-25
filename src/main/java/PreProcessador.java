import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class PreProcessador{

    // DESAFIO 5: Agrupa todas as amostras em células de 1x1 grau
    public Map<String, List<AmostraPonto>> agruparPorCoordenadas(List<AmostraPonto> amostras){
        
        Map<String, List<AmostraPonto>> celulas = new HashMap<>();

        for (AmostraPonto amostra : amostras){
            
            String chaveCelula = calcularChaveCelula(amostra.getLat(), amostra.getLon());
            
            // Lógica de agrupamento: se a chave é nova, cria a lista; senão, adiciona à lista existente
            if (!celulas.containsKey(chaveCelula)){
                celulas.put(chaveCelula, new ArrayList<>());
            }
            
            celulas.get(chaveCelula).add(amostra);
        }

        return celulas;
    }

    // DESAFIO 6: Transforma a lista de amostras na Média de Densidade da célula
    public Map<String, Double> calcularDensidadeMedia(Map<String, List<AmostraPonto>> celulasAgrupadas){

        Map<String, Double> celulasMedias = new HashMap<>();

        for (Map.Entry<String, List<AmostraPonto>> entry : celulasAgrupadas.entrySet()){
            
            String chaveCelula = entry.getKey();
            List<AmostraPonto> amostrasNaCelula = entry.getValue();
            
            double somaDensidades = 0.0;
            
            // Soma todas as densidades
            for (AmostraPonto amostra : amostrasNaCelula){
                somaDensidades += amostra.getDensidade();
            }
            
            int contagem = amostrasNaCelula.size();
            
            // Cálculo seguro da média
            if (contagem > 0){
                double densidadeMedia = somaDensidades / contagem;
                celulasMedias.put(chaveCelula, densidadeMedia);
            }
        }

        return celulasMedias;
    }

    // DESAFIO 4: Método auxiliar para criar a chave da célula (Math.floor)
    private String calcularChaveCelula(double lat, double lon){
        
        double latChao = Math.floor(lat); 
        double lonChao = Math.floor(lon);
        
        return latChao + "_" + lonChao;
    }
}