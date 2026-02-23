# 💵 WorldWideMoney - Analisador de Mercado

Uma aplicação robusta para monitoramento em tempo real de ativos financeiros, integrando cotações da B3 (Ações) e do mercado de Criptomoedas.

<img alt="Captura de tela 2026-02-23 - 01 45 46-fullpage" src="https://github.com/user-attachments/assets/d4a33e67-42ae-40bb-9dea-240353b6ad8e" />

## 📋 Visão Geral

O sistema foi desenvolvido para consolidar dados financeiros de diferentes fontes em uma interface única. Ele automatiza o consumo de APIs externas, trata a volatilidade dos dados e organiza o histórico para análise de performance.
## ⚡ Funcionalidades Chave:

  Monitoramento Multi-Ativos: Consulta simultânea de Ações (via Brapi) e Criptoativos (via Binance).

  Inteligência de Dados: Conversão automática de tipos de dados e tratamento de precisão decimal para ativos financeiros.

  Análise Histórica: Motor de busca capaz de gerar séries temporais customizadas (de 1 dia até 5 anos).

  Persistência Eficiente: Arquitetura de banco de dados otimizada para registrar cada consulta e evitar chamadas desnecessárias às APIs.

## 🛠️ Arquitetura Técnica

### Frontend

   - React.js: Biblioteca base para a construção da interface.

   - Tailwind CSS: Estilização utilitária para um design limpo e rápido.

   - Recharts: Renderização de gráficos de área e linha para análise de tendências.

### Backend

  - Java 17 & Spring Boot 3: Core da aplicação e gestão de serviços.

  - Spring Data JPA: Abstração da camada de persistência.

  - RestTemplate & Type Safety: Integração robusta com:

          Brapi Dev: Dados do mercado de ações brasileiro.

          Binance API: Cotações de criptoativos em tempo real.

Tratamento de Exceções: Lógica granular que evita que a falha de uma API externa interrompa a aplicação.

Código Moderno: Uso extensivo de Switch Expressions e ParameterizedTypeReference para garantir segurança de tipos no Java.

Modularização: Separação clara entre Models, Repositories, Services e Controllers.

## 📈 Exemplos de Performance

<img alt="Captura de tela 2026-02-23 - 01 45 02-fullpage" src="https://github.com/user-attachments/assets/ee603058-d935-4030-b488-acf677ebc29c" />

- Cripto Engine:	Processamento paralelo de múltiplas moedas.
- History Mapper:	Conversão dinâmica de Timestamps para formatos de leitura humana.

## ✒️ Desenvolvido por
Arthur Fedeli - www.linkedin.com/in/arthur-fedeli-696a9020b
