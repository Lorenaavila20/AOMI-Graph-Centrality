# AOMI-Graph-Centrality
## Modelagem e Otimização de Rotas de Coleta de Microplásticos com Teoria dos Grafos

[![Linguagem](https://img.shields.io/badge/Linguagem-Java-orange.svg)](https://www.java.com/)
[![Disciplina](https://img.shields.io/badge/Disciplina-AEDSII-blue.svg)]()
[![Licença](https://img.shields.io/badge/Licença-MIT-yellow.svg)](LICENSE)

---

## 📋 Resumo Executivo

Este repositório implementa computacionalmente a pesquisa:

> **"Modelagem e Otimização de Rotas de Navios Coletores de Microplásticos em Grandes Massas Oceânicas Utilizando Teoria dos Grafos e Algoritmos de Caminho Mínimo"**

A solução integra **Teoria dos Grafos**, quatro estratégias de roteamento (**Dijkstra Simples**, **Dijkstra Híbrido** multiobjetivo custo-densidade, **A\*** com heurística geodésica, e refinamento por **busca local 2-opt**), classificação regional por bacia oceânica, e dados reais da base **AOMI (Atlas of Ocean Microplastics)**, mantida pelo Ministério do Meio Ambiente do Japão.

**Achado Principal:** A Região 4 (Índico) concentra **55,7% da carga total** de microplásticos observada, seguida pela Região 7 — Atlântico Sul (24,3%). O A\* iguala ou supera a eficiência do Híbrido na maioria das regiões rodando em ~1/15 do tempo; após refinamento por 2-opt, o **Híbrido+2opt** passa a ser o mais eficiente na maioria das regiões (inclusive no Índico), enquanto o **A\*+2opt** chega a 97% dessa eficiência gastando ~12× menos tempo total.

---

## 🎯 Objetivos da Pesquisa

1. **Modelar a distribuição global de microplásticos** como grafo ponderado discretizado em células $1° \times 1°$;
2. **Identificar bacias oceânicas prioritárias** através de classificação geográfica regional;
3. **Otimizar rotas** considerando simultaneamente distância geográfica e densidade de poluentes, comparando estratégias de busca distintas;
4. **Refinar as rotas construídas** com busca local, medindo o ganho de cada estratégia de construção.

---

## 🧠 Fundamentação Teórica

### Representação Espacial e Grafo

- **Discretização:** Oceano modelado como grid de células geográficas $1° \times 1°$, agregando amostras por média de densidade e centroide;
- **Vértices:** Células com dados válidos de concentração de microplásticos (3.300 células não vazias);
- **Arestas:** Conectam pares de células cuja distância de Haversine entre centroides seja ≤ 800 km; células que ficariam isoladas por esse limiar são conectadas ao vizinho mais próximo, garantindo conectividade total do grafo;
- **Pesos:** Função de custo híbrida custo-densidade, calculada em `DijkstraHibrido.java` e reaproveitada por `AStar.java`.

### Função de Custo Híbrida

$$c(u,v) = \frac{\alpha \cdot d(u,v)}{1 + \beta \cdot \rho(v)}$$

| Parâmetro | Descrição | Valor adotado |
|-----------|-----------|----------------|
| $d(u,v)$ | Distância geográfica (Haversine, WGS-84) em km | — |
| $\rho(v)$ | Densidade média de microplásticos na célula destino | — |
| $\alpha$ | Peso do custo de deslocamento | 1,0 |
| $\beta$ | Peso do benefício de coleta | 0,5 (Híbrido/A\*) / 0 (Simples) |

### Classificação Regional

As 3.300 células são agrupadas em **9 regiões oceânicas** por **caixas fixas de latitude/longitude** (Atlântico Norte/Sul, Pacífico Norte/Sul/Equatorial, Índico, Mediterrâneo, Mar da China, e "Outras Regiões" para o que sobra). **Isto não é detecção de comunidades por modularidade (Louvain)** — ver a seção "Por que não Louvain?" abaixo para a justificativa dessa escolha.

### Algoritmos de Roteamento Implementados

| Algoritmo | Propósito | Parâmetros | Referência |
|-----------|-----------|------------|------------|
| **Dijkstra Simples** | Caminho de menor distância pura | $\beta=0$ | Dijkstra, 1959 |
| **Dijkstra Híbrido** | Caminho de menor custo multiobjetivo | $\alpha=1$, $\beta=0,5$ | Dijkstra, 1959 |
| **A\*** | Busca ponto-a-ponto com heurística Haversine, mesma função de custo do Híbrido | $\alpha=1$, $\beta=0,5$ | Hart, Nilsson & Raphael, 1968 |
| **Busca local 2-opt** | Refinamento de pós-processamento: reordena a rota já construída para reduzir distância, sem alterar quais células são visitadas | — | Croes, 1958 |

Os três primeiros constroem a rota de cobertura por expansão gulosa; o 2-opt é aplicado depois, sobre a saída de cada um dos três.

---

## 📊 Resultados Principais

### Distribuição de Densidade por Região

| Rank | Região (ID) | Nome | Células | Densidade Total (p/m³) | Participação |
|------|-------------|------|---------|--------------------------|---------------|
| 1 | 4 | Índico | 461 | 1.114,42 | 55,7% |
| 2 | 7 | Atlântico Sul | 278 | 485,89 | 24,3% |
| 3 | 2 | Pacífico Equatorial | 602 | 163,98 | 8,2% |
| 4 | 0 | Outras/não classificadas | 108 | 133,04 | 6,7% |
| 5 | 8 | Pacífico Sul | 674 | 52,42 | 2,6% |
| 6 | 6 | Mediterrâneo | 164 | 19,73 | 1,0% |
| 7 | 5 | Mar da China | 146 | 19,47 | 1,0% |
| 8 | 3 | Atlântico Norte | 570 | 6,98 | 0,3% |
| 9 | 1 | Pacífico Norte | 297 | 3,26 | 0,2% |
| — | — | **TOTAL** | **3.300** | **1.999,22** | **100%** |

### Comparação Agregada — Antes do Refinamento (9 regiões)

| Métrica | Dijkstra Simples | Dijkstra Híbrido | A\* |
|---------|-------------------|--------------------|------|
| Distância média (km) | 74.686 | 73.530 | 73.484 |
| Eficiência média (densidade/km) | 0,003543 | 0,004060 | **0,004133** |
| Regiões com maior eficiência (de 9) | 2 | 2 | **5** |
| Tempo médio de execução (ms) | ≈3.569 | ≈3.625 | **≈239** |

### Ganho do Refinamento 2-opt (9 regiões)

| Estratégia | Distância antes (km) | Distância depois (km) | Redução |
|------------|------------------------|--------------------------|---------|
| Simples + 2-opt | 74.686 | 63.793 | 14,6% |
| **Híbrido + 2-opt** | 73.530 | 58.466 | **20,5%** |
| A\* + 2-opt | 73.484 | 63.136 | 14,1% |

O 2-opt reduz distância em **todas as 9 regiões, para os 3 algoritmos**, sem mudar a densidade coletada (ele só reordena, não adiciona nem remove células). O ganho é maior no Híbrido porque sua construção gulosa, guiada por densidade, cria mais desvios ineficientes — exatamente o tipo de coisa que o 2-opt corrige.

**O refinamento muda qual estratégia é a melhor:** antes do 2-opt, o A\* sozinho vencia em eficiência 5 das 9 regiões. Depois do refinamento, é o **Híbrido+2opt** que vence 5 das 9 — inclusive a Região 4 (Índico), a mais importante. Mas o **A\*+2opt** chega a 97% dessa eficiência no Índico (0,019871 vs. 0,020460) gastando cerca de **12× menos tempo total** de construção+refinamento (≈429 ms vs. ≈5.183 ms). Onde tempo de computação importa, A\*+2opt é a escolha mais prática; onde só a qualidade da rota importa, Híbrido+2opt é marginalmente melhor.

---

## ❓ Por que não Louvain?

A ideia inicial do projeto era usar detecção de comunidades via Louvain — é por isso que versões anteriores deste README e do artigo associado mencionavam o algoritmo. Ao longo do desenvolvimento, a decisão foi manter a **classificação geográfica por caixas fixas de lat/lon** em vez disso, por alguns motivos:

1. **O grafo já é geograficamente restrito.** Arestas só existem entre células a até 800 km, então Louvain tenderia a redescobrir agrupamentos parecidos com os que a classificação por bacia já dá — sem nome de bacia oceânica, exigindo inferência manual pra rotular cada comunidade encontrada.
2. **Interpretabilidade operacional.** "Mande o navio pro Índico" é uma decisão que um operador entende e executa. Uma comunidade descoberta por modularidade pode não corresponder a nenhuma bacia reconhecível.
3. **Reprodutibilidade.** O modelo atual é determinístico (mesma entrada → mesma saída, sempre). Louvain tem uma etapa de otimização sensível à ordem/inicialização; sem fixar seed com cuidado, duas execuções podem convergir para agrupamentos ligeiramente diferentes.
4. **Custo de retrabalho.** Trocar a classificação muda todas as 9 regiões, o que exigiria recalcular todas as tabelas, figuras e análises deste README e do artigo associado.

Louvain continua sendo uma extensão futura legítima (ver abaixo) — a decisão é sobre fazer isso agora vs. depois, não sobre a técnica em si.

---

## 🧩 Arquitetura do Código

### Componentes Principais

```
┌─────────────────────────────────────────────────────────────┐
│                        Main.java                              │
│  (Orquestrador do pipeline experimental)                      │
└────────────────┬────────────────────────────────────────────┘
                  │
    ┌─────────────┼─────────────┬─────────────┬─────────────┐
    ▼             ▼             ▼             ▼             ▼
┌──────────┐ ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌──────────┐
│Leitor    │ │PreProc   │ │Detector   │ │Dijkstra  │ │AStar     │
│Dados     │ │essador   │ │Comunidades│ │Hibrido   │ │          │
└────┬─────┘ └────┬─────┘ └────┬──────┘ └────┬─────┘ └────┬─────┘
     │            │            │             │            │
     └────────────┼────────────┴─────────────┴────────────┘
                  ▼
         ┌────────────────────┐
         │ OtimizadorRotas     │
         │ (Simples/Híbrido/  │
         │  A* + estatísticas) │
         └────────┬───────────┘
                  │
         ┌────────┴───────────┐
         ▼                    ▼
┌──────────────────┐ ┌────────────────────┐
│ BuscaLocal2Opt    │ │ GeradorRelatorio    │
│ (refinamento)     │ │ (comparação, CSV)   │
└───────────────────┘ └─────────────────────┘
```

### Descrição dos Módulos

| Classe | Responsabilidade | Dependências |
|--------|-------------------|--------------|
| `Main.java` | Orquestra pipeline: leitura → pré-processamento → grafo → classificação regional → rotas (3 algoritmos + 2-opt) → relatórios | Todas |
| `LeitorDeDados.java` | Leitura, validação e filtragem de `data/survey_data.csv` (AOMI) | `AmostraPonto.java` |
| `PreProcessador.java` | Agregação espacial em células $1° \times 1°$; cálculo de densidade média e centroides | `AmostraPonto.java` |
| `AmostraPonto.java` | Modelo de domínio para amostras individuais | Nenhuma |
| `Grafo.java` | Construção do grafo por limiar de 800 km (Haversine) com garantia de conectividade | `Aresta.java` |
| `Aresta.java` | Estrutura de dados da aresta (destino, distância, peso) | Nenhuma |
| `DetectorComunidades.java` | Classificação regional por caixas fixas de lat/lon | `Grafo.java` |
| `DijkstraHibrido.java` | Dijkstra com custo multiobjetivo; usado tanto para Simples ($\beta=0$) quanto Híbrido ($\beta=0,5$) | `Grafo.java`, `Aresta.java` |
| `AStar.java` | Busca A\* ponto-a-ponto com heurística Haversine; conta nós explorados | `Grafo.java`, `GeoUtils.java` |
| `BuscaLocal2Opt.java` | Refinamento 2-opt: reordena rota já construída para reduzir distância | `GeoUtils.java` |
| `GeoUtils.java` | Cálculo de distância Haversine (WGS-84) | Nenhuma |
| `OtimizadorRotas.java` | Construção gulosa de rotas por região para os 3 algoritmos; orquestra chamada ao 2-opt; cálculo de estatísticas | `DijkstraHibrido.java`, `AStar.java`, `BuscaLocal2Opt.java` |
| `GeradorRelatorio.java` | Relatório comparativo dos 3 algoritmos; exportação CSV para gráficos | Todas as estatísticas |
| `ResultadoRota.java` | Registro de resultado por algoritmo/região para o ranking final | Nenhuma |
| `DijkstraSimples.java` | Implementação alternativa de Dijkstra simples; **não utilizada no fluxo atual** (o "Simples" ativo é `DijkstraHibrido` com $\beta=0$) | `Grafo.java` |

---

## 📁 Estrutura do Projeto

```
AOMI-GRAPH-CENTRALITY/
├── data/
│   └── survey_data.csv
│
├── src/
│   ├── AmostraPonto.java
│   ├── Aresta.java
│   ├── AStar.java
│   ├── BuscaLocal2Opt.java
│   ├── DetectorComunidades.java
│   ├── DijkstraHibrido.java
│   ├── DijkstraSimples.java            # não usado no fluxo atual
│   ├── GeoUtils.java
│   ├── Grafo.java
│   ├── GeradorRelatorio.java
│   ├── LeitorDeDados.java
│   ├── Main.java
│   ├── OtimizadorRotas.java
│   ├── PreProcessador.java
│   └── ResultadoRota.java
│
├── routes/                              # rotas de cobertura por região (CSV)
├── graficos/                            # dados exportados para plotagem
├── .gitignore
├── LICENSE
└── README.md
```

---

## ⚙️ Requisitos e Instalação

### Pré-requisitos

- **Java Development Kit (JDK) 8+** instalado e configurado no `PATH`
- Sistema operacional: Windows, macOS ou Linux

### Compilação

A partir da raiz do projeto:

```bash
javac src/*.java -d out
```

### Execução

```bash
java -cp out Main
```

Gera `relatorio_analise_microplasticos.txt`, `routes/*.csv`, `graficos/comparacao_algoritmos.csv`, `graficos/comparacao_2opt.csv`, e `grafo_nodes.csv`/`grafo_edges.csv` para Gephi.

---

## 🔬 Reprodutibilidade Científica

### Parâmetros Fixos

- **Discretização espacial:** células de $1° \times 1°$ latitude/longitude
- **Limiar de conectividade do grafo:** 800 km (Haversine), com fallback ao vizinho mais próximo
- **Parâmetros da função de custo:** $\alpha=1,0$; $\beta=0,5$ (Híbrido/A\*) ou $\beta=0$ (Simples)
- **Classificação regional:** caixas fixas de lat/lon por bacia oceânica (ver "Por que não Louvain?")
- **Métrica de distância:** Haversine (WGS-84)

### Reprodução

1. Obtenha os dados de monitoramento da base AOMI oficial (https://aomi.env.go.jp/)
2. Salve como `data/survey_data.csv`
3. Compile e execute conforme a seção acima
4. Compare `relatorio_analise_microplasticos.txt` e `graficos/*.csv` com as tabelas deste README

Pequenos desvios (<0,1%) são esperados por arredondamento em ponto flutuante.

---

## 🚧 Limitações do Modelo Atual

1. **Classificação regional geográfica, não topológica** (ver "Por que não Louvain?").
2. **Roteamento sobre o grafo completo:** os algoritmos buscam no grafo inteiro (3.300 nós) mesmo ao cobrir uma única região, causando "vazamento" de densidade entre regiões vizinhas e custo computacional extra para o A\* em regiões dispersas (ex.: Mar da China, 134.380 nós explorados para 146 células).
3. **Estático:** não incorpora dinâmica temporal de correntes oceânicas ou variabilidade sazonal.
4. **Capacidade infinita:** não modela restrições realísticas (carga máxima, autonomia, múltiplos navios).
5. **Sem métricas de centralidade:** apesar do nome do repositório, nenhuma métrica de centralidade (grau, betweenness) é calculada atualmente.

---

## 🔮 Extensões Futuras

### Curto prazo

- Restringir a busca do A\* a um subgrafo induzido pela região-alvo, eliminando o custo excessivo observado no Mar da China.
- Análise de sensibilidade do parâmetro $\beta$ da função de custo.

### Médio prazo

- Implementação real de detecção de comunidades por modularidade (Louvain), substituindo a classificação por caixas fixas — ver justificativa da escolha atual acima.
- Cálculo de métricas de centralidade (grau, betweenness) sobre o grafo, alinhando o projeto ao seu próprio nome.
- Enriquecimento dos nós com atributos ambientais adicionais (correntes marinhas, profundidade, temperatura, distância da costa).

### Longo prazo

- Roteamento multi-frota com restrições de capacidade e autonomia.
- Validação empírica com dados reais de embarcações coletoras.

---

## 📚 Referências Científicas

- **[Dijkstra, 1959]** Dijkstra, E. W. "A note on two problems in connexion with graphs." *Numerische Mathematik*, vol. 1, no. 1, pp. 269–271.
- **[Hart, Nilsson & Raphael, 1968]** Hart, P. E., Nilsson, N. J., Raphael, B. "A Formal Basis for the Heuristic Determination of Minimum Cost Paths." *IEEE Transactions on Systems Science and Cybernetics*, vol. 4, no. 2, pp. 100–107.
- **[Croes, 1958]** Croes, G. A. "A Method for Solving Traveling-Salesman Problems." *Operations Research*, vol. 6, no. 6, pp. 791–812.
- **[Blondel et al., 2008]** Blondel, V. D., et al. "Fast unfolding of communities in large networks." *Journal of Statistical Mechanics: Theory and Experiment*, vol. 2008, no. 10, p. P10008. *(referência para a extensão futura de detecção de comunidades — não implementada neste repositório; ver "Por que não Louvain?")*
- **[Jambeck et al., 2015]** Jambeck, J. R., et al. "Plastic waste inputs from land into the ocean." *Science*, vol. 347, no. 6223, pp. 768–771.

---

## 👩‍💻 Autoria e Contribuições

**Autora Principal:** Lorena Ávila
**Instituição:** Engenharia de Computação — CEFET-MG
**Contato:** lorenaavila15@outlook.com

Para contribuições, issues ou pull requests, abra uma issue no repositório GitHub.

---

## 📄 Licença

Este projeto está licenciado sob a **Licença MIT** — veja `LICENSE` para detalhes completos.

---

## 🔗 Links Úteis

- **Repositório GitHub:** https://github.com/Lorenaavila20/AOMI-Graph-Centrality
- **Base de Dados AOMI:** https://aomi.env.go.jp/
- **Documentação Java:** https://docs.oracle.com/javase/
