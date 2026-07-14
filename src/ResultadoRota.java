public class ResultadoRota {

    public String algoritmo;
    public int regiao;

    public double distanciaTotal;
    public double densidadeTotal;
    public double eficiencia;

    public double tempoExecucao;

    public ResultadoRota(String algoritmo, int regiao,
                         double distanciaTotal,
                         double densidadeTotal,
                         double tempoExecucao) {

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
            "%s | Região %d | Dist=%.2f | Dens=%.2f | Eff=%.6f | Tempo=%.2f ms\n",
            algoritmo, regiao,
            distanciaTotal,
            densidadeTotal,
            eficiencia,
            tempoExecucao 
        );
    }

    public double getEficiencia() {
        if (distanciaTotal == 0) return 0;
        return densidadeTotal / distanciaTotal;
    }
}
