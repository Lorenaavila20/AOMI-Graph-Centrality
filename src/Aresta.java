public class Aresta{
    // O ID do nó de destino
    private String destino;
    
    // O peso da aresta (a distância em quilômetros)
    private double peso;

    public Aresta(String destino, double peso){
        this.destino = destino;
        this.peso = peso;
    }

    // Getters
    public String getDestino(){
        return destino;
    }

    public double getPeso(){
        return peso;
    }
}