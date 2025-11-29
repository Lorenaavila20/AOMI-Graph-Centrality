import java.util.*;

public class Grafo {

    private Map<String, Double> nos; // densidades médias
    private Map<String, List<Aresta>> adjacencia;

    private static final double CELL = 110.0;

    public Grafo(Map<String, Double> densidadesMedias) {
        this.nos = densidadesMedias;
        this.adjacencia = new HashMap<>();
        construirArestas();
    }

    private void construirArestas() {

        for (String chaveAtual : nos.keySet()) {

            double[] c = parseChave(chaveAtual);
            int cy = (int) c[0];
            int cx = (int) c[1];

            adjacencia.putIfAbsent(chaveAtual, new ArrayList<>());

            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {

                    if (dy == 0 && dx == 0) continue;

                    String viz = (cy + dy) + "_" + (cx + dx);

                    if (nos.containsKey(viz)) {

                        double peso;
                        if (dy == 0 || dx == 0) {
                            peso = CELL;
                        } else {
                            peso = CELL * Math.sqrt(2);
                        }

                        adjacencia.get(chaveAtual).add(new Aresta(viz, peso));
                    }
                }
            }
        }
    }

    private double[] parseChave(String chave) {
        String[] p = chave.split("_");
        return new double[]{Double.parseDouble(p[0]), Double.parseDouble(p[1])};
    }

    public Map<String, List<Aresta>> getAdjacencia() { return adjacencia; }
    public Map<String, Double> getNos() { return nos; }
}
