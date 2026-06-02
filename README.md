# AOMI-Graph-Centrality
## Modelagem e Otimização de Rotas de Coleta de Microplásticos com Teoria dos Grafos

[![Linguagem](https://img.shields.io/badge/Linguagem-Java-orange.svg)](https://www.java.com/)  
[![Versão](https://img.shields.io/badge/Versão-experimento--densidade-green.svg)]()  
[![Licença](https://img.shields.io/badge/Licença-MIT-yellow.svg)](LICENSE)

---

## 📋 Resumo Executivo

Este repositório implementa computacionalmente a pesquisa:

> **"Modelagem e Otimização de Rotas de Navios Coletores de Microplásticos em Grandes Massas Oceânicas Utilizando Teoria dos Grafos e Algoritmos de Caminho Mínimo"**

A solução integra **Teoria dos Grafos**, **Algoritmo de Dijkstra adaptado** para otimização multiobjetivo (custo-densidade), **detecção de comunidades via Louvain**, e dados reais da base **AOMI (Atlantic and Oceanic Microplastics Index)** para identificar estratégias eficientes de coleta de poluentes marinhos.

**Achado Principal:** A Região 7 (Atlântico Sul) é atualmente identificada como região dominante, concentrando alta carga de microplásticos em território geograficamente delimitado, fundamentando uma alocação de frota altamente eficiente.

---

## 🎯 Objetivos da Pesquisa

1. **Modelar a distribuição global de microplásticos** como grafo ponderado discretizado em células $1° \times 1°$;
2. **Identificar bacias oceânicas prioritárias** através de detecção de comunidades (Algoritmo de Louvain);
3. **Otimizar rotas** considerando simultaneamente distância geográfica real e densidade de poluentes;
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

$$C(v_i, v_j) = \frac{\alpha \cdot d(v_i, v_j)}{1 + \beta \cdot \rho(v_j)}$$

onde:

| Parâmetro | Descrição |
|-----------|-----------|
| $d(v_i, v_j)$ | Distância geográfica real (fórmula de Haversine, WGS-84) em km |
| $\rho(v_j)$ | Densidade normalizada de microplásticos no vértice destino |
| $\alpha$, $\beta$ | Fatores de balanceamento entre custo de navegação e potencial de coleta |

**Interpretação:** Penaliza rotas longas e favorece rotas de alta densidade, balanceados pelos parâmetros $\alpha$ e $\beta$.

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
| Regiões Detectadas | 9 |

### Ranking de Comunidades por Concentração de Poluentes

| Rank | Comunidade | Região | Células | Densidade (p/m³) | Total (p/m³) | Eficiência |
|------|-----------|--------|---------|-----------------|-------------|-----------|
| **1** | **7** | **Atlântico Sul** | **278** | — | **~1164,97** | **Máxima** |
| 2 | 4 | Oceano Índico | 461 | — | — | Muito Alta |
| 3 | 2 | Pacífico Equatorial | 602 | — | — | Alta |
| 4 | 5 | Mar da China | 146 | — | — | Moderada |
| 5 | 6 | Mediterrâneo | 164 | — | — | Moderada |
| 6 | 8 | Pacífico Sul | 674 | — | — | Baixa-Moderada |
| 7 | 1 | Pacífico Norte | 297 | — | — | Baixa |
| 8 | 3 | Atlântico Norte | 570 | — | — | Baixa |

> **Nota:** Métricas detalhadas por comunidade disponíveis nos arquivos `output/relatorio_comunidades.csv` e `output/relatorio_rotas.csv`.

### Eficiência Operacional da Comunidade Prioritária

| Comunidade | Classificação |
|-----------|--------------|
| 7 (Atlântico Sul) | Concentrada — rotas convergem para núcleos de alta densidade |

**Implicação:** A função de custo híbrida valida eficiência operacional mesmo com múltiplos pontos de origem, com convergência para hotspots de alta concentração.

---

## 🧩 Arquitetura do Código

### Componentes Principais

```
┌─────────────────────────────────────────────────────────────┐
│                        Main.java                             │
│  (Orquestrador do pipeline experimental)                    │
└────────────────┬────────────────────────────────────────────┘
                 │
    ┌────────────┼────────────┬────────────┬────────────┐
    ▼            ▼            ▼            ▼            ▼
┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐
│Leitor    │ │PreProc   │ │Detector  │ │Dijkstra  │ │GeoUtils  │
│Dados     │ │essador   │ │Comunidade│ │Híbrido   │ │          │
└────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘ └────┬─────┘
     │            │            │            │            │
     └────────────┼────────────┼────────────┼────────────┘
                  ▼
         ┌────────────────────┐
         │ OtimizadorRotas    │
         │ (Geração paralela) │
         └────────┬───────────┘
                  │
                  ▼
         ┌────────────────────┐
         │GeradorRelatorio    │
         │(TXT/CSV/Gephi)     │
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
| `Aresta.java` | Implementação da função de custo híbrido custo-densidade | `GeoUtils.java` |
| `GeoUtils.java` | Cálculo de distância geográfica real via fórmula de Haversine (WGS-84, raio = 6371 km) | Nenhuma |
| `DetectorComunidade.java` | Integração do Algoritmo de Louvain; extração de comunidades | `Grafo.java` |
| `DijkstraHibrido.java` | Implementação customizada de Dijkstra com função de custo multiobjetivo | `Grafo.java`, `Aresta.java` |
| `OtimizadorRotas.java` | Seleção de origens (centroide, secundário, máx. densidade); execução de rotas paralelas | `DijkstraHibrido.java` |
| `GeradorRelatorio.java` | Consolidação de métricas; exportação TXT, CSV e arquivos Gephi | Todas |

---

## 📁 Estrutura do Projeto

```
AOMI-GRAPH-CENTRALITY/
├── data/
│   └── survey_data.csv                    # Dados brutos da base AOMI
│
├── src/
│   ├── AmostraPonto.java               # Modelo de domínio
│   ├── Aresta.java                     # Função de custo híbrido
│   ├── DetectorComunidade.java         # Integração Louvain
│   ├── DijkstraHibrido.java            # Dijkstra multiobjetivo
│   ├── GeoUtils.java                   # Cálculo Haversine (WGS-84)
│   ├── Grafo.java                      # Construção e análise de grafo
│   ├── GeradorRelatorio.java           # Exportação de resultados
│   ├── LeitorDeDados.java              # Parser AOMI
│   ├── Main.java                       # Orquestrador
│   ├── OtimizadorRotas.java            # Geração de rotas paralelas
│   └── PreProcessador.java             # Agregação espacial
│
├── output/
│   ├── relatorio_analitico.txt         # Relatório analítico completo
│   ├── relatorio_comunidades.csv       # Ranking e métricas por região
│   ├── relatorio_rotas.csv             # Dados de rotas para gráficos
│   ├── grafo_nodes.csv                 # Nós para visualização (Gephi)
│   └── grafo_edges.csv                 # Arestas para visualização (Gephi)
│
├── routes/
│   └── rota_regiao_XX.csv              # Rotas geradas por comunidade
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

A execução gerará arquivos em `output/` (relatórios e dados Gephi) e `routes/` (rotas por comunidade).

---

## 🔬 Reprodutibilidade Científica

Este repositório foi estruturado para garantir **reprodutibilidade total** dos resultados. O modelo é **determinístico** — reexecuções múltiplas e recompilações completas produzem resultados idênticos.

### Parâmetros Fixos

- **Discretização espacial:** Células de $1° \times 1°$ latitude/longitude
- **Conectividade:** 8-vizinhança (Moore)
- **Algoritmo de comunidades:** Louvain com modularity optimization
- **Métrica de distância:** Haversine (WGS-84, raio = 6371 km) — implementada em `GeoUtils.java`
- **Fatores de balanceamento:** $\alpha$ e $\beta$ configuráveis em `Aresta.java`

### Reprodução Exata

Para reproduzir os resultados:

1. Obtenha o arquivo `amostras.dat` da base AOMI oficial (https://aomi.env.go.jp/)
2. Coloque em `data/amostras.dat`
3. Compile e execute `Main.java`
4. Compare `output/relatorio_comunidades.csv` e `output/relatorio_rotas.csv` com as tabelas do artigo

Desvios mínimos (<0,1%) são esperados devido a arredondamentos em ponto flutuante.

---

## 📌 Changelog

### Versão: `experimento-densidade`

#### Implementação da Distância Geográfica Real (Haversine)
Substituição de cálculo aproximado por distância esférica real via criação de `GeoUtils.java`, usando o modelo WGS-84 com raio da Terra = 6371 km. As arestas do grafo passam a refletir distâncias reais entre coordenadas, conferindo validade geográfica global aos resultados (ex: totais coerentes na ordem de 11k–44k km).

#### Atualização da Função de Custo
Consolidação do Dijkstra híbrido com a função `custo = (α * distancia) / (1 + β * densidade)`, refinando o equilíbrio entre custo de navegação e potencial de coleta.

#### Geração Completa de Relatórios
Implementação de `GeradorRelatorio.java` com exportação para `.txt` (relatório analítico), `.csv` (dados para gráficos) e rotas individuais por região — incluindo ranking de regiões, densidade total, eficiência e recomendações operacionais.

#### Exportação para Visualização (Gephi)
Geração de `grafo_nodes.csv` e `grafo_edges.csv` (além de subgrafos por região) para análise estrutural e visualização de comunidades em ferramentas externas.

#### Estruturação das Rotas
Rotas geradas por comunidade salvas automaticamente em `routes/rota_regiao_XX.csv`, contendo ordem de visita, coordenadas e densidade de cada célula.

#### Correção de Inconsistência e Estabilização
Resultado anterior indicava Região 4 (Índico) como dominante; após correção de compilação desatualizada e reexecução, a **Região 7 (Atlântico Sul)** é confirmada como dominante de forma consistente e reproduzível.

---

## 🚧 Limitações do Modelo Atual

1. **Estático:** Não incorpora dinâmica temporal de correntes oceânicas ou variabilidade sazonal
2. **Capacidade infinita:** Não modela restrições realísticas (carga máxima, autonomia de combustível)
3. **Sem otimização TSP:** Não resolve o Problema do Caixeiro Viajante para sequenciamento ótimo intra-rota
4. **Sem custo operacional:** Não inclui custos fixos (manutenção, pessoal) nas análises
5. **Sem incerteza:** Presume distribuição de poluentes determinística

Essas limitações são discutidas como oportunidades de aprimoramento no artigo científico associado.

---

## 🔮 Extensões Futuras Recomendadas

### Curto Prazo

- Implementação de A* com heurística geográfica
- Integração de variáveis ambientais (correntes, sazonalidade)
- Análise de sensibilidade dos parâmetros $\alpha$ e $\beta$ da função de custo

### Médio Prazo

- Simulação de coleta real com restrições de capacidade
- Otimização multi-frota
- Implementação de TSP via meta-heurísticas (simulated annealing, algoritmo genético)
- Integração de modelos preditivos (Machine Learning) para antecipação de hotspots

### Longo Prazo

- Visualização 3D (globo interativo)
- Integração com sistemas reais de navios coletores (validação empírica)
- Cooperação internacional para otimização de rotas em bacias compartilhadas

---

## 📚 Referências Científicas

- **[Dijkstra, 1959]** Dijkstra, E. W. "A note on two problems in connexion with graphs." *Numerische Mathematik*, vol. 1, no. 1, pp. 269–271.
- **[Blondel et al., 2008]** Blondel, V. D., et al. "Fast unfolding of communities in large networks." *Journal of Statistical Mechanics: Theory and Experiment*, vol. 2008, no. 10, p. P10008.
- **[Jambeck et al., 2015]** Jambeck, J. R., et al. "Plastic waste inputs from land into the ocean." *Science*, vol. 347, no. 6223, pp. 768–771.
- **[Freeman, 1977]** Freeman, L. C. "A set of measures of centrality based on betweenness." *Sociometry*, vol. 40, no. 1, pp. 35–41.

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
