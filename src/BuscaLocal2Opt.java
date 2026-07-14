import java.util.*;

/**
 * Busca local 2-opt para refinamento de rotas.
 *
 * Reordena uma sequência de células já visitadas para reduzir a distância
 * total percorrida. Não adiciona nem remove nenhuma célula — por isso a
 * densidade total coletada não muda, só a distância. Como a densidade é
 * fixa, minimizar distância aqui é exatamente equivalente a maximizar a
 * eficiência (densidade/km) da rota.
 *
 * Trabalha sobre a rota DEDUPLICADA (cada célula uma vez, na ordem da
 * primeira visita), usando distância direta (haversine) entre células
 * consecutivas.
 *
 * Referência clássica: Croes, G. A. (1958). "A method for solving
 * traveling-salesman problems." Operations Research, 6(6), 791-812.
 */
public class BuscaLocal2Opt {

    private static final int MAX_PASSADAS = 200; // proteção contra loop excessivamente longo

    /**
     * @param rotaOriginal rota de entrada (pode ter nós repetidos, ex: saída
     *                     direta de OtimizadorRotas.calcularRotaCobertura,
     *                     que inclui nós intermediários do caminho reconstruído)
     * @param centroides   mapa global de centroides (lat, lon) por célula
     * @return nova rota, mesmas células únicas, ordem otimizada
     */
    public static List<String> aplicar(List<String> rotaOriginal, Map<String, double[]> centroides) {
        List<String> rota = removerDuplicatas(rotaOriginal);
        int n = rota.size();

        if (n < 4) return rota; // não há troca 2-opt não trivial possível

        boolean melhorou = true;
        int passadas = 0;

        while (melhorou && passadas < MAX_PASSADAS) {
            melhorou = false;
            passadas++;

            for (int i = 0; i <= n - 3; i++) {
                for (int j = i + 2; j <= n - 2; j++) {

                    String a = rota.get(i);
                    String b = rota.get(i + 1);
                    String c = rota.get(j);
                    String d = rota.get(j + 1);

                    double custoAtual = distancia(a, b, centroides) + distancia(c, d, centroides);
                    double custoNovo  = distancia(a, c, centroides) + distancia(b, d, centroides);

                    if (custoNovo < custoAtual - 1e-9) {
                        Collections.reverse(rota.subList(i + 1, j + 1));
                        melhorou = true;
                    }
                }
            }
        }

        return rota;
    }

    private static double distancia(String aKey, String bKey, Map<String, double[]> centroides) {
        double[] a = centroides.get(aKey);
        double[] b = centroides.get(bKey);
        if (a == null || b == null) return 0.0;
        return GeoUtils.haversineKm(a[0], a[1], b[0], b[1]);
    }

    private static List<String> removerDuplicatas(List<String> rota) {
        LinkedHashSet<String> unicos = new LinkedHashSet<>(rota);
        return new ArrayList<>(unicos);
    }
}