package com.financeiro.analisador.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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
        // Esta URL vai buscar o dado real na internet usando seu Token
        String url = "https://brapi.dev/api/quote/" + simbolo + "?token=" + token;

        try {
            Map response = restTemplate.getForObject(url, Map.class);
            List<Map> results = (List<Map>) response.get("results");
            Map data = results.get(0);

            Ativo ativo = new Ativo();
            ativo.setSimbolo(data.get("symbol").toString());
            ativo.setNome(data.get("longName").toString());

            // Aqui pegamos o preço real que vem da API
            String precoStr = data.get("regularMarketPrice").toString();
            ativo.setPrecoAtual(new BigDecimal(precoStr));
            ativo.setDataAtualizacao(LocalDateTime.now());

            return ativo;
        } catch (Exception e) {
            // Se der erro (ex: ticker errado), ele avisa
            throw new RuntimeException("Erro ao conectar na API: " + e.getMessage());
        }
    }

    public List<Ativo> buscarVariosAtivos(String simbolosAcoes, String simbolosCripto) {
        List<Ativo> listaFinal = new java.util.ArrayList<>();

        try {
            // 1. BUSCAR AÇÕES
            if (simbolosAcoes != null && !simbolosAcoes.isEmpty()) {
                String urlAcoes = "https://brapi.dev/api/quote/" + simbolosAcoes + "?token=" + token;
                Map res = restTemplate.getForObject(urlAcoes, Map.class);
                List<Map> results = (List<Map>) res.get("results");

                results.forEach(data -> {
                    Ativo a = transformarEmAtivo(data);
                    listaFinal.add(ativoRepository.save(a));
                });
            }

            // 2. BUSCAR CRIPTOS
            if (simbolosCripto != null && !simbolosCripto.isEmpty()) {
                // CORREÇÃO 1: Usar parênteses no split
                String[] moedas = simbolosCripto.split(",");

                for (String moeda : moedas) {
                    try {
                        String urlBinance = "https://api.binance.com/api/v3/ticker/price?symbol=" + moeda.trim() + "BRL";
                        Map resBinance = restTemplate.getForObject(urlBinance, Map.class);

                        if (resBinance != null && resBinance.containsKey("price")) {
                            Ativo a = new Ativo();
                            a.setSimbolo(moeda.trim());
                            a.setNome(mapearNomeCripto(moeda.trim()));

                            String precoStr = resBinance.get("price").toString();
                            a.setPrecoAtual(new BigDecimal(precoStr));
                            a.setDataAtualizacao(LocalDateTime.now());

                            listaFinal.add(ativoRepository.save(a));
                        }
                    } catch (Exception e) {
                        // CORREÇÃO 2: Faltou o sinal de + antes de e.getMessage()
                        System.err.println("Erro ao buscar a moeda " + moeda + ": " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            // CORREÇÃO 3: Removi o "moeda" daqui porque aqui o erro é geral
            System.err.println("Erro geral no service: " + e.getMessage());
        }

        return listaFinal;
    }

    private String mapearNomeCripto(String simbolo) {
        if (simbolo == null) {
            return "Desconhecido";
        }

        switch (simbolo.toUpperCase().trim()) {
            case "BTC":
                return "Bitcoin";
            case "ETH":
                return "Ethereum";
            case "SOL":
                return "Solana";
            case "BNB":
                return "Binance Coin";
            case "ADA":
                return "Cardano";
            case "XRP":
                return "Ripple";
            case "DOT":
                return "Polkadot";
            case "MATIC":
                return "Polygon";
            default:
                return simbolo; // Se não estiver na lista, retorna a própria sigla
        }
    }

    private Ativo transformarEmAtivo(Map data) {
        Ativo a = new Ativo();
        a.setSimbolo(data.get("symbol").toString());
        a.setNome(data.get("longName") != null ? data.get("longName").toString() : a.getSimbolo());
        a.setPrecoAtual(new BigDecimal(data.get("regularMarketPrice").toString()));
        a.setDataAtualizacao(LocalDateTime.now());
        return a;
    }

    public List<HistoricoDTO> buscarHistorico(String simbolo, String periodo) {
        // Se o símbolo estiver na sua lista de mapeamento de nomes, tratamos como Cripto
        // Ou podemos verificar se ele tem "BTC", "ETH", etc.
        boolean isCripto = isMoedaCripto(simbolo);

        if (isCripto) {
            return buscarHistoricoBinance(simbolo, periodo);
        } else {
            return buscarHistoricoBrapi(simbolo, periodo);
        }
    }

    private List<HistoricoDTO> buscarHistoricoBrapi(String simbolo, String periodo) {
        List<HistoricoDTO> lista = new ArrayList<>();
        try {
            // Criamos uma lógica de intervalo baseada no período
            String intervalo = "1h"; // Padrão: 1 hora para dar bastante detalhe

// Refinando a granularidade por período
            if (periodo.equals("1d")) {
                intervalo = "5m";
            } else if (periodo.equals("7d")) {
                intervalo = "15m"; // Fica muito mais fluido que 1h
            } else if (periodo.equals("1mo")) {
                intervalo = "60m";
            } else if (periodo.equals("6mo") || periodo.equals("1y")) {
                intervalo = "1d"; // Dia a dia é o ideal aqui
            } else if (periodo.equals("5y")) {
                intervalo = "1wk";
            }

            // Adicionamos o parâmetro &interval na URL
            String url = "https://brapi.dev/api/quote/" + simbolo
                    + "?range=" + periodo
                    + "&interval=" + intervalo
                    + "&token=" + token;

            Map response = restTemplate.getForObject(url, Map.class);
            List<Map> results = (List<Map>) response.get("results");
            List<Map> historicalData = (List<Map>) results.get(0).get("historicalDataPrice");

            if (historicalData != null) {
                for (Map data : historicalData) {
                    // 1. Pegamos os valores como Object primeiro (sem dar toString ainda)
                    Object rawDate = data.get("date");
                    Object rawClose = data.get("close");

                    // 2. Só processamos se AMBOS existirem
                    if (rawDate != null && rawClose != null) {
                        try {
                            Long timestamp = Long.valueOf(rawDate.toString());
                            Double preco = Double.valueOf(rawClose.toString());

                            lista.add(new HistoricoDTO(formatarData(timestamp), preco));
                        } catch (Exception e) {
                            // Se algum valor vier com formato errado, apenas pula esse ponto
                            continue;
                        }
                    }
                    // Se rawDate ou rawClose for null, o loop simplesmente pula para o próximo ponto
                }
            }
        } catch (Exception e) {
            System.err.println("Erro histórico BRAPI detalhado: " + e.getMessage());
        }
        return lista;
    }

    private List<HistoricoDTO> buscarHistoricoBinance(String simbolo, String periodo) {
        List<HistoricoDTO> lista = new ArrayList<>();
        try {
            // Ajuste de intervalo para a Binance
            String interval = "1d";
            int limit = 30;

            switch (periodo) {
                case "1d":
                    interval = "5m";  // 5 minutos é o padrão ouro para Day Trade
                    limit = 288;      // (24h * 60min) / 5min = 288 pontos
                    break;
                case "7d":
                    interval = "15m"; // 15 minutos dá um detalhe muito mais bonito que 1h
                    limit = 672;      // (7d * 24h * 4 pontos/hora)
                    break;
                case "1mo":
                    interval = "1h";  // 1 hora para o mensal é o equilíbrio perfeito
                    limit = 720;      // (30 dias * 24h)
                    break;
                case "6mo":
                    interval = "1d";  // 4 horas para 6 meses mostra bem as tendências
                    limit = 1080;     // (180 dias * 6 pontos/dia)
                    break;
                case "1y":
                    interval = "1d";  // Diário para 1 ano (padrão de análise técnica)
                    limit = 365;
                    break;
                case "5y":
                    interval = "1w";  // 1 semana para 5 anos (visão macro)
                    limit = 260;      // 52 semanas * 5
                    break;
                default:
                    interval = "1d";
                    limit = 30;
            }

            // Usamos USDT como base caso BRL falhe, mas BRL costuma funcionar para as grandes
            String url = "https://api.binance.com/api/v3/klines?symbol=" + simbolo.toUpperCase() + "BRL&interval=" + interval + "&limit=" + limit;

            // A Binance retorna um Array de Arrays. Usar List.class é mais seguro.
            List<List<Object>> res = restTemplate.getForObject(url, List.class);

            if (res != null) {
                for (List<Object> kline : res) {
                    // A Binance retorna o timestamp em milissegundos (kline[0])
                    Long timestampMs = Long.valueOf(kline.get(0).toString());
                    // O preço de fechamento é o índice 4
                    Double preco = Double.valueOf(kline.get(4).toString());

                    lista.add(new HistoricoDTO(formatarDataCripto(timestampMs), preco));
                }
            }
        } catch (Exception e) {
            System.err.println("Erro histórico Binance para " + simbolo + ": " + e.getMessage());
        }
        return lista;
    }

// Crie este formatador específico para milissegundos
    private String formatarDataCripto(Long timestampMs) {
        Date date = new Date(timestampMs);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        return sdf.format(date);
    }

    // Auxiliares
    private boolean isMoedaCripto(String simbolo) {
        List<String> criptosConhecidas = List.of("BTC", "ETH", "SOL", "BNB", "ADA", "XRP", "DOT", "MATIC");
        return criptosConhecidas.contains(simbolo.toUpperCase().trim());
    }

    private String formatarData(Long timestamp) {
        Date date = new Date(timestamp * 1000L);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM");
        return sdf.format(date);
    }

}
