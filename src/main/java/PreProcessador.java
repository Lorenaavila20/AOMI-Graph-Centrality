import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class PreProcessador{

    // Método principal que será preenchido em seguida
    public Map<String, List<AmostraPonto>> agruparPorCoordenadas(List<AmostraPonto> amostras){

        Map<String, List<AmostraPonto>> celulas = new HashMap<>();

        for (AmostraPonto amostra : amostras){
            
            String chaveCelula = calcularChaveCelula(amostra.getLat(), amostra.getLon());

            // 4. Lógica de Inserção no Mapa
            // PESQUISA 1: Como verificar se uma chave JÁ EXISTE no HashMap?
            if (!celulas.XXXXXXXXXXXXXXXXXXXX(chaveCelula)) {
                
                // AÇÃO 1: Se não existe, você deve criar uma nova lista 
                // e associá-la à chave. (Dica: use celulas.put(...)).
                celulas.XXXXXXXXXXXXXXXXXXXX;
            }
            
            // PESQUISA 2: Como adicionar o objeto 'amostra' à lista que 
            // já existe no mapa para essa chave? (Dica: use celulas.get(...) ).
            celulas.XXXXXXXXXXXXXXXXXXXX;
        }

        return celulas;
    }

    // O MÉTODO QUE VOCÊ VAI IMPLEMENTAR AGORA
    private String calcularChaveCelula(double lat, double lon){
        
        // 1. Arredondar a Latitude
        double latChao = Math.floor(lat); 
        
        // 2. Arredondar a Longitude
        double lonChao = Math.floor(lon);
        
        // 3. Formatar a chave: "LAT_LON"
        return latChao + "_" + lonChao;
    }
}