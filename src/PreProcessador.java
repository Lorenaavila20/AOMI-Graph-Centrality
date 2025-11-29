import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class PreProcessador {

    private static final double TAMANHO_CELULA_KM = 110.0; // mantido pra referência, mas não usado na centroid calc

    /**
     * Agrupa amostras em células usando floor em graus (1º x 1º) — mantém compatibilidade com sua ideia inicial.
     * Retorna mapa chave -> lista de amostras (onde chave = "celY_celX" usando floor(lat)_floor(lon)).
     */
    public Map<String, List<AmostraPonto>> agruparPorCoordenadas(List<AmostraPonto> amostras){
        Map<String, List<AmostraPonto>> celulas = new HashMap<>();
        for (AmostraPonto amostra : amostras){
            String chaveCelula = calcularChaveCelula(amostra.getLat(), amostra.getLon());
            celulas.computeIfAbsent(chaveCelula, k -> new ArrayList<>()).add(amostra);
        }
        return celulas;
    }

    /**
     * Calcula a densidade média por célula (como você já tinha).
     * Retorna mapa chave -> densidade média (não normalizada).
     */
    public Map<String, Double> calcularDensidadeMedia(Map<String, List<AmostraPonto>> celulasAgrupadas){
        Map<String, Double> celulasMedias = new HashMap<>();
        for (Map.Entry<String, List<AmostraPonto>> entry : celulasAgrupadas.entrySet()){
            double soma = 0.0;
            int n = entry.getValue().size();
            for (AmostraPonto p : entry.getValue()){
                soma += p.getDensidade();
            }
            if (n > 0){
                celulasMedias.put(entry.getKey(), soma / n);
            }
        }
        return celulasMedias;
    }

    /**
     * Cria um mapa de centróides (lat,lon) por célula — média simples das amostras da célula.
     * Retorna mapa chave -> double[]{mediaLat, mediaLon}
     */
    public Map<String, double[]> calcularCentroides(Map<String, List<AmostraPonto>> celulasAgrupadas){
        Map<String, double[]> centroides = new HashMap<>();
        for (Map.Entry<String, List<AmostraPonto>> entry : celulasAgrupadas.entrySet()){
            double somaLat = 0.0;
            double somaLon = 0.0;
            int n = entry.getValue().size();
            for (AmostraPonto p : entry.getValue()){
                somaLat += p.getLat();
                somaLon += p.getLon();
            }
            centroides.put(entry.getKey(), new double[]{somaLat / n, somaLon / n});
        }
        return centroides;
    }

    /**
     * Chave baseada em floor de graus. Mantive assim para compatibilidade com seus dados.
     * Ex: lat=35.7 lon=-120.2 -> "35.0_-121.0" (ou "-121" dependendo)
     */
    private String calcularChaveCelula(double lat, double lon){
        double latChao = Math.floor(lat); 
        double lonChao = Math.floor(lon);
        // Use separador "_" — seja consistente
        return latChao + "_" + lonChao;
    }
}
