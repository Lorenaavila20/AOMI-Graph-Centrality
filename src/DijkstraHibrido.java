import java.util.*;

public class DijkstraHibrido {
    
    /**
     * Dijkstra otimizado para COLETA de microplásticos.
     * 
     * Objetivo: Encontrar rota que MAXIMIZA coleta e MINIMIZA distância.
     * 
     * Custo = distância / (1 + k * densidade)
     * 
     * - Alta densidade -> custo MENOR (rota preferencial)
     * - Baixa densidade -> custo MAIOR (evita)
     * - k: fator de peso da densidade (quanto maior, mais prioriza densidade)
     * 
     * Exemplo com k=1:
     * - Célula com densidade 10: custo = 100km / (1 + 1*10) = 9.09km
     * - Célula com densidade 1:  custo = 100km / (1 + 1*1)  = 50km
     * - Célula com densidade 0:  custo = 100km / 1 = 100km
     */
    public static Map<String, String> dijkstra(
        Grafo grafo,
        String origem,
        String destino,
        double k
    ) {
        Map<String, Double> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        Map<String, Double> densidadeColetada = new HashMap<>(); // NOVO: tracking de coleta
        Map<String, List<Aresta>> adj = grafo.getAdjacencia();
        Map<String, Double> dens = grafo.getNos();
        Set<String> visitados = new HashSet<>();
        
        for (String node : dens.keySet()) {
            dist.put(node, Double.POSITIVE_INFINITY);
            prev.put(node, null);
            densidadeColetada.put(node, 0.0);
        }
        dist.put(origem, 0.0);
        
        PriorityQueue<String> pq = new PriorityQueue<>(
            Comparator.comparingDouble(dist::get)
        );
        pq.add(origem);
        
        final double EPS = 1e-6;
        
        while (!pq.isEmpty()) {
            String u = pq.poll();
            
            if (visitados.contains(u)) continue;
            visitados.add(u);
            
            if (u.equals(destino)) break;
            
            double du = dist.get(u);
            if (du == Double.POSITIVE_INFINITY) break;
            
            for (Aresta e : adj.getOrDefault(u, Collections.emptyList())) {
                String v = e.getDestino();
                
                if (visitados.contains(v)) continue;
                
                double distanciaKm = e.getPeso();
                double densV = dens.getOrDefault(v, 0.0);
                
                // NOVO CÁLCULO: Prioriza alta densidade
                // Custo diminui com densidade alta
                double fatorDensidade = 1.0 + (k * densV);
                double custo = distanciaKm / fatorDensidade;
                
                if (custo < EPS) custo = EPS;
                
                double alt = du + custo;
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    densidadeColetada.put(v, densidadeColetada.get(u) + densV);
                    pq.add(v);
                }
            }
        }
        
        // Imprime estatísticas da rota encontrada
        if (prev.get(destino) != null || origem.equals(destino)) {
            System.out.println("\n=== ESTATÍSTICAS DA ROTA ===");
            System.out.println("Custo total (ajustado): " + String.format("%.2f", dist.get(destino)));
            System.out.println("Densidade coletada: " + String.format("%.4f", densidadeColetada.get(destino)));
        }
        
        return prev;
    }
}