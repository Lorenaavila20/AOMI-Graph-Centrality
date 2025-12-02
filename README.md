# 🌊 AOMI-GRAPH-CENTRALITY  
### Modelagem de Fluxo de Microplásticos com Grafos e Centralidade de Intermediação

[![Java](https://img.shields.io/badge/Linguagem-Java-orange.svg)](https://www.java.com/)
[![Estruturas de Dados](https://img.shields.io/badge/Disciplina-AEDSII-blue.svg)]()
[![JGraphT](https://img.shields.io/badge/Lib-JGraphT-green)](https://jgrapht.org/)
[![Licença MIT](https://img.shields.io/badge/Licença-MIT-yellow.svg)](LICENSE)

---

## 💡 Sobre o Projeto

Este projeto implementa uma **modelagem de fluxo de microplásticos no oceano** utilizando:

- **Grafo direcionado ponderado**  
- **Centralidade de Intermediação (Betweenness Centrality)**
- **Dijkstra Híbrido** para rotas de menor custo
- **Pré-processamento espacial** com células geográficas de **1° × 1°**
- **Cálculo de densidade média e centróides** a partir das amostras da NOAA

O propósito é identificar:

- **Hotspots de acúmulo de microplásticos**
- **Regiões críticas de passagem (caminhos obrigatórios)**
- **Possíveis rotas dominantes das correntes oceânicas**

O foco didático está no uso de **Estruturas de Dados avançadas**, como mapas, listas, grafos e algoritmos clássicos.

---

## 🌐 Estrutura Geral do Processamento

1. **Leitura dos dados brutos (`.dat` da NOAA)**  
   Cada linha contém latitude, longitude e densidade medida.

2. **Agrupamento das amostras**  
   Cada ponto é colocado em uma célula geográfica de **1° × 1°** usando `floor(lat)` e `floor(lon)`.

3. **Cálculo das densidades médias por célula**  
   Reduz ruído e normaliza valores.

4. **Cálculo dos centróides das células**  
   Coordenada média de cada grupo.

5. **Construção do grafo**  
   Cada célula vira um nó.  
   Arestas ligam células vizinhas usando o centróide como referência.

6. **Peso das arestas**  
   Combinação híbrida baseada em:
   - distância geográfica
   - diferença de densidade  
   - fator de difusão

7. **Centralidade de Intermediação (Betweenness)**  
   Identifica nós que concentram fluxo — possíveis regiões críticas.

8. **Dijkstra Híbrido**  
   Calcula a menor rota entre duas células considerando:
   - peso físico + densidade  
   - custo dinâmico baseado no modelo de microplásticos

---

## 🚀 Requisitos Acadêmicos Atendidos

✔ Estruturas de Dados (Map, HashMap, List, ArrayList, PriorityQueue)  
✔ Grafo Direcionado Ponderado  
✔ Algoritmos de Caminho Mínimo (Dijkstra)  
✔ Métricas de Centralidade (Betweenness – via JGraphT)  
✔ Pré-processamento espacial  
✔ Análise de dados ambientais  
✔ Leitura robusta e filtragem de dados reais  

---

## 🧠 Principais Componentes do Código

| Arquivo | Função |
|--------|--------|
| `AmostraPonto.java` | Representa uma amostra individual (lat, lon, densidade). |
| `LeitorDeDados.java` | Faz a leitura e limpeza do dataset. |
| `PreProcessador.java` | Agrupamento, densidade média e cálculo dos centróides. |
| `.java` | Constrói o grafo com base nas células. |
| `.java` | Calcula betweenness centrality (JGraphT). |
| `.java` | Implementa Dijkstra Híbrido para simulação de fluxo. |
| `Main.java` | Pipeline completo do experimento. |

---

## 🧭 Guia de Execução

### 📌 Estrutura esperada do projeto

