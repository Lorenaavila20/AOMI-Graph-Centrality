import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class LeitorDeDados {
    
    public List<AmostraPonto> lerAmostras(String caminho) {
        List<AmostraPonto> amostras = new ArrayList<>();
        int linhasProcessadas = 0;
        int linhasValidas = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                
                // Ignora linhas vazias
                if (linha.isEmpty()) {
                    continue;
                }
                
                // Ignora TODAS as linhas que começam com #
                if (linha.startsWith("#")) {
                    continue;
                }
                
                // Ignora a linha de cabeçalho (começa com "0001:Data ID")
                if (linha.startsWith("0001:")) {
                    System.out.println("Cabeçalho encontrado, pulando...");
                    continue;
                }
                
                linhasProcessadas++;
                
                // Split por vírgula, mas precisa tratar aspas
                String[] partes = linha.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
                
                // Verifica se tem colunas suficientes
                if (partes.length < 193) {
                    System.out.println("Linha " + linhasProcessadas + " ignorada: colunas insuficientes (" + partes.length + ")");
                    continue;
                }
                
                try {
                    // Coluna 45: GPS_Lat Int (índice 44)
                    // Coluna 47: GPS_Lon Int (índice 46)
                    // Coluna 192: Particle density_m3 (d<5mm) (índice 191)
                    
                    String latStr = partes[44].replace("\"", "").trim();
                    String lonStr = partes[46].replace("\"", "").trim();
                    String densStr = partes[191].replace("\"", "").trim();
                    
                    // Ignora se algum campo estiver vazio
                    if (latStr.isEmpty() || lonStr.isEmpty() || densStr.isEmpty()) {
                        continue;
                    }
                    
                    double lat = Double.parseDouble(latStr);
                    double lon = Double.parseDouble(lonStr);
                    double dens = Double.parseDouble(densStr);
                    
                    // Validação básica
                    if (lat < -90 || lat > 90 || lon < -180 || lon > 180 || dens < 0) {
                        continue;
                    }
                    
                    amostras.add(new AmostraPonto(lat, lon, dens));
                    linhasValidas++;
                    
                    // Debug: mostra as primeiras 5 amostras
                    if (linhasValidas <= 5) {
                        System.out.println("Amostra " + linhasValidas + ": Lat=" + lat + ", Lon=" + lon + ", Dens=" + dens);
                    }
                    
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    // Ignora linhas com formato inválido
                    continue;
                }
            }
            
            System.out.println("\n=== RESUMO DA LEITURA ===");
            System.out.println("Linhas processadas (dados): " + linhasProcessadas);
            System.out.println("Amostras válidas carregadas: " + linhasValidas);
            
        } catch (Exception e) {
            System.err.println("ERRO ao ler arquivo: " + e.getMessage());
            e.printStackTrace();
        }
        
        return amostras;
    }
}