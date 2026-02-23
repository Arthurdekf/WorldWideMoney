package com.financeiro.analisador.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data                // Gera Getters, Setters, toString, equals e hashCode
@AllArgsConstructor  // Gera construtor com todos os campos (data e preco)
@NoArgsConstructor   // Gera construtor vazio (necessário para o Spring/Jackson)
public class HistoricoDTO {
    private String data; // Pode ser formatada como "dd/MM" ou "HH:mm"
    private Double preco;
}