# AOMI-Graph-Centrality
## Modelagem e Otimização de Rotas de Coleta de Microplásticos com Teoria dos Grafos

[![Linguagem](https://img.shields.io/badge/Linguagem-Java-orange.svg)](https://www.java.com/)  
[![Disciplina](https://img.shields.io/badge/Disciplina-AEDSII-blue.svg)]()  
[![Licença](https://img.shields.io/badge/Licença-MIT-yellow.svg)](LICENSE)

---

## 📋 Resumo Executivo

Este repositório implementa computacionalmente a pesquisa:

> **"Modelagem e Otimização de Rotas de Navios Coletores de Microplásticos em Grandes Massas Oceânicas Utilizando Teoria dos Grafos e Algoritmos de Caminho Mínimo"**

A solução integra **Teoria dos Grafos**, **Algoritmo de Dijkstra adaptado** para otimização multiobjetivo (custo-densidade), **detecção de comunidades via Louvain**, e dados reais da base **AOMI (Atlantic and Oceanic Microplastics Index)** para identificar estratégias eficientes de coleta de poluentes marinhos.

**Achado Principal:** Apenas duas comunidades oceânicas (Índico e Atlântico Sul) concentram **85,77% da carga total** de microplásticos em **23,15% do território**, fundamentando uma alocação de frota altamente eficiente.

---

## 🎯 Objetivos da Pesquisa

1. **Modelar a distribuição global de microplásticos** como grafo ponderado discretizado em células $1° \times 1°$;
2. **Identificar bacias oceânicas prioritárias** através de detecção de comunidades (Algoritmo de Louvain);
3. **Otimizar rotas** considerando simultaneamente distância geográfica e densidade de poluentes;
4. **Gerar múltiplas rotas por comunidade** para viabilizar operações paralelas de navios coletores.

---

## 🧠 Fundamentação Teórica

### Representação Espacial e Grafo

- **Discretização:** Oceano modelado como grid de células geográficas $1° \times 1°$;
- **Vértices:** Células com dados válidos de concentração de microplásticos;
- **Arestas:** Conexões entre células adjacentes via **8-vizinhança (Moore)**;
- **Pesos:** Função de custo híbrida custo-densidade para balanceamento multiobjetivo.

### Função de Custo Híbrida

A otimização multiobjetivo utiliza:

$$C(v_i, v_j) = \frac{d(v_i, v_j)}{1 + k \cdot \rho(v_j)}$$

onde:

| Parâmetro | Descrição |
|-----------|-----------|
| $d(v_i, v_j)$ | Distância geográfica (fórmula de Haversine) em km |
| $\rho(v_j)$ | Densidade normalizada de microplásticos no vértice destino |
| $k$ | Fator de balanceamento (adotado: $k = 0,5$) |

**Interpretação:** Penalizar rotas longas e favorecer rotas de alta densidade, balanceados pelo parâmetro $k$.

### Algoritmos Implementados

| Algoritmo | Propósito | Referência |
|-----------|-----------|-----------|
| **Dijkstra Híbrido** | Cálculo de caminhos de menor custo com função multiobjetivo | [Dijkstra, 1959] |
| **Louvain** | Detecção de comunidades via otimização de modularidade | [Blondel et al., 2008] |
| **Betweenness Centrality** | Identificação de gargalos e rotas críticas nas comunidades | [Freeman, 1977] |

---

## 📊 Resultados Principais

### Estrutura do Grafo Global

| Métrica | Valor |
|---------|-------|
| Vértices ($n$) | 3.300 |
| Arestas ($m$) | 11.781 |
| Densidade ($\delta$) | 0,00182 |
| Grau Médio ($\bar{k}$) | 7,13 |
| Diâmetro | 247 |
| Modularidade ($Q$) | 0,742 |

### Ranking de Comunidades por Concentração de Poluentes

| Rank | Comunidade | Região | Células | Densidade (p/m³) | Total (p/m³) | Eficiência |
|------|-----------|--------|---------|-----------------|-------------|-----------|
| 1 | 4 | Oceano Índico | 461 | 2.417 | **1.114,42** | **Máxima** |
| 2 | 7 | Atlântico Sul | 278 | 1.748 | **485,89** | **Muito Alta** |
| 3 | 2 | Pacífico Equatorial | 602 | 0,272 | 163,98 | Alta |
| 4 | 1 | Pacífico Norte | 297 | 0,011 | 3,26 | Baixa |
| 5 | 3 | Atlântico Norte | 570 | 0,012 | 6,98 | Baixa |
| 6 | 5 | Mar da China | 146 | 0,133 | 19,47 | Moderada |
| 7 | 6 | Mediterrâneo | 164 | 0,120 | 19,73 | Moderada |
| 8 | 8 | Pacífico Sul | 674 | 0,078 | 52,42 | Baixa-Moderada |
| — | — | **TOTAL** | **3.192** | **0,606** | **1.865,77** | — |

### Eficiência Operacional das Comunidades Prioritárias

| Comunidade | Comprimento Médio (km) | Células/Rota | Convergência (%) | Classificação |
|-----------|----------------------|--------------|-----------------|-----------------|
| 4 (Índico) | 1.247,3 | 154 | **78,4%** | Concentrada |
| 7 (Atlântico Sul) | 1.089,2 | 93 | **72,1%** | Concentrada |

**Implicação:** As rotas nessas comunidades convergem para núcleos de alta concentração mesmo com múltiplos pontos de origem, validando eficiência da função de custo híbrida.

---

## 🧩 Arquitetura do Código

### Componentes Principais

```
┌─────────────────────────────────────────────────────────────┐
│                        Main.java                             │
│  (Orquestrador do pipeline experimental)                    │
└────────────────┬────────────────────────────────────────────┘
                 │
    ┌────────────┼────────────┬────────────┐
    ▼            ▼            ▼            ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│Leitor    │ │PreProc   │ │Detector  │ │Dijkstra  │
│Dados     │ │essador   │ │Comunidade│ │Híbrido   │
└────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘
     │            │            │            │
     └────────────┼────────────┼────────────┘
                  ▼
         ┌────────────────────┐
         │ OtimizadorRotas    │
         │ (Geração paralela) │
         └────────┬───────────┘
                  │
                  ▼
         ┌────────────────────┐
         │GeradorRelatorio    │
         │(CSV/JSON/Visualiz.)│
         └────────────────────┘
```

### Descrição dos Módulos

| Classe | Responsabilidade | Dependências |
|--------|------------------|--------------|
| `Main.java` | Orquestra pipeline: leitura → pré-processamento → grafo → comunidades → rotas → relatórios | Todas |
| `LeitorDeDados.java` | Leitura, validação e filtragem do arquivo `.dat` (AOMI) | `AmostraPonto.java` |
| `PreProcessador.java` | Agregação espacial em células $1° \times 1°$; cálculo de densidade e centróides | `AmostraPonto.java` |
| `AmostraPonto.java` | Modelo de domínio para amostras e células agregadas | Nenhuma |
| `Grafo.java` | Construção, manutenção, análise e serialização do grafo ponderado | `Aresta.java`, `AmostraPonto.java` |
| `Aresta.java` | Implementação da função de custo híbrido custo-densidade; cálculo de Haversine | Nenhuma |
| `DetectorComunidade.java` | Integração do Algoritmo de Louvain; extração de comunidades | `Grafo.java` |
| `DijkstraHibrido.java` | Implementação customizada de Dijkstra com função de custo multiobjetivo | `Grafo.java`, `Aresta.java` |
| `OtimizadorRotas.java` | Seleção de origens (centroide, secundário, máx. densidade); execução de rotas paralelas | `DijkstraHibrido.java` |
| `GeradorRelatorio.java` | Consolidação de métricas, exportação (CSV/JSON), preparação de dados para visualização | Todas |

---

## 📁 Estrutura do Projeto

```
AOMI-GRAPH-CENTRALITY/
├── data/
│   └── amostras.dat                    # Dados brutos da base AOMI
│
├── src/
│   ├── AmostraPonto.java               # Modelo de domínio
│   ├── Aresta.java                     # Função de custo híbrido
│   ├── DetectorComunidade.java         # Integração Louvain
│   ├── DijkstraHibrido.java            # Dijkstra multiobjetivo
│   ├── Grafo.java                      # Construção e análise de grafo
│   ├── GeradorRelatorio.java           # Exportação de resultados
│   ├── LeitorDeDados.java              # Parser AOMI
│   ├── Main.java                       # Orquestrador
│   ├── OtimizadorRotas.java            # Geração de rotas paralelas
│   └── PreProcessador.java             # Agregação espacial
│
├── .gitignore
├── LICENSE
└── README.md
```

---

## ⚙️ Requisitos e Instalação

### Pré-requisitos

- **Java Development Kit (JDK) 8+** instalado e configurado no `PATH`
- Sistema operacional: Windows, macOS ou Linux
- Mínimo: 512 MB de RAM disponível (recomendado: 2+ GB)

### Compilação

A partir do diretório `src/`:

```bash
javac *.java
```

### Execução

```bash
java Main
```

A execução gerará arquivos de saída em `output/` com métricas detalhadas, relatórios e visualizações.

---

## 🔬 Reprodutibilidade Científica

Este repositório foi estruturado para garantir **reprodutibilidade total** dos resultados apresentados no artigo associado. Para tal:

### Parâmetros Fixos

- **Discretização espacial:** Células de $1° \times 1°$ latitude/longitude
- **Conectividade:** 8-vizinhança (Moore)
- **Fator de balanceamento:** $k = 0,5$ (tuning disponível em `Aresta.java`)
- **Algoritmo de comunidades:** Louvain com modularity optimization
- **Métrica de distância:** Haversine (WGS-84)

### Reprodução Exata

Para reproduzir os resultados do artigo:

1. Obtenha o arquivo `amostras.dat` da base AOMI oficial (https://aomi.env.go.jp/)
2. Coloque em `data/amostras.dat`
3. Compile e execute `Main.java`
4. Compare `output/relatorio_comunidades.csv` e `output/relatorio_rotas.csv` com tabelas do artigo

Desvios mínimos (<0,1%) são esperados devido a arredondamentos em ponto flutuante.

---

## 🚧 Limitações do Modelo Atual

O modelo implementado apresenta as seguintes limitações inerentes:

1. **Estático:** Não incorpora dinâmica temporal de correntes oceânicas ou variabilidade sazonal
2. **Capacidade infinita:** Não modela restrições realísticas (carga máxima, autonomia de combustível)
3. **Sem otimização TSP:** Não resolve o Problema do Caixeiro Viajante para sequenciamento ótimo intra-rota
4. **Sem custo operacional:** Não inclui custos fixos (manutenção, pessoal) nas análises
5. **Sem incerteza:** Presume distribuição de poluentes determinística

Essas limitações são discutidas como oportunidades de aprimoramento no artigo científico associado.

---

## 🔮 Extensões Futuras Recomendadas

### Curto Prazo (6-12 meses)

- Análise de sensibilidade do parâmetro $k$ da função de custo
- Integração de dados temporais (sazonalidade, correntes)
- Implementação de TSP via meta-heurísticas (simulated annealing, algoritmo genético)

### Médio Prazo (1-2 anos)

- Otimização multi-frota com restrições de capacidade
- Integração de modelos preditivos (Machine Learning) para antecipação de hotspots
- Análise de ciclo de vida (LCA) das operações de coleta

### Longo Prazo (2+ anos)

- Integração com sistemas reais de navios coletores (validação empírica)
- Modelagem de dinâmica de fragmentação e deposição de plásticos
- Cooperação internacional para otimização de rotas em bacias compartilhadas

---

## 📚 Referências Científicas

As referências bibliográficas completas encontram-se no artigo científico associado. Principais trabalhos citados:

- **[Dijkstra, 1959]** Dijkstra, E. W. "A note on two problems in connexion with graphs." *Numerische Mathematik*, vol. 1, no. 1, pp. 269–271.
- **[Blondel et al., 2008]** Blondel, V. D., et al. "Fast unfolding of communities in large networks." *Journal of Statistical Mechanics: Theory and Experiment*, vol. 2008, no. 10, p. P10008.
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
- **Biblioteca JGraphT:** https://jgrapht.org/
- **Documentação Java:** https://docs.oracle.com/javase/

---


