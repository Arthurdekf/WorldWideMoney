package com.financeiro.analisador.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.analisador.model.Ativo;
import com.financeiro.analisador.model.HistoricoDTO;
import com.financeiro.analisador.service.AtivoService;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/ativos")
public class AtivoController {

    private final AtivoService ativoService;

    public AtivoController(AtivoService ativoService) {
        this.ativoService = ativoService;
    }

    // 1. Dashboard 
    @GetMapping("/dashboard")
    public List<Ativo> getDashboard() {
        String acoes = "PETR4,VALE3,WEGE3,ITUB4,BBAS3,AAPL,TSLA,NVDA,GOOGL,MSFT";
        String criptos = "BTC,ETH,SOL,BNB,ADA,XRP,DOT,MATIC";

        return ativoService.buscarVariosAtivos(acoes, criptos);
    }

    // 2. Rotas individuais com nome diferente
    @GetMapping("/ticker/{simbolo}")
    public Ativo getCotacao(@PathVariable String simbolo) {
        return ativoService.buscarCotacaoReal(simbolo);
    }

    @GetMapping("/historico/{simbolo}")
    public List<HistoricoDTO> obterHistorico(@PathVariable String simbolo, @RequestParam String periodo) {
        return ativoService.buscarHistorico(simbolo, periodo);
    }
}
