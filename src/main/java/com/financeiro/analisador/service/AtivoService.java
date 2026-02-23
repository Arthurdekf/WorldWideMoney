package com.financeiro.analisador.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.financeiro.analisador.model.Ativo;
import com.financeiro.analisador.model.HistoricoDTO;
import com.financeiro.analisador.repository.AtivoRepository;

@Service
public class AtivoService {

    @Value("${brapi.token}")
    private String token;

    private final AtivoRepository ativoRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public AtivoService(AtivoRepository ativoRepository) {
        this.ativoRepository = ativoRepository;
    }

    public Ativo buscarCotacaoReal(String simbolo) {
        String url = "https://brapi.dev/api/quote/" + simbolo + "?token=" + token;

        try {
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("results")) {
                throw new RuntimeException("Resposta da API vazia");
            }

            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            Map<String, Object> data = results.get(0);

            Ativo ativo = new Ativo();
            ativo.setSimbolo(data.get("symbol").toString());
            ativo.setNome(data.get("longName").toString());

            String precoStr = data.get("regularMarketPrice").toString();
            ativo.setPrecoAtual(new BigDecimal(precoStr));
            ativo.setDataAtualizacao(LocalDateTime.now());

            return ativo;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao conectar na API: " + e.getMessage());
        }
    }

    public List<Ativo> buscarVariosAtivos(String simbolosAcoes, String simbolosCripto) {
        List<Ativo> listaFinal = new ArrayList<>();

        try {
            // 1. BUSCAR AÇÕES
            if (simbolosAcoes != null && !simbolosAcoes.isEmpty()) {
                String urlAcoes = "https://brapi.dev/api/quote/" + simbolosAcoes + "?token=" + token;
                Map<String, Object> res = restTemplate.getForObject(urlAcoes, Map.class);
                
                if (res != null && res.get("results") instanceof List) {
                    List<Map<String, Object>> results = (List<Map<String, Object>>) res.get("results");
                    results.forEach(data -> {
                        Ativo a = transformarEmAtivo(data);
                        listaFinal.add(ativoRepository.save(a));
                    });
                }
            }

            // 2. BUSCAR CRIPTOS
            if (simbolosCripto != null && !simbolosCripto.isEmpty()) {
                String[] moedas = simbolosCripto.split(",");

                for (String moeda : moedas) {
                    try {
                        String mTrim = moeda.trim();
                        String urlBinance = "https://api.binance.com/api/v3/ticker/price?symbol=" + mTrim + "BRL";
                        Map<String, Object> resBinance = restTemplate.getForObject(urlBinance, Map.class);

                        if (resBinance != null && resBinance.containsKey("price")) {
                            Ativo a = new Ativo();
                            a.setSimbolo(mTrim);
                            a.setNome(mapearNomeCripto(mTrim));
                            a.setPrecoAtual(new BigDecimal(resBinance.get("price").toString()));
                            a.setDataAtualizacao(LocalDateTime.now());

                            listaFinal.add(ativoRepository.save(a));
                        }
                    } catch (Exception e) {
                        System.err.println("Erro ao buscar a moeda " + moeda + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro geral no service: " + e.getMessage());
        }

        return listaFinal;
    }

    private String mapearNomeCripto(String simbolo) {
        if (simbolo == null) return "Desconhecido";
        
        return switch (simbolo.toUpperCase().trim()) {
            case "BTC" -> "Bitcoin";
            case "ETH" -> "Ethereum";
            case "SOL" -> "Solana";
            case "BNB" -> "Binance Coin";
            case "ADA" -> "Cardano";
            case "XRP" -> "Ripple";
            case "DOT" -> "Polkadot";
            case "MATIC" -> "Polygon";
            default -> simbolo;
        };
    }

    private Ativo transformarEmAtivo(Map<String, Object> data) {
        Ativo a = new Ativo();
        a.setSimbolo(data.get("symbol").toString());
        a.setNome(Optional.ofNullable(data.get("longName")).map(Object::toString).orElse(a.getSimbolo()));
        a.setPrecoAtual(new BigDecimal(data.get("regularMarketPrice").toString()));
        a.setDataAtualizacao(LocalDateTime.now());
        return a;
    }

    public List<HistoricoDTO> buscarHistorico(String simbolo, String periodo) {
        return isMoedaCripto(simbolo) 
            ? buscarHistoricoBinance(simbolo, periodo) 
            : buscarHistoricoBrapi(simbolo, periodo);
    }

    private List<HistoricoDTO> buscarHistoricoBrapi(String simbolo, String periodo) {
        List<HistoricoDTO> lista = new ArrayList<>();
        try {
            String intervalo = switch (periodo) {
                case "1d" -> "5m";
                case "7d" -> "15m";
                case "1mo" -> "60m";
                case "6mo", "1y" -> "1d";
                case "5y" -> "1wk";
                default -> "1h";
            };

            String url = String.format("https://brapi.dev/api/quote/%s?range=%s&interval=%s&token=%s", 
                                        simbolo, periodo, intervalo, token);

            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response != null && response.get("results") instanceof List) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
                List<Map<String, Object>> historicalData = (List<Map<String, Object>>) results.get(0).get("historicalDataPrice");

                if (historicalData != null) {
                    for (Map<String, Object> data : historicalData) {
                        Object rawDate = data.get("date");
                        Object rawClose = data.get("close");

                        if (rawDate != null && rawClose != null) {
                            try {
                                Long timestamp = Long.valueOf(rawDate.toString());
                                Double preco = Double.valueOf(rawClose.toString());
                                lista.add(new HistoricoDTO(formatarData(timestamp), preco));
                            } catch (NumberFormatException e) {
                                // Pula pontos mal formatados
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erro histórico BRAPI: " + e.getMessage());
        }
        return lista;
    }

    private List<HistoricoDTO> buscarHistoricoBinance(String simbolo, String periodo) {
        List<HistoricoDTO> lista = new ArrayList<>();
        try {
            var config = switch (periodo) {
                case "1d" -> Map.of("interval", "5m", "limit", "288");
                case "7d" -> Map.of("interval", "15m", "limit", "672");
                case "1mo" -> Map.of("interval", "1h", "limit", "720");
                case "6mo" -> Map.of("interval", "1d", "limit", "180");
                case "1y" -> Map.of("interval", "1d", "limit", "365");
                case "5y" -> Map.of("interval", "1w", "limit", "260");
                default -> Map.of("interval", "1d", "limit", "30");
            };

            String url = String.format("https://api.binance.com/api/v3/klines?symbol=%sBRL&interval=%s&limit=%s",
                                        simbolo.toUpperCase(), config.get("interval"), config.get("limit"));

            // Aqui usamos um tipo parametrizado para evitar o aviso de "unchecked conversion"
            List<List<Object>> res = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                new ParameterizedTypeReference<List<List<Object>>>() {}
            ).getBody();

            if (res != null) {
                for (List<Object> kline : res) {
                    Long timestampMs = Long.valueOf(kline.get(0).toString());
                    Double preco = Double.valueOf(kline.get(4).toString());
                    lista.add(new HistoricoDTO(formatarDataCripto(timestampMs), preco));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro histórico Binance para " + simbolo + ": " + e.getMessage());
        }
        return lista;
    }

    private String formatarDataCripto(Long timestampMs) {
        return new SimpleDateFormat("dd/MM").format(new Date(timestampMs));
    }

    private boolean isMoedaCripto(String simbolo) {
        List<String> criptosConhecidas = List.of("BTC", "ETH", "SOL", "BNB", "ADA", "XRP", "DOT", "MATIC");
        return simbolo != null && criptosConhecidas.contains(simbolo.toUpperCase().trim());
    }

    private String formatarData(Long timestamp) {
        return new SimpleDateFormat("dd/MM").format(new Date(timestamp * 1000L));
    }
}