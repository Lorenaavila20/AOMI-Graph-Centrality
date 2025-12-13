# 🌊 AOMI-GRAPH-CENTRALITY
### Modelagem de Fluxo de Microplásticos com Grafos e Centralidade de Intermediação

[![Java](https://img.shields.io/badge/Linguagem-Java-orange.svg)](https://www.java.com/)
[![Estruturas de Dados](https://img.shields.io/badge/Disciplina-AEDSII-blue.svg)]()
[![JGraphT](https://img.shields.io/badge/Lib-JGraphT-green)](https://jgrapht.org/)
[![Licença MIT](https://img.shields.io/badge/Licença-MIT-yellow.svg)](LICENSE)

---

## 💡 Sobre o Projeto

Este projeto implementa uma **modelagem do fluxo de microplásticos no oceano** utilizando a linguagem **Java** e a biblioteca **JGraphT**. O objetivo é aplicar conceitos avançados de **Estruturas de Dados e Algoritmos** para identificar padrões de acúmulo e rotas críticas de dispersão de poluentes em grandes massas oceânicas.

### 🔬 Elementos Técnicos Centrais

- **Grafo direcionado e ponderado** (implementado com JGraphT)
- **Centralidade de Intermediação (Betweenness Centrality)** para identificação de gargalos de fluxo
- **Dijkstra Híbrido** para cálculo de rotas de menor custo (otimização da coleta)
- **Pré-processamento espacial** com células geográficas de **1° × 1°**, a partir de dados da AOMI

O propósito final é identificar:

- **Hotspots de acúmulo de microplásticos** (alta densidade)
- **Regiões críticas de passagem** (alta centralidade de intermediação)
- **Rotas dominantes das correntes oceânicas**, modeladas como caminhos de menor custo no grafo

O foco didático está no uso de **Estruturas de Dados avançadas** (Map, List, PriorityQueue) e na aplicação de **Algoritmos clássicos** em um problema ambiental real.

---

## 🌐 Estrutura Geral do Processamento

O pipeline de processamento é dividido em etapas modulares:

1. **Leitura dos dados brutos (`.dat` da AOMI)**  
   Cada linha contém latitude, longitude e densidade medida.

2. **Pré-processamento espacial**  
   Agrupamento das amostras em células de 1° × 1°, com cálculo de densidade média e centróides.

3. **Construção do grafo**  
   As células são convertidas em vértices, e as relações de vizinhança formam as arestas.

4. **Definição do peso das arestas**  
   A classe `Aresta` define um **custo híbrido** baseado em distância geográfica e densidade.

5. **Detecção de comunidades**  
   O `DetectorComunidade` identifica regiões com alta modularidade (Algoritmo de Louvain).

6. **Otimização de rotas**  
   O `OtimizadorRotas` utiliza o `DijkstraHibrido` para calcular caminhos de menor custo.

7. **Geração de relatórios**  
   O `GeradorRelatorio` exporta gráficos, tabelas e dados de rotas para análise posterior.

---

## 🚀 Requisitos Acadêmicos Atendidos

- ✔ Estruturas de Dados (Map, HashMap, List, ArrayList, PriorityQueue)
- ✔ Grafo Direcionado Ponderado
- ✔ Algoritmos de Caminho Mínimo (Dijkstra)
- ✔ Métricas de Centralidade (Betweenness Centrality – via JGraphT)
- ✔ Pré-processamento espacial
- ✔ Análise de dados ambientais reais
- ✔ Leitura robusta e filtragem de dados

---

## 🧠 Principais Componentes do Código

| Arquivo | Função |
|-------|-------|
| `Main.java` | Pipeline principal; orquestra todas as etapas do experimento |
| `LeitorDeDados.java` | Leitura e tratamento inicial do arquivo `.dat` da AOMI |
| `PreProcessador.java` | Agrupamento das amostras em células 1° × 1°, cálculo de densidade e centróides |
| `AmostraPonto.java` | Classe de modelo para amostras individuais e células agregadas |
| `Grafo.java` | Estrutura central do grafo; criação e gerenciamento de nós e arestas |
| `Aresta.java` | Implementa a lógica do **Custo Híbrido** (distância + densidade) |
| `DetectorComunidade.java` | Aplica o Algoritmo de Louvain para identificação de comunidades |
| `DijkstraHibrido.java` | Implementação customizada do Dijkstra com custo híbrido |
| `OtimizadorRotas.java` | Seleção de origens/destinos e execução das rotas otimizadas |
| `GeradorRelatorio.java` | Geração de arquivos de saída (gráficos, tabelas e logs) |

---

## 📁 Estrutura do Projeto

```text
AOMI-GRAPH-CENTRALITY/
├── graficos/        # Saída de figuras e gráficos
├── routes/          # Saída de dados das rotas (CSV/JSON)
├── src/
│   ├── AmostraPonto.java
│   ├── Aresta.java
│   ├── DetectorComunidade.java
│   ├── DijkstraHibrido.java
│   ├── GeradorRelatorio.java
│   ├── Grafo.java
│   ├── LeitorDeDados.java
│   ├── Main.java
│   ├── OtimizadorRotas.java
│   └── PreProcessador.java
├── data/
│   └── amostras.dat  # Dados brutos da AOMI
├── lib/
│   └── jgrapht-core.jar
├── target/
└── .gitignore
```

 ### ⚙️ Como Compilar e Executar


1.  **Pré-requisitos:**

    * Certifique-se de ter o **Java Development Kit (JDK)** instalado no seu sistema.

    * A biblioteca `jgrapht-core.jar` deve estar presente no diretório `lib/`.


2.  **Dados:**

    * Coloque o arquivo de dados brutos da AOMI (`amostras.dat`) no diretório `data/`.


3.  **Compilação (via Terminal):**

    Navegue até o diretório `AOMI-GRAPH-CENTRALITY/src/` e compile todos os arquivos Java, incluindo a biblioteca JGraphT no *classpath* (`-cp`):


    ```bash

    javac -cp .:../lib/jgrapht-core.jar *.java

    ```


4.  **Execução:**

    Execute a classe principal (`Main`) do diretório `src/`, novamente incluindo a biblioteca no *classpath*:


    ```bash

    java -cp .:../lib/jgrapht-core.jar Main

    ```


O programa irá rodar o pipeline completo, desde o pré-processamento até o cálculo das rotas otimizadas, imprimindo os resultados e métricas no console.


--- 

**Autora:** Lorena Ávila
