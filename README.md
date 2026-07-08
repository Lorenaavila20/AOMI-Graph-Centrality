# AOMI-Graph-Centrality
## Modelagem e Otimização de Rotas de Coleta de Microplásticos com Teoria dos Grafos

[![Linguagem](https://img.shields.io/badge/Linguagem-Java-orange.svg)](https://www.java.com/)
[![Licença](https://img.shields.io/badge/Licença-MIT-yellow.svg)](LICENSE)

---

## 📋 Resumo Executivo

Este repositório implementa computacionalmente a pesquisa:

> **"Modelagem e Otimização de Rotas de Navios Coletores de Microplásticos em Grandes Massas Oceânicas Utilizando Teoria dos Grafos e Algoritmos de Caminho Mínimo"**

A solução integra **Teoria dos Grafos**, três estratégias de roteamento (**Dijkstra Simples**, **Dijkstra Híbrido** multiobjetivo custo-densidade, e **A\*** com heurística geodésica), classificação regional por bacia oceânica, e dados reais da base **AOMI (Atlas of Ocean Microplastics)**, mantida pelo Ministério do Meio Ambiente do Japão, para identificar estratégias eficientes de coleta de poluentes marinhos.

**Achado Principal:** Duas regiões oceânicas (Índico e Atlântico Sul) concentram **80,0% da carga total** de microplásticos observada em apenas **22,4% do território mapeado** (739 de 3.300 células), fundamentando uma alocação de frota altamente desigual. O A\* iguala ou supera a eficiência de coleta do Híbrido na maioria das regiões rodando em cerca de **1/15 do tempo**.

**Em desenvolvimento:** refinamento de rotas por **busca local 2-opt**, aplicada como pós-processamento sobre as rotas já construídas (ver Seção "Extensões em Andamento").

---

## 🎯 Objetivos da Pesquisa

1. **Modelar a distribuição global de microplásticos** como grafo ponderado discretizado em células $1° \times 1°$;
2. **Identificar bacias oceânicas prioritárias** através de classificação geográfica regional;
3. **Otimizar rotas** considerando simultaneamente distância geográfica e densidade de poluentes, comparando estratégias de busca distintas;
4. **Comparar o comportamento computacional e a qualidade de rota** entre Dijkstra clássico, Dijkstra multiobjetivo e busca heurística (A\*).

---

## 🧠 Fundamentação Teórica

### Representação Espacial e Grafo

- **Discretização:** Oceano modelado como grid de células geográficas $1° \times 1°$, agregando amostras individuais por média de densidade e centroide;
- **Vértices:** Células com dados válidos de concentração de microplásticos (3.300 células não vazias);
- **Arestas:** Conectam pares de células cuja distância de Haversine entre centroides seja ≤ 800 km; células que ficariam isoladas por esse limiar são conectadas ao vizinho geograficamente mais próximo, garantindo conectividade total do grafo;
- **Pesos:** Função de custo híbrida custo-densidade para balanceamento multiobjetivo (definida em `DijkstraHibrido.java` e reaproveitada por `AStar.java`, não em `Aresta.java`, que é apenas a estrutura de dados da aresta).

### Função de Custo Híbrida

$$c(u,v) = \frac{\alpha \cdot d(u,v)}{1 + \beta \cdot \rho(v)}$$

| Parâmetro | Descrição | Valor adotado |
|-----------|-----------|----------------|
| $d(u,v)$ | Distância geográfica (Haversine) em km | — |
| $\rho(v)$ | Densidade média de microplásticos na célula destino | — |
| $\alpha$ | Peso do custo de deslocamento | 1,0 |
| $\beta$ | Peso do benefício de coleta | 0,5 (Híbrido e A\*) / 0 (Simples) |

**Interpretação:** com $\beta=0$ o custo se reduz ao Dijkstra clássico por distância; com $\beta=0,5$, células mais densas tornam-se efetivamente "mais próximas", direcionando a rota para regiões poluídas a um custo de distância controlado.

### Classificação Regional

As 3.300 células são agrupadas em **9 regiões oceânicas** por **caixas fixas de latitude/longitude** correspondentes a bacias oceânicas conhecidas (Atlântico Norte/Sul, Pacífico Norte/Sul/Equatorial, Índico, Mediterrâneo, Mar da China, e uma categoria residual "Outras Regiões" para células fora de todas as caixas definidas). **Esta é uma regra geográfica determinística, não detecção de comunidades por modularidade (Louvain)** — a implementação de Louvain real permanece como extensão futura (ver abaixo).

### Algoritmos de Roteamento Implementados

| Algoritmo | Propósito | Parâmetros | Referência |
|-----------|-----------|------------|------------|
| **Dijkstra Simples** | Caminho de menor distância pura | $\beta=0$ | Dijkstra, 1959 |
| **Dijkstra Híbrido** | Caminho de menor custo multiobjetivo (distância + densidade) | $\alpha=1$, $\beta=0,5$ | Dijkstra, 1959 |
| **A\*** | Busca ponto-a-ponto com heurística admissível (Haversine até o destino), mesma função de custo do Híbrido | $\alpha=1$, $\beta=0,5$ | Hart, Nilsson & Raphael, 1968 |

Os três operam sobre o mesmo grafo completo de 3.300 nós; a rota de cobertura de cada região é construída por expansão gulosa (a cada passo, estende-se ao nó não visitado mais barato segundo o algoritmo escolhido).

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

### Comparação Agregada dos Três Algoritmos (9 regiões)

| Métrica | Dijkstra Simples | Dijkstra Híbrido | A\* |
|---------|-------------------|--------------------|------|
| Distância média (km) | 74.686 | 73.530 | 73.484 |
| Eficiência média (densidade/km) | 0,003543 | 0,004060 | **0,004133** |
| Regiões com maior eficiência (de 9) | 2 | 2 | **5** |
| Tempo médio de execução (ms) | ≈3.569 | ≈3.625 | **≈239** |

O A\* é, em média, **≈15× mais rápido** que as duas variantes de Dijkstra (chegando a ≈22× em regiões bem conectadas), pois interrompe a busca assim que atinge o destino, ao contrário do Dijkstra que expande o grafo inteiro a cada passo.

### Limitações Observadas nos Dados

- **"Vazamento" de densidade entre regiões:** como as três estratégias buscam sobre o grafo completo (não um subgrafo restrito à região-alvo), uma rota pode passar por, e contabilizar densidade de, células de regiões vizinhas. No Mar da China (densidade real da região: 19,47), a rota híbrida coleta 163,45 — quase 8,4× mais, por atravessar células densas de regiões adjacentes no caminho.
- **Custo computacional do A\* em regiões dispersas:** no Mar da China, o A\* explora 134.380 nós para cobrir apenas 146 células (ordens de grandeza acima do típico), reduzindo sua vantagem de velocidade nessa região para ≈3×, contra 15–22× nas demais.

---

## 🧩 Arquitetura do Código

### Componentes Principais

```
┌─────────────────────────────────────────────────────────────┐
│                        Main.java                             │
│  (Orquestrador do pipeline experimental)                     │
└────────────────┬──────────────────────────────────────────────┘
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
         │ OtimizadorRotas    │
         │ (Simples/Híbrido/  │
         │  A* + estatísticas)│
         └────────┬───────────┘
                  ▼
         ┌────────────────────┐
         │GeradorRelatorio    │
         │(comparação 3 algs, │
         │ CSV, Gephi)        │
         └────────────────────┘
```

### Descrição dos Módulos

| Classe | Responsabilidade | Dependências |
|--------|-------------------|--------------|
| `Main.java` | Orquestra pipeline: leitura → pré-processamento → grafo → classificação regional → rotas (3 algoritmos) → relatórios | Todas |
| `LeitorDeDados.java` | Leitura, validação e filtragem de `data/survey_data.csv` (AOMI) | `AmostraPonto.java` |
| `PreProcessador.java` | Agregação espacial em células $1° \times 1°$; cálculo de densidade média e centroides | `AmostraPonto.java` |
| `AmostraPonto.java` | Modelo de domínio para amostras individuais | Nenhuma |
| `Grafo.java` | Construção do grafo por limiar de 800 km (Haversine) com garantia de conectividade | `Aresta.java` |
| `Aresta.java` | Estrutura de dados da aresta (destino, distância, peso) | Nenhuma |
| `DetectorComunidades.java` | Classificação regional por caixas fixas de lat/lon; método alternativo de componentes conexos (BFS) não usado no fluxo principal | `Grafo.java` |
| `DijkstraHibrido.java` | Dijkstra com custo multiobjetivo; usado tanto para Simples ($\beta=0$) quanto Híbrido ($\beta=0,5$) | `Grafo.java`, `Aresta.java` |
| `AStar.java` | Busca A\* ponto-a-ponto com heurística Haversine; conta nós explorados | `Grafo.java`, `GeoUtils.java` |
| `GeoUtils.java` | Cálculo de distância Haversine | Nenhuma |
| `OtimizadorRotas.java` | Construção gulosa de rotas de cobertura por região para os 3 algoritmos; cálculo de estatísticas (distância, densidade, eficiência, tempo, nós explorados) | `DijkstraHibrido.java`, `AStar.java` |
| `GeradorRelatorio.java` | Relatório comparativo dos 3 algoritmos; exportação CSV para gráficos | Todas as estatísticas |
| `ResultadoRota.java` | Registro de resultado por algoritmo/região para o ranking final | Nenhuma |
| `DijkstraSimples.java` | Implementação alternativa de Dijkstra simples; **não utilizada no fluxo atual** (o "Simples" ativo é `DijkstraHibrido` com $\beta=0$) | `Grafo.java` |

---

## 📁 Estrutura do Projeto

```
AOMI-GRAPH-CENTRALITY/
├── data/
│   └── survey_data.csv                 # Dados brutos da base AOMI
│
├── src/
│   ├── AmostraPonto.java
│   ├── Aresta.java
│   ├── AStar.java
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

A execução gera `relatorio_analise_microplasticos.txt`, `routes/*.csv`, `graficos/comparacao_algoritmos.csv`, e os arquivos `grafo_nodes.csv`/`grafo_edges.csv` para Gephi.

---

## 🔬 Reprodutibilidade Científica

### Parâmetros Fixos

- **Discretização espacial:** células de $1° \times 1°$ latitude/longitude
- **Limiar de conectividade do grafo:** 800 km (Haversine), com conexão de fallback ao vizinho mais próximo
- **Parâmetros da função de custo:** $\alpha=1,0$; $\beta=0,5$ (Híbrido/A\*) ou $\beta=0$ (Simples)
- **Classificação regional:** caixas fixas de lat/lon por bacia oceânica (não Louvain)
- **Métrica de distância:** Haversine (WGS-84)

### Reprodução

1. Obtenha os dados de monitoramento da base AOMI oficial (https://aomi.env.go.jp/)
2. Salve como `data/survey_data.csv`
3. Compile e execute conforme a seção acima
4. Compare `relatorio_analise_microplasticos.txt` e `graficos/comparacao_algoritmos.csv` com as tabelas deste README

Pequenos desvios (<0,1%) são esperados por arredondamento em ponto flutuante.

---

## 🚧 Limitações do Modelo Atual

1. **Classificação regional geográfica, não topológica:** as 9 regiões vêm de caixas fixas de lat/lon, não de detecção de comunidades sobre a estrutura real do grafo (Louvain).
2. **Roteamento sobre o grafo completo:** os três algoritmos buscam no grafo inteiro (3.300 nós) mesmo ao cobrir uma única região, causando "vazamento" de densidade entre regiões vizinhas e custo computacional desnecessário para o A\* em regiões geograficamente dispersas (ver Mar da China).
3. **Construção gulosa, sem refinamento de rota:** as rotas são construídas por expansão ao vizinho mais barato a cada passo, sem reconsiderar decisões anteriores — não há otimização da sequência completa (tipo TSP). *Em desenvolvimento: busca local 2-opt para endereçar exatamente esse ponto (ver abaixo).*
4. **Estático:** não incorpora dinâmica temporal de correntes oceânicas ou variabilidade sazonal.
5. **Capacidade infinita:** não modela restrições realísticas (carga máxima, autonomia de combustível, múltiplos navios).
6. **Sem métricas de centralidade:** apesar do nome do repositório, nenhuma métrica de centralidade de grafo (grau, intermediação/betweenness, etc.) é calculada atualmente — item a decidir: implementar de fato, ou ajustar a descrição do projeto.

---

## 🔮 Extensões em Andamento e Futuras

### Em andamento

- **Busca local 2-opt:** refinamento de pós-processamento aplicado sobre as rotas já construídas pelos três algoritmos. Como o 2-opt apenas reordena as células já visitadas (não adiciona nem remove nenhuma), a densidade total coletada não muda — apenas a distância percorrida, o que melhora diretamente a métrica de eficiência (densidade/km). Objetivo: quantificar o quanto cada rota gulosa (Simples/Híbrida/A\*) ainda pode ser encurtada sem abrir mão de nenhuma célula já coletada.

### Curto prazo

- Restringir a busca do A\* a um subgrafo induzido pela região-alvo, em vez do grafo completo, para eliminar o custo excessivo observado no Mar da China.
- Análise de sensibilidade do parâmetro $\beta$ da função de custo.

### Médio prazo

- Implementação real de detecção de comunidades por modularidade (Louvain), substituindo a classificação por caixas fixas.
- Cálculo de métricas de centralidade (grau, betweenness) sobre o grafo, alinhando o projeto ao seu próprio nome.
- Enriquecimento dos nós com atributos ambientais adicionais (correntes marinhas, profundidade, temperatura, distância da costa).

### Longo prazo

- Roteamento multi-frota com restrições de capacidade e autonomia.
- Validação empírica com dados reais de embarcações coletoras.

---

## 📚 Referências Científicas

- **[Dijkstra, 1959]** Dijkstra, E. W. "A note on two problems in connexion with graphs." *Numerische Mathematik*, vol. 1, no. 1, pp. 269–271.
- **[Hart, Nilsson & Raphael, 1968]** Hart, P. E., Nilsson, N. J., Raphael, B. "A Formal Basis for the Heuristic Determination of Minimum Cost Paths." *IEEE Transactions on Systems Science and Cybernetics*, vol. 4, no. 2, pp. 100–107.
- **[Blondel et al., 2008]** Blondel, V. D., et al. "Fast unfolding of communities in large networks." *Journal of Statistical Mechanics: Theory and Experiment*, vol. 2008, no. 10, p. P10008. *(referência para a extensão futura de detecção de comunidades — ainda não implementada neste repositório)*
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
