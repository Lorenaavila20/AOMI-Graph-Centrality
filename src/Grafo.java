import java.util.*;

public class Grafo {

    private Map<String, Double> nos;
    private Map<String, List<Aresta>> adjacencia;
    private Map<String, double[]> centroides;

    public Grafo(Map<String, Double> densidadesMedias,
                 Map<String, double[]> centroides) {
        this.nos = densidadesMedias;
        this.centroides = centroides;
        this.adjacencia = new HashMap<>();
        construirArestas();
    }

    private void construirArestas() {

        final double LIMITE_KM = 800; // ajuste fino aqui (600–1200 ideal)
    
        for (String chaveAtual : nos.keySet()) {
    
            adjacencia.putIfAbsent(chaveAtual, new ArrayList<>());
    
            double[] a = centroides.get(chaveAtual);
            if (a == null) continue;
    
            List<Aresta> vizinhos = new ArrayList<>();
    
            for (String viz : nos.keySet()) {
    
                if (viz.equals(chaveAtual)) continue;
    
                double[] b = centroides.get(viz);
                if (b == null) continue;
    
                double distancia = haversine(a[0], a[1], b[0], b[1]);
    
                // conecta só se estiver dentro do raio
                if (distancia <= LIMITE_KM) {
                    vizinhos.add(new Aresta(viz, distancia, distancia));
                }
            }
    
            //  GARANTIA DE CONECTIVIDADE 
            if (vizinhos.isEmpty()) {
    
                String maisProximo = null;
                double menorDist = Double.POSITIVE_INFINITY;
    
                for (String viz : nos.keySet()) {
    
                    if (viz.equals(chaveAtual)) continue;
    
                    double[] b = centroides.get(viz);
                    if (b == null) continue;
    
                    double distancia = haversine(a[0], a[1], b[0], b[1]);
    
                    if (distancia < menorDist) {
                        menorDist = distancia;
                        maisProximo = viz;
                    }
                }
    
                if (maisProximo != null) {
                    vizinhos.add(new Aresta(maisProximo, menorDist, menorDist));
                }
            }
    
            adjacencia.put(chaveAtual, vizinhos);
        }
    }

    private double haversine(double lat1, double lon1,
                             double lat2, double lon2) {

        final double R = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    public Map<String, List<Aresta>> getAdjacencia() {
        return adjacencia;
    }

    public Map<String, Double> getNos() {
        return nos;
    }
}