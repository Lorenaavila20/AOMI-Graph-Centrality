import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException; // Para tratar erros específicos do CSV
import java.io.FileReader;    // Para abrir o arquivo no sistema
import java.io.IOException;     // Para tratar erros de Input/Output (E/S)
import java.util.ArrayList;   // Para criar a lista de amostras
import java.util.List;        // Para usar a interface List


public class LeitorDeDados{

    public List<AmostraPonto> lerAmostras(String caminhoArquivo){
        
        List<AmostraPonto> amostras = new ArrayList<>(); 
        
        try (FileReader fileReader = new FileReader(caminhoArquivo);

            CSVReader reader = new CSVReaderBuilder(fileReader).build()){ 

            // 'linha' irá armazenar o array de strings de cada linha lida
            String[] linha;  
    
            boolean lendoMetadados = true;             
            
            // Loop para pular o cabeçalho (Metadados # + Linha de Nomes)
            while (lendoMetadados && (linha = reader.readNext()) != null){

                if (linha[0].startsWith("#")){
                    continue;
                    
                } else{
                    lendoMetadados = false;
                }
            }
            
            while ((linha = reader.readNext()) != null){
                
                try{
                    // 1. EXTRAIR OS DADOS e converter para double
                    double lat = Double.parseDouble(linha[44]);
                    
                    double lon = Double.parseDouble(linha[46]);
                
                    double densidade = Double.parseDouble(linha[193]);
                    
                    // 2. CRIAR O OBJETO E ARMAZENAR
                    AmostraPonto amostra = new AmostraPonto(lat, lon, densidade);
                    amostras.add(amostra);

                } catch (NumberFormatException e) {
                    // Ignora a linha com erro de número
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Ignora a linha que não tem todas as 193 colunas (opcional, mas bom)
                }
            }
                
    
        } catch (IOException | CsvException e){ 
            // Corrigido para IOException
            System.err.println("Erro ao ler o arquivo CSV: " + e.getMessage());
        }
        
        return amostras;
    }
}