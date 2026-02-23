package com.financeiro.analisador.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data // O Lombok cria os getters e setters automaticamente
public class Ativo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String simbolo; // Ex: BTC, PETR4, AAPL
    private String nome;
    private BigDecimal precoAtual;
    private LocalDateTime dataAtualizacao;

    // Construtor padrão
    public Ativo() {
        this.dataAtualizacao = LocalDateTime.now();
    }
}