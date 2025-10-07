// src/main/java/AmostraPonto.java

package com.lorenaavila;

/**
 * Representa um único ponto de coleta de microplásticos (uma linha do CSV do AOMI).
 * É o modelo de dados de entrada antes do pré-processamento (gridificação).
 */
public class AmostraPonto {
    private double latitude;
    private double longitude;
    // Sugestão de métrica: Densidade de Partículas (ex: particles/m³ ou particles/km²)
    private double densidadeParticulas; 

    // O CSV do AOMI tem muitas outras colunas (data, tipo de rede, etc.).
    // Podemos adicionar mais campos conforme a necessidade do projeto:
    // private String dataAmostra;
    // private String equipamentoColeta;
    
    // Construtor: Usado pelo LeitorDeDados.java para criar um novo objeto a partir de cada linha do CSV
    public AmostraPonto(double latitude, double longitude, double densidadeParticulas) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.densidadeParticulas = densidadeParticulas;
    }

    // Métodos Getters: Permitem acessar os valores privados da classe
    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDensidadeParticulas() {
        return densidadeParticulas;
    }

    // Método de Debugging útil
    @Override
    public String toString() {
        return String.format("AmostraPonto [Lat=%.2f, Lon=%.2f, Dens=%.2f]", 
                             latitude, longitude, densidadeParticulas);
    }
}