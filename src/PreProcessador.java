import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class PreProcessador {
    private static final double TAMANHO_CELULA_KM = 110.0;

    public Map<String, List<AmostraPonto>> agruparPorCoordenadas(List<AmostraPonto> amostras) {
        Map<String, List<AmostraPonto>> celulas = new HashMap<>();
        for (AmostraPonto amostra : amostras) {
            String chaveCelula = calcularChaveCelula(amostra.getLat(), amostra.getLon());
            celulas.computeIfAbsent(chaveCelula, k -> new ArrayList<>()).add(amostra);
        }
        return celulas;
    }

    public Map<String, Double> calcularDensidadeMedia(Map<String, List<AmostraPonto>> celulasAgrupadas) {
        Map<String, Double> celulasMedias = new HashMap<>();
        for (Map.Entry<String, List<AmostraPonto>> entry : celulasAgrupadas.entrySet()) {
            double soma = 0.0;
            int n = entry.getValue().size();
            for (AmostraPonto p : entry.getValue()) {
                soma += p.getDensidade();
            }
            if (n > 0) {
                celulasMedias.put(entry.getKey(), soma / n);
            }
        }
        return celulasMedias;
    }

    public Map<String, double[]> calcularCentroides(Map<String, List<AmostraPonto>> celulasAgrupadas) {
        Map<String, double[]> centroides = new HashMap<>();
        for (Map.Entry<String, List<AmostraPonto>> entry : celulasAgrupadas.entrySet()) {
            double somaLat = 0.0;
            double somaLon = 0.0;
            int n = entry.getValue().size();
            for (AmostraPonto p : entry.getValue()) {
                somaLat += p.getLat();
                somaLon += p.getLon();
            }
            centroides.put(entry.getKey(), new double[]{somaLat / n, somaLon / n});
        }
        return centroides;
    }

    /**
     * CORRIGIDO: Usa Locale.US para garantir ponto decimal
     */
    private String calcularChaveCelula(double lat, double lon) {
        double latChao = Math.floor(lat); 
        double lonChao = Math.floor(lon);
        return String.format(Locale.US, "%.1f_%.1f", latChao, lonChao);
    }
}