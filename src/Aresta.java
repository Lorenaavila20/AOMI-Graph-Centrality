public class Aresta {
    private String destino;
    private double peso;       // custo híbrido
    private double distancia;  // distância real

    public Aresta(String destino, double distancia, double peso) {
        this.destino = destino;
        this.distancia = distancia;
        this.peso = peso;
    }

    public String getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }

    public double getDistancia() {
        return distancia;
    }
}