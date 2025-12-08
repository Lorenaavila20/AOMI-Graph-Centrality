public class Aresta {
    private String destino;
    private double peso;

    public Aresta(String destino, double peso) {
        this.destino = destino;
        this.peso = peso;
    }

    public String getDestino() {
        return destino;
    }

    public double getPeso() {
        return peso;
    }
}