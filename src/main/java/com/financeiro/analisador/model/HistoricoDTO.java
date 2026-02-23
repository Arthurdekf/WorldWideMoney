package com.financeiro.analisador.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                // Gera Getters, Setters, toString, equals e hashCode
@AllArgsConstructor  // Gera construtor com todos os campos
@NoArgsConstructor   // Gera construtor vazio
public class HistoricoDTO {
    private String data; // "dd/MM" ou "HH:mm"
    private Double preco;
}