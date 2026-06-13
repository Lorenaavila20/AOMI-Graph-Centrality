public class ResultadoRota {

    public String algoritmo;
    public int regiao;

    public double distanciaTotal;
    public double densidadeTotal;
    public double eficiencia;

    public long tempoExecucao;

    public ResultadoRota(String algoritmo, int regiao,
                         double distanciaTotal,
                         double densidadeTotal,
                         long tempoExecucao) {

        this.algoritmo = algoritmo;
        this.regiao = regiao;
        this.distanciaTotal = distanciaTotal;
        this.densidadeTotal = densidadeTotal;
        this.tempoExecucao = tempoExecucao;

        this.eficiencia = (distanciaTotal == 0)
                ? 0
                : densidadeTotal / distanciaTotal;
    }

    public void imprimir() {
        System.out.printf(
            "%s | Região %d | Dist=%.2f | Dens=%.2f | Eff=%.6f | Tempo=%d ms\n",
            algoritmo, regiao,
            distanciaTotal,
            densidadeTotal,
            eficiencia,
            tempoExecucao / 1_000_000
        );
    }
}
