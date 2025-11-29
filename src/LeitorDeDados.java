import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class LeitorDeDados {

    public List<AmostraPonto> lerAmostras(String caminho) {
        List<AmostraPonto> amostras = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {

            String linha;
            boolean primeira = true;

            while ((linha = br.readLine()) != null) {

                linha = linha.trim();

                // Ignora comentários
                if (linha.startsWith("#") || linha.isEmpty()) {
                    continue;
                }

                // Ignora o cabeçalho
                if (primeira) {
                    primeira = false;
                    continue;
                }

                String[] partes = linha.split(",");

                if (partes.length < 3) continue;

                try {
                    double lat = Double.parseDouble(partes[0]);
                    double lon = Double.parseDouble(partes[1]);
                    double dens = Double.parseDouble(partes[2]);

                    amostras.add(new AmostraPonto(lat, lon, dens));

                } catch (NumberFormatException e) {
                    // Se tiver alguma linha com texto perdido, só pula
                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return amostras;
    }
}
